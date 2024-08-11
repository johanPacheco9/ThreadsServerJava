package johan;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        System.out.println("Java Server Socket");

        try {
            ServerSocket serverSocket = new ServerSocket(8081);

            while (true) {
                // Accept a new client connection
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                
                clientThread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
