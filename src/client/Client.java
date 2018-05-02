package client;

import client.reps.ClientChannel;
import common.Constants;
import common.Tuple;
import networking.*;
import networking.headers.*;
import static common.Constants.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;

public class Client implements Runnable {

  private static final int CLIENT_PARALLELISM = 4;
  private static final long HEARTBEAT_MANAGER_CLEAN_DELAY = 20 * Constants.SECONDS_TO_NANOS;
  private static final int WRITE_TIMEOUT = 5;

  private final int port;
  private final LinkedBlockingQueue<Header> sendQueue = new LinkedBlockingQueue<>();
  private final HeartbeatSender heartbeatSender;
  private final HeaderIOManager hio;
  private final SocketManager socket;
  private final InetSocketAddress server;
  private final HeartbeatManager heartbeatManager;
  private final ExecutorService pool = Executors.newFixedThreadPool(CLIENT_PARALLELISM);
  private final int timeout = 2000;

  // Maps (Channel ID) -> (Nickname for this client in that channel)
  private final ConcurrentHashMap<Long, String> channels = new ConcurrentHashMap<>();

  private boolean sourceReceived = false;
  // Maps (write header magic) -> (time sent, corresponding write header)
  private ConcurrentSkipListMap<Long, Tuple<Long, WriteHeader>> writeRecvQueue = new ConcurrentSkipListMap<>();
  private boolean leaveSuccess = false;
  private String awaitNick = null;
  private Header prevHeader;
  private long nanoTime = System.nanoTime();

  public Client(InetSocketAddress server, int port) throws IOException {

    this.port = port; this.server = server;
    this.hio = new HeaderIOManager(new InetSocketAddress(InetAddress.getLocalHost(), port), 8);
    this.socket = this.hio.getSocket();
    this.heartbeatSender = new HeartbeatSender(this.socket, server);
    this.heartbeatManager = new HeartbeatManager();
  }

  @Override
  public void run() {
    long last = System.nanoTime();
    for (;;)
      try {
        nanoTime = System.nanoTime();

        System.out.println("Hey");
        this.hio.update();
        this.heartbeatSender.update();
        this.heartbeatManager.update();

        if (nanoTime - last > Client.HEARTBEAT_MANAGER_CLEAN_DELAY) {
          this.heartbeatManager.clean();
          last = nanoTime;
        }

        updateWriteRecvQueue();

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
              // check if write response
              String username = this.channels.get(writeHeader.getChannelID());
              // If this client has a username for the channel this writeHeader is intended for,
              // the username the header specifies is the same as ours, and if we are waiting
              // for a writeHeader with the same magic value.
              System.out.println("Received magic: " + writeHeader.getMagic() + " and have magic: " +
                (!this.writeRecvQueue.isEmpty() ? this.writeRecvQueue.pollFirstEntry().getValue().second().getMagic() : 0xDEADBEEF));
              
              if (username != null && username.equals(writeHeader.getUsername()) &&
                  this.writeRecvQueue.containsKey(writeHeader.getMagic())) {
                System.out.println("Howdy");
                prevHeader = header;
                this.writeRecvQueue.remove(writeHeader.getMagic());
              }
              // Display the message either way
              System.out.println("Received message with message ID '" + writeHeader.getMsgID() + "'");
              pool.submit(() -> GUI.writeMessage( writeHeader.getChannelID(), writeHeader.getMsgID(),
                                                  writeHeader.getUsername(), writeHeader.getMsg()));

              break;
            case OP_JOIN:       break;
            case OP_LEAVE:      break;
            case OP_SOURCE:
                prevHeader =  header;
                sourceReceived = true;
                SourceHeader sourceHeader = (SourceHeader) header;
                if (channels.containsKey(sourceHeader.getChannelID()))
                  channels.remove(sourceHeader.getChannelID());
                channels.put(sourceHeader.getChannelID(), sourceHeader.getAssignedUsername());
                System.out.println("Before");
                synchronized (this) {
                    notifyAll();
                }
              System.out.println("After");
                break;
            case OP_NAK:        break;
            case OP_ERROR:      break;
            case OP_HEARTBEAT:
              HeartbeatHeader heartbeatHeader = (HeartbeatHeader) header;
              this.heartbeatManager.processHeartbeat(heartbeatHeader.getChannelID(), srcAddr);
              break;
            case OP_INFO:
                InfoHeader infoHeader = (InfoHeader) header;
                if (infoHeader.getInfoCode() == 4) {
                    // connection closed confirmation
                    leaveSuccess = true;
                    synchronized (this) {
                        notifyAll();
                    }
                } else {
                    // channel info
                    pool.submit(() -> GUI.writeInfo(infoHeader.getChannelID(),
                            infoHeader.getMessageID(), infoHeader.getMessage()));
                }
                break;
            case OP_COMMAND:    break;
            case OP_CONG:       break;
            case OP_ACK:
              hio.processAckHeader((AckHeader) header, srcAddr);
              break;
            default:
              System.err.println("Received header from server with invalid opcode: " + header.opcode());
          }
        }
    } catch (InterruptedException e) {
        e.printStackTrace();
      }

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
    hio.send(hio.packetSender(new JoinHeader(desiredUsername, channelName), server));
  }

  private void sendLeaveHeader(long channelID) { hio.send(hio.packetSender(new LeaveHeader(channelID), server)); }

  public void sendErrorHeader(byte errorCode, String errorMessage) {
    hio.send(hio.packetSender(new ErrorHeader(errorCode, errorMessage), server));
  }

  public void sendNAKHeader(long channelID, long lower, long upper) {
    hio.send(hio.packetSender(new NakHeader(lower, upper, channelID), server));
  }

  public void addChannelHeartbeat(long channelID) throws InterruptedException {
    this.heartbeatSender.addChannel(channelID);
  }

  public void removeChannelHeartbeat(long channelID) {
    this.heartbeatSender.removeChannel(channelID);
  }

  public boolean isServerAlive() {
    Optional<ArrayList<InetSocketAddress>> result = heartbeatManager.getActiveClients(Constants.CLIENT_HEARTBEAT_CHANNEL);
    return result.isPresent() && result.get().size() > 0;
  }

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

  public synchronized Void sendMessage(long channelID, long messageID, String nick, String message) {
    sendWriteHeader(channelID, messageID, nick, message);
    return null;
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
