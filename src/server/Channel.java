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
    private ArrayList<String> usernames = new ArrayList<>();
    private ArrayList<User> usersToPurge = new ArrayList<>();

  /*
     * TreeMap buffering messages sent from the server to clients in the given channel.
     */
    private TreeMap<Long,BufferedMessageEntry> bufferedMessages = new TreeMap<>();

    public Channel(String channelName, long id) {
        this.channelName = channelName;
//        this.log = new File("logs/" + channelName + ".txt");
        this.channelID = id;
    }

    /*
     *
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

    /*
     *
     */
    public synchronized void removeUser(User user) {
        if (!users.isEmpty())
          this.sendPacket(new InfoHeader(channelID, InfoHeader.INFO_SERVER_MSG, this
            .getAndIncrementMsgID(),
        "User '" + user.username + "' has left."));
        users.remove(user.username);
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

    public synchronized void update(HashSet<InetSocketAddress> heartbeatClients) {
/*        Calendar calendar = Calendar.getInstance();
        if (lastLoggedMsg != msgID) {
            for (long index = lastLoggedMsg+1; index != msgID; index++) {
                BufferedMessageEntry e = bufferedMessages.get(index);
                if (e == null) continue;
                calendar.setTimeInMillis(e.militime);
                lastLoggedMsg = index;
            }
        }*/
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

  public synchronized User removeUser(String s) {
    return this.users.remove(s);
  }

  public synchronized void muteUser(String s) {
      User u = this.users.get(s);
      if (u != null) u.setMuted(true);
  }

  public synchronized boolean containsUser(String s) {
      return this.users.containsKey(s);
  }

  public synchronized User getUser(String s) {
    return this.users.get(s);
  }

  public ArrayList<String> getUsers() {
    return new ArrayList<>(this.users.keySet());
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
