package semaine3;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Node owns the clocks and the networking.
 * Clocks are updated:
 *  - before sending (tick)
 *  - upon receiving (merge + tick rules)
 */
public final class Node implements AutoCloseable {
    private final int myId;
    private final Config config;

    private final LamportClock lamportClock = new LamportClock();
    private final VectorClock vectorClock;
    private final NetClient netClient = new NetClient();
    private final NetServer server;

    private final BlockingQueue<Message> inbox = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    public Node(int myId, Config config) {
        this.myId = myId;
        this.config = config;
        this.vectorClock = new VectorClock(config.n(), myId);
        this.server = new NetServer(config.get(myId).port, this);
    }

    public void start() throws Exception {
        server.start();

        // Processor thread: prints received messages and current clocks
        Thread processor = new Thread(() -> {
            while (running) {
                try {
                    Message msg = inbox.take();
                    System.out.println("\n[Node " + myId + "] RECEIVED: " + msg.payload);
                    System.out.println("  from=" + msg.senderId
                            + " lamport(received)=" + msg.lamportTs
                            + " vc(received)=" + Arrays.toString(msg.vectorClock));
                    printClocks("after-receive");
                    System.out.print("> ");
                } catch (InterruptedException ignored) {
                    return;
                }
            }
        }, "processor-" + myId);
        processor.setDaemon(true);
        processor.start();
    }

    /**
     * Called by ReceiverThread after parsing message.
     */
    public void onNetworkReceive(Message msg) {
        // Update clocks on receive
        lamportClock.onReceive(msg.lamportTs);

        // Vector clock: merge then increment local component
        vectorClock.onReceive(msg.vectorClock);

        // Enqueue for processing/demo
        inbox.offer(msg);
    }

    public void send(int destId, String payload) {
        if (destId < 0 || destId >= config.n()) {
            System.err.println("[Node " + myId + "] Invalid destId: " + destId);
            return;
        }
        try {
            // Update clocks on send
            lamportClock.tick();
            vectorClock.tick();

            Message msg = new Message(
                    payload,
                    myId,
                    lamportClock.get(),
                    vectorClock.snapshot()
            );

            Config.NodeInfo dest = config.get(destId);
            netClient.send(dest.host, dest.port, msg.serialize());

            System.out.println("[Node " + myId + "] SENT to " + destId + ": " + payload);
            printClocks("after-send");
        } catch (Exception e) {
            System.err.println("[Node " + myId + "] send error: " + e.getMessage());
        }
    }

    public void broadcast(String payload) {
        for (int i = 0; i < config.n(); i++) {
            if (i == myId) continue;
            send(i, payload);
        }
    }

    public void printClocks(String where) {
        System.out.println("  clocks(" + where + "): lamport=" + lamportClock.get()
                + " vc=" + vectorClock.snapshotString());
    }

    public void shutdown() {
        running = false;
        close();
        System.out.println("[Node " + myId + "] Shutdown.");
    }

    @Override
    public void close() {
        try { server.close(); } catch (Exception ignored) {}
    }
}
