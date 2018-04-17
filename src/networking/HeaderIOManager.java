package networking;

import networking.headers.AckHeader;
import networking.headers.Constants;
import networking.headers.Header;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class HeaderIOManager {
  private final ThreadPoolExecutor pool;
  private final LinkedBlockingQueue<AckResult> ackResultQueue = new LinkedBlockingQueue<>(1024);
  private final HashMap<AckHeader, ArrayBlockingQueue<InetSocketAddress>> ackQueues = new HashMap<>();
  private final LinkedBlockingQueue<SendJob> doneQueue = new LinkedBlockingQueue<>(1024);

  public SocketManager getSocket() {
    return socket;
  }

  private final SocketManager socket;

  public HeaderIOManager(InetSocketAddress address, int parallelism) throws IOException {
    pool = new ThreadPoolExecutor(parallelism, parallelism, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    socket = new SocketManager(address, pool);
  }

  public SendJob packetSender(Header h, InetSocketAddress to) {
    return new PacketSender(h.opcode() != Constants.OP_ACK, h, to, socket, doneQueue);
  }

  public SendJob multicastPacketSender(Header h, Collection<InetSocketAddress> to) {
    return new MulticastPacketSender(h.opcode() != Constants.OP_ACK, h, new HashSet<>(to), socket, doneQueue);
  }

  public void send(SendJob job) { pool.execute(job); }

  public SocketRequest recv() throws InterruptedException { return socket.recv(); }

  /**
   * Updates the HeaderIOManager. This involves checking for finished SendJobs and resending them if necessary.
   */
  public void update() {
    // Process all SendJobs that have finished
    try {
      while (!doneQueue.isEmpty()) {
        SendJob finishedJob = doneQueue.poll(0, TimeUnit.MILLISECONDS);
        if (finishedJob.needsAck()) {
          AckHeader header = finishedJob.getAckHeader();
          ArrayBlockingQueue<InetSocketAddress> queue = new ArrayBlockingQueue<>(finishedJob.numClients());
          ackQueues.put(header, queue);
          pool.execute(finishedJob.getAckJob(queue, ackResultQueue));
        }
      }
    } catch (InterruptedException e) {
      System.err.println("Failed to poll from doneQueue without waiting.");
    }

    // Process all AckResults
    try {
      while (!ackResultQueue.isEmpty()) {
        AckResult r = ackResultQueue.poll(0, TimeUnit.MILLISECONDS);
        if (!r.wasSuccessful()) {
          SendJob sendJob = r.resend(doneQueue);
          sendJob.setNeedsAck(false);
          send(sendJob);
        } else {
          this.ackQueues.remove(r.resend(doneQueue).getAckHeader());
        }
      }
    } catch (InterruptedException e) {
      System.err.println("Failed to poll from ackResultQueue without waiting.");
    }
  }

  public void processAckHeader(AckHeader ackHeader, InetSocketAddress source) throws InterruptedException {
    if (this.ackQueues.containsKey(ackHeader)) {
      ArrayBlockingQueue<InetSocketAddress> queue = this.ackQueues.get(ackHeader);
      queue.put(source);
    }
  }

  public LinkedBlockingQueue<SendJob> getDoneQueue() { return doneQueue; }

  public List<Runnable> shutdownNow() throws InterruptedException { this.socket.send(new KillRequest()); return this.pool.shutdownNow(); }
}
