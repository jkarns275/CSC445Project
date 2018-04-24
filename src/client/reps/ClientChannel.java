package client.reps;

public class ClientChannel {

    private final long id;
    private final String name;
    private final String nick;

    public ClientChannel(long id, String name, String nick) {
        this.id = id;
        this.name = name;
        this.nick = nick;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNick() {
        return nick;
    }
}
