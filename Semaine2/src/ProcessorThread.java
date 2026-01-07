import java.util.concurrent.BlockingQueue;

public class ProcessorThread extends Thread {
    private BlockingQueue<Message> queue;

    public ProcessorThread(BlockingQueue<Message> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (true) {

                Message msg = queue.take();

                System.out.println("⚙️ Processor: Traitement en cours de " + msg.getContent());
                Thread.sleep(1000);

                System.out.println("✅ Processor: Fini avec " + msg.getContent());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}