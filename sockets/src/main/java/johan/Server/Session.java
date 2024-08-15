package johan.Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


//User receivedUser = new Gson().fromJson(message, User.class);

public class Session {
    private BufferedReader reader;
    private BufferedWriter writer;
    private Socket socket;

    public Session(Socket socket) {
        try {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            this.reader = null;
            this.writer = null;
            this.socket = null;
        }
    }


    public Socket getSocket() {
        return this.socket;
    }

    public String read() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean write(String data) {
        try {
            writer.write(data);
            writer.newLine();
            writer.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean close() {
        try {
            if (reader != null) {
                reader.close();
                reader = null; 
            }
            if (writer != null) {
                writer.close();
                writer = null;
            }
            if (socket != null) {
                socket.close();
                socket = null;
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
}
