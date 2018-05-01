package client.reps;

public class Message implements Comparable<Message> {

    private long id;
    private final String nick;
    private final String content;

    public Message(long id, String nick, String content) {
        this.id = id;
        this.nick = nick;
        this.content = content;
    }

    public Message(String nick, String content) {
        this.nick = nick;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getNick() {
        return nick;
    }

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

}
