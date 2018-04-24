package client.workers;

import client.Client;

import javax.swing.*;
import java.util.Optional;

public class WriteSwingWorker extends SwingWorker<Optional<Long>, Void> {

    private final Client client;
    private final long channelID;
    private final String nick;
    private final String message;

    public WriteSwingWorker(Client client, long channelID, String nick, String message) {
        this.client = client;
        this.channelID = channelID;
        this.nick = nick;
        this.message = message;
    }

    @Override
    protected Optional<Long> doInBackground() throws Exception {
        return client.sendMessage(channelID, -1, nick, message);
    }

}
