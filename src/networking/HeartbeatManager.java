package networking;

import common.Constants;
import networking.headers.HeartbeatHeader;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class HeartbeatManager {

  private static final long UPDATE_FREQUENCY = Constants.SECONDS_TO_NANOS * 1;

  private final HashMap<Long, HeartbeatReceiver> receivers = new HashMap<>();

  private final AtomicBoolean shouldKill = new AtomicBoolean(false);

  private class HeartbeatWorker implements Runnable {
    private final AtomicBoolean shouldKill;
    private final HeartbeatManager heartbeatManager;
    public HeartbeatWorker(AtomicBoolean shouldkill, HeartbeatManager heartbeatManager) {
      this.shouldKill = shouldkill;
      this.heartbeatManager = heartbeatManager;
    }

    @Override
    public void run() {
      long lastUpdateTime = 0;
      while (!shouldKill.get()) {
        if (System.nanoTime() - lastUpdateTime > UPDATE_FREQUENCY) {
          heartbeatManager.update();
        }
      }
    }

  };

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
