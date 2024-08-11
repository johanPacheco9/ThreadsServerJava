package johan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import johan.Services.UserService;

public class ClientHandler implements Runnable {
    private static List<Socket> clientSockets = new ArrayList<>();
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        synchronized (clientSockets) {
            clientSockets.add(socket);
        }
    }

    @Override
    public void run() {
        try {
            // Usa InputStreamReader junto con BufferedReader para leer del socket
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String message;

            while ((message = in.readLine()) != null) {
                System.out.println("Received from client: " + message);

                // Envía el mensaje a todos los clientes
                broadcastMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            synchronized (clientSockets) {
                clientSockets.remove(clientSocket);
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void broadcastMessage(String message) {
        synchronized (clientSockets) {
            for (Socket socket : clientSockets) {
                try {
                    if (!socket.isClosed()) {
                        PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);
                        socketOut.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
