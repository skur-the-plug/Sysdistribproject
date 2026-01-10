package semaine3;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Node owns the clocks and the networking.
 * Clocks are updated:
 *  - before sending (tick)
 *  - upon DELIVERY (merge + tick rules) <- PAS à la réception réseau!
 */
public final class Node implements AutoCloseable {
    private final int myId;
    private final Config config;

    private final LamportClock lamportClock = new LamportClock();
    private final VectorClock vectorClock;
    private final NetClient netClient = new NetClient();
    private final NetServer server;

    private final BlockingQueue<Message> inbox = new LinkedBlockingQueue<>();

    // Buffer causal : messages reçus mais pas encore livrés
    private final java.util.List<Message> buffer =
            java.util.Collections.synchronizedList(new java.util.ArrayList<>());

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
                    System.out.println("\n[Node " + myId + "] DELIVERED: " + msg.payload);
                    System.out.println("  from=" + msg.senderId
                            + " lamport(msg)=" + msg.lamportTs
                            + " vc(msg)=" + Arrays.toString(msg.vectorClock));
                    printClocks("after-delivery");
                    System.out.print("> ");
                } catch (InterruptedException ignored) {
                    return;
                }
            }
        }, "processor-" + myId);
        processor.setDaemon(true);
        processor.start();

        // Delivery thread: tente de livrer les messages du buffer
        Thread delivery = new Thread(() -> {
            while (running) {
                try {
                    tryDeliverBuffered();
                    Thread.sleep(50); // toutes les 50ms
                } catch (InterruptedException e) {
                    return;
                }
            }
        }, "delivery-" + myId);
        delivery.setDaemon(true);
        delivery.start();
    }

    /**
     * Vérifie si un message peut être livré selon l'ordre causal.
     * Règle: V(m) = [V₁, V₂, ..., Vₙ]
     * On peut livrer si:
     *   V(m)[sender] = L[sender] + 1
     *   V(m)[i] ≤ L[i] pour tout i ≠ sender
     */
    private boolean canDeliver(Message msg) {
        int[] V = msg.vectorClock;
        int[] L = vectorClock.snapshot(); // état actuel "livré"

        if (V == null || V.length != L.length) return false;

        int s = msg.senderId;
        if (s < 0 || s >= L.length) return false;

        // Condition 1: V[s] = L[s] + 1
        if (V[s] != L[s] + 1) return false;

        // Condition 2: V[i] ≤ L[i] pour i ≠ s
        for (int i = 0; i < L.length; i++) {
            if (i == s) continue;
            if (V[i] > L[i]) return false;
        }
        return true;
    }

    /**
     * Tente de livrer les messages en attente dans le buffer.
     */
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
                        break; // recommencer car la livraison débloque d'autres messages
                    }
                }
            }

        } while (progressed);
    }

    /**
     * Livre un message : met à jour le VC puis envoie au processor.
     */
    private void deliver(Message msg) {
        // Appliquer le VC du message : merge + increment
        int[] L = vectorClock.snapshot();
        int[] received = msg.vectorClock;

        // Merge: L[i] = max(L[i], received[i])
        for (int i = 0; i < L.length; i++) {
            L[i] = Math.max(L[i], received[i]);
        }

        // Increment ma propre composante
        L[myId]++;

        // Mettre à jour le VC
        vectorClock.setFrom(L);

        // Envoyer au processor pour affichage
        inbox.offer(msg);
    }

    /**
     * Called by ReceiverThread after parsing message.
     * NE PAS mettre à jour vectorClock ici!
     */
    public void onNetworkReceive(Message msg) {
        // Update Lamport clock on receive
        lamportClock.onReceive(msg.lamportTs);

        // Ajouter au buffer pour vérification causale
        synchronized (buffer) {
            buffer.add(msg);
        }

        System.out.println("[Node " + myId + "] RECEIVED (buffered): "
                + msg.payload + " from=" + msg.senderId
                + " vc=" + Arrays.toString(msg.vectorClock));
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