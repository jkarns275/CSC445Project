package client;

import client.reps.ClientChannel;
import common.Constants;
import networking.*;
import networking.headers.*;
import static common.Constants.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

public class Client implements Runnable {

  private static final long CLIENT_PARALLELISM = 4;
  private static final long HEARTBEAT_MANAGER_CLEAN_DELAY = 20 * Constants.SECONDS_TO_NANOS;

  private final int port;
  private final LinkedBlockingQueue<Header> sendQueue = new LinkedBlockingQueue<>();
  private final HeartbeatSender heartbeatSender;
  private final HeaderIOManager hio;
  private final SocketManager socket;
  private final InetSocketAddress server;
  private final HeartbeatManager heartbeatManager;
  private final int timeout = 5000;

  private boolean sourceReceived = false;
  private Header prevHeader;

  public Client(InetSocketAddress server, int port) throws IOException {
    this.port = port; this.server = server;
    this.hio = new HeaderIOManager(new InetSocketAddress(InetAddress.getLocalHost(), port), 4);
    this.socket = this.hio.getSocket();
    this.heartbeatSender = new HeartbeatSender(this.socket, server);
    this.heartbeatManager = new HeartbeatManager();
  }

  @Override
  public void run() {
    long last = System.nanoTime();
    for (;;)
      try {
        this.hio.update();
        this.heartbeatSender.update();
        this.heartbeatManager.update();

        if (System.nanoTime() - last > Client.HEARTBEAT_MANAGER_CLEAN_DELAY) {
          this.heartbeatManager.clean();
          last = System.nanoTime();
        }

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
            case OP_WRITE:      break;
            case OP_JOIN:       break;
            case OP_LEAVE:      break;
            case OP_SOURCE:
                prevHeader =  header;
                sourceReceived = true;
                notifyAll();
                break;
            case OP_NAK:        break;
            case OP_ERROR:      break;
            case OP_HEARTBEAT:
              HeartbeatHeader heartbeatHeader = (HeartbeatHeader) header;
              this.heartbeatManager.processHeartbeat(heartbeatHeader.getChannelID(), srcAddr);
              break;
            case OP_INFO:       break;
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

  public void sendJoinHeader(String channelName, String desiredUsername) throws InterruptedException {
    hio.send(hio.packetSender(new JoinHeader(desiredUsername, channelName), server));
  }

  public void sendLeaveHeader(long channelID) { hio.send(hio.packetSender(new LeaveHeader(channelID), server)); }

  public void addChannelHeartbeat(long channelID) throws InterruptedException {
    this.heartbeatSender.addChannel(channelID);
  }

  public void removeChannelHeartbeat(long channelID) throws InterruptedException {
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

}
