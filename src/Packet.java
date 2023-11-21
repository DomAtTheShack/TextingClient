import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
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
    private int oldRoom;


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
    public Packet(int room, Type ID, String userSent, int oldRoom){
        this.room = room;
        this.ID = ID;
        this.userSent = userSent;
        this.oldRoom = oldRoom;
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

    public int getOldRoom() {
        return oldRoom;
    }

    public static void checksum(String[] args) {
            String className = "GUI.class"; // Replace with the path to the class file within the JAR

            try {
                Class<?> myClass = GUI.class;
                InputStream inputStream = myClass.getClassLoader().getResourceAsStream(className);

                if (inputStream != null) {
                    DigestInputStream digestInputStream = new DigestInputStream(inputStream, MessageDigest.getInstance("SHA-256"));
                    byte[] buffer = new byte[8192];
                    while (digestInputStream.read(buffer) != -1) {
                        // Read the class file and update the digest
                    }
                    digestInputStream.close();

                    byte[] checksumBytes = digestInputStream.getMessageDigest().digest();

                    // Convert the byte array to a hexadecimal string
                    StringBuilder checksumHex = new StringBuilder();
                    for (byte b : checksumBytes) {
                        checksumHex.append(String.format("%02x", b));
                    }

                    System.out.println("SHA-256 Checksum of " + className + " in this JAR: " + checksumHex.toString());
                } else {
                    System.out.println("Class file not found in the JAR.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    enum Type{
        Image, Video, Message, UserRequest, RoomChange, Ping;
    }
}

