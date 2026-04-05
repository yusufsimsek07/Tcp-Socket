import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private int port;
    private ConcurrentHashMap<String, ClientHandler> clients;
    private ServerLogger logger;

    public ChatServer(int port) {
        this.port = port;
        this.clients = new ConcurrentHashMap<>();
        this.logger = new ServerLogger();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.log("Server baslatildi. Port: " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, this);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            logger.log("Server hatasi: " + e.getMessage());
        }
    }

    public synchronized boolean addClient(String username, ClientHandler handler) {
        if (!clients.containsKey(username)) {
            clients.put(username, handler);
            logger.log(username + " baglandi.");
            broadcastUserList();
            return true;
        }
        return false;
    }

    public synchronized void removeClient(String username) {
        clients.remove(username);
        logger.log(username + " ayrildi.");
        broadcastUserList();
    }

    public void broadcastMessage(String message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }

    public void sendPrivateMessage(String targetUsername, String message) {
        ClientHandler handler = clients.get(targetUsername);
        if (handler != null) {
            handler.sendMessage(message);
        }
    }

    public void broadcastFile(String sender, String filename, byte[] fileData) {
        for (String username : clients.keySet()) {
            if (!username.equals(sender)) {
                clients.get(username).sendFile(sender, filename, fileData);
            }
        }
    }

    public synchronized void broadcastUserList() {
        String users = String.join(",", clients.keySet());
        String listMessage = "USERLIST|" + users;
        for (ClientHandler client : clients.values()) {
            client.sendMessage(listMessage);
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer(5000);
        server.start();
    }
}
