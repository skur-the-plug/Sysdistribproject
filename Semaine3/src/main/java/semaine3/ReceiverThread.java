package semaine3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Receives messages from a socket (line-based protocol).
 * For each received message:
 *  - update Lamport clock
 *  - update Vector clock
 *  - enqueue the message for processing (printing/demo)
 */
public final class ReceiverThread implements Runnable {
    private final Socket socket;
    private final semaine3.Node node;

    public ReceiverThread(Socket socket, semaine3.Node node) {
        this.socket = socket;
        this.node = node;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.isBlank()) continue;
                Message msg = Message.deserialize(line);
                node.onNetworkReceive(msg);
            }
        } catch (IOException e) {
            // normal when peer closes
        } catch (Exception e) {
            System.err.println("[Receiver] Error: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}
