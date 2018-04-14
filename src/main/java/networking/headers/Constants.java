package networking.headers;

public class Constants {
  // Opcode values.
  protected final static int OP_WRITE =               0x00;
  protected final static int OP_JOIN =                0x01;
  protected final static int OP_LEAVE =               0x02;
  protected final static int OP_SOURCE =              0x03;
  protected final static int OP_NAK =                 0x04;
  protected final static int OP_ERROR =               0x05;
  protected final static int OP_HEARTBEAT=            0x06;
  protected final static int OP_INFO =                0x07;
  protected final static int OP_COMMAND =             0x08;
  protected final static int OP_CONG =                0x0FF;

  protected final static int OPCODE_INDEX =           0;

  protected final static long UNORDERED_MSG_ID = -1;

  // Maximum lengths of header fields
  protected final static int MAX_USERNAME_LEN =       0xFF;
  protected final static int MAX_CHANNEL_NAME_LEN =   0xFF;
  protected final static int MAX_MSG_LEN =            0xFFFF;
  protected final static int MAX_CONG_PACKETS =       0xFF;

  // Sizes in number of bytes
  protected final static int OPCODE_SIZE =            1;
  protected final static int ERROR_CODE_SIZE =        1;
  protected final static int MIN_STRING_SIZE =        1;
  protected final static int USERNAME_LEN_SIZE =      1;
  protected final static int CHANNEL_NAME_LEN_SIZE =  1;
  protected final static int CONG_N_PACKETS_SIZE =    1;
  protected final static int MSG_LEN_SIZE =           2;
  protected final static int CONG_PACKET_LEN_SIZE =   4;
  protected final static int MIN_CONG_PACKET_SIZE =   5;
  protected final static int CHANNEL_ID_SIZE =        8;
  protected final static int MSG_ID_SIZE =            8;

  protected final static int MAX_HEADER_SIZE =        1024 * 64; // 64 kilobytes
}
