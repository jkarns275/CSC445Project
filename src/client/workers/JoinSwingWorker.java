package client.workers;

import client.ChannelPanel;
import client.Client;
import client.reps.ClientChannel;

import javax.swing.*;
import java.util.Optional;

public class JoinSwingWorker extends SwingWorker<Optional<ChannelPanel>, Void> {

    private Client client;
    private String channelName;
    private String nick;

    public JoinSwingWorker(Client client, String channelName, String nick) {
        this.client = client;
        this.channelName = channelName;
        this.nick = nick;
    }

    @Override
    protected Optional<ChannelPanel> doInBackground() {
        Optional<ClientChannel> channel = client.joinChannel(channelName, nick);
        if (channel.isPresent()) {
            try {
                client.addChannelHeartbeat(channel.get().getId());
            } catch (InterruptedException e) {
                e.printStackTrace();
                return Optional.empty();
            }
            ChannelPanel channelPanel = new ChannelPanel(channel.get().getId(),
                    channel.get().getName(), channel.get().getNick());
            return Optional.of(channelPanel);
        } else {
            return Optional.empty();
        }
    }

}
