package semaine3;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Simple CLI for the demo:
 *   send <destId> <message>
 *   broadcast <message>
 *   clocks
 *   quit
 */
public final class SenderThread implements Runnable {
    private final Node node;

    public SenderThread(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        System.out.println("Commands:");
        System.out.println("  send <destId> <message>");
        System.out.println("  broadcast <message>");
        System.out.println("  clocks");
        System.out.println("  quit");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                    node.shutdown();
                    return;
                }

                if (line.equalsIgnoreCase("clocks")) {
                    node.printClocks("CLI");
                    continue;
                }

                if (line.startsWith("send ")) {
                    String[] parts = line.split("\\s+", 3);
                    if (parts.length < 3) {
                        System.out.println("Usage: send <destId> <message>");
                        continue;
                    }
                    int destId = Integer.parseInt(parts[1]);
                    node.send(destId, parts[2]);
                    continue;
                }

                if (line.startsWith("broadcast ")) {
                    String msg = line.substring("broadcast ".length());
                    node.broadcast(msg);
                    continue;
                }

                System.out.println("Unknown command. Try: send, broadcast, clocks, quit");
            }
        } catch (Exception e) {
            System.err.println("[Sender] Error: " + e.getMessage());
        }
    }
}
