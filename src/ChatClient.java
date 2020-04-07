import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Apoorva Gupta, gupta481@purdue.edu
 */

final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;


    private final String server;
    private final String username;
    private final int port;


    private ChatClient(String username, int port, String server) {

        this.username = username;
        this.port = port;
        this.server = server;
    }

    private ChatClient(String username, int port) {

        this.username = username;
        this.port = port;
        this.server = "localhost";
    }

    private ChatClient(String username) {

        this.username = username;
        this.port = 1500;
        this.server = "localhost";
    }

    private ChatClient() {

        this.username = "Anonymous";
        this.port = 1500;
        this.server = "localhost";
    }

    private boolean start() {

        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            System.out.println("Connection Refused: Server is Closed!");
            return false;
        }

        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void sendMessage(ChatMessage msg) {

        try {

            sOutput.writeObject(msg);
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        int size = args.length;
        ChatClient client = null;
        boolean check = true;

        if (size == 0) {

            client = new ChatClient();
            check = client.start();
        } else if (size == 1) {

            client = new ChatClient(args[0]);
            check = client.start();
        } else if (size == 2) {

            try {
                client = new ChatClient(args[0], Integer.parseInt(args[1]));
                check = client.start();
            } catch (NumberFormatException e) {
                System.out.println("Port number is not a valid number!") ;
            }
        } else if (size == 3) {

            try {
                client = new ChatClient(args[0], Integer.parseInt(args[1]), args[2]);
                check = client.start();
            } catch (NumberFormatException e) {
                System.out.println("Port number is not a valid number!") ;
            }
        } else {

            System.out.println("Invalid number of arguments entered!");
        }

        if (!check) {

            return;
        }

        Scanner scanner = new Scanner(System.in);

        while (true) {

            int type = 0;
            String input = scanner.nextLine();
            String test = input.toLowerCase();

            if (input.isEmpty()) {

                continue ;
            } else {

                if (input.charAt(0) == '/' && input.substring(1).equalsIgnoreCase("logout")) {

                    type = 1;
                }

                client.sendMessage(new ChatMessage(input, type));
                if (type == 1) {

                    try {

                        client.sInput.close();
                        client.sOutput.close();
                        client.socket.close();
                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    private final class ListenFromServer implements Runnable {

        public void run() {

            try {

                boolean ans = true ;
                while (ans) {

                    String msg = (String) sInput.readObject();
                    System.out.print(msg);
                }
            } catch (IOException | ClassNotFoundException e) {

                System.out.println("Server has closed the connection.");
                System.exit(0);
            }
        }
    }
}
