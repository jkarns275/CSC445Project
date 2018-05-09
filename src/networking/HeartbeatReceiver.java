package networking;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.*;

/**
 * Manages heartbeats for a specific channel.
 */
class HeartbeatReceiver {

  public static int HEARTBEAT_MAX = 3;

  private class Client {
    public InetSocketAddress address;
    private Instant receivedAt;

    public Client(InetSocketAddress address, Instant receivedAt) {
      this.address = address; this.receivedAt = receivedAt;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Client) {
        Client c = (Client) obj;
        return  this.address.getPort() == c.address.getPort()
                && this.address.getAddress().equals(c.address.getAddress());
      }
      return false;
    }

    public Instant getReceivedAt() {
      return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
      this.receivedAt = receivedAt;
    }
  }


  private final HashSet<InetSocketAddress> clients = new HashSet<>();
  private final ArrayDeque<Client> leastRecentHeartbeat = new ArrayDeque<>();

  /**
   * Check the priority queue to see if there are any clients who haven't send a heartbeat
   * within HEARTBEAT_MAX seconds. If there are, remove all of them!
   *
   * This should be called at least every couple of seconds
   */
  public void update() {
    Client c;
    // Remove all invalid clients.
    while(!leastRecentHeartbeat.isEmpty()) {
      c = leastRecentHeartbeat.peek();
      long timedif = Instant.now().getEpochSecond() - c.receivedAt.getEpochSecond();
      if (timedif > HEARTBEAT_MAX) {
        c = leastRecentHeartbeat.poll();
        clients.remove(c.address);
      }
    }
  }

  /**
   * Signals a heartbeat from the specified socket address was received. processHeartbeat should only be called if the
   * channelID specified in the header corresponds to this HeartbeatReceiver.
   * @param socketAddress
   */
  public synchronized void processHeartbeat(InetSocketAddress socketAddress) {
    if (socketAddress == null) { return; }
    Client c = new Client(socketAddress, Instant.now());
    if (leastRecentHeartbeat.contains(c))
      leastRecentHeartbeat.remove(c);
    leastRecentHeartbeat.offer(c);
    clients.add(socketAddress);
  }

  public void removeClient(InetSocketAddress clientAddress) {
    leastRecentHeartbeat.remove(new Client(clientAddress, Instant.now()));
    clients.remove(clientAddress);
  }

  public HashSet<InetSocketAddress> getClients() {
    return new HashSet<>(clients);
  }
}
