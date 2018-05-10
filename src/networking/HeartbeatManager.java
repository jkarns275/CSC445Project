package networking;

import common.Constants;
import common.Tuple;
import networking.headers.HeartbeatHeader;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *   The HeartbeatManager is just a router - it routes heartbeats to their corresponding HeartbeatReceivers (if there
 *   is one). If there is not one it is ignored. Updating of the HeartbeatReceivers and processing of the heartbeats
 *   is done in a worker thread.
 */
public class HeartbeatManager {

  private static final long UPDATE_FREQUENCY = Constants.SECONDS_TO_NANOS * 4;

  private final ConcurrentHashMap<Long, HeartbeatReceiver> receivers = new ConcurrentHashMap<>();

  /**
   * Used to quickly end the HeartbeatWorker thread.
   */
  public final AtomicBoolean shouldKill = new AtomicBoolean(false);

  private class HeartbeatWorker implements Runnable {
    private final AtomicBoolean shouldKill;
    private final HeartbeatManager heartbeatManager;
    private final LinkedBlockingQueue<Tuple<Long, InetSocketAddress>> heartbeatQueue;
    public HeartbeatWorker(AtomicBoolean shouldkill, HeartbeatManager heartbeatManager,
                           LinkedBlockingQueue <Tuple<Long, InetSocketAddress>> heartbeatQueue) {
      this.shouldKill = shouldkill;
      this.heartbeatManager = heartbeatManager;
      this.heartbeatQueue = heartbeatQueue;
    }

    @Override
    public void run() {
      long lastUpdateTime = 0;
      Tuple<Long, InetSocketAddress> item;
      while (!shouldKill.get()) {
        heartbeatManager.update();
        heartbeatManager.clean();

        for (int i = 0; i < 32; i++) {
          try {
            if ((item = heartbeatQueue.poll(2, TimeUnit.MILLISECONDS)) != null) {
              if (!heartbeatManager.receivers.containsKey(item.first()))
                heartbeatManager.receivers.put(item.first(), new HeartbeatReceiver());
              heartbeatManager.receivers
                .get(item.first())
                .processHeartbeat(item.second());
            } else {
              break;
            }
          } catch (InterruptedException e) {
            e.printStackTrace();
            break;
          }
        }
      }
    }

  }

  private LinkedBlockingQueue<Tuple<Long, InetSocketAddress>> heartbeatQueue = new LinkedBlockingQueue<>();
  private HeartbeatWorker heartbeatWorker = new HeartbeatWorker(shouldKill, this, heartbeatQueue);
  private Thread heartbeatWorkerThread = new Thread(heartbeatWorker);

  public HeartbeatManager() {
    heartbeatWorkerThread.start();
  }

  /**
   * Should be called as frequently as you would like dead clients to be dropped. Simply calls update on each
   * HeartbeatReceiver.
   */
  public void update() {
    for (HeartbeatReceiver hr : this.receivers.values())
      hr.update();
  }

  /*
   * Removes all HeartBeat receivers that are empty
   */
  private void clean() {
    ArrayList<Long> toRemove = new ArrayList<>();
    receivers.forEach((key, value) -> { if (value.getClients().size() == 0) toRemove.add(key); });
    for (Long removeMe : toRemove) receivers.remove(removeMe);
  }

  /**
   * offers the supplied heartbeat to the heartbeatQueue
   * @param channelID The channelID to route the heartbeat to
   * @param address The source of the heartbeat
   */
  public void processHeartbeat(long channelID, InetSocketAddress address) {
    try {
      this.heartbeatQueue.offer(new Tuple<>(channelID, address));
    } catch (Exception e) {
      System.out.println("Failed to process heartbeat!");
    }
  }

  /**
   * @param channelID
   * @return The active clients for the specified channel - if that channel exists and there are clients.
   */
  public Optional<HashSet<InetSocketAddress>> getActiveClients(long channelID) {
    HeartbeatReceiver heartbeatReceiver = this.receivers.get(channelID);
    if (heartbeatReceiver != null) {
      Optional<HashSet<InetSocketAddress>> p = Optional.of(new HashSet<InetSocketAddress>(heartbeatReceiver.getClients()));
      return p;
    } else {
      return Optional.empty();
    }
  }

}
