package networking;

import networking.headers.AckHeader;
import networking.headers.Header;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class PacketSender implements SendJob {
  private final InetSocketAddress to;
  private final SocketManager socket;
  private final Header packet;
  private final boolean needsAck;

  private ArrayBlockingQueue<SendJob> doneQueue;

  public PacketSender(boolean needsAck, Header packet, InetSocketAddress to, SocketManager socket,
                      ArrayBlockingQueue<SendJob> doneQueue) {
    this.needsAck = needsAck; this.to = to; this.socket = socket; this.packet = packet; this.doneQueue = doneQueue;
  }

  public void run() {
    try {
      // Serialize the data
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bout);
      packet.writeObject(out);
      ByteBuffer data = ByteBuffer.wrap(bout.toByteArray());
      byte[] arr = data.array();

      socket.send(packet, to);

    } catch (IOException e) {
      System.err.println("Failed to create ObjectOutputStream:");
      e.printStackTrace();
    } catch (InterruptedException e) {
      System.err.println("Failed to acquire socket semaphore:");
      e.printStackTrace();
    }
    try {
      this.doneQueue.put(this);
    } catch (InterruptedException e) {
      System.err.println("Failed to add to done queue.");
      e.printStackTrace();
    }
  }

  public boolean isNeedsAck() { return needsAck; }

  @Override
  public boolean needsAck() { return this.needsAck; }

  @Override
  public AckJob getAckJob(ArrayBlockingQueue<InetSocketAddress> ackQueue, ArrayBlockingQueue<AckResult> resultQueue) {
    return new AckHandler(this.packet, this.to, ackQueue, resultQueue, socket);
  }

  @Override
  public AckHeader getAckHeader() { return new AckHeader(packet); }
}
