# Network Setup Guide for WhatsApp Clone

This guide explains how to set up the WhatsApp Clone to work across multiple devices on the same network.

## Prerequisites

1. **Java Runtime Environment (JRE)** installed on all devices
2. **All devices connected to the same network** (WiFi or Ethernet)
3. **Windows Firewall** configured to allow Java connections (see below)

## Step 1: Find Your Server's IP Address

### On Windows:
1. Open Command Prompt
2. Type: `ipconfig`
3. Look for "IPv4 Address" under your active network adapter
4. Note down the IP address (e.g., 192.168.1.100)

### On Mac/Linux:
1. Open Terminal
2. Type: `ifconfig` (Mac/Linux) or `ip addr` (Linux)
3. Look for "inet" followed by your IP address

## Step 2: Configure Windows Firewall

### Allow Java through Windows Firewall:
1. Open Windows Defender Firewall
2. Click "Allow an app or feature through Windows Defender Firewall"
3. Click "Change settings"
4. Click "Allow another app"
5. Browse to your Java installation (usually `C:\Program Files\Java\jre-version\bin\java.exe`)
6. Make sure both "Private" and "Public" are checked
7. Click "OK"

### Alternative: Create a Firewall Rule for Port 12345:
1. Open Windows Defender Firewall with Advanced Security
2. Click "Inbound Rules" → "New Rule"
3. Select "Port" → "Next"
4. Select "TCP" and enter "12345" → "Next"
5. Select "Allow the connection" → "Next"
6. Select all profiles → "Next"
7. Name it "WhatsApp Clone Server" → "Finish"

## Step 3: Start the Server

### Option A: Using the provided script
```bash
start_server.bat
```

### Option B: Manual command
```bash
java -cp .;sqlite-jdbc-3.50.2.0.jar Server
```

The server will start and show your IP address. Keep this running on the device that will act as the server.

## Step 4: Start Clients on Other Devices

### Option A: Using the provided script
```bash
start_client.bat SERVER_IP_ADDRESS
```

Example:
```bash
start_client.bat 192.168.1.100
```

### Option B: Manual command
```bash
java -cp .;sqlite-jdbc-3.50.2.0.jar Client SERVER_IP_ADDRESS
```

### Option C: Using the GUI
1. Start the client: `java -cp .;sqlite-jdbc-3.50.2.0.jar Client`
2. Click the "Change" button next to the server address
3. Enter the server's IP address
4. Login with a username and password

## Step 5: Test the Connection

1. **On the server device**: Start the server and note the IP address
2. **On client device 1**: Start client and connect to server IP
3. **On client device 2**: Start client and connect to server IP
4. **Register different users** on each client
5. **Try sending messages** between the clients

## Troubleshooting

### "Connection refused" error:
- Make sure the server is running
- Check that the IP address is correct
- Verify Windows Firewall settings
- Ensure both devices are on the same network

### "Cannot connect to server" error:
- Check if the server IP is reachable: `ping SERVER_IP`
- Verify port 12345 is not blocked by firewall
- Try temporarily disabling Windows Firewall for testing

### Messages not appearing:
- Check that users are registered
- Verify both clients are connected to the same server
- Look at the server console for error messages

## Network Configuration Examples

### Home Network Setup:
- **Server IP**: 192.168.1.100 (your computer)
- **Client 1**: 192.168.1.101 (phone/tablet)
- **Client 2**: 192.168.1.102 (laptop)

### Office Network Setup:
- **Server IP**: 10.0.0.50 (server computer)
- **Client 1**: 10.0.0.51 (employee computer)
- **Client 2**: 10.0.0.52 (employee computer)

## Security Notes

- This setup is for **local network use only**
- No encryption is implemented
- Passwords are stored in plain text
- Do not use this for sensitive communications
- Consider implementing SSL/TLS for production use

## Advanced Configuration

### Changing the Port:
If you need to use a different port, modify:
1. `Server.java` line 12: `private static final int PORT = 12345;`
2. `Client.java` line 16: `private static final int SERVER_PORT = 12345;`

### Running on Different Networks:
For devices on different networks, you'll need:
1. Port forwarding on the server's router
2. The server's public IP address
3. Proper firewall configuration

## Support

If you encounter issues:
1. Check the server console for error messages
2. Verify network connectivity between devices
3. Ensure Java is properly installed on all devices
4. Check Windows Firewall settings 