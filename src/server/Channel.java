package server;

import networking.MulticastPacketSender;
import networking.PacketSender;
import networking.headers.Header;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class Channel {
    final int MAX_BUFFERED_MESSAGES = 100;
    public long channelID;
    private long msgID = 0;
    private long lastLoggedMsg = 0;

    public String channelName;
    private File log;
    public HashMap<String, User> users = new HashMap<>();
    private ArrayList<String> usernames = new ArrayList<>();

    /*
     * TreeMap buffering messages sent from the server to clients in the given channel.
     */
    private TreeMap<Long,BufferedMessageEntry> bufferedMessages = new TreeMap<>();

    public Channel(String channelName, long id) {
        this.channelName = channelName;
        this.log = new File("logs/" + channelName + ".txt");
        this.channelID = id;
    }

    /*
     *
     */
    public synchronized String addUser(User user) {
        for (User u : users.values()) {
            if (u.address.getAddress().equals(user.address.getAddress())) {
                return null;
            }
        }
        String assignedUsername = user.username;
        Integer number = 0;
        while(usernames.contains(assignedUsername)) {
            number++;
            assignedUsername = assignedUsername.split("(?<=\\D)(?=\\d)")[0] + Integer.toString(number);
        }
        user.username = assignedUsername;
        users.put(assignedUsername,user);
        usernames.add(assignedUsername);
        return user.username;
    }

    /*
     *
     */
    public synchronized void removeUser(User user) {
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
        return ++msgID;
    }

    public synchronized  long getLastLoggedMsg () {
        return lastLoggedMsg;
    }

    public synchronized void incrementLastLoggedMsg(long increment) {
        lastLoggedMsg += increment;
    }

    public void update() throws FileNotFoundException {
        if (lastLoggedMsg != msgID) {
//            PrintWriter pw = new PrintWriter(log);
            for (long index = lastLoggedMsg+1; index != msgID; index++) {
                BufferedMessageEntry e = bufferedMessages.get(index);
                System.err.printf("[" + e.militime + "] " + e.header.toString());
//                pw.println("[" + e.militime + "] " + e.header.toString());
                lastLoggedMsg = index;
            }
//            pw.close();
        }
        if (bufferedMessages.keySet().size() > MAX_BUFFERED_MESSAGES) {
            for (Long index : bufferedMessages.keySet()) {
                if (index < msgID - MAX_BUFFERED_MESSAGES) {
                    bufferedMessages.remove((index));
                }
            }
        }
    }

    public synchronized void log(Long msgID, Header header) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(log);
        pw.println("[" + msgID + "] " + header.toString());
    }

    public synchronized void addToBufferedTreeMap(long msgID, Header header) {
        this.bufferedMessages.put(msgID,new BufferedMessageEntry(header));
    }

    public synchronized void removeFromBufferedTreeMap(long msgID) {
        this.bufferedMessages.remove(msgID);
    }

    public synchronized BufferedMessageEntry getFromBufferedTreeMap(Long msgID) {
        return this.bufferedMessages.get(msgID);
    }

    public class BufferedMessageEntry implements Comparable<BufferedMessageEntry> {
        Header header;
        Long militime;

        BufferedMessageEntry(Header header) {
            this.header = header;
            this.militime = System.currentTimeMillis();
        }

        public Header getHeader() { return header; }

        public Long getMilitime() { return militime; }

        @Override
        public int compareTo(BufferedMessageEntry messageEntry) {
            return (int) (this.militime - messageEntry.militime);
        }
    }
}
