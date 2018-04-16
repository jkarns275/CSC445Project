package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

public class Channel {
    public long channelID;
    public ArrayList<User> users;
    String channelName;
    InetAddress channelAdress;
    MulticastSocket channelSocket;

    public Channel(String channelName, InetAddress channelAdress, long id) throws IOException {
        this.channelAdress = channelAdress;
        this.channelName = channelName;
        this.channelID = id;
//        channelSocket = new MulticastSocket(27031);
    }

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
    public void removeUser(User user) {
        for (User u : users) {
            if (u.username.equals(user.username)) {
                users.remove(u);
            }
        }
    }
}
