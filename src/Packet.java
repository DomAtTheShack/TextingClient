import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

/**
 * @author Dominic Hann
 * @version 1.0
 * @since 2/12/2021
 * This is the packet class that will be used to send information between the client and the server
 * This class implements Serializable so that it can be sent over the network
 * This class is used to send information between the client and the server
 */
public class Packet implements Serializable
{

    /**
     * This is the message that will be sent to the server
     */
    private String message;
    /**
     * This is the type of packet that will be sent to the server
     */
    private final Type ID;
    /**
     * This is the list of users that will be sent to the server
     */
    private List<String> users;
    /**
     * This is the username of the user that sent the packet
     */
    private String userSent;
    /**
     * This is the room that the user is currently in
     */
    private int room;
    /**
     * This is the image that will be sent to the server
     */
    private byte[] image;
    /**
     * This is the old room that the user was in
     */
    private int oldRoom;


    /**
     * This is the constructor that will be used to create a packet that will be sent to the server
     * @param message This is the message that will be sent to the server
     * @param ID This is the type of packet that will be sent to the server
     * @param room This is the room that the user is currently in
     */
    public Packet(String message, Type ID, int room)
    {
        this.message = message;
        this.ID = ID;
        this.room = room;
    }

    /**
     * This is the constructor that will be used to create a packet that will be sent to the server
     * @param users This is the list of users that will be sent to the server
     * @param ID This is the type of packet that will be sent to the server
     */
    public Packet(List<String> users, Type ID)
    {
        this.users = users;
        this.ID = ID;
    }

    /**
     * This is the constructor that will be used to create a packet that will be sent to the server
     * @param ID This is the type of packet that will be sent to the server
     */
    public Packet(Type ID)
    {
        this.ID = ID;
    }

    /**
     * This is the constructor that will be used to create a packet that will be sent to the server
     * @param imageByteArray This is the image that will be sent to the server
     * @param ID This is the type of packet that will be sent to the server
     * @param userSent This is the username of the user that sent the packet
     * @param room This is the room that the user is currently in
     */
    public Packet(byte[] imageByteArray, Type ID, String userSent, int room)
    {
        this.ID = ID;
        this.userSent = userSent;
        image = imageByteArray;
        this.room = room;
    }

    /**
     * This is the constructor that will be used to create a packet that will be sent to the server
     * @param room This is the room that the user is currently in
     * @param ID This is the type of packet that will be sent to the server
     * @param userSent This is the username of the user that sent the packet
     * @param oldRoom This is the old room that the user was in
     */
    public Packet(int room, Type ID, String userSent, int oldRoom)
    {
        this.room = room;
        this.ID = ID;
        this.userSent = userSent;
        this.oldRoom = oldRoom;
    }

    /**
     * This is the method that will be used to get the room that the user is currently in
     * @return This is the room that the user is currently in
     */
    public int getRoom()
    {
        return room;
    }

    /**
     * This is the method that will be used to get the message that was sent to the server
     * @return This is the message sent to the server
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * This is the method that will be used to get the type of packet that was sent to the server
     * @return This is the type of packet that was sent to the server
     */
    public Type getID()
    {
        return ID;
    }

    /**
     * This is the method that will be used to get the list of users that was sent to the server
     * @return This is the list of users that was sent to the server
     */
    public List<String> getUsers()
    {
        return users;
    }

    /**
     * This is the method that will be used to get the username of the user that sent the packet
     * @return This is the username of the user that sent the packet
     */
    public String getUserSent()
    {
        return userSent;
    }


    /**
     * This is the method that will be used to send a packet to the server
     * @param objectOutputStream This is the output stream that will be used to send the packet to the server
     * @param packet This is the packet that will be sent to the server
     */
    public static void sendObjectAsync(ObjectOutputStream objectOutputStream, Packet packet)
    {
        Thread senderThread = new Thread(() -> {
            try {
                objectOutputStream.writeObject(packet);
                objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        senderThread.start();
    }

    /**
     * This is the method that will be used to receive a packet from the server
     * @param objectInputStream This is the input stream that will be used to receive the packet from the server
     * @return This is the packet that was received from the server
     * @throws IOException This is the exception that will be thrown if there is an error receiving the packet
     * @throws ClassNotFoundException This is the exception that will be thrown if the class is not found
     */
    public static Packet receiveObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException
    {
        return (Packet) objectInputStream.readObject();
    }

    /**
     * This is the method that will be used to get the image that was sent to the server
     * @return This is the image that was sent to the server
     */
    public byte[] getByteData()
    {
        return image;
    }

    /**
     * This is the method that will be used to get the old room that the user was in
     * @return This is the old room that the user was in
     */
    public int getOldRoom()
    {
        return oldRoom;
    }

    /**
     * This is the enum that will be used to identify the type of packet that was sent to the server
     */
    enum Type
    {
        Image, Video, Message, UserRequest, RoomChange, Ping;
    }
}

