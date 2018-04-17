package networking;

import networking.headers.AckHeader;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public interface SendJob extends Runnable {
  boolean needsAck();
  void setNeedsAck(boolean needsAck);
  AckJob getAckJob(ArrayBlockingQueue<InetSocketAddress> ackQueue, LinkedBlockingQueue<AckResult> resultQueue);
  AckHeader getAckHeader();
  int numClients();
}
