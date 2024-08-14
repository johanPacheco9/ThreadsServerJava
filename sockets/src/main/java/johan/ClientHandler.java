package johan;

import com.google.gson.Gson;
import java.net.Socket;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import johan.Models.User;
import johan.Server.Session;

public class ClientHandler implements Runnable {
    private static List<Session> sessions = new ArrayList<>();
    private static HashSet<String> connectedUserNames = new HashSet<>();
    private static Map<Session, String> sessionUserMap = new HashMap<>(); // Mapa de sesiones a nombres de usuario
    private Session session;
    private Gson gson = new Gson();

    public ClientHandler(Socket socket) {
        this.session = new Session(socket);
        synchronized (sessions) {
            sessions.add(this.session);
        }
    }

    @Override
    public void run() {
        String message;

        try {
            // Lee el primer mensaje para obtener el nombre de usuario
            if ((message = session.read()) != null) {
                System.out.println("Received from client: " + message);

                User newUser = gson.fromJson(message, User.class);
                if (newUser != null && newUser.getUserName() != null) {
                    String userName = newUser.getUserName();

                    synchronized (connectedUserNames) {
                        if (connectedUserNames.add(userName)) {
                            UserConnected(userName);
                        }
                    }

                    synchronized (sessionUserMap) {
                        sessionUserMap.put(session, userName);
                    }

                    String userListMessage = "Connected users: " + getConnectedUserNames();
                    session.write(userListMessage);

                    broadcastMessage(message);
                }
            }

            while ((message = session.read()) != null) {
                System.out.println("Received from client: " + message);
                broadcastMessage(message);
            }
        } finally {
            // Cleanup cuando el cliente se desconecta
            String disconnectedUserName;

            synchronized (sessionUserMap) {
                disconnectedUserName = sessionUserMap.remove(session);
            }

            if (disconnectedUserName != null) {
                synchronized (connectedUserNames) {
                    connectedUserNames.remove(disconnectedUserName);
                    UserDisconnected(disconnectedUserName);
                }
            }

            synchronized (sessions) {
                sessions.remove(session);
                session.close(); // Asegúrate de cerrar el socket y liberar recursos
            }
        }
    }

    private void broadcastMessage(String message) {
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.getSocket() != null && !session.getSocket().isClosed()) {
                    session.write(message);
                }
            }
        }
    }

    private void UserConnected(String userName) {
        // Notifica a todos los clientes que un nuevo usuario se ha conectado
        broadcastMessage("User connected: " + userName);

        // Envía la lista de usuarios conectados a todos los clientes
        String userListMessage = "Connected users: " + getConnectedUserNames();
        broadcastMessage(userListMessage);

        System.out.println("Usuarios conectados actualmente: " + getConnectedUserNames());
    }

    private void UserDisconnected(String userName) {
        broadcastMessage("User disconnected: " + userName);
        System.out.println("Usuarios conectados actualmente: " + getConnectedUserNames());
        String userListMessage = "Connected users: " + getConnectedUserNames();
        broadcastMessage(userListMessage);
    }

    private String getConnectedUserNames() {
        StringBuilder sb = new StringBuilder();
        synchronized (connectedUserNames) {
            for (String userName : connectedUserNames) {
                sb.append(userName).append(", ");
            }
        }
        // Eliminar la última coma y espacio
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }
}
