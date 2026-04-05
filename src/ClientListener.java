import java.io.DataInputStream;
import java.io.IOException;
import javax.swing.SwingUtilities;

public class ClientListener implements Runnable {
    private ChatClient client;
    private DataInputStream in;
    private ChatGUI gui;

    public ClientListener(ChatClient client, DataInputStream in, ChatGUI gui) {
        this.client = client;
        this.in = in;
        this.gui = gui;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String command = in.readUTF();
                if (command.startsWith("MESSAGE|")) {
                    String message = command.substring(8);
                    SwingUtilities.invokeLater(() -> gui.appendMessage(message));
                } else if (command.startsWith("USERLIST|")) {
                    String usersStr = command.length() > 9 ? command.substring(9) : "";
                    String[] users = usersStr.isEmpty() ? new String[0] : usersStr.split(",");
                    SwingUtilities.invokeLater(() -> gui.updateUserList(users));
                } else if (command.startsWith("FILE|")) {
                    String[] parts = command.split("\\|");
                    if (parts.length == 4) {
                        String sender = parts[1];
                        String filename = parts[2];
                        int size = Integer.parseInt(parts[3]);
                        byte[] fileData = new byte[size];
                        in.readFully(fileData);
                        SwingUtilities.invokeLater(() -> gui.receiveFile(sender, filename, fileData));
                    }
                } else if (command.startsWith("ERROR|")) {
                    String error = command.substring(6);
                    SwingUtilities.invokeLater(() -> gui.showError(error));
                    client.disconnect();
                    break;
                }
            }
        } catch (IOException e) {
            client.disconnect();
        }
    }
}
