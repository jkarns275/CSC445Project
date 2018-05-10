package client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainFrame extends JFrame {
    private static final String HELP_STRING = "/join <Channel Name> <User Name> \n" +
            String.format("%1$" + 41 + "s","/leave\n") +
            String.format("%1$" + 55 + "s","/op mute <User Name>\n") +
            String.format("%1$" + 57 + "s","/op unmute <User Name>\n") +
            String.format("%1$" + 55 + "s","/op kick <User Name>\n") +
            String.format("%1$" + 45 + "s","/listusers\n") +
            String.format("%1$" + 48 + "s","/listchannels\n");

    private JTabbedPane channels;
    private ChannelPanel messagePanel;
    private JTextField input;
    private Client client;
    private Thread clientThread;
    private GridBagLayout gridBagLayout;

    /**
     * Constructor for MainFrame class.
     */
    public MainFrame() {
        super();
        gridBagLayout = new GridBagLayout();
        this.getContentPane().setLayout(gridBagLayout);
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
        GridBagConstraints channelsConstraints = new GridBagConstraints();
        channelsConstraints.gridwidth = 6;
        channelsConstraints.gridheight = 6;
        channelsConstraints.gridx = 0;
        channelsConstraints.gridy = 0;
        channelsConstraints.anchor = GridBagConstraints.NORTHWEST;
        channelsConstraints.weighty = 1;
        channelsConstraints.weightx = 1;
        channelsConstraints.fill = GridBagConstraints.BOTH;
        //channelsConstraints.fill = GridBagConstraints.BOTH;
        this.add(channels, channelsConstraints);

        messagePanel = new ChannelPanel(-1, "*MSG*", "");
        GridBagConstraints messagePanelConstraints = new GridBagConstraints();
        messagePanelConstraints.gridx = 6;
        messagePanelConstraints.gridy = 0;
        messagePanelConstraints.gridheight = 6;
        messagePanelConstraints.gridwidth = 2;
        messagePanelConstraints.anchor = GridBagConstraints.NORTHEAST;
        messagePanelConstraints.weightx = 1;
        messagePanelConstraints.weighty = 2;
        messagePanelConstraints.fill = GridBagConstraints.BOTH;
        this.add(messagePanel, messagePanelConstraints);

        input = new JTextField();
        input.addActionListener(e -> {
            parseMessage(input.getText());
            input.setText("");
        });
        //input.setPreferredSize(new Dimension(800, 10));
        GridBagConstraints inputConstraints = new GridBagConstraints();
        inputConstraints.gridwidth = 6;
        inputConstraints.gridheight = 2;
        inputConstraints.gridy = 6;
        inputConstraints.gridx = 0;
        inputConstraints.anchor = GridBagConstraints.SOUTHWEST;
        inputConstraints.fill = GridBagConstraints.BOTH;
        this.add(input, inputConstraints);
    }

    private boolean disconnect() {
      if (client != null) {
        this.client.kill();
        this.client = null;
        return false;
      }
      return false;
    }

    private boolean connect(String hostname, int hostport, int clientport) {
      if (client != null) {
        this.client.kill();
      }
        try {
            this.client = new Client(new InetSocketAddress(hostname, hostport), clientport);
            clientThread = new Thread(client);
            clientThread.start();
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
        if (channel != null && !channel.isMuted()) {
            client.sendMessage(channel.getChannelID(), -1, channel.getNick(), input);
        }
    }

    /**
     * Direct message or command inputted by the user to the correct function for sending.
     * @param input String input from user
     */
    public void parseMessage(String input) {
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
                    client.joinChannel(substrings[1], substrings[2]);
                    break;
                case "/leave":
                    removeChannel(channel.getChannelID());
                    break;
                case "/op":
                    client.sendCommandHeader(channel.getChannelID(), input);
                    break;
                case "/demo":
                    // Send a message.
                    sendMessage("Hello, whats up.");
                    client.sendCommandHeader(channel.getChannelID(), "/op kick Josh");
                    client.sendCommandHeader(channel.getChannelID(), "/op mute John");
                    /*
                    // then leave
                    leaveWorker = new LeaveSwingWorker(client, channel.getChannelID());
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

                    // Then rejoin.
                    joinWorker = new JoinSwingWorker(client, channelName, channelNickname);
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
                    */
                    client.sendCommandHeader(channel.getChannelID(), "/listusers");
                    client.sendCommandHeader(channel.getChannelID(), "/listchannels");

                    /*
                    //Send first message
                    Runnable doSendMessage = () -> {
                        client.sendMessage(channel.getChannelID(), -1, channel.getNick(), "First message");
                    };
                    doSendMessage.run();

                    //leave the channel
                    leaveWorker = new LeaveSwingWorker(client, channel.getChannelID());
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

                    //join the channel again
                    joinWorker = new JoinSwingWorker(client, "Channel1", "Guest");
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

                    //send second message
                    doSendMessage = () -> {
                        client.sendMessage(channel.getChannelID(), -1, channel.getNick(), "Second message");
                    };
                    doSendMessage.run();

                    //mute user
                    Runnable doMute = () -> {
                        client.sendCommandHeader(channel.getChannelID(), "/op mute " + channel.getNick());
                    };
                    doMute.run();

                    //send muted message
                    client.sendMessage(channel.getChannelID(), -1, channel.getNick(), "This should be an error");

                    //unmute user
                    Runnable doUnMute = () -> {
                        client.sendCommandHeader(channel.getChannelID(), "/op unmute " + channel.getNick());
                    };
                    doUnMute.run();

                    //send third message
                    doSendMessage = () -> {
                        client.sendMessage(channel.getChannelID(), -1, channel.getNick(), "Third message");
                    };
                    SwingUtilities.invokeLater(doSendMessage);

                    Runnable doCommands = () -> {
                        client.sendCommandHeader(channel.getChannelID(), "/users");
                        client.sendCommandHeader(channel.getChannelID(), "/channels");
                    };
                    SwingUtilities.invokeLater(doCommands);

                    doSendMessage = () -> {
                        client.sendMessage(channel.getChannelID(), -1, channel.getNick(), "Fourth message");
                    };
                    SwingUtilities.invokeLater(doSendMessage);
                    */

                    break;
                case "/help":
                    printToMesssageChannel("HELP", HELP_STRING);
                    break;
                case "/listusers":
                    client.sendCommandHeader(channel.getChannelID(), input);
                    break;
                case "/listchannels":
                    client.sendCommandHeader(channel.getChannelID(), input);
                    break;
                default:
                  printToMesssageChannel("HELP", HELP_STRING);
                    // not a command
            }
        } else {
            sendMessage(input);
        }
    }

    static boolean confirm(long prop, DatagramSocket[] hosts) throws IOException {
      final ByteArrayOutputStream bo = new ByteArrayOutputStream(Long.BYTES);
      final ObjectOutputStream oo = new ObjectOutputStream(bo);
      oo.writeLong(prop);

      for (DatagramSocket host : hosts) {
        try {
        final DatagramPacket toSend = new DatagramPacket(bo.toByteArray(), bo.size());
        host.send(toSend);
        } catch (Exception e) {
        }
      }


      final ArrayList<Long> replies = new ArrayList<>();

      for (DatagramSocket host : hosts) {
        try {
          final DatagramPacket received = new DatagramPacket(new byte[8], 8);
          host.receive(received);
          final ByteArrayInputStream bi =
            new ByteArrayInputStream(received.getData(), received.getOffset(), received.getLength());
          final ObjectInputStream oi = new ObjectInputStream(bi);
          replies.add(oi.readLong());
        } catch (Exception e) {
        }
      }
      long numGreater = replies.stream().reduce(0L, (acc, i) -> i > prop ? acc + 1 : acc);
      return numGreater > (hosts.length / 2 )+ 1;
    }

    /**
     * Add and display a channel to the client gui.
     * @param panel ChannelPanel representing some channel
     */
    public void addChannel(ChannelPanel panel) {
      SwingUtilities.invokeLater(() -> {
            channels.addTab(panel.getChannelName(), panel);
            channels.setSelectedComponent(panel);
        });
    }

    public void removeChannel(long channelID) {
        for (int i = 0; i < channels.getTabCount(); i++) {
            ChannelPanel channel = (ChannelPanel) channels.getComponentAt(i);
            if (channel.getChannelID() == channelID) {
                channels.remove(channel);
                client.removeChannelHeartbeat(channelID);
                return;
            }
        }
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
      //client.sendErrorHeader((byte) 2, "No Such Channel");
    }

    public void setMuteChannel(long channelID, boolean status) {

        for (int i = 0; i < channels.getTabCount(); i++) {
            ChannelPanel channel = (ChannelPanel) channels.getComponentAt(i);
            if (channel.getChannelID() == channelID) {
                channel.setIsMuted(status);
              break;
            }
        }

    }

    public String getChannelNick(long channelID) {
        for (int i = 0; i < channels.getTabCount(); i++) {
            ChannelPanel channel = (ChannelPanel) channels.getComponentAt(i);
            if (channel.getChannelID() == channelID) {
              return channel.getNick();
            }
        }
        return null;
    }
}
