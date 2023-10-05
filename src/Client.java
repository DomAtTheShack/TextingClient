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

            String username = user;
            out.println(username);
            connected = true;
            currentClients = new ArrayList<>();


            // Create a separate thread to handle receiving messages from the server
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            new Thread(() -> {
                try{
                    Thread.sleep(10000);
                    requestClientList(out);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            requestClientList(out);

            System.out.println("Type 'exit' to quit.");
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("CLIENT_LIST:")) {
                    message = message.substring("CLIENT_LIST:".length());
                    System.out.println(message);
                    ArrayList<String> clients = new ArrayList<>(Arrays.asList(message.split(",")));
                    currentClients = clients;
                    System.out.println(clients.get(0));
                } else {
                    System.out.println(message);
                }
            }
            connected = false;
            socket.close();
        } catch (IOException e) {
            connected = false;
            System.out.println(e);
            if(e.getMessage().contains("Connection Reset") || e.getMessage().contains("Connection refused: connect")){
                System.out.println("Server Not Available");
            }
        }
    }
    public static boolean isConnected(){
        return connected;
    }
    public static void requestClientList(PrintWriter out) throws InterruptedException {
        String clients = null;
        out.println("GET_CLIENTS");
        for(String x:currentClients) {
            System.out.println(x);
            clients += (x)+("\n");
        }
        GUI.addText(clients);
    }
    public static void killSocket() throws IOException {
        socket.close();
    }
}
