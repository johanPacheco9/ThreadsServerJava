package johan.Services;

import johan.Models.User;
import johan.Server.Session;

import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public interface IUserService {
    void addSession(Session session, String userName);
    void removeSession(Session session);

    CompletableFuture<Void> sendMessage(User user);

    void notifyUserConnected(String userName);

    void notifyUserDisconnected(String userName);

    String getUserName(Socket socket);
    String getConnectedUserNames();  // Agregado aquí
}
