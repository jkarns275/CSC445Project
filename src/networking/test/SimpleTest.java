package networking.test;

import networking.HeaderIOManager;
import networking.SocketRequest;
import networking.headers.WriteHeader;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.*;

import static networking.SocketManager.job;

public class SimpleTest {
  public static void main(String[] args) throws InterruptedException {
    ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS,
      new LinkedBlockingQueue<>());

    poolExecutor.execute(() -> {
      try {
        HeaderIOManager hiom = new HeaderIOManager(new InetSocketAddress(InetAddress.getLocalHost(), 4444), 4);
        hiom.send(hiom.packetSender(new WriteHeader(0, 0, "Hello", "Josh"),
                                    new InetSocketAddress(InetAddress.getLocalHost(), 4445)));
        for (;;) {
          try {
            hiom.update();
            SocketRequest sr = hiom.recv();
            if (sr != null) {
              System.out.println("received header with opcode: " + sr.getHeader().opcode());
              break;
            }
          } catch (Exception e) {
            System.err.println("Timed out.");
            e.printStackTrace();
          }
        }

        hiom.shutdownNow();
      } catch (Exception e) {
        System.err.println("Encountered error in test:");
        e.printStackTrace();
      }
    });
    poolExecutor.execute(() -> {
      try {
        HeaderIOManager hiom = new HeaderIOManager(new InetSocketAddress(InetAddress.getLocalHost(), 4445), 4);
        for (;;) {
          try {
            hiom.update();
            SocketRequest sr = hiom.recv();
            if (sr != null) {
              System.out.println("received header with opcode: " + sr.getHeader().opcode());
              break;
            }
          } catch (Exception e) {
            System.err.println("Timed out.");
            e.printStackTrace();
          }
        }

        hiom.shutdownNow();
      } catch (Exception e) {
        System.err.println("Encountered error in test:");
        e.printStackTrace();
      }
    });

    poolExecutor.shutdown();
    poolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    poolExecutor.shutdownNow();
  }
}
