# WhatsApp Clone - Java Chat Application

A real-time messaging application built with Java Swing and SQLite, featuring a modern chat interface similar to WhatsApp.

## Features

### 🚀 Core Features
- **Real-time messaging** - Instant message delivery between users
- **User authentication** - Login and registration system
- **Contact management** - View online/offline status of other users
- **Message history** - Persistent chat history stored in SQLite database
- **Modern UI** - Clean, WhatsApp-like interface with styled message bubbles

### 💬 Chat Features
- **Private messaging** - Send messages to specific users
- **Message timestamps** - See when messages were sent
- **Online status** - Real-time updates of user online/offline status
- **Message persistence** - All messages are saved and can be viewed later
- **Self-exclusion** - Users cannot see themselves or message themselves

### 🎨 User Interface
- **Login/Register screen** - Clean authentication interface
- **Contact list** - Shows all other users with online status
- **Chat window** - Modern message bubbles with different colors for sent/received messages
- **Responsive design** - Adapts to different window sizes

## Technology Stack

- **Frontend**: Java Swing (GUI)
- **Backend**: Java Socket Programming
- **Database**: SQLite with JDBC
- **Network**: TCP/IP Socket Communication
- **Architecture**: Client-Server Model

## Prerequisites

- Java 8 or higher
- SQLite JDBC Driver (included: `sqlite-jdbc-3.50.2.0.jar`)

## Installation & Setup

### 1. Clone or Download
```bash
git clone <repository-url>
cd WhatsAppClone/deep
```

### 2. Compile the Application
```bash
javac -cp "sqlite-jdbc-3.50.2.0.jar" *.java
```

### 3. Run the Server
```bash
java -cp "sqlite-jdbc-3.50.2.0.jar;." Server
```
**Note**: For Linux/Mac, use `:` instead of `;`:
```bash
java -cp "sqlite-jdbc-3.50.2.0.jar:." Server
```

### 4. Run the Client(s)
Open a new terminal window and run:
```bash
java -cp "sqlite-jdbc-3.50.2.0.jar;." Client
```

## How to Use

### Starting the Application
1. **Start the server first** - The server must be running before any clients can connect
2. **Launch multiple clients** - You can run multiple client instances to simulate different users
3. **Register new users** - Each client can register a new account or login with existing credentials

### Using the Chat
1. **Login/Register** - Enter username and password
2. **Select a contact** - Click on any user from the contact list
3. **Send messages** - Type in the message field and press Enter or click Send
4. **View chat history** - Previous messages are automatically loaded when selecting a contact

### Features in Action
- **Real-time updates** - Messages appear instantly when sent
- **Online status** - See which users are currently online (green) or offline (gray)
- **Message styling** - Your messages appear in blue bubbles on the right, others' messages in gray bubbles on the left
- **Persistent storage** - All messages are saved and persist between sessions

## Project Structure

```
WhatsAppClone/deep/
├── Server.java              # Main server application
├── Client.java              # Main client application
├── sqlite-jdbc-3.50.2.0.jar # SQLite JDBC driver
├── messenger.db             # SQLite database (created automatically)
├── Server.class            # Compiled server bytecode
├── Client.class            # Compiled client bytecode
└── README.md               # This file
```

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    online BOOLEAN DEFAULT FALSE
);
```

### Messages Table
```sql
CREATE TABLE messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sender_id INTEGER NOT NULL,
    receiver_id INTEGER NOT NULL,
    message TEXT NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);
```

## Network Protocol

### Authentication Messages
- `LOGIN:username:password` - User login request
- `REGISTER:username:password` - User registration request
- `AUTH_SUCCESS` - Successful authentication
- `AUTH_FAILED` - Failed authentication
- `REGISTER_SUCCESS` - Successful registration
- `REGISTER_FAILED` - Failed registration

### Chat Messages
- `PRIVATE:recipient:message` - Send private message
- `PRIVATE_MSG:sender:message` - Receive private message
- `GET_CONTACTS` - Request contact list
- `CONTACTS:data` - Contact list response
- `GET_HISTORY:user` - Request chat history
- `HISTORY:data` - Chat history response
- `ONLINE_UPDATE:data` - Online status update

## Configuration

### Server Configuration
- **Port**: 12345 (default)
- **Database**: `messenger.db` (SQLite)
- **Max Connections**: 20 concurrent users

### Client Configuration
- **Server IP**: localhost (default)
- **Server Port**: 12345 (default)

## Troubleshooting

### Common Issues

1. **"Cannot connect to server"**
   - Ensure the server is running before starting clients
   - Check if port 12345 is available
   - Verify firewall settings

2. **"Database initialization failed"**
   - Ensure write permissions in the project directory
   - Check if SQLite JDBC driver is present

3. **"Compilation errors"**
   - Ensure Java 8+ is installed
   - Verify all files are in the same directory
   - Check classpath includes the SQLite JAR

4. **Messages not appearing in real-time**
   - Ensure both client and server are compiled with latest changes
   - Check console for error messages

### Debug Information
- Server logs are displayed in the server GUI window
- Client debug messages are printed to the console
- Database file can be inspected with SQLite browser tools

## Security Features

- **Password storage** - Passwords are stored in the database (consider encryption for production)
- **User isolation** - Users cannot see or message themselves
- **Input validation** - Basic validation for usernames and messages
- **Connection management** - Proper cleanup of disconnected users

## Future Enhancements

Potential improvements for the application:
- [ ] Message encryption
- [ ] File sharing
- [ ] Group chats
- [ ] Message status (sent, delivered, read)
- [ ] User profiles and avatars
- [ ] Message search functionality
- [ ] Message deletion
- [ ] Offline message queuing
- [ ] Push notifications
- [ ] Mobile client

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is open source and available under the [MIT License](LICENSE).

## Support

For issues and questions:
- Check the troubleshooting section above
- Review console output for error messages
- Ensure all prerequisites are met
- Verify network connectivity

---

**Note**: This is a demonstration project for learning purposes. For production use, consider implementing additional security measures, error handling, and scalability improvements. 