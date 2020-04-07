import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Apoorva Gupta, gupta481@purdue.edu
 */

final class ChatServer {

    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    private final List<Integer> id = new ArrayList<>();
    private String fileName ;
    ChatFilter chatFilter ;


    private ChatServer(int port) {
        this.port = port;
    }

    private ChatServer() {
        this.port = 1500;
    }

    private ChatServer(int port, String fileName) {

        this.port = port ;
        this.fileName = fileName ;
        chatFilter = new ChatFilter(fileName);

    }

    private synchronized void broadcast(String message) {

        for (int i = 0; i < clients.size(); i++) {

            if (!clients.get(i).socket.isClosed()) {

                clients.get(i).writeMessage(chatFilter.filter(message));
            }
        }
    }



    private synchronized void remove(int ID) {
        clients.remove(ID);
    }

    private void start() {

        try {

            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {

                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);

                if (((ClientThread) r).checkName()) {

                    ((ClientThread) r).close();

                } else {

                    clients.add((ClientThread) r);
                    id.add(uniqueId);
                    t.start();
                    System.out.print(((ClientThread) r).username + " has connected!\n");
                    broadcast(((ClientThread) r).username + " has connected!\n");
                }
            }
        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        int size = args.length ;

        if (size == 0) {

            ChatServer server = new ChatServer();
            server.start();
        }  else if (size == 1) {

            try {

                ChatServer server = new ChatServer(Integer.parseInt(args[0])) ;
                server.start();
            } catch (NumberFormatException e) {

                System.out.println("Port number is not a valid number!") ;
            }
        } else if (size == 2) {

            try {

                ChatServer server = new ChatServer(Integer.parseInt(args[0]), args[1]);
                server.start();
            } catch (NumberFormatException e) {

                System.out.println("Port number is not a valid number!") ;
            }
        } else {

            System.out.println("Invalid number of arguments entered!");
        }
    }

    private final class ClientThread implements Runnable {

        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        private ClientThread(Socket socket, int id) {

            this.id = id;
            this.socket = socket;
            try {

                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {

                e.printStackTrace();
            }
        }

        private boolean checkName() {

            for (int i = 0 ; i < clients.size() ; i++) {

                if (clients.get(i).username.equals(this.username)) {

                    this.writeMessage("Error! User already exists!\n");
                    return true;
                }
            }
            return false;
        }


        private boolean writeMessage(String msg) {

            boolean output;
            if (!this.socket.isClosed()) {
                try {

                    sOutput.writeObject(msg);
                    output = true;
                } catch (IOException e) {

                    e.printStackTrace();
                    output = false;
                }
            } else {

                output = false;
            }
            return output;
        }

        private void close() {
            try {

                this.sInput.close();
                this.sOutput.close();
                this.socket.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            while (true) {
                try {

                    cm = (ChatMessage) sInput.readObject();
                } catch (IOException | ClassNotFoundException e) {

                    System.out.println(username + " forcefully disconnected!");
                    this.close();
                    broadcast(username + " forcefully disconnected!");
                    remove(clients.indexOf(this));
                    return ;
                }

                if (cm.getType() == 1) {

                    System.out.println(username + " has disconnected with a LOGOUT message.");
                    this.close();
                    broadcast(username + " has disconnected with a LOGOUT message.\n");
                    remove(clients.indexOf(this));
                    break;
                } else if (cm.getType() == 0) {

                    SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
                    String time = f.format(new Date());

                    if (cm.getMessage().charAt(0) == '/' &&
                            cm.getMessage().substring(1).equalsIgnoreCase("list")) {

                        writeMessage(list());
                    } else if (cm.getMessage().charAt(0) == '/' &&
                            cm.getMessage().substring(1,4).equalsIgnoreCase("msg")) {

                        String[] words = cm.getMessage().split(" ");
                        String name = words[1];
                        String send = "";
                        for (int i = 0 ; i < clients.size() ; i++) {

                            if (name.equals(clients.get(i).username) && !name.equals(this.username)) {

                                for (int j = 2 ; j < words.length; j ++) {
                                    send += words[j] + " ";
                                }
                                directMessage(send, name) ;
                                break;
                            }
                        }
                    } else {

                        System.out.println(time + " " + username + ": " + cm.getMessage());
                        try {

                            broadcast(time + " " + username + ": " + cm.getMessage() + '\n');
                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        private synchronized void directMessage(String message, String user) {

            ChatFilter chatFilter = new ChatFilter("badwords.txt");

            message = chatFilter.filter(message) ;
            SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
            String time = f.format(new Date());
            this.writeMessage(time + " " + this.username + " -> " + user + ": " + message + '\n') ;

            for (int i = 0 ; i < clients.size() ; i ++) {

                if (user.equals(clients.get(i).username)) {
                    clients.get(i).writeMessage(time + " " + this.username + " -> " + user + ": " + message + '\n') ;
                }
            }

        }

        private synchronized String list() {

            String list = ("Users online: " + clients.size() + '\n');
            for (int i = 0; i < clients.size(); i++) {

                if (!(clients.get(i).username.equals(this.username))) {
                    list += ("Other user " + i + ": " + clients.get(i).username) + '\n';
                }

            }
            return list;
        }

    }
}
