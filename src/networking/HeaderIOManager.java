package networking;

import networking.headers.AckHeader;
import networking.headers.Header;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class HeaderIOManager {
  private final ThreadPoolExecutor pool;
  private final ArrayBlockingQueue<AckResult> resultQueue = new ArrayBlockingQueue<>(1024);
  private final HashMap<AckHeader, ArrayBlockingQueue<InetSocketAddress>> ackQueues = new HashMap<>();
  private final ArrayBlockingQueue<SendJob> doneQueue = new ArrayBlockingQueue<>(1024);
  private final SocketManager socket;

  public HeaderIOManager(InetSocketAddress address, int parallelism) throws IOException {
    pool = (ThreadPoolExecutor) Executors.newWorkStealingPool(parallelism);
    socket = new SocketManager(address, pool);
  }

  public void send(SendJob job) { pool.execute(job); }

  public Header recv() throws InterruptedException { throw new NotImplementedException(); }

  /**
   * Updates the HeaderIOManager. This involves checking for finished SendJobs and resending them if necessary.
   */
  public void update() {  }

  public void processAckHeader(AckHeader ackHeader, InetSocketAddress source) throws InterruptedException {
    if (this.ackQueues.containsKey(ackHeader)) {
      ArrayBlockingQueue<InetSocketAddress> queue = this.ackQueues.get(ackHeader);
      queue.put(source);
    }
  }

  public ArrayBlockingQueue<SendJob> getDoneQueue() {
    return doneQueue;
  }
}
