package common;

import java.io.*;
import java.net.*;

public class NetworkServer extends Thread {

    private int port;

    public NetworkServer(int port) {
        this.port = port;
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("🟢 Serveur en écoute sur le port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );

                String message = in.readLine();
                System.out.println("📩 Message reçu : " + message);

                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}