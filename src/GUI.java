import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * This is the GUI class that will be used to create the GUI for the client
 */
public class GUI extends Application
{

    /**
     * This is the text area that will be used to display the users that are connected to the server
     */
    private static TextArea usersTextArea;
    /**
     * This is the path to the image that will be sent to the server
     */
    private static String imagePath;
    /**
     * This is the path to the video that will be sent to the server
     */
    private static String videoPath;
    /**
     * This is the grid pane that will be used to display the GUI
     */
    private static final GridPane grid = new GridPane();

    /**
     * This is the main method that will be used to run the GUI
     * @param args This is the arguments that will be passed to the main method
     */
    public static void main(String[] args)
    {
        launch(args);
    }

    /**
     * This is the method that will be used to start the GUI
     * @param primaryStage This is the stage that will be used to display the GUI
     */
    @Override
    public void start(Stage primaryStage)
    {
        primaryStage.setTitle("ChatterBox");

        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(12);
        grid.setHgap(8);

        // Create UI components
        Button connect = new Button("Connect");
        TextField connectIP = new TextField();
        TextField connectUser = new TextField();
         Button disconnect = new Button("Disconnect");
        Label connectLabel = new Label("Connection IP:Port");
        Label usernameLabel = new Label("Username");
        Label connectedUsersLabel = new Label("People Connected");
        Label SendMessageLabel = new Label("Message Here: ");
        TextField sendMessage = new TextField();

        // Create the image and video buttons
        HBox Spacer = new HBox(2);

        Button image = new Button("Image", new ImageView(
                new Image(Objects.requireNonNull
                        (getClass().getResourceAsStream("images/image.png")))));
        Button video = new Button("Video", new ImageView(
                new Image(Objects.requireNonNull
                        (getClass().getResourceAsStream("images/video.png")))));

        TextArea consoleTextArea = new TextArea();
        consoleTextArea.setEditable(false);
        ScrollPane consoleScrollPane =
                new ScrollPane(consoleTextArea); // Wrap the TextArea in a ScrollPane
        consoleScrollPane.setFitToWidth(true);
        consoleScrollPane.setFitToHeight(true);

        // Set the TextArea to grow vertically and horizontally
        usersTextArea = new TextArea();
        usersTextArea.setEditable(false);
        GridPane.setValignment(usersTextArea, VPos.TOP); // Align to the top
        GridPane.setVgrow(usersTextArea, Priority.ALWAYS); // Allow vertical growth

        // Redirect System.out and System.err to the TextArea
        PrintStream printStream = new PrintStream(
                new CustomOutputStream(consoleTextArea));
        System.setOut(printStream);
        System.setErr(printStream);

        // Add UI components to the grid
        grid.add(connectLabel, 0, 0);
        grid.add(connectIP, 1, 0);
        grid.add(connect, 2, 0);
        grid.add(disconnect, 3, 0);
        grid.add(usernameLabel, 4, 0);
        grid.add(connectUser, 5, 0);
        grid.add(Spacer,6,0);
        grid.add(video, 7, 0);
        grid.add(image, 8, 0);

        grid.add(connectedUsersLabel, 7, 1);
        grid.add(sendMessage, 0, 20, 4, 1);         // span 4 columns

        grid.add(consoleScrollPane, 0, 2, 7, 12);   // span 7 columns for the console
        grid.add(SendMessageLabel,0,19,4,1);
        grid.add(usersTextArea, 7, 2,2,1);          // Set event handlers

        // Set event handlers
        image.setOnAction(e ->
        {
            if (fileExplore(true, false) && Client.isConnected())
            {
                try {
                    Packet.sendObjectAsync(Client.out, sendImage(imagePath));
                    imagePath = "";
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        sendMessage.setOnAction(e ->
        {
            if (Client.isConnected())
            {
                if (sendMessage.getText().startsWith("/room "))
                {
                    if (getRoom(sendMessage.getText()) != -1)
                    {
                        Client.packet = new Packet(getRoom((sendMessage.getText())),
                                Packet.Type.RoomChange,
                                Client.getUsername(), Client.getRoom());
                    }
                } else
                {
                    Client.packet = new Packet(sendMessage.getText(),
                            Packet.Type.Message, Client.getRoom());
                }
                sendMessage.setText("");
            } else
            {
                Platform.runLater(() -> System.out.println("Not Connected"));
            }
        });

        //Listens for the connected button then
        // checks both username and IP to see if its entered incorrectly

        connect.setOnAction(actionEvent ->
        {
            String[] IP = getIpPort(connectIP.getText());
            if (IP[0].isEmpty() || IP[1].isEmpty())
            {
                Platform.runLater(() -> System.out.println("Invalid IP/Port"));
            } else if (connectUser.getText().length() > 24 ||
                        connectUser.getText().isEmpty() ||
                        connectUser.getText().endsWith(" ") ||
                        connectUser.getText().startsWith(" "))
            {
                Platform.runLater(() -> System.out.println("Invalid Username:" +
                        " Check Spaces and Length"));
            } else if (!Client.isConnected())
            {
                new Thread(() ->
                {
                    try
                    {
                        Client.connect(getIpPort(connectIP.getText().trim())
                                , connectUser.getText());
                    } catch (InterruptedException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                }).start();
            } else
            {
                Platform.runLater(() -> System.out.println("Already connected!"));
            }
        });

        //Sets the listener for the disconnect
        // button to disconnect the user if connected

        disconnect.setOnAction(actionEvent ->
        {
            try
            {
                Client.disconnect();
            } catch (IOException | InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        });

        //Sets the Action Listener to Open the
        // file explorer and select a proper video file

        video.setOnAction(e ->
        {
            if (fileExplore(false, true) && Client.isConnected())
            {
                try
                {
                    Packet.sendObjectAsync(Client.out, sendData(videoPath));
                    videoPath = "";
                } catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        });

        // Create a scene and set it on the stage
        Scene scene = new Scene(grid, 900, 500);
        primaryStage.setScene(scene);

        // Show the stage
        primaryStage.show();
    }


    /**
     * This is the method that will be used to get the IP and Port from the text field
     * @param text This is the text that will be used to get the IP and Port
     * @return This is the IP and Port that was entered by the user
     */
    private String[] getIpPort(String text)
    {
        int colan;
        String[] r = new String[2];
        for (int i = 0; i < text.length() - 1; i++)
        {
            if (text.charAt(i) == ':')
            {
                colan = i + 1;
                r[0] = text.substring(0, colan - 1);
                r[1] = text.substring(colan);
            }
        }
        return r;
    }

    /**
     * This is the method that will be used to open the file explorer
     * @param image This is the boolean that will be used to check if the file explorer is for an image
     * @param video This is the boolean that will be used to check if the file explorer is for a video
     * @return This is the boolean that will be used to check if the file explorer was opened
     */
    private boolean fileExplore(boolean image, boolean video)
    {
        FileChooser fileChooser = new FileChooser();
        File selectedFile;
        if (image)
        {
            fileChooser.getExtensionFilters().add
                    (new FileChooser.ExtensionFilter
                            ("Image Files", "*.png", "*.jpg", "*.gif"));

            selectedFile = fileChooser.showOpenDialog(null);
            imagePath = selectedFile != null ? selectedFile.getAbsolutePath() : null;
        } else if (video)
        {
            fileChooser.getExtensionFilters().add
                    (new FileChooser.ExtensionFilter
                            ("Video Files", "*.mp4", "*.avi", "*.mkv"));

            selectedFile = fileChooser.showOpenDialog(null);
            videoPath = selectedFile != null ? selectedFile.getAbsolutePath() : null;
        }

        return (imagePath != null || videoPath != null) && Client.isConnected();
    }

    /**
     * This is the method that will be used to get the room number from the text field
     * @param room This is the text that will be used to get the room number
     * @return This is the room number that was entered by the user
     */
    private static int getRoom(String room)
    {
        int index = room.indexOf("/room ");
        if (index != -1)
        {
            try
            {
                return Integer.parseInt(
                        room.substring(index + "/room ".length()));
            } catch (NumberFormatException e)
            {
                return -1;
            }
        }
        return -1;
    }


    /**
     * This is the method that will be used to send the data to the server
     * @param dataPath This is the path to the data that will be sent to the server
     * @return This is the packet that will be sent to the server
     */
    private static Packet sendData(String dataPath) throws IOException
    {
        byte[] sendData = loadFileToByteArray(dataPath);

        // Create the message with the data
        return new Packet(sendData,
                Packet.Type.Video, usersTextArea.getText(), Client.getRoom());
    }

    /**
     * This is the method that will be used to print the text to the console
     * @param filePath This is the text that will be printed to the console
     * @return This is the text that will be printed to the console
     */
    private static byte[] loadFileToByteArray(String filePath)
    {
        File file = new File(filePath);

        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream())
        {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1)
            {
                bos.write(buffer, 0, bytesRead);
            }

            return bos.toByteArray();

        } catch (IOException e)
        {
            e.printStackTrace();
            return null; // Handle the exception according to your needs
        }
    }

    /**
     * This is the private class to help redirect the output stream to the console
     * from <a href="https://stackoverflow.com/questions/342990/create-java-console-inside-a-gui-panel">...</a>
     */
    private static class CustomOutputStream extends OutputStream
    {
        private final TextArea textArea;

        public CustomOutputStream(TextArea textArea)
        {
            this.textArea = textArea;
        }

        @Override
        public void write(int b)
        {
                textArea.appendText(String.valueOf((char) b));
                textArea.positionCaret(textArea.getLength());
        }
    }

    /**
     * This is the method that will be used to open the data that was sent to the server
     * @param data This is the data that was sent to the server
     * @param userSent This is the user that sent the data to the server
     * @param WhatIsSent This is the type of data that was sent to the server
     */

    public static void openData(byte[] data, String userSent, String WhatIsSent)
    {
        Platform.runLater(() ->
        {
            Stage stage = new Stage();
            stage.setTitle("Display " + WhatIsSent);

            Label userLabel = new Label("A " + WhatIsSent + " was sent by " + userSent);
            Button yesButton = new Button("Yes");
            Button noButton = new Button("No");

            userLabel.setLayoutX(90);
            userLabel.setLayoutY(20);

            yesButton.setLayoutX(100);
            yesButton.setLayoutY(100);

            noButton.setLayoutX(200);
            noButton.setLayoutY(100);

            StackPane stackPane = new StackPane();

            // Display the window if the user clicks the yes button
            yesButton.setOnAction(e ->
            {

                stage.close();
                Stage displayStage = new Stage();
                displayStage.setTitle(WhatIsSent + " Display from Byte Array");

                if (WhatIsSent.equals("Image"))
                {
                    Image image = convertToImage(data);
                    ImageView imageView = new ImageView(image);
                    stackPane.getChildren().add(imageView);
                    Scene scene = new Scene(stackPane, 800, 600);
                    displayStage.setScene(scene);
                    displayStage.show();
                } else
                {
                    try
                    {
                        playVideo(data);
                    } catch (IOException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                }
            });
            // Close the window if the user clicks the no button

            noButton.setOnAction(e -> stage.close());

            Group root = new Group(userLabel, yesButton, noButton);
            Scene scene = new Scene(root, 400, 200);
            stage.setScene(scene);
            stage.showAndWait();
        });
    }

    /**
     * This is the method that will be used to convert the data to an image
     * @param data This is the data that will be converted to an image
     * @return This is the image that was converted from the data
     */
    private static Image convertToImage(byte[] data)
    {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        return new Image(inputStream);
    }

    /**
     * This is the method that will be used to play the video that was sent to the server
     * @param video This is the video that was sent to the server
     */
    private static void playVideo(byte[] video) throws IOException
    {
        Platform.runLater(() ->
        {
            try
            {
                Path tempFile = Files.createTempFile("chatterVid", ".mp4");
                Files.write(tempFile, video, StandardOpenOption.WRITE);
                File videoFile = new File(tempFile.toString());

                if (videoFile.exists())
                {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.open(videoFile);
                }
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        });
    }


    /**
     * This is the method that will be used to clear the text area
     */
    public static void clear()
    {
        Platform.runLater(() -> usersTextArea.setText(""));
    }

    /**
     * This is the method that will be used to add text to the text area
     * @param x This is the text that will be added to the text area
     */
    public static void addText(String x)
    {
        String str = x.replace("null", "");

        Platform.runLater(() -> Platform.runLater(() -> usersTextArea.appendText(str)));
    }

    /**
     * This is the method that will be used to send an image to the server
     * @param path This is the path to the image that will be sent to the server
     * @return This is the packet that will be sent to the server
     */
    public static Packet sendImage(String path) throws IOException
    {
        BufferedImage image = ImageIO.read(new File(path));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ImageIO.write(image, "jpg", baos);
        byte[] imageByteArray = baos.toByteArray();

        // Create the message with the image data
        return new Packet(imageByteArray,
                Packet.Type.Image, Client.getUsername(), Client.getRoom());
    }

    /**
     * This is the method that will be used to play a sound when the screen is not focused
     */
    public static void playSound()
    {
        Platform.runLater(() ->
        {
            if(!grid.isFocused())
            {
                java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
                toolkit.beep();  // Play a beep sound

                new Thread(GUI::playWav).start();

                grid.toFront();  // Bring the frame to the front
                toolkit.beep();  // Play a beep sound
            }
        });
    }

    /**
     * This is the method that will be used to play a sound like a ping
     */
    public static void playWav() {
        try
        {
            Clip clip = AudioSystem.getClip();
            URL pingUrl = GUI.class.getResource("sounds/ping.wav");
            clip.open(AudioSystem.getAudioInputStream(pingUrl));
            clip.start();

            FloatControl gainControl = (FloatControl)
                    clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(-25.0f);

            // Wait for the sound to finish
            Thread.sleep(clip.getMicrosecondLength() / 1000);

            // Close the clip
            clip.close();
        }
        catch (Exception exc)
        {
            exc.printStackTrace(System.out);
        }
    }
}
