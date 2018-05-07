package networking;

import networking.headers.AckHeader;
import common.Constants;
import networking.headers.Header;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;

public class HeaderIOManager {
  private final ThreadPoolExecutor pool;
  private final LinkedBlockingQueue<AckResult> ackResultQueue = new LinkedBlockingQueue<>(1024);
  private final HashMap<AckHeader, ArrayBlockingQueue<InetSocketAddress>> ackQueues = new HashMap<>();
  private final InetSocketAddress sa;

  public SocketManager getSocket() {
    return socket;
  }

  private final SocketManager socket;

  // If the last packet the SocketManager tried to send wasn't successfully sent, it probably isn't connected
  public boolean probablyConnected() {
    return socket.probablyConnected();
  }

  public HeaderIOManager(InetSocketAddress address, int parallelism) throws IOException {
    pool = new ThreadPoolExecutor(parallelism, parallelism, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    socket = new SocketManager(address, pool);
    sa = address;
  }

  public SendJob packetSender(Header h, InetSocketAddress to) {
    SendJob job = new PacketSender(h.opcode() != Constants.OP_ACK && h.opcode() != Constants.OP_HEARTBEAT, h, to,
      socket);
    addToAckQueues(job);
    return job;
  }

  public SendJob multicastPacketSender(Header h, Collection<InetSocketAddress> to) {
    SendJob job = new MulticastPacketSender(h.opcode() != Constants.OP_ACK && h.opcode() != Constants.OP_HEARTBEAT,
      h, new HashSet<>(to), socket);
    addToAckQueues(job);
    return job;
  }

  private void addToAckQueues(SendJob job) {
    if (job.needsAck()) {
      AckHeader header = job.getAckHeader();
      ArrayBlockingQueue<InetSocketAddress> queue = new ArrayBlockingQueue<>(job.numClients());
//      System.out.println("Adding " + header.getBody() + " to ack queues");
      ackQueues.put(header, queue);
      pool.execute(job.getAckJob(queue, ackResultQueue));
    }
  }

  public void send(SendJob job) {
    // Only put multicast jobs into the background, since single jobs will be quick
    if (job instanceof PacketSender) job.run();
    else pool.execute(job);
  }

  public SocketRequest recv() throws InterruptedException { return socket.recv(); }

  /**
   * Updates the HeaderIOManager. This involves checking for finished SendJobs and resending them if necessary.
   */
  public void update() {
    // Process all AckResults
    try {
      while (!ackResultQueue.isEmpty()) {
        AckResult r = ackResultQueue.poll(0, TimeUnit.MILLISECONDS);
        this.ackQueues.remove(r.resend().getAckHeader());
        if (!r.wasSuccessful()) {
          SendJob sendJob = r.resend();
          sendJob.setNeedsAck(false);
          send(sendJob);
        }
      }
    } catch (InterruptedException e) {
      System.err.println("Failed to poll from ackResultQueue without waiting.");
    }
  }

  public void processAckHeader(AckHeader ackHeader, InetSocketAddress source) throws InterruptedException {
    if (this.ackQueues.containsKey(ackHeader)) {
//      System.out.println("Received ack for " + ackHeader);
      ArrayBlockingQueue<InetSocketAddress> queue = this.ackQueues.get(ackHeader);
      queue.put(source);
    }
  }


  public List<Runnable> shutdownNow() throws InterruptedException { this.socket.send(new KillRequest()); return this.pool.shutdownNow(); }

  public InetSocketAddress getSa() {
    return sa;
  }
}
