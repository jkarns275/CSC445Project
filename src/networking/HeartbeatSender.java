package networking;

import networking.headers.HeartbeatHeader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.HashSet;

/**
 * Sends heartbeats to the specified server for the specified channels every ~8 seconds.
 */
public class HeartbeatSender {

  private final static long HEARTBEAT_INTERVAL = 8; // Every 8 seconds

  private final HashSet<Long> channels = new HashSet<>();
  private final SocketManager socket;
  private final InetSocketAddress serverAddress;
  private Instant lastSendTime;
  private final HeartbeatHeader heartbeatHeader = new HeartbeatHeader(0);

  public HeartbeatSender(SocketManager socket, InetSocketAddress serverAddress) {
    this.lastSendTime = Instant.now();
    this.socket = socket;
    this.serverAddress = serverAddress;
  }

  public void update() throws InterruptedException {
    if (Instant.now().getEpochSecond() - this.lastSendTime.getEpochSecond() > HeartbeatSender.HEARTBEAT_INTERVAL) this.send();
  }

  public void addChannel(long channelID) throws InterruptedException {
    this.heartbeatHeader.setChannelID(channelID);
    this.socket.send(this.heartbeatHeader, this.serverAddress);
    this.channels.add(channelID);
  }
  public void removeChannel(long channelID) { this.channels.remove(channelID); }

  private void send() throws InterruptedException {
    this.lastSendTime = Instant.now();
    for (Long channel : this.channels) {
      this.heartbeatHeader.setChannelID(channel);
      this.socket.send(this.heartbeatHeader, this.serverAddress);
    }
  }

}
