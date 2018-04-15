package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import static networking.headers.Constants.*;

public class HeaderFactory {
  private static final HeaderFactory instance = new HeaderFactory();
  private HeaderFactory() {}

  public Header readHeader(ObjectInputStream in) throws IOException {
    int opcode = (int) in.readByte();

    Header header;
    switch (opcode) {
      case OP_WRITE:      header = new WriteHeader();         break;
      case OP_JOIN:       header = new JoinHeader();          break;
      case OP_LEAVE:      header = new LeaveHeader();         break;
      case OP_SOURCE:     header = new SourceHeader();        break;
      case OP_NAK:        header = new NakHeader();           break;
      case OP_ERROR:      header = new ErrorHeader();         break;
      case OP_HEARTBEAT:  header = new HeartbeatHeader();     break;
      case OP_INFO:       header = new InfoHeader();          break;
      case OP_COMMAND:    header = new CommandHeader();       break;
      case OP_CONG:       header = new ConglomerateHeader();  break;
      default:            throw new IOException("Invalid opcode: " + opcode);
    }
    header.readObject(in);
    return header;
  }

  public static HeaderFactory getInstance() { return instance; }
}
