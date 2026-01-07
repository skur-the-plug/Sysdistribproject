import common.Node;
import common.NetworkClient;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static void main(String[] args) {
        BlockingQueue<Message> sharedQueue = new LinkedBlockingQueue<>();

        int myPort = 5001;
        ReceiverThread receiver = new ReceiverThread(myPort, sharedQueue);
        ProcessorThread processor = new ProcessorThread(sharedQueue);

        receiver.start();
        processor.start();

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Node mySelf = new Node(1, "127.0.0.1", myPort);
                NetworkClient.sendTo(mySelf, "Hello Distributed World!");

                Thread.sleep(1000);
                NetworkClient.sendTo(mySelf, "Deuxième tâche à traiter");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}