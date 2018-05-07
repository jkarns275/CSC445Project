package server;

import jdk.nashorn.internal.runtime.options.Option;
import networking.MulticastPacketSender;
import networking.PacketSender;
import networking.headers.Header;
import networking.headers.InfoHeader;
import networking.headers.WriteHeader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

public class Channel {
    final int MAX_BUFFERED_MESSAGES = 2048;
    public long channelID;
    private long msgID = 0;
    private long lastLoggedMsg = 0;

    public String channelName;
    private File log;
    public HashMap<String, User> users = new HashMap<>();
    private ArrayList<String> usernames = new ArrayList<>();
    private ArrayList<User> usersToPurge = new ArrayList<>();

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
            if (u.address.equals(user.address)) {
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
        if (!users.isEmpty())
          this.sendPacket(new InfoHeader(channelID, InfoHeader.INFO_SERVER_MSG, this
            .getAndIncrementMsgID(),
        "User '" + user.username + "' has left."));
        users.remove(user.username);
        usernames.remove(user.username);
    }

    /*
     *
     */
    public void sendPacket(Header header) {
        MulticastPacketSender packetSender = (MulticastPacketSender) Server.headerManager.multicastPacketSender(
          header,users.values().stream()
            .map(u -> u.address)
            .collect(Collectors.toCollection(ArrayList::new))
        );
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

    public void update(HashSet<InetSocketAddress> heartbeatClients) {
        if (lastLoggedMsg != msgID) {
//            PrintWriter pw = new PrintWriter(log);
            for (long index = bufferedMessages.firstKey(); index != msgID; index++) {
                BufferedMessageEntry e = bufferedMessages.get(index);
                if (e == null) break;
                //System.err.println("[" + e.militime + "] " + e.header);
//                pw.println("[" + e.militime + "] " + e.header.toString());
                lastLoggedMsg = index;
            }
//            pw.close();
        }
        if (bufferedMessages.keySet().size() > MAX_BUFFERED_MESSAGES) {
          for (long i = bufferedMessages.firstKey(); i >= 0; i++) {
            bufferedMessages.remove(i);
          }
        }
        usersToPurge.clear();
        users.forEach((_nickname, user) -> {
          if (!heartbeatClients.contains(user.address)) usersToPurge.add(user);
        });
        for (User user: usersToPurge) removeUser(user);
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

    public synchronized boolean hasMessage(long msgID) {
      return this.bufferedMessages.containsKey(msgID);
    }

    public synchronized BufferedMessageEntry getMessage(long msgID) {
        return this.bufferedMessages.get(msgID);
    }

    public class BufferedMessageEntry implements Comparable<BufferedMessageEntry> {
        Header header;
        long militime;

        BufferedMessageEntry(Header header) {
            this.header = header;
            this.militime = System.currentTimeMillis();
        }

        public Header getHeader() { return header; }

        public long getMilitime() { return militime; }

        @Override
        public int compareTo(BufferedMessageEntry messageEntry) {
            return (int) (this.militime - messageEntry.militime);
        }

    }
}
