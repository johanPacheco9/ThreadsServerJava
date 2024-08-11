package johan.Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.google.gson.Gson;

import johan.Models.User;

public class UserService implements IUserService {

    private static List<Socket> clients;
    private Socket clientSocket;
    private Gson gson = new Gson();

    // Constructor para aceptar un Socket y lista de clientes
    public UserService(Socket socket, List<Socket> clients) {
        this.clientSocket = socket;
        UserService.clients = clients;
    }

    @Override
    public CompletableFuture<Void> sendMessage(User user) {
        return CompletableFuture.runAsync(() -> {
            String messageJson = gson.toJson(user);
            synchronized (clients) {
                for (Socket socket : clients) {
                    try {
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println(messageJson);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public CompletableFuture<User> getMessage() {
        return CompletableFuture.supplyAsync(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                // Recibe el JSON del servidor
                String jsonString = in.readLine();
                if (jsonString != null) {
                    User receivedUser = gson.fromJson(jsonString, User.class);
                    System.out.println("usuario: " + receivedUser.getUserName());
                    System.out.println("mensaje: " + receivedUser.getMessage());

                    // Crear un mensaje de respuesta
                    User responseUser = new User();
                    responseUser.setUserName("Server");
                    responseUser.setMessage("Received your message");

                    // Convertir el objeto User a JSON y enviar
                    String responseJson = gson.toJson(responseUser);
                    out.println(responseJson);

                    return receivedUser;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });
    }
}
