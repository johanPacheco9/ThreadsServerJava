package johan.Server;

import java.util.List;

public interface IServerService<T> {
    public boolean bind();
    public T listen();
    public boolean response(T data);
    public boolean close();
}
