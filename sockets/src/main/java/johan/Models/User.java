package johan.Models;

import java.io.Serializable;

public class User implements Serializable {
    private int Id;
    private String UserName;
    private String Message;

    public User() {}

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        this.Id = id;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        this.UserName = userName;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        this.Message = message;
    }
}
