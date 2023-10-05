import javax.jws.Oneway;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client {

    private static boolean connected;

    private static ArrayList<String> currentClients;
    private static Socket socket;

    public static void connect(String[] server, String user) throws InterruptedException {
        try {
            socket = new Socket(server[0], Integer.parseInt(server[1]));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            out.println(user);
            connected = true;
            currentClients = new ArrayList<>();
            requestClientList(out);


            // Create a separate thread to handle receiving messages from the server
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        if (message.startsWith("CLIENT_LIST:")) {
                            System.out.println("HI"+ message);
                            message = message.substring("CLIENT_LIST:".length());
                            ArrayList<String> clients = new ArrayList<>(Arrays.asList(message.split(",")));
                            currentClients = clients;
                            clients.get(0);
                        } else {
                            System.out.println(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep(10000);
                        requestClientList(out);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            System.out.println("Type 'exit' to quit.");
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("CLIENT_LIST:")) {
                    message = message.substring("CLIENT_LIST:".length());
                    ArrayList<String> clients = new ArrayList<>(Arrays.asList(message.split(",")));
                    currentClients = clients;
                } else {
                    System.out.println(message);
                }
            }
            connected = false;
            socket.close();
        } catch (IOException e) {
            connected = false;
            System.out.println(e);
            if (e.getMessage().contains("Connection Reset") || e.getMessage().contains("Connection refused: connect")) {
                System.out.println("Server Not Available");
            }
        }
    }

    public static boolean isConnected() {
        return connected;
    }

    public static void requestClientList(PrintWriter out) throws InterruptedException {
        GUI.clear();
        final String[] clients = {null};
        out.println("GET_CLIENTS");
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (String x : currentClients) {
                clients[0] += (x) + ("\n");
            }
            try {
                GUI.addText(clients[0]);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
}
    public static void killSocket() throws IOException {
        socket.close();
    }
}
