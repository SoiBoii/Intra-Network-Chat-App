// Server.java
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 12345;
    private static final String DB_URL = "jdbc:sqlite:messenger.db";
    private static Connection dbConnection;
    private static JTextArea serverLog;
    private static Map<String, PrintWriter> onlineUsers = new ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        initializeDatabase();
        createServerGUI();
        log("Database initialized successfully"); // Now serverLog is initialized
        startServer();
    }

    private static void initializeDatabase() {
        try {
            dbConnection = DriverManager.getConnection(DB_URL);
            Statement stmt = dbConnection.createStatement();
            stmt.executeUpdate("PRAGMA foreign_keys = ON");
            
            // Create tables if they don't exist
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "online BOOLEAN DEFAULT FALSE" +
                ")"
            );
            
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS messages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "sender_id INTEGER NOT NULL," +
                "receiver_id INTEGER NOT NULL," +
                "message TEXT NOT NULL," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (sender_id) REFERENCES users(id)," +
                "FOREIGN KEY (receiver_id) REFERENCES users(id)" +
                ")"
            );
            
            // log("Database initialized successfully"); // Moved to after GUI creation
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void createServerGUI() {
        JFrame frame = new JFrame("Messenger Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        
        serverLog = new JTextArea();
        serverLog.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(serverLog);
        
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
        log("Server started. Waiting for connections...");
    }

    private static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            ExecutorService pool = Executors.newFixedThreadPool(20);
            while (true) {
                pool.execute(new ClientHandler(serverSocket.accept()));
            }
        } catch (IOException e) {
            log("Server exception: " + e.getMessage());
        }
    }

    private static void log(String message) {
        SwingUtilities.invokeLater(() -> {
            serverLog.append(new java.util.Date() + ": " + message + "\n");
        });
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Authentication
                String authRequest = in.readLine();
                String[] authParts = authRequest.split(":", 3);
                String action = authParts[0];
                username = authParts[1];
                String password = authParts[2];

                if ("LOGIN".equals(action)) {
                    try {
                        if (authenticateUser(username, password)) {
                            out.println("AUTH_SUCCESS");
                            onlineUsers.put(username, out);
                            updateUserStatus(username, true);
                            broadcastOnlineUsers();
                            log(username + " logged in successfully");
                        } else {
                            out.println("AUTH_FAILED");
                            return;
                        }
                    } catch (SQLException e) {
                        log("Authentication error: " + e.getMessage());
                        out.println("AUTH_FAILED");
                        return;
                    }
                } else if ("REGISTER".equals(action)) {
                    try {
                        if (registerUser(username, password)) {
                            out.println("REGISTER_SUCCESS");
                            log(username + " registered successfully");
                        } else {
                            out.println("REGISTER_FAILED");
                            return;
                        }
                    } catch (SQLException e) {
                        log("Registration error: " + e.getMessage());
                        out.println("REGISTER_FAILED");
                        return;
                    }
                }

                // Handle messages
                String message;
                while ((message = in.readLine()) != null) {
                    try {
                        if (message.startsWith("PRIVATE:")) {
                            handlePrivateMessage(message);
                        } else if (message.equals("GET_CONTACTS")) {
                            sendContactList();
                        } else if (message.startsWith("GET_HISTORY:")) {
                            sendMessageHistory(message);
                        }
                    } catch (SQLException e) {
                        log("Error handling message: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                log(username + " error: " + e.getMessage());
            } finally {
                // Clean up
                if (username != null) {
                    onlineUsers.remove(username);
                    try {
                        updateUserStatus(username, false);
                        broadcastOnlineUsers();
                    } catch (SQLException e) {
                        log("Error updating user status: " + e.getMessage());
                    }
                    log(username + " disconnected");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Error closing socket: " + e.getMessage());
                }
            }
        }

        private boolean authenticateUser(String username, String password) throws SQLException {
            String sql = "SELECT password FROM users WHERE username = ?";
            try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("password").equals(password);
                }
            }
            return false;
        }

        private boolean registerUser(String username, String password) throws SQLException {
            String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
            try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                return false;
            }
        }

        private void updateUserStatus(String username, boolean online) throws SQLException {
            String sql = "UPDATE users SET online = ? WHERE username = ?";
            try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
                stmt.setBoolean(1, online);
                stmt.setString(2, username);
                stmt.executeUpdate();
            }
        }

        private void handlePrivateMessage(String message) throws SQLException {
            String[] parts = message.split(":", 3);
            String recipient = parts[1];
            String content = parts[2];
            
            // Prevent self-messaging
            if (username.equals(recipient)) {
                return;
            }
            
            // Store message in database
            storeMessage(username, recipient, content);
            
            // Forward to recipient if online
            PrintWriter recipientWriter = onlineUsers.get(recipient);
            if (recipientWriter != null) {
                recipientWriter.println("PRIVATE_MSG:" + username + ":" + content);
            }
        }

        private void storeMessage(String sender, String recipient, String content) throws SQLException {
            String sql = "INSERT INTO messages(sender_id, receiver_id, message) " +
                         "VALUES((SELECT id FROM users WHERE username = ?), " +
                         "(SELECT id FROM users WHERE username = ?), ?)";
            try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
                stmt.setString(1, sender);
                stmt.setString(2, recipient);
                stmt.setString(3, content);
                stmt.executeUpdate();
            }
        }

        private void sendContactList() throws SQLException {
            StringBuilder contacts = new StringBuilder("CONTACTS:");
            String sql = "SELECT username, online FROM users WHERE username != ?";
            try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    contacts.append(rs.getString("username"))
                           .append(",")
                           .append(rs.getBoolean("online") ? "1" : "0")
                           .append(";");
                }
            }
            out.println(contacts.toString());
        }

        private void sendMessageHistory(String message) throws SQLException {
            String otherUser = message.split(":")[1];
            String sql = "SELECT u1.username as sender, u2.username as receiver, m.message, m.timestamp " +
                         "FROM messages m " +
                         "JOIN users u1 ON m.sender_id = u1.id " +
                         "JOIN users u2 ON m.receiver_id = u2.id " +
                         "WHERE (u1.username = ? AND u2.username = ?) OR (u1.username = ? AND u2.username = ?) " +
                         "ORDER BY m.timestamp";
            try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, otherUser);
                stmt.setString(3, otherUser);
                stmt.setString(4, username);
                ResultSet rs = stmt.executeQuery();
                
                StringBuilder history = new StringBuilder("HISTORY:");
                while (rs.next()) {
                    history.append(rs.getString("sender"))
                          .append(":")
                          .append(rs.getString("receiver"))
                          .append(":")
                          .append(rs.getString("message"))
                          .append(":")
                          .append(rs.getString("timestamp"))
                          .append(";");
                }
                out.println(history.toString());
            }
        }

        private void broadcastOnlineUsers() {
            try {
                String contacts = buildContactsList();
                for (PrintWriter writer : onlineUsers.values()) {
                    writer.println("ONLINE_UPDATE:" + contacts);
                }
            } catch (SQLException e) {
                log("Error broadcasting online users: " + e.getMessage());
            }
        }

        private String buildContactsList() throws SQLException {
            StringBuilder contacts = new StringBuilder();
            String sql = "SELECT username, online FROM users WHERE username != ?";
            try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    contacts.append(rs.getString("username"))
                           .append(",")
                           .append(rs.getBoolean("online") ? "1" : "0")
                           .append(";");
                }
            }
            return contacts.toString();
        }
    }
}