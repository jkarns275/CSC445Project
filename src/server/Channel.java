package server;

import networking.MulticastPacketSender;
import networking.PacketSender;
import networking.headers.Header;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class Channel {
    final int MAX_BUFFERED_MESSAGES = 100;
    public long channelID;
    private long msgID = 0;

    public final Object lock = new Object();

    public String channelName;

    public HashMap<String, User> users = new HashMap<>();

    /*
     * TreeMap buffering messages sent from the server to clients in the given channel.
     */
    private TreeMap<Long,Header> bufferedMessages = new TreeMap<>();

    public Channel(String channelName, long id) {
        this.channelName = channelName;
        this.channelID = id;
    }

    /*
     *
     */
    public String addUser(User user) {
        String tmpName = null;
        String number;
        int userNumber = 0;
        for (User u : users.values()) {
            if (u.username.split("-")[0].equals(user.username)) {
                if ((number = u.username.split("-")[1]) != null) {
                    if (Integer.parseInt(number) > userNumber) {
                        userNumber = Integer.parseInt(number);
                        tmpName = u.username;
                    }
                }
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
            users.put(user.username,user);
            return newName;
        }
        users.put(user.username,user);
        return user.username;
    }

    /*
     *
     */
    public void removeUser(User user) {
        users.remove(user.username);
    }

    /*
     *
     */
    public void sendPacket(Header header) {
        ArrayList<InetSocketAddress> addresses = new ArrayList<>();
        for (User user : users.values()) addresses.add(user.address);
        MulticastPacketSender packetSender = (MulticastPacketSender) Server.headerManager.multicastPacketSender(header,addresses);
        packetSender.run();
    }

    public void sendPacket(Header header, InetSocketAddress address) {
        PacketSender packetSender = (PacketSender) Server.headerManager.packetSender(header,address);
        packetSender.run();
    }

    public synchronized long getAndIncrementMsgID() {
        long tmp = msgID;
        ++msgID;
        return tmp;
    }

    public void update() {
        if (bufferedMessages.keySet().size() > MAX_BUFFERED_MESSAGES) {
            for (long index : bufferedMessages.keySet()) {
                if (index < msgID - MAX_BUFFERED_MESSAGES) {
                    bufferedMessages.remove(index);
                }
            }
        }
    }

    public synchronized void addToTreeMap(Long msgID, Header header) {
        this.bufferedMessages.put(msgID,header);
    }

    public synchronized void removeFromTreeMap(Long msgID) {
        this.bufferedMessages.remove(msgID);
    }

    public synchronized Header getFromTreeMap(Long msgID) {
        return this.bufferedMessages.get(msgID);
    }
}
