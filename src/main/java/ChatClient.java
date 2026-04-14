import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {
    private String serverIp;
    private int port;
    private String username;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ChatGUI gui;

    public ChatClient(ChatGUI gui) {
        this.gui = gui;
    }

    public boolean connect(String serverIp, int port, String username) {
        try {
            this.serverIp = serverIp;
            this.port = port;
            this.username = username;
            socket = new Socket(serverIp, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            out.writeUTF("CONNECT|" + username);
            
            ClientListener listener = new ClientListener(this, in, gui);
            new Thread(listener).start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF("MESSAGE|" + username + "|" + message);
        } catch (IOException e) {
            disconnect();
        }
    }

    public void sendFileCommand(String filename, byte[] fileData) {
        try {
            out.writeUTF("FILE|" + username + "|" + filename + "|" + fileData.length);
            out.write(fileData);
        } catch (IOException e) {
            disconnect();
        }
    }

    public void disconnect() {
        try {
            if (out != null) {
                out.writeUTF("EXIT|" + username);
                out.close();
            }
            if (in != null) in.close();
            if (socket != null) socket.close();
            gui.onDisconnected();
        } catch (IOException e) {
            
        }
    }

    public String getUsername() {
        return username;
    }
}
