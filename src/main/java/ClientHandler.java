import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ChatServer server;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            while (true) {
                String command = in.readUTF();
                if (command.startsWith("CONNECT|")) {
                    String[] parts = command.split("\\|");
                    if (parts.length > 1) {
                        String requestedUsername = parts[1];
                        if (server.addClient(requestedUsername, this)) {
                            this.username = requestedUsername;
                        } else {
                            out.writeUTF("ERROR|Kullanici adi zaten alinmis.");
                            break;
                        }
                    }
                } else if (command.startsWith("MESSAGE|")) {
                    String[] parts = command.split("\\|", 3);
                    if (parts.length == 3) {
                        String msg = parts[2];
                        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                        
                        if (msg.startsWith("@")) {
                            int spaceIndex = msg.indexOf(" ");
                            if (spaceIndex != -1) {
                                String target = msg.substring(1, spaceIndex);
                                String privateMsg = msg.substring(spaceIndex + 1);
                                String formattedMessage = "MESSAGE|[" + time + "] " + this.username + " (Ozel): " + privateMsg;
                                server.sendPrivateMessage(target, formattedMessage);
                                sendMessage("MESSAGE|[" + time + "] " + target + " kisisine (Ozel): " + privateMsg);
                            } else {
                                String formattedMessage = "MESSAGE|[" + time + "] " + this.username + ": " + msg;
                                server.broadcastMessage(formattedMessage);
                            }
                        } else {
                            String formattedMessage = "MESSAGE|[" + time + "] " + this.username + ": " + msg;
                            server.broadcastMessage(formattedMessage);
                        }
                    }
                } else if (command.startsWith("FILE|")) {
                    String[] parts = command.split("\\|");
                    if (parts.length == 4) {
                        String sender = parts[1];
                        String filename = parts[2];
                        int size = Integer.parseInt(parts[3]);
                        byte[] fileData = new byte[size];
                        in.readFully(fileData);
                        server.broadcastFile(sender, filename, fileData);
                    }
                } else if (command.startsWith("EXIT|")) {
                    break;
                }
            }
        } catch (IOException e) {
            
        } finally {
            closeConnections();
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            closeConnections();
        }
    }

    public void sendFile(String sender, String filename, byte[] fileData) {
        try {
            out.writeUTF("FILE|" + sender + "|" + filename + "|" + fileData.length);
            out.write(fileData);
        } catch (IOException e) {
            closeConnections();
        }
    }

    private void closeConnections() {
        if (username != null) {
            server.removeClient(username);
        }
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            
        }
    }
}
