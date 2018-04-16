package networking;

import networking.headers.AckHeader;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class HeaderIOManager {
  private final ThreadPoolExecutor pool;
  private final SynchronousQueue<AckResult> resultQueue = new SynchronousQueue<>();
  private final HashMap<AckHeader, SynchronousQueue<InetSocketAddress>> ackQueues = new HashMap<>();

  public HeaderIOManager(int parallelism) { pool = (ThreadPoolExecutor) Executors.newWorkStealingPool(); }

  public void send(SendJob job) { pool.execute(job); }

  public void update() {

  }

  public void shutdown() { }

  public void processAckHeader(AckHeader ackHeader, InetSocketAddress source) {
    if ()
  }
}
