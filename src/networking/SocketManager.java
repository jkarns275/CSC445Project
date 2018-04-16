package networking;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import networking.headers.Constants;
import networking.headers.Header;
import networking.headers.HeaderFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.concurrent.*;

public class SocketManager {
  public static SocketJob job(Header header, InetSocketAddress to) { return new SocketJob(header, to); }

  private final DatagramSocket socket;
  private final ArrayBlockingQueue<SocketJob> receivedItems = new ArrayBlockingQueue<>(1024);
  private final ArrayBlockingQueue<SocketJob> toSend = new ArrayBlockingQueue<>(1024);

  public SocketManager(int port, ThreadPoolExecutor pool) throws IOException {
    socket = new DatagramSocket(port);
    this.begin(pool, socket);
  }

  public SocketManager(InetSocketAddress InetSocketAddress, ThreadPoolExecutor pool) throws IOException {
    socket = new DatagramSocket(InetSocketAddress);
    this.begin(pool, socket);
  }

  private void begin(ThreadPoolExecutor pool, DatagramSocket socket) {
    pool.execute(() -> {
      try {
        socket.setSoTimeout(0);
      } catch (SocketException e) {
        e.printStackTrace();
      }
      final byte[] buffer = new byte[Constants.MAX_HEADER_SIZE];
      for (;;) {
        try {
          final SocketJob job = toSend.poll(0, TimeUnit.MILLISECONDS);
          final ByteOutputStream bout = new ByteOutputStream(Constants.MAX_HEADER_SIZE);
          final ObjectOutputStream out = new ObjectOutputStream(bout);

          job.getHeader().writeObject(out);
          out.close();

          final DatagramPacket toSend = new DatagramPacket(bout.getBytes(), bout.getCount());
          socket.send(toSend);

        } catch (InterruptedException ignored) {
        } catch (Exception e) {
          System.err.println("Encountered error in SocketManager:");
          e.printStackTrace();
        }
        try {
          final DatagramPacket received = new DatagramPacket(buffer, buffer.length);
          socket.receive(received);

          final ByteInputStream bin = new ByteInputStream(received.getData(), received.getLength());
          final ObjectInputStream in = new ObjectInputStream(bin);
          final Header header = HeaderFactory.getInstance().readHeader(in);

          receivedItems.put(job(header, new InetSocketAddress(received.getAddress(), received.getPort())));

        } catch (InterruptedException ignored) {
        } catch (Exception e) {
          System.err.println("Encountered error in SocketManager:");
          e.printStackTrace();
        }
      }
    });
  }

  public void send(Header header, InetSocketAddress to) throws InterruptedException {
    if (this.toSend.offer(SocketManager.job(header, to))) return;
    throw new InterruptedException();
  }

  public SocketJob recv() throws InterruptedException {
    return this.receivedItems.poll(0, TimeUnit.MILLISECONDS);
  }

}
