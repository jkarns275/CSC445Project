package networking;

import common.Constants;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.*;

/**
 * Manages heartbeats for a specific channel.
 */
class HeartbeatReceiver {

  public static long HEARTBEAT_MAX = 4 * Constants.SECONDS_TO_NANOS;

  private class Client {
    public InetSocketAddress address;
    private long receivedAt;

    public Client(InetSocketAddress address, long receivedAt) {
      this.address = address; this.receivedAt = receivedAt;
    }

    @Override
    public int hashCode() {
      return address.hashCode();
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

    public long getReceivedAt() {
      return receivedAt;
    }

    public void setReceivedAt(long receivedAt) {
      this.receivedAt = receivedAt;
    }
  }


  private final HashMap<InetSocketAddress, Client> clients = new HashMap<>();
  private final ArrayList<InetSocketAddress> clientsToPurge = new ArrayList<>();

  /**
   * Check the priority queue to see if there are any clients who haven't send a heartbeat
   * within HEARTBEAT_MAX seconds. If there are, remove all of them!
   *
   * This should be called at least every couple of seconds
   */
  public void update() {
    long now = System.nanoTime();
    clientsToPurge.clear();
    clients.forEach((address, client) -> {
      if (now - client.receivedAt > HEARTBEAT_MAX)
        clientsToPurge.add(address);
    });
    clientsToPurge.forEach(clients::remove);
  }

  /**
   * Signals a heartbeat from the specified socket address was received. processHeartbeat should only be called if the
   * channelID specified in the header corresponds to this HeartbeatReceiver.
   * @param socketAddress
   */
  public void processHeartbeat(InetSocketAddress socketAddress) {
    long now = System.nanoTime();
    if (socketAddress == null) return;
    if (clients.containsKey(socketAddress))
      clients.get(socketAddress).setReceivedAt(now);
    else
      clients.put(socketAddress, new Client(socketAddress, now));
  }

  public void removeClient(InetSocketAddress clientAddress) {
    clients.remove(clientAddress);
  }

  public HashSet<InetSocketAddress> getClients() {
    return new HashSet<>(clients.keySet());
  }
}
