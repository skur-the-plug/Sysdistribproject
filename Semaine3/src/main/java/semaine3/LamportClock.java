package semaine3;

/**
 * Lamport logical clock.
 * Rules:
 *  - On local event / send: increment.
 *  - On receive: clock = max(clock, received) + 1.
 */
public final class LamportClock {
    private int time = 0;

    public synchronized int tick() {
        time += 1;
        return time;
    }

    public synchronized int onReceive(int receivedTs) {
        time = Math.max(time, receivedTs) + 1;
        return time;
    }

    public synchronized int get() {
        return time;
    }
}
