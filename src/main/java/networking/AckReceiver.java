package networking;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class AckReceiver extends Fiber<HashSet<InetAddress>> {

  /**
   * Timeout time in seconds.
   */
  private final static int timeout = 2;

  // A set of addresses that the server is still waiting for acks from.
  private final HashSet<InetAddress> waitingForAck = new HashSet<InetAddress>();
  private final SynchronousQueue<InetAddress> ackQueue;

  public AckReceiver(Collection<InetAddress> addresses, SynchronousQueue<InetAddress> ackQueue) {
    waitingForAck.addAll(addresses); this.ackQueue = ackQueue;
  }

  @Override
  protected HashSet<InetAddress> run() throws SuspendExecution, InterruptedException {
    try {
      for (;;) {
        InetAddress item = ackQueue.poll(timeout, TimeUnit.SECONDS);
        waitingForAck.remove(item);
      }
    } catch (InterruptedException te) {
      return waitingForAck;
    }
  }
}
