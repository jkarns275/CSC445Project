package client.reps;

/**
 * Representation of a channel from the client's perspective.
 */
public class ClientChannel {

    private final long id;
    private final String name;
    private final String nick;

    /**
     * Constructor for ClientChannel.
     * @param id identifier for this channel
     * @param name name of this channel
     * @param nick User's assigned nickname of this channel
     */
    public ClientChannel(long id, String name, String nick) {
        this.id = id;
        this.name = name;
        this.nick = nick;
    }

    /**
     * Get channel identifier.
     * @return channel identifier
     */
    public long getId() {
        return id;
    }

    /**
     * Get channel name.
     * @return channel name
     */
    public String getName() {
        return name;
    }

    /**
     * Get User's nick in this channel
     * @return assigned user nick
     */
    public String getNick() {
        return nick;
    }
}
