package networking;

import java.net.InetSocketAddress;
import java.util.*;

public class HeartbeatManager {

  private final HashMap<Long, HeartbeatReceiver> receivers = new HashMap<>();

  public HeartbeatManager() { }

  /**
   * Should be called as frequently as you would like dead clients to be dropped.
   */
  public void update() {
    for (HeartbeatReceiver hr : this.receivers.values()) hr.update();
  }

  /*
   * Removes all HeartBeat receivers that are empty
   */
  public void clean() {
    ArrayList<Long> toRemove = new ArrayList<>();
    receivers.forEach((key, value) -> { if (value.getClients().size() == 0) toRemove.add(key); });
    for (Long removeMe : toRemove) receivers.remove(removeMe);
  }

  public void processHeartbeat(long channelID, InetSocketAddress address) {
    this.receivers.putIfAbsent(channelID, new HeartbeatReceiver());
    this.receivers.get(channelID).processHeartbeat(address);
  }

  public void removeClient(long channelID, InetSocketAddress address) {
    HeartbeatReceiver heartbeatReceiver = this.receivers.get(channelID);
    if (heartbeatReceiver != null) {
      heartbeatReceiver.removeClient(address);
    }
  }

  public Optional<HashSet<InetSocketAddress>> getActiveClients(long channelID) {
    HeartbeatReceiver heartbeatReceiver = this.receivers.get(channelID);
    if (heartbeatReceiver != null) {
      return Optional.of(new HashSet<InetSocketAddress>(heartbeatReceiver.getClients()));
    } else {
      return Optional.empty();
    }
  }

}
