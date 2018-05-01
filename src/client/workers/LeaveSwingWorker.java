package client.workers;

import client.Client;

import javax.swing.*;

public class LeaveSwingWorker extends SwingWorker<Boolean, Void> {

    private Client client;
    private long channelID;

    public LeaveSwingWorker(Client client, long channelID) {
        this.client = client;
        this.channelID = channelID;
    }

    @Override
    protected Boolean doInBackground() {
        boolean leaveSuccess = client.leaveChannel(channelID);
        if (leaveSuccess) {
            client.removeChannelHeartbeat(channelID);
        }
        return leaveSuccess;
    }

}
