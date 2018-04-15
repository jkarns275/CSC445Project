package server;

import java.net.InetAddress;

public class User {
    public String username;
    public InetAddress address;

    public User (String username, InetAddress address) {
        this.username = username;
    }
}
