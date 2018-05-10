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

/**
 *   The HeaderIOManager is a poorly named class that handles sending and multicasting of headers to clients. The
 * multicasting used is software-based. In order to add things to the SocketManager blocking queues without actually
 * blocking, SendJob's are used. They ran in a thread pool and all they do is add to the blocking queue. SendJob's
 * may be completely obsolete and unnecessary, since adding to a LinkedBlockingQueue probably won't take very long.
 * In the event that a LinkedBlockingQueue were to be filled to capacity, the SendJobs would prevent blocking in the
 * main server / client threads.
 */
public class HeaderIOManager {
  private final ThreadPoolExecutor pool;
  private final LinkedBlockingQueue<AckResult> ackResultQueue = new LinkedBlockingQueue<>(1024);
  private final HashMap<AckHeader, ArrayBlockingQueue<InetSocketAddress>> ackQueues = new HashMap<>();
  private final InetSocketAddress sa;

  /**
   * @return the SocketManager used by this HeaderIOManager
   */
  public SocketManager getSocket() {
    return socket;
  }

  private final SocketManager socket;

  /**
   * If the last packet the SocketManager tried to send wasn't successfully sent, it probably isn't connected!
   * */
  public boolean probablyConnected() {
    return socket.probablyConnected();
  }

  public HeaderIOManager(InetSocketAddress address, int parallelism) throws IOException {
    pool = new ThreadPoolExecutor(parallelism, parallelism, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    socket = new SocketManager(address, pool);
    sa = address;
  }

  /**
   * @param h the header to send
   * @param to the destination of the header
   * @return a SendJob to which will send the specified header h to the specified address
   */
  public SendJob packetSender(Header h, InetSocketAddress to) {
    SendJob job = new PacketSender(h.opcode() != Constants.OP_ACK && h.opcode() != Constants.OP_HEARTBEAT, h, to,
      socket);
    addToAckQueues(job);
    return job;
  }

  /**
   * @param h The header to send.
   * @param to The collection of addresses to send to.
   * @return Creates a send job that will send the specified header h to all of the specified addresses in
   * the supplied collection.
   * */
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

  /**
   * Runs the supplied SendJob
   * @param job
   */
  public void send(SendJob job) {
    // Only put multicast jobs into the background, since single jobs will be quick
    if (job instanceof PacketSender) job.run();
    else pool.execute(job);
  }

  /**
   * @return A SocketRequest from the SocketManager
   * @throws InterruptedException thrown when there is nothing to return.
   */
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

  /**
   * Processes an ack header by routing it to the appropriate ackQueue.
   * @param ackHeader
   * @param source
   * @throws InterruptedException
   */
  public void processAckHeader(AckHeader ackHeader, InetSocketAddress source) throws InterruptedException {
    if (this.ackQueues.containsKey(ackHeader)) {
      ArrayBlockingQueue<InetSocketAddress> queue = this.ackQueues.get(ackHeader);
      queue.put(source);
    }
  }

  /**
   * Shutdown the threadpool
   * @return the List of Runnables returned by the threadpool.
   * @throws InterruptedException thrown when the threadpool failed to shutdown.
   */
  public List<Runnable> shutdownNow() throws InterruptedException { this.socket.send(new KillRequest()); return this.pool.shutdownNow(); }

  /**
   * @return the socket address the SocketManager is bound to.
   */
  public InetSocketAddress getSa() { return sa; }
}
