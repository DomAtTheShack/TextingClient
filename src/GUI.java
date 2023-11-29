import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
import java.util.Optional;

public class GUI extends Application {
    private static TextArea usersTextArea;
    private static String imagePath;
    private static String videoPath;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ChatterBox");

        GridPane grid = new GridPane();
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
        HBox Spacer = new HBox(2);
        Button image = new Button("Image", new ImageView(new Image(getClass().getResourceAsStream("images/image.png"))));
        Button video = new Button("Video", new ImageView(new Image(getClass().getResourceAsStream("images/video.png"))));

        TextArea consoleTextArea = new TextArea();
        consoleTextArea.setEditable(false);
        ScrollPane consoleScrollPane = new ScrollPane(consoleTextArea); // Wrap the TextArea in a ScrollPane
        consoleScrollPane.setFitToWidth(true);
        consoleScrollPane.setFitToHeight(true);
        usersTextArea = new TextArea();
        usersTextArea.setEditable(false);

        // Redirect System.out and System.err to the TextArea
        PrintStream printStream = new PrintStream(new CustomOutputStream(consoleTextArea));
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
        grid.add(sendMessage, 0, 20, 4, 1); // span 4 columns
        grid.add(consoleScrollPane, 0, 2, 7, 12); // span 7 columns for the console
        grid.add(SendMessageLabel,0,19,4,1);
        grid.add(usersTextArea, 7, 2, 2, 1); // span 2 columns for the user list
        // Set event handlers
        image.setOnAction(e -> {
            if (fileExplore(true, false) && Client.isConnected()) {
                try {
                    Packet.sendObjectAsync(Client.out, sendImage(imagePath));
                    imagePath = null;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        sendMessage.setOnAction(e -> {
            if (Client.isConnected()) {
                if (sendMessage.getText().startsWith("/room ")) {
                    if (getRoom(sendMessage.getText()) != -1) {
                        Client.packet = new Packet(getRoom((sendMessage.getText())), Packet.Type.RoomChange, Client.getUsername(), Client.getRoom());
                    }
                } else {
                    Client.packet = new Packet(sendMessage.getText(), Packet.Type.Message, Client.getRoom());
                }
                sendMessage.setText("");
            } else {
                System.out.println("Not Connected");
            }
        });

        connect.setOnAction(actionEvent -> {
            if (getIpPort(connectIP.getText())[0] == null || getIpPort(connectIP.getText())[1] == null) {
                System.out.println("Invalid IP/Port");
            } else if (connectUser.getText().length() > 24 || connectUser.getText().equals("") || connectUser.getText().endsWith(" ") || connectUser.getText().charAt(0) == ' ') {
                System.out.println("Invalid Username: Check Spaces and Length");
            } else if (!Client.isConnected()) {
                new Thread(() -> {
                    try {
                        Client.connect(getIpPort(connectIP.getText()), connectUser.getText());
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }).start();
            } else {
                System.out.println("Already connected!");
            }
        });

        disconnect.setOnAction(actionEvent -> {
            try {
                Client.disconnect();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        video.setOnAction(e -> {
            if (fileExplore(false, true) && Client.isConnected()) {
                try {
                    Packet.sendObjectAsync(Client.out, sendData(videoPath));
                    videoPath = null;
                } catch (IOException ex) {
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

    // Other methods (sendImage, sendData, getRoom, etc.) go here
    private String[] getIpPort(String text) {
        int colan;
        String[] r = new String[2];
        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == ':') {
                colan = i + 1;
                r[0] = text.substring(0, colan - 1);
                r[1] = text.substring(colan);
            }
        }
        return r;
    }
    private boolean fileExplore(boolean image, boolean video) {
        FileChooser fileChooser = new FileChooser();
        File selectedFile;
        if (image) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
            selectedFile = fileChooser.showOpenDialog(null);
            imagePath = selectedFile != null ? selectedFile.getAbsolutePath() : null;
        } else if (video) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mkv"));
            selectedFile = fileChooser.showOpenDialog(null);
            videoPath = selectedFile != null ? selectedFile.getAbsolutePath() : null;
        }

        return (imagePath != null || videoPath != null) && Client.isConnected();
    }

    private static int getRoom(String room) {
        int index = room.indexOf("/room ");
        if (index != -1) {
            try {
                return Integer.parseInt(room.substring(index + "/room ".length()));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    private static Packet sendData(String dataPath) throws IOException {
        byte[] sendData = loadFileToByteArray(dataPath);

        // Create the message with the data
        return new Packet(sendData, Packet.Type.Video, usersTextArea.getText(), Client.getRoom());
    }

    private static byte[] loadFileToByteArray(String filePath) {
        File file = new File(filePath);

        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            return bos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return null; // Handle the exception according to your needs
        }
    }

    private class CustomOutputStream extends OutputStream {
        private final TextArea textArea;

        public CustomOutputStream(TextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) {
            textArea.appendText(String.valueOf((char) b));
            textArea.positionCaret(textArea.getLength());
        }
    }

    public static void openData(byte[] data, String userSent, String WhatIsSent) {
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

        yesButton.setOnAction(e -> {
            stage.close();
            Stage displayStage = new Stage();
            displayStage.setTitle(WhatIsSent + " Display from Byte Array");

            if (WhatIsSent.equals("Image")) {
                // Convert byte[] to Image
                Image image = convertToImage(data);

                // Create an ImageView to display the image
                ImageView imageView = new ImageView(image);

                // Add the ImageView to the StackPane
                stackPane.getChildren().add(imageView);
            } else {
                try {
                    // Play video
                    playVideo(data);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            // Create a Scene with the StackPane
            Scene scene = new Scene(stackPane, 800, 600);

            // Set the Scene on the Stage
            displayStage.setScene(scene);

            // Show the Stage
            displayStage.show();
        });

        noButton.setOnAction(e -> stage.close());

        stackPane.getChildren().addAll(userLabel, yesButton, noButton);

        // Create a Scene and set it on the Stage
        Scene scene = new Scene(stackPane, 400, 200);
        stage.setScene(scene);

        // Show the Stage
        stage.showAndWait();
    }

    private static Image convertToImage(byte[] data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        return new Image(inputStream);
    }

    private static void playVideo(byte[] video) throws IOException {
        // Create a temporary file
        Path tempFile = Files.createTempFile("chatterVid", ".mp4");
        Files.write(tempFile, video, StandardOpenOption.WRITE);
        File videoFile = new File(tempFile.toString());

        if (videoFile.exists()) {
            Desktop desktop = Desktop.getDesktop();
            desktop.open(videoFile);
        }
    }

    public static void clear() {
        usersTextArea.setText("");
    }

    public static void addText(String x) {
        String str = x.replace("null", "");

        Platform.runLater(() -> {
            usersTextArea.appendText(str);
        });
    }

    public static Packet sendImage(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] imageByteArray = baos.toByteArray();

        // Create the message with the image data
        return new Packet(imageByteArray, Packet.Type.Image, Client.getUsername(), Client.getRoom());
    }
    public static void playSound() {
      /*  if(!f.isFocused()) {
            java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
            toolkit.beep();  // Play a beep sound
            new Thread(GUI::playWav).start();
            f.toFront();  // Bring the frame to the front
            toolkit.beep();  // Play a beep sound
        }*/
        playWav();
    }
    public static void playWav() {
        try
        {
            Clip clip = AudioSystem.getClip();
            URL pingUrl = GUI.class.getResource("sounds/ping.wav");
            clip.open(AudioSystem.getAudioInputStream(pingUrl));
            clip.start();
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
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
