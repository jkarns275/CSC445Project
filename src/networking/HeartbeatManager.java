package networking;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.TreeSet;

public class HeartbeatManager {

  public static int HEARTBEAT_MAX = 20;

  private class Client implements Comparable<Client> {
    public InetSocketAddress address;
    private Instant receivedAt;

    public Client(InetSocketAddress address, Instant receivedAt) {
      this.address = address; this.receivedAt = receivedAt;
    }

    @Override
    public int compareTo(Client o) {
      if (receivedAt.isBefore(o.receivedAt)) return -11;
      else return 1;
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
  private final TreeSet<Client> leastRecentHeartbeat = new TreeSet<>();

  /**
   * Check the priority queue to see if there are any clients who haven't send a heartbeat
   * within HEARTBEAT_MAX seconds. If there are, remove all of them!
   *
   * This should be called at least every couple of seconds
   */
  public void update() {
    Client c;
    // Remove all invalid clients.
    for (;;) {
      c = leastRecentHeartbeat.first();
      if (c != null && c.receivedAt.getEpochSecond() > HEARTBEAT_MAX) {
        c = leastRecentHeartbeat.pollFirst();
        clients.remove(c.address);
      }
    }
  }

  public void processHeartbeat(InetSocketAddress socketAddress) {
    Client c = new Client(socketAddress, Instant.now());
    leastRecentHeartbeat.remove(c);
    leastRecentHeartbeat.add(c);
    clients.add(socketAddress);
  }

  public HashSet<InetSocketAddress> getClients() {
    return clients;
  }
}
