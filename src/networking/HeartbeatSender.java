package networking;

import common.Constants;
import networking.headers.HeartbeatHeader;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Sends heartbeats to the specified server for the specified channels every 8th of a second.
 */
public class HeartbeatSender {

  private final static long HEARTBEAT_INTERVAL = Constants.SECONDS_TO_NANOS / 8;

  private final ArrayList<Long> channels = new ArrayList<>();
  private final SocketManager socket;
  private final InetSocketAddress serverAddress;
  private long lastSendTime;
  private final HeartbeatHeader heartbeatHeader = new HeartbeatHeader(0);

  public HeartbeatSender(SocketManager socket, InetSocketAddress serverAddress) {
    this.lastSendTime = System.nanoTime();
    this.socket = socket;
    this.serverAddress = serverAddress;
  }

  public void update() throws InterruptedException {
    if (System.nanoTime() - this.lastSendTime > HeartbeatSender.HEARTBEAT_INTERVAL) this.send();
  }

  public void addChannel(long channelID) throws InterruptedException {
    this.heartbeatHeader.setChannelID(channelID);
    this.socket.send(this.heartbeatHeader, this.serverAddress);
    this.channels.add(channelID);
  }
  public void removeChannel(long channelID) { this.channels.remove(channelID); }

  private void send() throws InterruptedException {
    this.lastSendTime = System.nanoTime();
    for (long channel : this.channels) {
      this.heartbeatHeader.setChannelID(channel);
      this.socket.send(new HeartbeatHeader(this.heartbeatHeader), this.serverAddress);
    }
  }

}
