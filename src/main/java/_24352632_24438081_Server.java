import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class _24352632_24438081_Server {
    private static ServerSocket servSocket;
    private static final int PORT = 1234;
    private static int clientConnections = 0;

    public static void main(String[] args) {
        System.out.println("Listening on port 1234!");
        try {
            servSocket = new ServerSocket(PORT);

            do {
                Socket link = null;
                try {
                    link = servSocket.accept();
                    clientConnections++;
                    System.out.println("Client " + clientConnections + " connected!");

                    Thread t = new Thread(new ClientHandler(link, clientConnections));
                    t.start();
                } catch (IOException e) {
                    System.out.println("Unable to accept client connection!");
                }
            } while (true);
        } catch (IOException e) {
            System.out.println("Unable to listen on port 1234!");
            System.exit(1);
        } finally {
            try {
                if (servSocket != null) {
                    servSocket.close();
                }
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}