package semaine3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TCP server that accepts connections and hands sockets to ReceiverThread.
 */
public final class NetServer implements AutoCloseable {
    private final int port;
    private final Node node;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private volatile boolean running = true;
    private ServerSocket serverSocket;

    public NetServer(int port, Node node) {
        this.port = port;
        this.node = node;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        pool.submit(() -> {
            System.out.println("[Server] Listening on port " + port);
            while (running) {
                try {
                    Socket s = serverSocket.accept();
                    pool.submit(new ReceiverThread(s, node));
                } catch (IOException e) {
                    if (running) System.err.println("[Server] accept() error: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void close() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}
        pool.shutdownNow();
    }
}
