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

        Thread delivery = new Thread(() -> {
            while (running) {
                try {
                    tryDeliverBuffered();
                    Thread.sleep(50); // toutes les X ms
                } catch (InterruptedException e) {
                    return;
                }
            }
        }, "delivery-" + myId);
        delivery.setDaemon(true);
        delivery.start();

    }
    private boolean canDeliver(Message msg) {
        int[] V = msg.vectorClock;
        int[] L = vectorClock.snapshot(); // état "livré" (important)

        if (V == null || V.length != L.length) return false;

        int s = msg.senderId;
        if (s < 0 || s >= L.length) return false;

        if (V[s] != L[s] + 1) return false;

        for (int i = 0; i < L.length; i++) {
            if (i == s) continue;
            if (V[i] > L[i]) return false;
        }
        return true;
    }

    private void tryDeliverBuffered() {
        boolean progressed;
        do {
            progressed = false;

            synchronized (buffer) {
                for (int idx = 0; idx < buffer.size(); idx++) {
                    Message m = buffer.get(idx);
                    if (canDeliver(m)) {
                        buffer.remove(idx);
                        deliver(m);
                        progressed = true;
                        break; // recommencer (car la livraison débloque d'autres messages)
                    }
                }
            }

        } while (progressed);
    }

    private void deliver(Message msg) {
        // IMPORTANT: ici seulement on "applique" l'horloge vectorielle du message
        // Ton VectorClock actuel ne permet pas d'appliquer "sans tick en plus" proprement,
        // donc on va faire une version simple:
        applyVectorOnDeliver(msg.vectorClock, msg.senderId);

        // maintenant on envoie au processor (comme avant)
        inbox.offer(msg);
    }

    private void applyVectorOnDeliver(int[] received, int sender) {
        // On veut que L[sender] devienne L[sender]+1 (et received[sender] == L+1)
        // et que L[i] = max(L[i], received[i]) (mais received[i] <= L[i] pour i!=sender)
        int[] L = vectorClock.snapshot();

        for (int i = 0; i < L.length; i++) {
            L[i] = Math.max(L[i], received[i]);
        }

        // ⚠️ Comme ton VectorClock encapsule vc[] en private,
        // le plus propre est d'ajouter une méthode "setFrom(int[] newVc)" dans VectorClock.
        // Je te donne ça juste après.
        vectorClock.setFrom(L);
    }


    /**
     * Called by ReceiverThread after parsing message.
     */
    // // buffer causal
    private final java.util.List<Message> buffer = java.util.Collections.synchronizedList(new java.util.ArrayList<>());

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
