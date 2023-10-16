import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

/**
 * @author Dominic Hann
 * @version 1.0
 * This is the Message class that can hold a message to be sent
 * or n image to be sent or requests of information from the server
 */
public class Packet implements Serializable {

    private String message;
    private final Type ID;
    private List<String> users;
    private String userSent;
    private int room;
    private byte[] image;


    /**
     *
     * @param message It will contain the message to be sent.
     * @param ID is an Enum ID for the packet type EX: an image, audio, Room Change.
     */
    public Packet(String message, Type ID, int room) {
        this.message = message;
        this.ID = ID;
        this.room = room;
    }

    public Packet(List<String> users, Type ID) {
        this.users = users;
        this.ID = ID;
    }

    public Packet(Type ID) {
        this.ID = ID;
    }

    public Packet(byte[] imageByteArray, Type ID, String userSent, int room) {
        this.ID = ID;
        this.userSent = userSent;
        image = imageByteArray;
        this.room = room;
    }
    public Packet(int room, Type ID, String userSent){
        this.room = room;
        this.ID = ID;
        this.userSent = userSent;
    }
    public int getRoom(){
        return room;
    }

    public String getMessage() {
        return message;
    }

    public Type getID(){
        return ID;
    }

    public List<String> getUsers() {
        return users;
    }
    public String getUserSent(){
        return userSent;
    }

    public static void sendObjectAsync(ObjectOutputStream objectOutputStream, Packet packet) {
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

    public static Packet receiveObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        return (Packet) objectInputStream.readObject();
    }

    public byte[] getByteData() {
        return image;
    }
    enum Type{
        Image, Audio, Message, UserRequest, RoomChange, Ping;
    }
}

