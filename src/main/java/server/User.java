package server;

import java.net.InetAddress;

public class User {
    public String username;
    public InetAddress address;
    public int port;

    public User (String username, InetAddress address, int port) {
        this.username = username;
        this.address = address;
        this.port = port;
    }
}
