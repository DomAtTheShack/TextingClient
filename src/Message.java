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
public class Message implements Serializable {

    private String message;
    private boolean isImage;
    private boolean isRequest;
    private List<String> users;

    /**
     *
     * @param message It will contain the message to be sent
     * @param isImage This will say if it's an image so the correct image processing can be done
     * @param isRequest This says if information is being requested EX: Users connected
     */
    public Message(String message, boolean isImage, boolean isRequest) {
        this.message = message;
        this.isImage = isImage;
        this.isRequest = isRequest;
    }

    public Message(List<String> users, boolean isImage, boolean isRequest) {
        this.users = users;
        this.isImage = isImage;
        this.isRequest = isRequest;
    }

    public Message(boolean isImage, boolean isRequest) {
        this.isImage = isImage;
        this.isRequest = isRequest;
    }

    public String getMessage() {
        return message;
    }

    public boolean isImage() {
        return isImage;
    }

    public boolean isRequest() {
        return isRequest;
    }

    public List<String> getUsers() {
        return users;
    }

    public static void sendObject(ObjectOutputStream objectOutputStream, Message message) throws IOException {
        objectOutputStream.writeObject(message);
        objectOutputStream.flush();
    }

    public static Message receiveObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        return (Message) objectInputStream.readObject();
    }
}
