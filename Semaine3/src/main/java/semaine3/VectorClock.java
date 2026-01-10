package semaine3;

import java.util.Arrays;

/**
 * Vector clock.
 * Rules:
 *  - On local event / send: VC[myId]++.
 *  - On receive: VC[i] = max(VC[i], received[i]) for all i, then VC[myId]++.
 */
public final class VectorClock {
    private final int[] vc;
    private final int myId;

    public VectorClock(int n, int myId) {
        if (n <= 0) throw new IllegalArgumentException("n must be > 0");
        if (myId < 0 || myId >= n) throw new IllegalArgumentException("invalid myId");
        this.vc = new int[n];
        this.myId = myId;
    }

    public synchronized void tick() {
        vc[myId] += 1;
    }

    public synchronized void onReceive(int[] received) {
        if (received == null || received.length != vc.length) {
            throw new IllegalArgumentException("bad received vector length");
        }
        for (int i = 0; i < vc.length; i++) {
            vc[i] = Math.max(vc[i], received[i]);
        }
        vc[myId] += 1;
    }

    public synchronized int[] snapshot() {
        return Arrays.copyOf(vc, vc.length);
    }

    public synchronized String snapshotString() {
        return Arrays.toString(vc);
    }

    public int size() {
        return vc.length;
    }
}
