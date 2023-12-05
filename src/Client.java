import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {

    private static boolean connected;
    private static List<String> currentClients;
    private static Socket socket;
    public static Packet packet;

    public static ObjectOutputStream out;
    private static String username;

    private static int room;


    public static void connect(String[] server, String user) throws InterruptedException {
        try {
            socket = new Socket(server[0], Integer.parseInt(server[1]));
            out = new ObjectOutputStream(socket.getOutputStream());

            // Send the user information
            Packet userPacket = new Packet(user, Packet.Type.Message, getRoom());
            username = user;
            Packet.sendObjectAsync(out, userPacket);
            connected = true;
            currentClients = new ArrayList<>();
            Thread.sleep(1000);

            // Create a separate thread to handle receiving messages from the server
            new Thread(() -> {
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

                    while (connected) {
                        // Receive Message object
                        Packet receivedPacket = Packet.receiveObject(objectInputStream);
                        if (receivedPacket != null) {
                            if (receivedPacket.getID() == Packet.Type.UserRequest && receivedPacket.getRoom() == room) {
                                // Handle CLIENT_LIST response
                                currentClients = receivedPacket.getUsers();
                            } else if(receivedPacket.getID() == Packet.Type.RoomChange){
                                room = receivedPacket.getRoom();
                                requestClientList(out);
                            } else if (receivedPacket.getID() == Packet.Type.Image && receivedPacket.getRoom() == room) {
                                Platform.runLater(() -> GUI.openData(receivedPacket.getByteData(), receivedPacket.getUserSent(), "Image"));
                                GUI.playSound();
                            } else if(receivedPacket.getID() == Packet.Type.Video && receivedPacket.getRoom() == room){
                                Platform.runLater(() -> GUI.openData(receivedPacket.getByteData(), receivedPacket.getUserSent(), "Video"));
                                GUI.playSound();
                            } else if(receivedPacket.getID() == Packet.Type.Message && receivedPacket.getRoom() == room){
                                // Handle regular messages
                                Platform.runLater(() -> printText(receivedPacket.getMessage()));
                                GUI.playSound();
                            }
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    if (!e.toString().contains("Socket closed")) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            try {
                requestClientList(out);
            }catch (NullPointerException e){

                requestClientList(out);
            }
            new Thread(() -> {
                while (connected) {
                    try {
                        while (true) {
                            Thread.sleep(10000);

                            requestClientList(out);
                        }
                    } catch (InterruptedException | IOException | NullPointerException e) {
                        //Do nothing so no error is thrown
                    }
                }
            }).start();
            printText("Don't be like Jorge!");
            while (true) {
                while (packet != null) {
                    Packet.sendObjectAsync(out, packet);
                    packet = null;
                }
                Thread.sleep(100);
            }
        } catch (IOException e) {
            connected = false;
            e.printStackTrace();
            if (e.getMessage().contains("Connection Reset") || e.getMessage().contains("Connection refused: connect") || e.getMessage().contains("UnknownHostException:")) {
                printText("Server Not Available");
            }
        }
    }

    public static boolean isConnected() {
        return connected;
    }
    private static void printText(String message) {
        Platform.runLater(() -> {
            System.out.println(message);
        });
    }

    public static void requestClientList(ObjectOutputStream out) throws InterruptedException, IOException {
        if (connected) {
            GUI.clear();
            final String[] clients = {null};
            Packet requestPacket = new Packet(Packet.Type.UserRequest);
            Packet.sendObjectAsync(out, requestPacket);

            new Thread(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                for (String x : currentClients) {
                    clients[0] += (x) + ("\n");
                }
                GUI.addText("Room #" + room + "\n");
                try {
                    GUI.addText(clients[0]);
                }catch (NullPointerException e){
                    GUI.clear();
                }
            }).start();
        }
    }

    public static void disconnect() throws IOException, InterruptedException {
        if (connected) {
            GUI.clear();
            room = 0;
            connected = false;
            Thread.sleep(100);
            socket.close();
            // Inform the user about the disconnection status
            printText("Disconnected from the server!");
        } else {
            printText("Not Connected!");
        }
    }

    public static String getUsername() {
        return username;
    }
    public static int getRoom(){
        return room;
    }
}
