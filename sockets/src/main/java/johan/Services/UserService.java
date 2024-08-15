package johan.Services;

import com.google.gson.Gson;
import johan.Models.User;
import johan.Server.Session;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.net.Socket;

public class UserService implements IUserService {
    private final Gson gson = new Gson();
    private final Map<Session, String> sessionUserMap = new HashMap<>();
    private final Set<String> connectedUsers = new HashSet<>();

    @Override
    public void addSession(Session session, String userName) {
        synchronized (sessionUserMap) {
            sessionUserMap.put(session, userName);
            connectedUsers.add(userName);
        }
    }

    @Override
    public void removeSession(Session session) {
        synchronized (sessionUserMap) {
            String userName = sessionUserMap.remove(session);
            if (userName != null) {
                connectedUsers.remove(userName);
                notifyUserDisconnected(userName);
            }
        }
    }

    @Override
    public CompletableFuture<Void> sendMessage(User user) {
        return CompletableFuture.runAsync(() -> {
            String messageJson = gson.toJson(user);
            synchronized (sessionUserMap) {
                for (Map.Entry<Session, String> entry : sessionUserMap.entrySet()) {
                    if (entry.getKey().getSocket() != null && !entry.getKey().getSocket().isClosed()) {
                        entry.getKey().write(messageJson);
                    }
                }
            }
        });
    }

    public void notifyUserConnected(String userName) {
        // Asegúrate de que el usuario se añade a la lista de usuarios conectados
        connectedUsers.add(userName);
    
        // Crear mensaje de usuarios conectados
        String userListMessage = "Usuarios conectados: " + String.join(", ", connectedUsers);
        
        // Enviar el mensaje a todos los clientes conectados excepto al que envía
        synchronized (sessionUserMap) {
            for (Map.Entry<Session, String> entry : sessionUserMap.entrySet()) {
                if (!entry.getValue().equals(userName)) { // No enviar al mismo usuario
                    entry.getKey().write(userListMessage);
                }
            }
        }
    }
    
    @Override
    public void notifyUserDisconnected(String userName) {
        String userListMessage = "Connected users: " + getConnectedUserNames();
        synchronized (sessionUserMap) {
            for (Session session : sessionUserMap.keySet()) {
                session.write(userListMessage);
            }
        }
    }

    @Override
    public String getUserName(Socket socket) {
        synchronized (sessionUserMap) {
            for (Map.Entry<Session, String> entry : sessionUserMap.entrySet()) {
                if (entry.getKey().getSocket().equals(socket)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public String getConnectedUserNames() {
        synchronized (sessionUserMap) {
            return String.join(", ", connectedUsers);
        }
    }
}
