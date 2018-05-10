package client.reps;

/**
 * Representation of a message.
 * Sortable by a linearly applied identifier.
 */
public class Message implements Comparable<Message> {

    private long id;
    private final String nick;
    private final String content;

    /**
     * Constructor for class message.
     * @param id Identifier for this message
     * @param nick sender of this message
     * @param content contents of this message
     */
    public Message(long id, String nick, String content) {
        this.id = id;
        this.nick = nick;
        this.content = content;
    }

    /**
     * Constructor for class message.
     * Used for messages lacking identifiers, typically for use with the client message channel.
     * @param nick sender of this message
     * @param content contents of this message
     */
    public Message(String nick, String content) {
        this.nick = nick;
        this.content = content;
    }

    /**
     * Get identifier for this message.
     * @return Long identifier
     */
    public long getId() {
        return id;
    }

    /**
     * Get nick of the sender of this message.
     * @return Nick of sender
     */
    public String getNick() {
        return nick;
    }

    /**
     * Get contents of this message
     * @return contents of message
     */
    public String getContent() {
        return content;
    }

    @Override
    public int compareTo(Message message) {
        if (this.id > message.id) {
            return 1;
        } else if (this.id == message.id) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
  public boolean equals(Object o) {
      return o instanceof Message && ((Message) o).id == this.id;
    }
}
