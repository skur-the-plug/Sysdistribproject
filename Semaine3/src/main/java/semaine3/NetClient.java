package semaine3;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Very small TCP client: opens a socket, sends one line, closes.
 * Simple and reliable for demos/defense.
 */
public final class NetClient {
    public void send(String host, int port, String line) throws IOException {
        try (Socket socket = new Socket(host, port);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            out.write(line);
            out.write("\n");
            out.flush();
        }
    }
}
