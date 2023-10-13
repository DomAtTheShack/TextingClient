import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {

    private static boolean connected;
    private static List<String> currentClients;
    private static Socket socket;
    public static Message message;

    public static ObjectOutputStream out;
    private static String username;


    public static void connect(String[] server, String user) throws InterruptedException {
        try {
            socket = new Socket(server[0], Integer.parseInt(server[1]));
            out = new ObjectOutputStream(socket.getOutputStream());

            // Send the user information
            Message userMessage = new Message(user, false, false);
            username = user;
            Message.sendObjectAsync(out,userMessage);
            connected = true;
            currentClients = new ArrayList<>();
            Thread.sleep(1000);
            requestClientList(out);

            // Create a separate thread to handle receiving messages from the server
            new Thread(() -> {
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

                    while (connected) {
                        // Receive Message object
                        Message receivedMessage = Message.receiveObject(objectInputStream);

                        if (receivedMessage != null) {
                            if (receivedMessage.isRequest()) {
                                // Handle CLIENT_LIST response
                                currentClients = receivedMessage.getUsers();
                            } else if (receivedMessage.isImage()) {
                                GUI.openImage(receivedMessage.getByteData(),receivedMessage.getUserSent());
                                GUI.playSound();
                            } else if(receivedMessage.isAudio()){
                                System.out.println("HI");
                                AudioPlayer.playAudio(receivedMessage.getByteData());
                                GUI.playSound();
                            } else {
                                // Handle regular messages
                                System.out.println(receivedMessage.getMessage());
                                GUI.playSound();
                            }
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    if (!e.toString().contains("Socket closed")) {
                        e.printStackTrace();
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
                    } catch (InterruptedException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
            System.out.println("Don't be A Jerk!");
            while (true) {
                while (message != null) {
                    Message.sendObjectAsync(out, message);
                    message = null;
                }
                Thread.sleep(100);
            }
        } catch (IOException e) {
            connected = false;
            e.printStackTrace();
            if (e.getMessage().contains("Connection Reset") || e.getMessage().contains("Connection refused: connect")) {
                System.out.println("Server Not Available");
            }
        }
    }

    public static boolean isConnected() {
        return connected;
    }

    public static void requestClientList(ObjectOutputStream out) throws InterruptedException, IOException {
        if (connected) {
            GUI.clear();
            final String[] clients = {null};
            Message requestMessage = new Message(false, true);
            Message.sendObjectAsync(out, requestMessage);

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
        if (connected) {
            Thread.sleep(1000);
            GUI.clear();
            connected = false;
            Thread.sleep(100);
            socket.close();
        } else {
            System.out.println("Not Connected!");
        }
    }

    public static String getUsername() {
        return username;
    }
}
