import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerLogger {
    public void log(String message) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.println("[" + time + "] " + message);
    }
}
