package client;

import client.workers.JoinSwingWorker;
import client.workers.LeaveSwingWorker;
import client.workers.WriteSwingWorker;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainFrame extends JFrame {
    private static final String HELP_STRING = "This should have a list of commands";

    private JTabbedPane channels;
    private ChannelPanel messagePanel;
    private JTextField input;
    private Client client;

    /**
     * Constructor for MainFrame class.
     */
    public MainFrame() {
        super();
        this.getContentPane().setLayout(new BorderLayout());
        initWidgets();
        this.setTitle("Chat");
        this.setSize(800, 600);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    /**
     * Instantiate all the widgets used in this class.
     */
    private void initWidgets() {
        channels = new JTabbedPane();
        this.add(channels, BorderLayout.CENTER);

        messagePanel = new ChannelPanel(-1, "*MSG*", "");
        channels.addTab(messagePanel.getChannelName(), messagePanel);

        input = new JTextField();
        input.addActionListener(e -> {
            parseMessage(input.getText());
            input.setText("");
        });
        //input.setPreferredSize(new Dimension(800, 10));
        this.add(input, BorderLayout.SOUTH);
    }

    private boolean connect(String hostname, int hostport, int clientport) {
        try {
            this.client = new Client(new InetSocketAddress(hostname, hostport), clientport);
            new Thread(client).start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized void printToMesssageChannel(String sender, String message) {
        messagePanel.addMessage(sender, message);
    }

    private void sendMessage(String input) {
        // write packet
        ChannelPanel channel = (ChannelPanel) channels.getSelectedComponent();
        client.sendMessage(channel.getChannelID(), -1, channel.getNick(), input);
    }

    /**
     * Direct message or command inputted by the user to the correct function for sending.
     * @param input String input from user
     */
    private void parseMessage(String input) {
        // check if message is a command
        if (input.regionMatches(0, "/", 0, 1)) {
            // command packet
            String[] substrings = input.split(" ");
            ChannelPanel channel = (ChannelPanel) channels.getSelectedComponent();
            switch(substrings[0]) {
                case "/connect":
                    if (!connect(substrings[1], Integer.valueOf(substrings[2]), Integer.valueOf(substrings[3]))) {
                        printToMesssageChannel("ERROR","Connect to server failed.");
                    }
                    break;
                case "/me":
                    sendMessage(channel.getNick() + " " + substrings[1]);
                    break;
                case "/whois":
                    break;
                case "/join":
                    JoinSwingWorker joinWorker = new JoinSwingWorker(client, substrings[1], substrings[2]);
                    joinWorker.execute();
                    try {
                        Optional<ChannelPanel> newChannel = joinWorker.get(2, TimeUnit.SECONDS);
                        if (newChannel.isPresent()) {
                            addChannel(newChannel.get());
                        } else {
                            printToMesssageChannel("ERROR",
                                    "Joining channel " + substrings[1] + " failed.");
                        }
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        e.printStackTrace();
                        printToMesssageChannel("ERROR",
                                "Joining channel " + substrings[1] + " failed.");
                    }
                    break;
                case "/leave":
                    LeaveSwingWorker leaveWorker = new LeaveSwingWorker(client, channel.getChannelID());
                    leaveWorker.execute();
                    try {
                        boolean leaveSuccess = leaveWorker.get(2, TimeUnit.SECONDS);
                        if (leaveSuccess) {
                            channels.remove(channel);
                        }
                        else {
                            this.printToMesssageChannel("SERVER",
                                    "Failed to leave channel " + channel.getChannelName());
                        }
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        e.printStackTrace();
                        this.printToMesssageChannel("SERVER",
                                "Failed to leave channel " + channel.getChannelName());
                    }
                    break;
                case "/op":
                    client.sendCommandHeader(channel.getChannelID(), input.substring(4));
                    break;
                case "/help":
                default:
                  printToMesssageChannel("HELP", HELP_STRING);
                    // not a command
            }
        } else {
            sendMessage(input);
        }
    }

    /**
     * Add and display a channel to the client gui.
     * @param panel ChannelPanel representing some channel
     */
    private void addChannel(ChannelPanel panel) {
        SwingUtilities.invokeLater(() -> {
            channels.addTab(panel.getChannelName(), panel);
            channels.setSelectedComponent(panel);
        });
    }

    public void addMessageToChannel(long channelID, long messageID, String nick, String message) {
        for (int i = 0; i < channels.getTabCount(); i++) {
            ChannelPanel channel = (ChannelPanel) channels.getComponentAt(i);
            if (channel.getChannelID() == channelID) {
                channel.addMessage(messageID, nick, message);
                long[] missing = channel.validateOrdering();
                if (missing.length > 0) {
                    // send nak
                    client.sendNAKHeader(missing[0], missing[1], channel.getChannelID());
                }
                return;
            }
        }
        // no such channel
        client.sendErrorHeader((byte) 2, "No Such Channel");
    }

}
