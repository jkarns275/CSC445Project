package networking;

import networking.headers.Header;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.concurrent.SynchronousQueue;

public class MulticastPacketSender implements SendJob {

  private final HashSet<InetSocketAddress> recipients;
  private final SocketSemaphore socket;
  private final Header packet;
  private final boolean needsAck;

  MulticastPacketSender(boolean needsAck, Header packet, HashSet<InetSocketAddress> recipients, SocketSemaphore socket) {
    this.needsAck = needsAck; this.recipients = recipients; this.socket = socket; this.packet = packet;
  }

  public void run() {
    try {
      // Serialize the data
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bout);
      packet.writeObject(out);
      ByteBuffer data = ByteBuffer.wrap(bout.toByteArray());
      byte[] arr = data.array();

      { // Actually send the data
        DatagramSocket sock = this.socket.acquire();
        DatagramPacket datagram = new DatagramPacket(arr, arr.length);
        for (InetSocketAddress to : recipients) {
          datagram.setSocketAddress(to);
          sock.send(datagram);
        }
        this.socket.release();
      }
    } catch (IOException e) {
      System.err.println("Failed to create ObjectOutputStream:");
      e.printStackTrace();
    } catch (InterruptedException e) {
      System.err.println("Failed to acquire socket semaphore:");
      e.printStackTrace();
    }
  }

  @Override
  public boolean needsAck() {
    return false;
  }

  @Override
  public AckJob getAckJob(SynchronousQueue<InetSocketAddress> ackQueue, SynchronousQueue<AckResult> resultQueue) {
   return new MulticastAckHandler(this.packet, this.recipients, ackQueue, resultQueue, this.socket);
  }

  public boolean isNeedsAck() { return this.needsAck; }
}
