package networking;

import java.net.InetSocketAddress;
import java.util.concurrent.SynchronousQueue;

public interface SendJob extends Runnable {
  boolean needsAck();
  AckJob getAckJob(SynchronousQueue<InetSocketAddress> ackQueue, SynchronousQueue<AckResult> resultQueue);
}
