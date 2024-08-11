package johan.Services;

import java.util.concurrent.CompletableFuture;

import johan.Models.User;

public interface IUserService {
    CompletableFuture<Void> sendMessage(User user);

    CompletableFuture<User> getMessage();
}
