package johan;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import johan.Models.User;
import johan.Server.Session;
import johan.Services.IUserService;
import johan.Services.UserService;

public class Main {
    private static final int PORT = 8081;
    private static IUserService userService = new UserService();

    public static void main(String[] args) {
        System.out.println("Esperando clientes...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Session session = new Session(clientSocket);

                // Crear y ejecutar un hilo para manejar el cliente
                Thread clientThread = new Thread(() -> {
                    try {
                        // Obtener mensajes del cliente
                        String message;
                        boolean userConnected = false;

                        while ((message = session.read()) != null) {
                            System.out.println("Mensaje recibido: " + message);

                            try {
                                User receivedUser = new Gson().fromJson(message, User.class);
                                if (receivedUser != null) {
                                    // Añadir sesión y notificar a los demás usuarios al conectar
                                    if (receivedUser.getUserName() != null && !userConnected) {
                                        userService.addSession(session, receivedUser.getUserName());
                                        userService.notifyUserConnected(receivedUser.getUserName());
                                        userConnected = true;
                                    }

                                    // Enviar el mensaje a todos los clientes
                                    CompletableFuture<Void> sendFuture = userService.sendMessage(receivedUser);
                                    sendFuture.join(); // Asegúrate de manejar posibles excepciones aquí
                                }
                            } catch (JsonSyntaxException e) {
                                System.err.println("Error al deserializar el JSON: " + e.getMessage());
                            }
                        }
                    } finally {
                        // Cleanup cuando el cliente se desconecta
                        String disconnectedUserName = userService.getUserName(session.getSocket());
                        userService.removeSession(session);

                        if (disconnectedUserName != null) {
                            userService.notifyUserDisconnected(disconnectedUserName);
                        }
                        session.close(); // Asegúrate de cerrar el socket y liberar recursos

                        // Mostrar los nombres de los usuarios conectados después de una desconexión
                        String connectedUserNames = userService.getConnectedUserNames();
                        System.out.println("Usuarios conectados: " + connectedUserNames);
                    }
                });
                clientThread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
