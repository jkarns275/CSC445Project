package networking.headers;

public class Constants {
  // Opcode values.
  public final static int OP_WRITE =               0x00;
  public final static int OP_JOIN =                0x01;
  public final static int OP_LEAVE =               0x02;
  public final static int OP_SOURCE =              0x03;
  public final static int OP_NAK =                 0x04;
  public final static int OP_ERROR =               0x05;
  public final static int OP_HEARTBEAT=            0x06;
  public final static int OP_INFO =                0x07;
  public final static int OP_COMMAND =             0x08;
  public final static int OP_ACK =                 0x09;
  public final static int OP_CONG =                0x0FF;

  public final static int OPCODE_INDEX =           0;

  public final static long UNORDERED_MSG_ID =      -1;

  // Maximum lengths of header fields
  public final static int MAX_USERNAME_LEN =       0xFF;
  public final static int MAX_CHANNEL_NAME_LEN =   0xFF;
  public final static int MAX_MSG_LEN =            0xFFFF;
  public final static int MAX_CONG_PACKETS =       0xFF;

  // Sizes in number of bytes
  public final static int OPCODE_SIZE =            1;
  public final static int ERROR_CODE_SIZE =        1;
  public final static int MIN_STRING_SIZE =        1;
  public final static int USERNAME_LEN_SIZE =      1;
  public final static int CHANNEL_NAME_LEN_SIZE =  1;
  public final static int CONG_N_PACKETS_SIZE =    1;
  public final static int MSG_LEN_SIZE =           2;
  public final static int CONG_PACKET_LEN_SIZE =   4;
  public final static int MIN_CONG_PACKET_SIZE =   5;
  public final static int CHANNEL_ID_SIZE =        8;
  public final static int MSG_ID_SIZE =            8;

  public final static int MAX_HEADER_SIZE =        1024 * 64; // 64 kilobytes
}
