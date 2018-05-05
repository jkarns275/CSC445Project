package networking.test;

import common.Constants;
import networking.SocketRequest;
import networking.headers.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SerdeTest {
  private static long CHANNEL_ID = 0xBEEF;
  private static long MSG_ID = 0xCAFE;
  private static byte ERROR_CODE = 0x00f;
  private static byte INFO_CODE = -2;
  private static String MSG = "Howdy";
  private static String USERNAME = "John_Doe";
  private static String CHANNEL_NAME = "The Channel!";
  private static String ERROR_MSG = "Ouch";
  private static String COMMAND = "/op kick John_Doe";

  private static final ArrayList<Header> testHeaders = new ArrayList<>(Arrays.asList(
    new WriteHeader(CHANNEL_ID, MSG_ID, MSG, USERNAME),
    new JoinHeader(USERNAME, CHANNEL_NAME),
    new LeaveHeader(CHANNEL_ID),
    new SourceHeader(CHANNEL_ID, CHANNEL_NAME, USERNAME),
    new NakHeader(MSG_ID, MSG_ID + 1, CHANNEL_ID),
    new ErrorHeader(ERROR_CODE, ERROR_MSG),
    new HeartbeatHeader(CHANNEL_ID),
    new InfoHeader(CHANNEL_ID, INFO_CODE, MSG_ID, MSG),
    new CommandHeader(CHANNEL_ID, COMMAND),
    new AckHeader(new WriteHeader(CHANNEL_ID, MSG_ID, MSG, USERNAME))
  ));

  public static void main(String[] args) throws Exception {
    ConglomerateHeader congHeader = new ConglomerateHeader(testHeaders);
    testHeaders.add(congHeader);

    for (Header header : testHeaders) {
      final ByteArrayOutputStream bout = new ByteArrayOutputStream(Constants.MAX_HEADER_SIZE);
      final ObjectOutputStream out = new ObjectOutputStream(bout);

      header.writeObject(out);
      out.close();

      final ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray(), 0, bout.size());
      final ObjectInputStream in = new ObjectInputStream(bin);
      final Header deserialized = HeaderFactory.getInstance().readHeader(in);
      if (!deserialized.equals(header)) {
        System.out.println("Failed to properly deserialize: " + header + " / " + deserialized);
      } else {
        System.out.println("Successfully deserialized: " + header + " / " + deserialized);
      }
    }
  }
}
