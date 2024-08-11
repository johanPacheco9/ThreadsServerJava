package johan.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerService implements IServerService<String> {
    private ServerSocket serverSocket;
    private Session session;

    public ServerService(ServerSocket serverSocket2) {
        this.serverSocket = serverSocket2;
        this.session = null;
    }

    @Override
    public boolean bind() {
        try {
            Socket socket = this.serverSocket.accept();
            this.session = new Session(socket);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    
@Override
public String listen() {
    if (this.session != null) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = this.session.getSocket().getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                // Check if the line is the delimiter
                if (line.trim().isEmpty()) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
    return null;
}


    @Override
    public boolean response(String data) {
        if (this.session != null) {
            return this.session.write(data);
        }
        return false;
    }

    @Override
    public boolean close() {
        if (this.session != null) {
            return this.session.close();
        }
        return false;
    }
}
