// Client.java
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class Client {
    private static String SERVER_IP = "localhost"; // Default, can be changed
    private static final int SERVER_PORT = 8080;
    
    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel loginPanel;
    private JPanel chatPanel;
    private JTextPane chatArea;
    private JTextField messageField;
    private JList<String> contactsList;
    private DefaultListModel<String> contactsModel;
    private Map<String, Boolean> contactStatus = new HashMap<>();
    private PrintWriter out;
    private String username;
    private String currentChatWith;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    public static void main(String[] args) {
        // Handle command line arguments for server IP
        if (args.length > 0) {
            SERVER_IP = args[0];
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new Client().initialize();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    // Method to set server IP programmatically
    public static void setServerIP(String ip) {
        SERVER_IP = ip;
    }
    
    private void changeServerIP() {
        String newIP = JOptionPane.showInputDialog(frame, 
            "Enter server IP address:", 
            "Change Server", 
            JOptionPane.QUESTION_MESSAGE);
        
        if (newIP != null && !newIP.trim().isEmpty()) {
            SERVER_IP = newIP.trim();
            // Update the server label
            SwingUtilities.invokeLater(() -> {
                Component[] components = loginPanel.getComponents();
                for (Component comp : components) {
                    if (comp instanceof JPanel) {
                        JPanel panel = (JPanel) comp;
                        Component[] subComps = panel.getComponents();
                        for (Component subComp : subComps) {
                            if (subComp instanceof JLabel && subComp.getName() == null) {
                                JLabel label = (JLabel) subComp;
                                if (label.getText().startsWith("Server:")) {
                                    label.setText("Server: " + SERVER_IP + ":" + SERVER_PORT);
                                    break;
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    private void initialize() {
        createLoginPanel();
        createChatPanel();
        
        frame = new JFrame("Messenger App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setMinimumSize(new Dimension(600, 400));
        
        mainPanel = new JPanel(new CardLayout());
        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(chatPanel, "CHAT");
        
        frame.add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void createLoginPanel() {
        loginPanel = new JPanel(new BorderLayout(10, 10));
        loginPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        loginPanel.setBackground(new Color(240, 242, 245));
        
        // Title
        JLabel titleLabel = new JLabel("Messenger", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 132, 255));
        
        // Server IP display and change button
        JPanel serverPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        serverPanel.setBackground(new Color(240, 242, 245));
        
        JLabel serverLabel = new JLabel("Server: " + SERVER_IP + ":" + SERVER_PORT);
        serverLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        serverLabel.setForeground(new Color(101, 103, 107));
        
        JButton changeServerButton = new JButton("Change");
        changeServerButton.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        changeServerButton.setPreferredSize(new Dimension(60, 20));
        changeServerButton.addActionListener(e -> changeServerIP());
        
        serverPanel.add(serverLabel);
        serverPanel.add(changeServerButton);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 242, 245));
        formPanel.setBorder(new EmptyBorder(30, 50, 30, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        
        // Styled text fields
        styleTextField(usernameField, "Username");
        styleTextField(passwordField, "Password");
        
        // Buttons
        JButton loginButton = createStyledButton("Log In", new Color(0, 132, 255));
        JButton registerButton = createStyledButton("Register", new Color(66, 183, 42));
        
        // Add components to form
        formPanel.add(usernameField, gbc);
        formPanel.add(Box.createVerticalStrut(10), gbc);
        formPanel.add(passwordField, gbc);
        formPanel.add(Box.createVerticalStrut(20), gbc);
        formPanel.add(loginButton, gbc);
        formPanel.add(Box.createVerticalStrut(5), gbc);
        formPanel.add(registerButton, gbc);
        
        // Add action listeners
        loginButton.addActionListener(e -> authenticate("LOGIN", usernameField.getText(), new String(passwordField.getPassword())));
        registerButton.addActionListener(e -> authenticate("REGISTER", usernameField.getText(), new String(passwordField.getPassword())));
        
        // Add enter key listener
        passwordField.addActionListener(e -> authenticate("LOGIN", usernameField.getText(), new String(passwordField.getPassword())));
        
        loginPanel.add(titleLabel, BorderLayout.NORTH);
        loginPanel.add(serverPanel, BorderLayout.CENTER);
        loginPanel.add(formPanel, BorderLayout.SOUTH);
    }

    private void styleTextField(JTextField field, String placeholder) {
        field.setPreferredSize(new Dimension(250, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(204, 208, 213)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        if (field instanceof JPasswordField) {
            ((JPasswordField)field).setEchoChar('•');
        }
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(250, 40));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    private void createChatPanel() {
        chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(new Color(240, 242, 245));
        
        // Contacts panel
        JPanel contactsPanel = new JPanel(new BorderLayout());
        contactsPanel.setPreferredSize(new Dimension(200, 0));
        contactsPanel.setBackground(Color.WHITE);
        contactsPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(204, 208, 213)));
        
        JLabel contactsHeader = new JLabel(" Contacts", SwingConstants.LEADING);
        contactsHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        contactsHeader.setBorder(new EmptyBorder(15, 15, 15, 15));
        contactsHeader.setForeground(new Color(101, 103, 107));
        
        contactsModel = new DefaultListModel<>();
        contactsList = new JList<>(contactsModel);
        contactsList.setCellRenderer(new ContactListRenderer());
        contactsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = contactsList.getSelectedValue();
                if (selected != null && !selected.equals(currentChatWith) && !selected.equals(username)) {
                    currentChatWith = selected;
                    loadChatHistory();
                    updateChatTitle();
                }
            }
        });
        
        JScrollPane contactsScroll = new JScrollPane(contactsList);
        contactsScroll.setBorder(null);
        
        contactsPanel.add(contactsHeader, BorderLayout.NORTH);
        contactsPanel.add(contactsScroll, BorderLayout.CENTER);
        
        // Chat area
        JPanel chatAreaPanel = new JPanel(new BorderLayout());
        chatAreaPanel.setBackground(Color.WHITE);
        
        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBorder(new EmptyBorder(10, 15, 10, 15));
        chatHeader.setBackground(new Color(240, 242, 245));
        
        JLabel chatTitle = new JLabel();
        chatTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        chatTitle.setForeground(new Color(101, 103, 107));
        
        chatHeader.add(chatTitle, BorderLayout.CENTER);
        
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setBorder(new EmptyBorder(10, 15, 10, 15));
        chatArea.setBackground(new Color(248, 249, 250));
        
        // Set initial text
        javax.swing.text.StyledDocument doc = chatArea.getStyledDocument();
        javax.swing.text.Style welcomeStyle = doc.addStyle("welcome", null);
        javax.swing.text.StyleConstants.setForeground(welcomeStyle, new Color(108, 117, 125));
        javax.swing.text.StyleConstants.setFontSize(welcomeStyle, 14);
        javax.swing.text.StyleConstants.setAlignment(welcomeStyle, javax.swing.text.StyleConstants.ALIGN_CENTER);
        javax.swing.text.StyleConstants.setItalic(welcomeStyle, true);
        
        try {
            doc.insertString(0, "Welcome to the chat!\n\n", welcomeStyle);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(null);
        
        // Message input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(new Color(240, 242, 245));
        
        messageField = new JTextField();
        messageField.setPreferredSize(new Dimension(0, 40));
        messageField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(204, 208, 213)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageField.addActionListener(e -> sendMessage());
        
        JButton sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(80, 40));
        sendButton.setBackground(new Color(0, 132, 255));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.addActionListener(e -> sendMessage());
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        chatAreaPanel.add(chatHeader, BorderLayout.NORTH);
        chatAreaPanel.add(chatScroll, BorderLayout.CENTER);
        chatAreaPanel.add(inputPanel, BorderLayout.SOUTH);
        
        chatPanel.add(contactsPanel, BorderLayout.WEST);
        chatPanel.add(chatAreaPanel, BorderLayout.CENTER);
    }

    private void authenticate(String action, String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter both username and password", 
                                         "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Send authentication request
            out.println(action + ":" + username + ":" + password);
            
            // Get response
            String response = in.readLine();
            if ("AUTH_SUCCESS".equals(response) || "REGISTER_SUCCESS".equals(response)) {
                this.username = username;
                
                // Start message listener thread
                new Thread(() -> {
                    try {
                        String message;
                        while ((message = in.readLine()) != null) {
                            handleServerMessage(message);
                        }
                    } catch (IOException e) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(frame, "Connection to server lost", 
                                                         "Error", JOptionPane.ERROR_MESSAGE);
                            System.exit(0);
                        });
                    }
                }).start();
                
                // Switch to chat panel
                SwingUtilities.invokeLater(() -> {
                    cardLayout = (CardLayout) mainPanel.getLayout();
                    cardLayout.show(mainPanel, "CHAT");
                    frame.setTitle("Messenger - " + username);
                    out.println("GET_CONTACTS");
                });
            } else {
                String errorMsg = "LOGIN".equals(action) ? "Invalid username or password" : "Username already exists";
                JOptionPane.showMessageDialog(frame, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Cannot connect to server: " + e.getMessage(), 
                                         "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleServerMessage(String message) {
        System.out.println("Received message: " + message); // Debug line
        if (message.startsWith("PRIVATE_MSG:")) {
            String[] parts = message.split(":", 3);
            String sender = parts[1];
            String content = parts[2];
            
            System.out.println("Private message from " + sender + ": " + content); // Debug line
            System.out.println("Current chat with: " + currentChatWith); // Debug line
            
            SwingUtilities.invokeLater(() -> {
                if (sender.equals(currentChatWith)) {
                    System.out.println("Appending message to chat"); // Debug line
                    appendMessage(sender, content, false);
                    
                    // Ensure the chat area is visible and focused
                    chatArea.requestFocusInWindow();
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    
                    // Force the frame to come to front if it's not already
                    if (!frame.isFocused()) {
                        frame.toFront();
                        frame.requestFocus();
                    }
                } else {
                    // Show notification for new message from another contact
                    JOptionPane.showMessageDialog(frame, "New message from " + sender, 
                                                 "New Message", JOptionPane.INFORMATION_MESSAGE);
                    // Request updated contact list to refresh any status indicators
                    out.println("GET_CONTACTS");
                }
            });
        } else if (message.startsWith("CONTACTS:")) {
            updateContactList(message.substring(9));
        } else if (message.startsWith("ONLINE_UPDATE:")) {
            updateContactStatuses(message.substring(14));
        } else if (message.startsWith("HISTORY:")) {
            displayMessageHistory(message.substring(8));
        }
    }

    private void updateContactList(String contactsData) {
        SwingUtilities.invokeLater(() -> {
            contactsModel.clear();
            contactStatus.clear();
            
            String[] contacts = contactsData.split(";");
            for (String contact : contacts) {
                if (!contact.isEmpty()) {
                    String[] parts = contact.split(",");
                    if (parts.length == 2) {
                        String contactName = parts[0];
                        // Double-check: Don't add yourself to the contact list
                        if (!contactName.equals(username) && !contactName.trim().isEmpty()) {
                            contactsModel.addElement(contactName);
                            contactStatus.put(contactName, "1".equals(parts[1]));
                        }
                    }
                }
            }
            
            if (!contactsModel.isEmpty()) {
                contactsList.setSelectedIndex(0);
                currentChatWith = contactsModel.getElementAt(0);
                updateChatTitle();
                loadChatHistory();
            }
        });
    }

    private void updateContactStatuses(String contactsData) {
        SwingUtilities.invokeLater(() -> {
            String[] contacts = contactsData.split(";");
            for (String contact : contacts) {
                if (!contact.isEmpty()) {
                    String[] parts = contact.split(",");
                    if (parts.length == 2) {
                        String contactName = parts[0];
                        boolean isOnline = "1".equals(parts[1]);
                        
                        // Don't add yourself to the contact list
                        if (!contactName.equals(username) && !contactName.trim().isEmpty()) {
                            contactStatus.put(contactName, isOnline);
                            
                            // Add new contacts to the list if they don't exist
                            if (!contactsModel.contains(contactName)) {
                                contactsModel.addElement(contactName);
                            }
                        }
                    }
                }
            }
            contactsList.repaint();
            
            // Refresh current chat if there's a current chat partner
            if (currentChatWith != null) {
                loadChatHistory();
            }
        });
    }

    private void loadChatHistory() {
        if (currentChatWith != null) {
            out.println("GET_HISTORY:" + currentChatWith);
        }
    }

    private void displayMessageHistory(String historyData) {
        SwingUtilities.invokeLater(() -> {
            // Clear the chat area
            chatArea.setText("");
            
            String[] messages = historyData.split(";");
            for (String msg : messages) {
                if (!msg.isEmpty()) {
                    String[] parts = msg.split(":", 4);
                    if (parts.length == 4) {
                        boolean isMe = parts[0].equals(username);
                        appendMessage(parts[0], parts[2], isMe);
                    }
                }
            }
            
            // Scroll to bottom
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private void appendMessage(String sender, String message, boolean isMe) {
        String time = timeFormat.format(new Date());
        String senderText = isMe ? "You" : sender;
        
        try {
            // Use Document.insertString for reliable real-time updates
            javax.swing.text.StyledDocument doc = chatArea.getStyledDocument();
            
            // Create styles for different parts
            javax.swing.text.Style timeStyle = doc.addStyle("time", null);
            javax.swing.text.StyleConstants.setForeground(timeStyle, new Color(108, 117, 125));
            javax.swing.text.StyleConstants.setFontSize(timeStyle, 12);
            
            javax.swing.text.Style messageStyle = doc.addStyle("message", null);
            javax.swing.text.StyleConstants.setForeground(messageStyle, isMe ? Color.WHITE : Color.BLACK);
            javax.swing.text.StyleConstants.setBackground(messageStyle, isMe ? new Color(0, 123, 255) : new Color(233, 236, 239));
            javax.swing.text.StyleConstants.setFontSize(messageStyle, 14);
            javax.swing.text.StyleConstants.setAlignment(messageStyle, isMe ? javax.swing.text.StyleConstants.ALIGN_RIGHT : javax.swing.text.StyleConstants.ALIGN_LEFT);
            
            // Insert timestamp
            doc.insertString(doc.getLength(), senderText + " • " + time + "\n", timeStyle);
            
            // Insert message
            doc.insertString(doc.getLength(), message + "\n\n", messageStyle);
            
            // Scroll to bottom
            chatArea.setCaretPosition(doc.getLength());
            
            // Force UI repaint to ensure message is visible
            chatArea.revalidate();
            chatArea.repaint();
            
            System.out.println("Message appended successfully: " + message);
        } catch (Exception e) {
            System.out.println("Error appending message: " + e.getMessage());
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && currentChatWith != null && !currentChatWith.equals(username)) {
            out.println("PRIVATE:" + currentChatWith + ":" + message);
            appendMessage(username, message, true);
            messageField.setText("");
        }
    }

    private void updateChatTitle() {
        if (currentChatWith != null) {
            Component[] components = chatPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    Component[] subComps = panel.getComponents();
                    for (Component subComp : subComps) {
                        if (subComp instanceof JLabel && subComp.getName() == null) {
                            JLabel label = (JLabel) subComp;
                            label.setText(currentChatWith);
                            break;
                        }
                    }
                }
            }
        }
    }

    private class ContactListRenderer extends DefaultListCellRenderer {
        private final Color onlineColor = new Color(66, 183, 42);
        private final Color offlineColor = new Color(101, 103, 107);
        
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                    boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            String contact = (String) value;
            setText(contact);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setBorder(new EmptyBorder(10, 15, 10, 15));
            
            if (contactStatus.getOrDefault(contact, false)) {
                setForeground(onlineColor);
            } else {
                setForeground(offlineColor);
            }
            
            if (isSelected) {
                setBackground(new Color(228, 230, 235));
            } else {
                setBackground(Color.WHITE);
            }
            
            return this;
        }
    }
}