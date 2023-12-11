import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
/**
 * This is the client class that will handle the connection to the server
 */
public class Client
{


    /**
     * This is the boolean that will determine if the client is connected to the server
     */
    private static boolean connected;
    /**
     * This is the list of clients that are currently connected to the server
     */
    private static List<String> currentClients;
    /**
     * This is the socket that will be used to connect to the server
     */
    private static Socket socket;
    /**
     * This is the packet that will be sent to the server
     */
    public static Packet packet;

    /**
     * This is the output stream that will be used to send packets to the server
     */
    public static ObjectOutputStream out;
    /**
     * This is the username that will be used to identify the client
     */
    private static String username;

    /**
     * This is the room that the client is currently in on the server
     */
    private static int room;

    /**
     * This is the method that will be used to connect to the server
     * @param server This is the server that the client will connect to
     * @param user This is the username that the client will use to identify itself
     * @throws InterruptedException This is the exception that will be thrown if the thread is interrupted
     */

    public static void connect(String[] server, String user) throws InterruptedException
    {
        try
        {
            socket = new Socket(server[0], Integer.parseInt(server[1]));
            out = new ObjectOutputStream(socket.getOutputStream());

            // Send the user information
            Packet userPacket = new Packet(user, Packet.Type.Message, getRoom());
            username = user;
            Packet.sendObjectAsync(out, userPacket);
            connected = true;
            currentClients = new ArrayList<>();
            Thread.sleep(1000);

            // Start the thread that will handle the incoming messages
            new Thread(() ->
            {
                try
                {
                    ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

                    while (connected)
                    {
                        // Receive Message object
                        Packet receivedPacket = Packet.receiveObject(objectInputStream);
                        if (receivedPacket != null)
                        {
                            if (receivedPacket.getID() == Packet.Type.UserRequest && receivedPacket.getRoom() == room) {
                                // Handle user list
                                currentClients = receivedPacket.getUsers();
                            } else if(receivedPacket.getID() == Packet.Type.RoomChange)
                            {
                                // Handle room change
                                room = receivedPacket.getRoom();
                                requestClientList(out);
                            } else if (receivedPacket.getID() == Packet.Type.Image && receivedPacket.getRoom() == room)
                            {
                                // Handle image
                                Platform.runLater(() -> GUI.openData(receivedPacket.getByteData(), receivedPacket.getUserSent(), "Image"));
                                GUI.playSound();
                            } else if(receivedPacket.getID() == Packet.Type.Video && receivedPacket.getRoom() == room)
                            {
                                Platform.runLater(() -> GUI.openData(receivedPacket.getByteData(), receivedPacket.getUserSent(), "Video"));
                                // Handle video
                                GUI.playSound();
                            } else if(receivedPacket.getID() == Packet.Type.Message && receivedPacket.getRoom() == room)
                            {
                                // Handle regular messages
                                Platform.runLater(() -> printText(receivedPacket.getMessage()));
                                GUI.playSound();
                            }
                        }
                    }
                } catch (IOException | ClassNotFoundException e)
                {
                    if (!e.toString().contains("Socket closed"))
                    {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            try
            {
                requestClientList(out);
            }catch (NullPointerException e)
            {
                requestClientList(out);
            }
            new Thread(() ->
            {
                while (connected)
                {
                    try
                    {
                        while (true)
                        {
                            Thread.sleep(10000);

                            requestClientList(out);
                        }
                    } catch (InterruptedException | IOException | NullPointerException e)
                    {
                        //Do nothing so no error is thrown
                    }
                }
            }).start();
            printText("Don't be like Jorge!");
            while (true)
            {
                // Send the message to the server
                while (packet != null)
                {
                    Packet.sendObjectAsync(out, packet);
                    packet = null;
                }
                Thread.sleep(100);
            }
        } catch (IOException e)
        {
            connected = false;
            e.printStackTrace();
            if (e.getMessage().contains("Connection Reset") || e.getMessage().contains("Connection refused: connect") || e.getMessage().contains("UnknownHostException:")) {
                printText("Server Not Available");
            }
        }
    }

    /**
     * This is the method that will be used to send a message to the server
     * @return This is the packet that will be sent to the server
     */
    public static boolean isConnected()
    {
        return connected;
    }

    /**
     * This is the method that will be used to send a message to the server
     * @param message This is the message that will be sent to the server
     */
    private static void printText(String message)
    {
        Platform.runLater(() -> {
            System.out.println(message);
        });
    }

    /**
     * This is the method that will be used to send a message to the server
     * @param out This is the output stream that will be used to send the packet to the server
     * @throws InterruptedException This is the exception that will be thrown if the thread is interrupted
     * @throws IOException This is the exception that will be thrown if there is an error with the output stream
     */
    public static void requestClientList(ObjectOutputStream out) throws InterruptedException, IOException
    {
        if (connected)
        {
            GUI.clear();
            final String[] clients = {null};
            Packet requestPacket = new Packet(Packet.Type.UserRequest);
            Packet.sendObjectAsync(out, requestPacket);

            new Thread(() ->
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
                for (String x : currentClients)
                {
                    clients[0] += (x) + ("\n");
                }
                GUI.addText("Room #" + room + "\n");
                try {
                    GUI.addText(clients[0]);
                }catch (NullPointerException e)
                {
                    GUI.clear();
                }
            }).start();
        }
    }

    /**
     * This is the method that will be used to send a message to the server
     * @throws IOException This is the exception that will be thrown if there is an error with the output stream
     * @throws InterruptedException This is the exception that will be thrown if the thread is interrupted
     */
    public static void disconnect() throws IOException, InterruptedException
    {
        if (connected)
        {
            GUI.clear();
            room = 0;
            connected = false;
            Thread.sleep(100);
            socket.close();
            // Inform the user about the disconnection status
            printText("Disconnected from the server!");
        } else
        {
            printText("Not Connected!");
        }
    }

    /**
     * This is the method that will be used to send a message to the server
     * @return This is the username that the client is using to identify itself
     */
    public static String getUsername()
    {
        return username;
    }

    /**
     * This is the method that will be used to send a message to the server
     * @return This is the room that the client is currently in on the server
     */
    public static int getRoom()
    {
        return room;
    }
}
