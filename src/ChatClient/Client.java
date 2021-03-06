package ChatClient;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.ProtocolStrings;

/**
 *
 * @author Gruppe 4, Andreas, Michael og Sebastian
 */
public class Client extends Thread {

    private Socket socket;
    private int port;
    private InetAddress serverAddress;
    private Scanner input;
    private PrintWriter output;
    private static List<ClientListener> listeners = new ArrayList();
    private String message;
    private String userName;
    private ArrayList<String> onlineUserList = new ArrayList<>();
    private boolean keepListening = true;

    public void connect(String address, int port, String name) throws UnknownHostException, IOException {
        if (socket == null) {
            keepListening = true;
            this.userName = name;
            this.port = port;
            serverAddress = InetAddress.getByName(address);
            socket = new Socket(serverAddress, port);
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);  //Set to true, to get auto flush behaviour
            start();
            command(ProtocolStrings.CONNECT + userName);
        }
    }

    public void send(String msg, List<String> receivers) {
        if (receivers.size() > 0) {
            String receiverString = "";
            for (String receiverName : receivers) {
                receiverString = receiverString + receiverName + ",";
            }
            if (receiverString.endsWith(",")) {
                receiverString = receiverString.substring(0, receiverString.length() - 1); // remove last character from the string, which will always be a surplus ","
            }
            msg = receiverString + "#" + msg;
        }
        else {
            msg = "*#" + msg;
        }
        command(ProtocolStrings.SEND + msg);
    }

    public void stopClient() throws IOException {
        output.println(ProtocolStrings.CLOSE);
        keepListening = false;
    }

    public void command(String commandString) {
        output.println(commandString);
    }

    public void registerEchoListener(ClientListener l) {
        listeners.add(l);
    }

    public void unregisterEchoListener(ClientListener l) {
        listeners.remove(l);
    }

    private void notifyListeners(String msg) {
        for (ClientListener echoListener : listeners) {
            echoListener.messageArrived(msg);
        }
    }

    private void notifyListeners(ArrayList<String> onlineUserList) {
        for (ClientListener echoListener : listeners) {
            echoListener.messageArrived(onlineUserList);
        }
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void run() {
        String msg = input.nextLine();

        while (!msg.equals(ProtocolStrings.CLOSE) && keepListening) {

            String[] partsArray = msg.split("#");
            String command = partsArray[0] + "#";
            if (command.equals(ProtocolStrings.ONLINE)) {
                ArrayList<String> newUserList;
                String userListString = partsArray[1];
                if (userListString.contains(",")) {
                    String[] userList = userListString.split(",");
                    newUserList = new ArrayList<>(Arrays.asList(userList));
                }
                else {
                    newUserList = new ArrayList<>();
                    newUserList.add(userListString);
                }
                if (onlineUserList.isEmpty()) {
                    notifyListeners("Welcome to the chat-server " + "[" + userName + "].");
                }
                if (newUserList.size() > onlineUserList.size() && onlineUserList.size() > 0) {
                    notifyListeners("[" + newUserList.get(newUserList.size() - 1) + "] " + "connected."); //finds the last connected user
                }
                if (newUserList.size() < onlineUserList.size()) {
                    for (String user : onlineUserList) {
                        if (!newUserList.contains(user)) {
                            notifyListeners("[" + user + "] " + "disconnected.");
                        }
                    }
                }
                onlineUserList = newUserList;
                Collections.sort(onlineUserList);
                notifyListeners(onlineUserList);
            }
            if (command.equals(ProtocolStrings.MESSAGE)) {
                String[] messageParts = msg.split("#", 3);
                notifyListeners("From: " + "[" + messageParts[1] + "]" + ": " + messageParts[2]);
            }
            msg = input.nextLine();
        }
        notifyListeners(msg);
        try {
            socket.close();
        }
        catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
