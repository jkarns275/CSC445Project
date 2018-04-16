package networking;

import networking.headers.AckHeader;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;

public interface SendJob extends Runnable {
  boolean needsAck();
  AckJob getAckJob(ArrayBlockingQueue<InetSocketAddress> ackQueue, ArrayBlockingQueue<AckResult> resultQueue);
  AckHeader getAckHeader();
}
