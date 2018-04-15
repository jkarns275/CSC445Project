package server;

import java.util.ArrayList;

public class Channel {
    public long chatRoomID;
    public ArrayList<User> users;

    /**
     *
     **/
    public String addUser(User user) {
        String tmpName = null;
        for (User u : users) {
            if (u.username.equals(user.username)) {
                tmpName = u.username;
            }
        }
        if (tmpName != null) {
            String newName;
            String[] parts = tmpName.split("-");
            if (parts.length == 2) {
                Integer n = Integer.parseInt(parts[1]);
                ++n;
                newName = tmpName + "-" + n.toString();
            } else {
                newName = tmpName + "-1";
            }
            user.username = newName;
            users.add(user);
            return newName;
        }
        users.add(user);
        return user.username;
    }

    /**
     *
     **/
    public void removeUser() {

    }
}
