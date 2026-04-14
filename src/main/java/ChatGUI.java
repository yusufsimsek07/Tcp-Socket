import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ChatGUI extends JFrame {
    private ChatClient client;
    private JTextField txtIp, txtPort, txtUsername, txtMessage;
    private JButton btnConnect, btnDisconnect, btnSend, btnSelectFile, btnSendFile;
    private JTextArea txtChat;
    private JList<String> listUsers;
    private DefaultListModel<String> userListModel;
    private FileTransferManager fileTransferManager;

    public ChatGUI() {
        client = new ChatClient(this);
        fileTransferManager = new FileTransferManager(client, this);
        initComponents();
    }

    private void initComponents() {
        setTitle("Sohbet ve Dosya Paylasim Uygulamasi");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtIp = new JTextField("127.0.0.1", 10);
        txtPort = new JTextField("5555", 5);
        txtUsername = new JTextField(10);
        btnConnect = new JButton("Baglan");
        btnDisconnect = new JButton("Cikis");
        btnDisconnect.setEnabled(false);

        pnlTop.add(new JLabel("IP:"));
        pnlTop.add(txtIp);
        pnlTop.add(new JLabel("Port:"));
        pnlTop.add(txtPort);
        pnlTop.add(new JLabel("Kullanici Adi:"));
        pnlTop.add(txtUsername);
        pnlTop.add(btnConnect);
        pnlTop.add(btnDisconnect);
        add(pnlTop, BorderLayout.NORTH);

        userListModel = new DefaultListModel<>();
        listUsers = new JList<>(userListModel);
        JScrollPane scrollUsers = new JScrollPane(listUsers);
        scrollUsers.setPreferredSize(new Dimension(150, 0));
        scrollUsers.setBorder(BorderFactory.createTitledBorder("Online Kullanicilar"));
        add(scrollUsers, BorderLayout.WEST);

        txtChat = new JTextArea();
        txtChat.setEditable(false);
        JScrollPane scrollChat = new JScrollPane(txtChat);
        add(scrollChat, BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new BorderLayout());
        JPanel pnlMessage = new JPanel(new BorderLayout());
        txtMessage = new JTextField();
        btnSend = new JButton("Gonder");
        pnlMessage.add(txtMessage, BorderLayout.CENTER);
        pnlMessage.add(btnSend, BorderLayout.EAST);

        JPanel pnlFile = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnSelectFile = new JButton("Dosya Sec");
        btnSendFile = new JButton("Dosya Gonder");
        pnlFile.add(btnSelectFile);
        pnlFile.add(btnSendFile);

        pnlBottom.add(pnlMessage, BorderLayout.NORTH);
        pnlBottom.add(pnlFile, BorderLayout.SOUTH);
        add(pnlBottom, BorderLayout.SOUTH);

        btnConnect.addActionListener(e -> {
            String ip = txtIp.getText().trim();
            int port = Integer.parseInt(txtPort.getText().trim());
            String username = txtUsername.getText().trim();
            if (!username.isEmpty()) {
                if (client.connect(ip, port, username)) {
                    toggleUI(false);
                } else {
                    JOptionPane.showMessageDialog(this, "Baglanti hatasi!");
                }
            }
        });

        btnDisconnect.addActionListener(e -> client.disconnect());

        btnSend.addActionListener(e -> {
            String msg = txtMessage.getText().trim();
            if (!msg.isEmpty()) {
                client.sendMessage(msg);
                txtMessage.setText("");
            }
        });

        txtMessage.addActionListener(e -> btnSend.doClick());

        btnSelectFile.addActionListener(e -> fileTransferManager.selectFile());
        btnSendFile.addActionListener(e -> fileTransferManager.sendFile());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.disconnect();
            }
        });
        
        toggleUI(true);
    }

    private void toggleUI(boolean isDisconnected) {
        txtIp.setEnabled(isDisconnected);
        txtPort.setEnabled(isDisconnected);
        txtUsername.setEnabled(isDisconnected);
        btnConnect.setEnabled(isDisconnected);
        btnDisconnect.setEnabled(!isDisconnected);
        txtMessage.setEnabled(!isDisconnected);
        btnSend.setEnabled(!isDisconnected);
        btnSelectFile.setEnabled(!isDisconnected);
        btnSendFile.setEnabled(!isDisconnected);
        if (isDisconnected) {
            userListModel.clear();
        }
    }

    public void appendMessage(String message) {
        txtChat.append(message + "\n");
        txtChat.setCaretPosition(txtChat.getDocument().getLength());
    }

    public void updateUserList(String[] users) {
        userListModel.clear();
        for (String u : users) {
            userListModel.addElement(u);
        }
    }

    public void onDisconnected() {
        toggleUI(true);
        appendMessage("Sunucu ile baglanti kesildi.");
    }

    public void receiveFile(String sender, String filename, byte[] fileData) {
        appendMessage("Dosya alindi: " + filename + " (Gonderen: " + sender + ")");
        fileTransferManager.saveFile(filename, fileData);
    }

    public void showError(String error) {
        JOptionPane.showMessageDialog(this, error, "Hata", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatGUI().setVisible(true);
        });
    }
}
