package server;

import common.Constants;
import networking.MulticastPacketSender;
import networking.PacketSender;
import networking.headers.Header;
import networking.headers.InfoHeader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TreeMap;

public class Channel {
    private final int MAX_BUFFERED_MESSAGES = 2048;
    public long channelID;
    private long msgID = 1;
    private long lastLoggedMsg = 0;

    public String channelName;
    private File log;
    private HashMap<String, User> users = new HashMap<>();
    private ArrayList<User> usersToPurge = new ArrayList<>();

    /**
     * TreeMap buffering messages sent from the server to clients in the given channel.
     */
    private TreeMap<Long,BufferedMessageEntry> bufferedMessages = new TreeMap<>();

    public Channel(String channelName, long id) {
        this.channelName = channelName;
        this.channelID = id;
    }

    /**
     * @param user
     * @return String assignedUsername
     */
    public synchronized String addUser(User user) {
        for (User u : users.values()) {
            if (u.address.getAddress().equals(user.address.getAddress()) && u.address.getPort() == user.address.getPort()) {
                return null;
            }
        }
        String assignedUsername = user.username;
        int number = 0;
        while(users.keySet().contains(assignedUsername)) {
            number++;
            assignedUsername = assignedUsername.split("(?<=\\D)(?=\\d)")[0] + Integer.toString(number);
        }
        user.username = assignedUsername;
        users.put(assignedUsername,user);
        this.sendPacket(new InfoHeader(channelID, InfoHeader.INFO_SERVER_MSG, this
            .getAndIncrementMsgID(),
        "User '" + user.username + "' has joined."));
        return user.username;
    }

    /**
     * @param user - user to be removed
     */
    public synchronized void removeUser(User user) {
        if (!users.isEmpty())
          this.sendPacket(new InfoHeader(channelID, InfoHeader.INFO_SERVER_MSG, this
            .getAndIncrementMsgID(),
        "User '" + user.username + "' has left."));
        users.remove(user.username);
    }

    /**
     * @param header - header packet to be multicast to clients in the channel
     */
    public void sendPacket(Header header) {
        MulticastPacketSender packetSender = (MulticastPacketSender) Server.headerManager.multicastPacketSender(
          header,users.values().stream()
            .map(u -> u.address)
            .collect(Collectors.toCollection(ArrayList::new))
        );
        packetSender.run();
    }

    /**
     * @param header - header packet to be sent to an individual client
     * @param address - address of the client the header will be sent to
     */
    public void sendPacket(Header header, InetSocketAddress address) {
        PacketSender packetSender = (PacketSender) Server.headerManager.packetSender(header,address);
        packetSender.run();
    }

    /**
     * @return long - the next message id index for the given channel
     */
    public synchronized long getAndIncrementMsgID() {
        return ++msgID;
    }

    /**
     * @param increment - increments the index to indicate the number of the last logged message
     */
    public synchronized void incrementLastLoggedMsg(long increment) {
        lastLoggedMsg += increment;
    }

    /**
     * @param heartbeatClients
     * Update method removes buffered messages and disconnected clients.  The method checks if the number of buffered
     * messages exceed the MAX_BUFFERED_MESSAGE limit, if so it remove the earlier half of the messages. If client
     * heartbeats have not been received given the time constraint, the method removes specific clients from the channel.
     */
    public synchronized void update(HashSet<InetSocketAddress> heartbeatClients) {
        Calendar calendar = Calendar.getInstance();
        if (lastLoggedMsg != msgID) {
            for (long index = lastLoggedMsg+1; index != msgID; index++) {
                BufferedMessageEntry e = bufferedMessages.get(index);
                if (e == null) continue;
                calendar.setTimeInMillis(e.militime);
                System.out.println("[" + calendar.getTime() + "] " + e.header.toString());
                lastLoggedMsg = index;
            }
        }
        if (bufferedMessages.keySet().size() > MAX_BUFFERED_MESSAGES) {
            for (long i = msgID - MAX_BUFFERED_MESSAGES; i <= msgID - MAX_BUFFERED_MESSAGES/2; i++) {
                bufferedMessages.remove(i);
            }
        }
        usersToPurge.clear();
        long now = System.nanoTime();
        users.forEach((_nickname, user) -> {
          if (!heartbeatClients.contains(user.address) && now - user.joinTime > 8 * Constants.SECONDS_TO_NANOS)
            usersToPurge.add(user);
        });
        for (User user: usersToPurge) {
          System.out.println("Removing user \"" + user.username + "\" at address " + user.address.toString());
          removeUser(user);
        }
    }

    /**
     * @param msgID
     * @param header
     * @throws FileNotFoundException
     * Intended to log all chat messages and commands by writing them out to a text file.  This functionality is
     * currectly not used.
     */
    public synchronized void log(Long msgID, Header header) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(log);
        pw.println("[" + msgID + "] " + header.toString());
    }

    /**
     * @param msgID
     * @param header
     */
    public synchronized void addToBufferedTreeMap(long msgID, Header header) {
        this.bufferedMessages.put(msgID,new BufferedMessageEntry(header));
    }

    /**
     * @param msgID
     */
    public synchronized void removeFromBufferedTreeMap(long msgID) {
        this.bufferedMessages.remove(msgID);
    }

    /**
     * @param msgID
     * @return BUfferedMessageEntry - buffered message with message id matching msgID.
     */
    public synchronized BufferedMessageEntry getFromBufferedTreeMap(Long msgID) {
        return this.bufferedMessages.get(msgID);
    }

    /**
     * @param s
     * @return User with username matching s and removes user from hashmap
     */
  public synchronized User removeUser(String s) {
    return this.users.remove(s);
  }

    /**
     * @param s
     */
  public synchronized void muteUser(String s) {
      User u = this.users.get(s);
      if (u != null) u.setMuted(true);
  }

    /**
     * @param s
     * @return boolean
     */
  public synchronized boolean containsUser(String s) {
      return this.users.containsKey(s);
  }

    /**
     * @param s
     * @return User with name matching s
     */
  public synchronized User getUser(String s) {
    return this.users.get(s);
  }

    /**
     * @return the list of users currently used in the users hashmap keyset.
     */
  public ArrayList<String> getUsers() {
    return new ArrayList<>(this.users.keySet());
  }

    /**
     * Class used to encapsulate write and command headers to be stored in the buffered messages treemap. Includes the
     * time a header was received which is intended to be used with logging.
     */
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
