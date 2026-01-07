package common;

import java.io.*;
import java.net.*;

public class NetworkClient {

    public static void sendTo(Node node, String message) {
        try {
            Socket socket = new Socket(node.ip, node.port);

            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true
            );

            out.println(message);
            socket.close();

            System.out.println("📩 Message envoyé à common.Node " + node.id);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}