import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class FileTransferManager {
    private ChatClient client;
    private ChatGUI gui;
    private File selectedFile;

    public FileTransferManager(ChatClient client, ChatGUI gui) {
        this.client = client;
        this.gui = gui;
    }

    public void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(gui);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            gui.appendMessage("Secilen dosya: " + selectedFile.getName());
        }
    }

    public void sendFile() {
        if (selectedFile != null && selectedFile.exists()) {
            try {
                byte[] fileData = Files.readAllBytes(selectedFile.toPath());
                client.sendFileCommand(selectedFile.getName(), fileData);
                gui.appendMessage("Dosya gonderildi: " + selectedFile.getName());
                selectedFile = null;
            } catch (IOException e) {
                gui.showError("Dosya okuma hatasi.");
            }
        } else {
            gui.showError("Lutfen once gecerli bir dosya secin.");
        }
    }

    public void saveFile(String filename, byte[] fileData) {
        int response = JOptionPane.showConfirmDialog(gui, 
            "Yeni bir dosya geldi: " + filename + "\nKaydetmek ister misiniz?", 
            "Gelen Dosya", JOptionPane.YES_NO_OPTION);
            
        if (response == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(filename));
            int result = fileChooser.showSaveDialog(gui);
            if (result == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                    fos.write(fileData);
                    gui.appendMessage("Dosya kaydedildi: " + fileToSave.getAbsolutePath());
                } catch (IOException e) {
                    gui.showError("Dosya kaydetme hatasi.");
                }
            }
        }
    }
}
