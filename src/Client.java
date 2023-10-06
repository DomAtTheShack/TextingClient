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

    public static String message;


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
                while (connected) {
                    try {
                        String message;
                        while ((message = in.readLine()) != null) {
                            if (message.startsWith("CLIENT_LIST:")) {
                                message = message.substring("CLIENT_LIST:".length());
                                ArrayList<String> clients = new ArrayList<>(Arrays.asList(message.split(",")));
                                currentClients = clients;
                                clients.get(0);
                            } else {
                                System.out.println(message);
                                message = null;
                            }
                        }
                    } catch (IOException e) {
                        if(!e.toString().contains("Socket closed")) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
            new Thread(() -> {
                while (connected) {
                    try {
                        while (true) {
                            Thread.sleep(10000);
                            requestClientList(out);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
            System.out.println("Don't be A Jerk!");
            while (true) {
                while (message != null) {
                    if (message.startsWith("CLIENT_LIST:")) {
                        message = message.substring("CLIENT_LIST:".length());
                        ArrayList<String> clients = new ArrayList<>(Arrays.asList(message.split(",")));
                        currentClients = clients;
                    } else {
                        out.println(message);
                        message = null;
                    }
                }
                Thread.sleep(100);
            }
        } catch (IOException e) {
            connected = false;
            System.out.println("HIIII");

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
        if(connected) {
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
}
    public static void disconnect() throws IOException, InterruptedException {
        if(connected) {
            Thread.sleep(1000);
            GUI.clear();
            connected = false;
            Thread.sleep(100);
            socket.close();
        }else {
            System.out.println("Not Connected!");
        }
    }
}
