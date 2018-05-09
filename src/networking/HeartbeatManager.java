package networking;

import common.Constants;
import common.Tuple;
import networking.headers.HeartbeatHeader;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

public class HeartbeatManager {

  private static final long UPDATE_FREQUENCY = Constants.SECONDS_TO_NANOS * 1;

  private final ConcurrentHashMap<Long, HeartbeatReceiver> receivers = new ConcurrentHashMap<>();

  public final AtomicBoolean shouldKill = new AtomicBoolean(false);

  private class HeartbeatWorker implements Runnable {
    private final AtomicBoolean shouldKill;
    private final HeartbeatManager heartbeatManager;
    private final SynchronousQueue<Tuple<Long, InetSocketAddress>> heartbeatQueue;
    public HeartbeatWorker(AtomicBoolean shouldkill, HeartbeatManager heartbeatManager,
                           SynchronousQueue<Tuple<Long, InetSocketAddress>> heartbeatQueue) {
      this.shouldKill = shouldkill;
      this.heartbeatManager = heartbeatManager;
      this.heartbeatQueue = heartbeatQueue;
    }

    @Override
    public void run() {
      long lastUpdateTime = 0;
      while (!shouldKill.get()) {
        heartbeatManager.update();
        heartbeatManager.clean();
        Tuple<Long, InetSocketAddress> item;

        for (int i = 0; i < 8; i++) {
          if ((item = heartbeatQueue.poll()) != null) {
            if (!heartbeatManager.receivers.containsKey(item.first()))
              heartbeatManager.receivers.put(item.first(), new HeartbeatReceiver());
            heartbeatManager.receivers
              .get(item.first())
              .processHeartbeat(item.second());
          } else {
            break;
          }
        }
      }
    }

  }

  private SynchronousQueue<Tuple<Long, InetSocketAddress>> heartbeatQueue = new SynchronousQueue<>();
  private HeartbeatWorker heartbeatWorker = new HeartbeatWorker(shouldKill, this, heartbeatQueue);
  private Thread heartbeatWorkerThread = new Thread(heartbeatWorker);

  public HeartbeatManager() {
    heartbeatWorkerThread.start();
  }

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
    try {
      this.heartbeatQueue.put(new Tuple<>(channelID, address));
    } catch (Exception e) {
      System.out.println("Failed to process heartbeat!");
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
