package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static networking.headers.Constants.OP_JOIN;

public class JoinHeader implements Header {
    private String desiredUsername =  "ERROR";
    private String channelName =      "ERROR";

    public JoinHeader(String desiredUsername, String channelName) {
        this.desiredUsername = desiredUsername; this.channelName = channelName;
    }

    JoinHeader() { }

    public void writeObject(ObjectOutputStream out) throws IOException {

    }

    public void readObject(ObjectInputStream in) throws IOException {
        int opcode = (int) in.readByte();
        if (opcode != OP_JOIN) {
            throw new IOException("Wrong opcode for JoinHeader.");
        }
        byte userNameLength = in.readByte();
        byte[] bytes = new byte[userNameLength];
        in.read(bytes);
        desiredUsername = new String(bytes);
        byte channelNameLength = in.readByte();
        bytes = new byte[channelNameLength];
        in.read(bytes);
        channelName = new String(bytes);
    }

    public String getDesiredUsername() {
    return desiredUsername;
  }

    public String getChannelName() {
    return channelName;
  }
}
