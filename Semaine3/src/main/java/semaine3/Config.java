package semaine3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple config loader.
 *
 * Default: 3 nodes on localhost ports 5000, 5001, 5002.
 *
 * Optional file format (one node per line):
 *   host port
 * Example:
 *   127.0.0.1 5000
 *   127.0.0.1 5001
 *   127.0.0.1 5002
 */
public final class Config {
    public static final class NodeInfo {
        public final String host;
        public final int port;
        public NodeInfo(String host, int port) {
            this.host = host;
            this.port = port;
        }
        @Override public String toString() { return host + ":" + port; }
    }

    private final List<NodeInfo> nodes;

    private Config(List<NodeInfo> nodes) {
        this.nodes = nodes;
    }

    public int n() { return nodes.size(); }

    public NodeInfo get(int id) { return nodes.get(id); }

    public static Config defaultLocal3() {
        List<NodeInfo> nodes = new ArrayList<>();
        nodes.add(new NodeInfo("127.0.0.1", 5000));
        nodes.add(new NodeInfo("127.0.0.1", 5001));
        nodes.add(new NodeInfo("127.0.0.1", 5002));
        return new Config(nodes);
    }

    public static Config fromFileOrDefault(String pathOrNull) {
        if (pathOrNull == null || pathOrNull.isBlank()) {
            return defaultLocal3();
        }
        List<NodeInfo> nodes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(pathOrNull))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\s+");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Bad config line: " + line);
                }
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                nodes.add(new NodeInfo(host, port));
            }
        } catch (Exception e) {
            System.err.println("Failed to read config file '" + pathOrNull + "'. Using default.");
            return defaultLocal3();
        }
        if (nodes.isEmpty()) return defaultLocal3();
        return new Config(nodes);
    }
}
