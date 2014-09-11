package echoserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.Utils;

public class EchoServer {

    private static boolean keepRunning = true;
    private static ServerSocket serverSocket;
    private static final Properties properties = Utils.initProperties("server.properties");
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void stopServer() {
        keepRunning = false;
    }

    protected static void removeHandler(ClientHandler ch) {
        clients.remove(ch);
    }
    
//    protected static synchronized void addHandler(ClientHandler ch){
//        clients.add(ch);
//    }

    public static void send(String messageString, String msg, String... receivers) {
        if (receivers.length > 0) {  // sends to specific users if array.length > 0
            for (String receiver : receivers) {
                for (ClientHandler ch : clients) {
                    if (ch.getUserName().equals(receiver)) {
                        ch.send(messageString+msg);
                    }
                }
            }
        }
        else {
            for (ClientHandler ch : clients) {  // sends to all users, since none were specified
                ch.send(messageString+msg);
            }
        }
    }

    protected static synchronized void broadcastUserList() {
        ArrayList<String> userList = new ArrayList<>();
        for (ClientHandler ch : clients) {
            userList.add(ch.getUserName());
        }
        for (ClientHandler ch : clients) {
            ch.sendUserList(userList);
        }
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(properties.getProperty("port"));
        String ip = properties.getProperty("serverIp");
        String logFile = properties.getProperty("logFile");
        Utils.setLogFile(logFile, EchoServer.class.getName());
        Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Server started");
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(ip, port));
            do {
                Socket socket = serverSocket.accept(); //Important Blocking call
                Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Connected to a client");
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                clientHandler.start();
            }
            while (keepRunning);
        }
        catch (IOException ex) {
            Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            Utils.closeLogger(EchoServer.class.getName());
        }
    }
}
