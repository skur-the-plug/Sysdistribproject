package semaine3;

import java.util.Arrays;

/**
 * Week 3 Message:
 *  - payload
 *  - senderId
 *  - lamportTs
 *  - vectorClock[]
 *
 * Wire format (one line):
 *   senderId|lamportTs|v0,v1,v2,...|payload
 *
 * Payload can contain anything; we escape newlines and \r for safety.
 */
public final class Message {
    public final String payload;
    public final int senderId;
    public final int lamportTs;
    public final int[] vectorClock;

    public Message(String payload, int senderId, int lamportTs, int[] vectorClock) {
        this.payload = payload;
        this.senderId = senderId;
        this.lamportTs = lamportTs;
        this.vectorClock = vectorClock;
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(senderId).append('|')
          .append(lamportTs).append('|')
          .append(encodeVector(vectorClock)).append('|')
          .append(escape(payload));
        return sb.toString();
    }

    public static Message deserialize(String line) {
        if (line == null) throw new IllegalArgumentException("null line");
        String[] parts = line.split("\\|", 4);
        if (parts.length != 4) throw new IllegalArgumentException("bad message format: " + line);
        int sender = Integer.parseInt(parts[0]);
        int lamport = Integer.parseInt(parts[1]);
        int[] vc = decodeVector(parts[2]);
        String payload = unescape(parts[3]);
        return new Message(payload, sender, lamport, vc);
    }

    private static String encodeVector(int[] vc) {
        if (vc == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vc.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(vc[i]);
        }
        return sb.toString();
    }

    private static int[] decodeVector(String s) {
        if (s == null || s.isEmpty()) return new int[0];
        String[] items = s.split(",");
        int[] vc = new int[items.length];
        for (int i = 0; i < items.length; i++) {
            vc[i] = Integer.parseInt(items[i].trim());
        }
        return vc;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        // unescape in reverse order
        return s.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\\\", "\\");
    }

    @Override
    public String toString() {
        return "Message{" +
                "payload='" + payload + '\'' +
                ", senderId=" + senderId +
                ", lamportTs=" + lamportTs +
                ", vectorClock=" + Arrays.toString(vectorClock) +
                '}';
    }
}
