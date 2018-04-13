package Server;

import java.util.ArrayList;

public class ChatRoom {

    ArrayList<User> users;

    public boolean addUser(User user) {
        if (!users.contains(user)) {
            users.add(user);
            return true;
        }
        return false;
    }

    public void deleteUser(User user) {

    }

}
