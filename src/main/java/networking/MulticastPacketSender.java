package networking;

import networking.headers.Header;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;

public class MulticastPacketSender implements Runnable {

  private final HashSet<SocketAddress> recipients;
  private final SocketSemaphore socket;
  private final Header packet;
  public MulticastPacketSender(Header packet, HashSet<SocketAddress> recipients, SocketSemaphore socket) {
    this.recipients = recipients;
    this.socket = socket;
    this.packet = packet;

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
        for (SocketAddress to : recipients) {
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
}
