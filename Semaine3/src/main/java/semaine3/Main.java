package semaine3;

/**
 * Run:
 *   java semaine3.Main <myId> [configFile]
 *
 * configFile is optional. If not provided, defaults to 3 nodes on localhost ports 5000..5002.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java semaine3.Main <myId> [configFile]");
            System.out.println("Example:");
            System.out.println("  java semaine3.Main 0");
            System.out.println("  java semaine3.Main 1");
            System.out.println("  java semaine3.Main 2");
            return;
        }

        int myId = Integer.parseInt(args[0]);
        String cfgPath = (args.length >= 2) ? args[1] : null;

        semaine3.Config config = semaine3.Config.fromFileOrDefault(cfgPath);

        if (myId < 0 || myId >= config.n()) {
            System.err.println("myId must be in [0.." + (config.n() - 1) + "]");
            return;
        }

        semaine3.Node node = new semaine3.Node(myId, config);
        node.start();

        System.out.println("[Node " + myId + "] Started. Peers=" + config.n());
        node.printClocks("startup");
        System.out.print("> ");

        Thread cli = new Thread(new semaine3.SenderThread(node), "cli-" + myId);
        cli.start();
        cli.join();
    }
}
