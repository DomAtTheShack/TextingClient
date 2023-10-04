package Client;

import java.io.*;
import java.net.*;

public class Client {
    private static String SERVER_ADDRESS;
    private static int SERVER_PORT;

    public static void connect(String[] server) throws InterruptedException {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter your username: ");
            String username = userInput.readLine();
            out.println(username);

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

            System.out.println("Type 'exit' to quit.");
            String message;
            while (true) {
                message = userInput.readLine();
                if (message.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting...");
                    System.exit(0);
                    break;
                }
                out.println(message);
            }

            socket.close();
        } catch (IOException e) {
            if(e.getMessage().contains("Connection Reset") || e.getMessage().contains("Connection refused: connect")){
                System.out.println("Server Not Available Ctrl-C to Exit");
                System.out.println("Retrying in 10 seconds...");
                Thread.sleep(10000);
                connect(server);
            }
        }
    }
}
