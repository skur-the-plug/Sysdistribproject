import common.NetworkClient;
import common.NetworkServer;
import common.Node;

public class Main {
    public Main() {
    }

    public static void main(String[] var0) {
        int var1 = Integer.parseInt(var0[0]);
        int var2 = Integer.parseInt(var0[1]);
        NetworkServer var3 = new NetworkServer(var1);
        var3.start();

        try {
            Thread.sleep(3000L);
        } catch (InterruptedException var5) {
        }

        Node var4 = new Node(2, "127.0.0.1", 5001);
        NetworkClient.sendTo(var4, "Bonjour depuis le node " + var2);
    }
}