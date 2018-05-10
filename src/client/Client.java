package client;

import client.reps.ClientChannel;
import common.Constants;
import common.Tuple;
import networking.*;
import networking.headers.*;
import static common.Constants.*;
import static networking.headers.ErrorHeader.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client implements Runnable {

  private static final int CLIENT_PARALLELISM = 4;
  private static final long HEARTBEAT_MANAGER_CLEAN_DELAY = Constants.SECONDS_TO_NANOS / 8;
  private static final int WRITE_TIMEOUT = 5;

  private final int port;
  private final LinkedBlockingQueue<Header> sendQueue = new LinkedBlockingQueue<>();
  private final HeartbeatSender heartbeatSender;
  private final HeaderIOManager hio;
  private final SocketManager socket;
  private final InetSocketAddress server;
  private final ExecutorService pool = Executors.newFixedThreadPool(CLIENT_PARALLELISM);
  private final int timeout = 2000;
  private final AtomicBoolean shouldKill = new AtomicBoolean(false);

  // Maps (Channel ID) -> (Nickname for this client in that channel)
  private final ConcurrentHashMap<Long, String> channels = new ConcurrentHashMap<>();

  // Maps room name to corresponding source header
  private ConcurrentSkipListMap<String, SourceHeader> sourcesReceived = new ConcurrentSkipListMap<>();
  private ArrayList<Long> joinHeadersToPurge = new ArrayList<>();

  // Maps (write header magic) -> (time sent, corresponding write header)
  private ConcurrentSkipListMap<Long, Tuple<Long, WriteHeader>> writeRecvQueue = new ConcurrentSkipListMap<>();
  // Maps (Join Header) -> (time sent)
  private ConcurrentSkipListMap<Long, JoinHeader> joinRecvQueue = new ConcurrentSkipListMap<>();

  private long nanoTime = System.nanoTime();
  private ArrayList<Tuple<String, String>> retries = new ArrayList<>();

  public Client(InetSocketAddress server, int port) throws IOException {

    this.port = port; this.server = server;
    this.hio = new HeaderIOManager(new InetSocketAddress(port), 8);
    this.socket = this.hio.getSocket();
    this.heartbeatSender = new HeartbeatSender(this.socket, server);
  }

  @Override
  public void run() {
    long last = System.nanoTime();
    for (;;) {
      try {
        if (shouldKill.get()) {
          hio.shutdownNow();
          pool.shutdown();
          pool.awaitTermination(1, TimeUnit.SECONDS);
        }

        if (!this.hio.probablyConnected()) {
          MainFrame m = GUI.getInstance();
          ArrayList<Tuple<String, String>> retries = new ArrayList<>();
          channels.forEach((channelID, channelName) -> {
            retries.add(new Tuple<>(channelName, GUI.getInstance().getChannelNick(channelID)));
            m.removeChannel(channelID);
            heartbeatSender.removeChannel(channelID);
          });
          channels.clear();
        } else if (!this.retries.isEmpty()) {
          for (Tuple<String, String> r : retries) {
            GUI.getInstance().parseMessage("/join " + r.first() + " " + r.second());
          }
          retries.clear();
        }

        nanoTime = System.nanoTime();

        this.hio.update();
        this.heartbeatSender.update();

        updateWriteRecvQueue();
        updateJoinRecvQueue();

        SocketRequest receive;
        while ((receive = this.hio.recv()) != null) {
          Header header = receive.getHeader();
          InetSocketAddress srcAddr = receive.getAddress();

          if (!(srcAddr.getAddress().equals(this.server.getAddress()))
              || srcAddr.getPort() != this.server.getPort()) {
            System.err.println("Received message from: '" + receive.getAddress() + "', this is not the server address.");
            continue;
          }

          // TODO: Create handlers for the remaining headers
          switch (header.opcode()) {
            case OP_WRITE:
              WriteHeader writeHeader = (WriteHeader) header;
              if (writeHeader.getMsgID() == -1) break;

              // check if write response
              String username = this.channels.get(writeHeader.getChannelID());
              // If this client has a username for the channel this writeHeader is intended for,
              // the username the header specifies is the same as ours, and if we are waiting
              // for a writeHeader with the same magic value.
              if (username != null && username.equals(writeHeader.getUsername()) &&
                  this.writeRecvQueue.containsKey(writeHeader.getMagic())) {
                  this.writeRecvQueue.remove(writeHeader.getMagic());
              }
              // Display the message either way
              pool.submit(() -> GUI.writeMessage( writeHeader.getChannelID(), writeHeader.getMsgID(),
                                                  writeHeader.getUsername(), writeHeader.getMsg()));

              break;
           case OP_SOURCE:
              SourceHeader sourceHeader = (SourceHeader) header;
                sourcesReceived.put(sourceHeader.getChannelName(), sourceHeader);
                this.channels.put(sourceHeader.getChannelID(), sourceHeader.getAssignedUsername());
                /*
                prevHeader =  header;
                sourceReceived = true;
                SourceHeader sourceHeader = (SourceHeader) header;
                if (channels.containsKey(sourceHeader.getChannelID()))
                  channels.remove(sourceHeader.getChannelID());
                channels.put(sourceHeader.getChannelID(), sourceHeader.getAssignedUsername());

                synchronized (this) {
                    notifyAll();
                }*/
                break;
            case OP_INFO:
                InfoHeader infoHeader = (InfoHeader) header;
                pool.submit(() -> handleInfo(infoHeader));
                break;
            case OP_ACK:
              hio.processAckHeader((AckHeader) header, srcAddr);
              break;

            case OP_ERROR:
              System.out.println("Received error header from server with error message \"" + ((ErrorHeader) header)
                .getErrorMsg());
              break;
            case OP_NAK:
            case OP_JOIN:
            case OP_LEAVE:
            case OP_COMMAND:
            case OP_HEARTBEAT:
            case OP_CONG:
            default:
              System.err.println("Received header from server with invalid opcode: " + header.opcode());
          }
        }
    } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

  }

  public void handleInfo(InfoHeader infoHeader) {
      long channelID = infoHeader.getChannelID();
      String message = infoHeader.getMessage();
      switch (infoHeader.getInfoCode()) {
          case 0: // user kicked
              GUI.kickUser(channelID);
              break;
          case 1: // mute user
              GUI.setMuteStatus(channelID, true);
              break;
          case 2: // unmute user
              GUI.setMuteStatus(channelID, false);
              break;
          case 3: // server message
              GUI.writeInfo(channelID, infoHeader.getMessageID(), infoHeader.getMessage());
              break;
          case 4: // closing connection
              GUI.getInstance().removeChannel(channelID);
      }
      GUI.writeInfo(channelID, infoHeader.getMessageID(), infoHeader.getMessage());
  }

  public void sendCommandHeader(long channelID, String command) {
    hio.send(hio.packetSender(new CommandHeader(channelID, command), server));
  }

  private void sendWriteHeader(long channelID, long messageID, String nick, String message) {
    WriteHeader writeHeader = new WriteHeader(channelID, messageID, message, nick);
    this.writeRecvQueue.put(writeHeader.getMagic(), new Tuple<>(nanoTime, writeHeader));
    hio.send(hio.packetSender(writeHeader, server));
  }

  private void sendJoinHeader(String channelName, String desiredUsername) {
    JoinHeader joinHeader = new JoinHeader(desiredUsername, channelName);
    this.joinRecvQueue.put(nanoTime, joinHeader);
    hio.send(hio.packetSender(joinHeader, server));
  }

  public void sendErrorHeader(byte errorCode, String errorMessage) {
    hio.send(hio.packetSender(new ErrorHeader(errorCode, errorMessage), server));
  }

  public void sendNAKHeader(long channelID, long lower, long upper) {
    hio.send(hio.packetSender(new NakHeader(lower, upper, channelID), server));
  }

  public void addChannelHeartbeat(long channelID) throws InterruptedException {
    this.heartbeatSender.addChannel(channelID);
  }

  public void removeChannel(long channelID) {
    this.heartbeatSender.removeChannel(channelID);
    this.channels.remove(channelID);
  }

  /*
  public synchronized Optional<ClientChannel> joinChannel(String channelName, String nick) {
      try {
          sendJoinHeader(channelName, nick);
          wait(timeout);
          if (!sourceReceived) {
              return Optional.empty();
          }
          sourceReceived = false;
          SourceHeader header = (SourceHeader) prevHeader;
          ClientChannel channel = new ClientChannel(header.getChannelID(),
                  header.getChannelName(), header.getAssignedUsername());
          return Optional.of(channel);
      } catch (InterruptedException e) {
          e.printStackTrace();
          return Optional.empty();
      }
  }

  public synchronized boolean leaveChannel(long channelID) {
      try {
          sendLeaveHeader(channelID);
          wait(timeout);
          if (!leaveSuccess) {
              return false;
          }
          leaveSuccess = false;
          return true;
      } catch (InterruptedException e) {
          e.printStackTrace();
          return false;
      }
  }
  */

  public void kill() {
    this.shouldKill.set(true);
    try {
      Thread.sleep(250, 0);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public synchronized void joinChannel(String channelName, String desiredUsername) {
    sendJoinHeader(channelName, desiredUsername);
  }

  public synchronized void sendMessage(long channelID, long messageID, String nick, String message) {
    sendWriteHeader(channelID, messageID, nick, message);
  }


  private void updateJoinRecvQueue() {
    while (!this.joinRecvQueue.isEmpty()) {
      if (nanoTime - this.joinRecvQueue.firstEntry().getKey() > Constants.SECONDS_TO_NANOS * WRITE_TIMEOUT) {
        Map.Entry<Long, JoinHeader> entry = this.joinRecvQueue.pollFirstEntry();
        JoinHeader joinHeader = entry.getValue();
        pool.submit(() -> GUI.getInstance().printToMesssageChannel("ERROR", "Failed join channel'" +
          joinHeader.getChannelName() + "'"));
        continue;
      }
      break;
    }
    this.joinHeadersToPurge.clear();
    this.joinRecvQueue.forEach((timeSent, joinHeader) -> {
      if (this.sourcesReceived.containsKey(joinHeader.getChannelName())) {
        SourceHeader sourceHeader = this.sourcesReceived.get(joinHeader.getChannelName());
        try {
          addChannelHeartbeat(sourceHeader.getChannelID());
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        this.joinHeadersToPurge.add(timeSent);
        GUI.getInstance().addChannel(new ChannelPanel(sourceHeader.getChannelID(), sourceHeader.getChannelName(),
          sourceHeader.getAssignedUsername()));
        channels.put(sourceHeader.getChannelID(),sourceHeader.getAssignedUsername());
        this.sourcesReceived.remove(joinHeader.getChannelName());
      }
    });
    for (Long timeSent: joinHeadersToPurge)
      this.joinRecvQueue.remove(timeSent);
  }

  private void updateWriteRecvQueue() {
    while (!this.writeRecvQueue.isEmpty()) {
      if (nanoTime - this.writeRecvQueue.firstEntry().getValue().first() > Constants.SECONDS_TO_NANOS *
        WRITE_TIMEOUT) {
        Map.Entry<Long, Tuple<Long, WriteHeader>> entry = this.writeRecvQueue.pollFirstEntry();
        WriteHeader writeHeader = entry.getValue().second();
        pool.submit(() -> GUI.getInstance().printToMesssageChannel("ERROR", "Failed to send message '" +
          writeHeader.getMsg() + "' to channel " + writeHeader.getChannelID()));
        continue;
      }
      break;
    }
  }

}
