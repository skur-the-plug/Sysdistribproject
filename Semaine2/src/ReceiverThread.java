import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;

public class ReceiverThread extends Thread {
    private int port;
    private BlockingQueue<Message> queue;

    public ReceiverThread(int port, BlockingQueue<Message> queue) {
        this.port = port;
        this.queue = queue;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("🎧 Receiver (Server) en écoute sur le port " + port);

            while (true) {
                Socket socket = serverSocket.accept();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );
                String receivedText = in.readLine();

                if (receivedText != null) {
                    Message msg = new Message(receivedText);

                    queue.put(msg);
                    System.out.println("📥 Receiver: Message ajouté à la file : " + receivedText);
                }

                socket.close();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}