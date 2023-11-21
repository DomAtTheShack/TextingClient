import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class VideoToByteArray extends Application {
    private static byte[] videoBytes;


    public static void play(byte[] bytes){
        videoBytes = bytes;
        launch((String) null);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Video Player");

        try {
            // Read video file as byte array
            byte[] bytes = videoBytes;

            // Save byte array to a temporary file
            Path tempFile = Files.createTempFile("temp_video", ".mp4");
            Files.write(tempFile, bytes, StandardOpenOption.CREATE);

            // Create a media source from the temporary file
            Media media = new Media(tempFile.toUri().toString());

            // Create a media player
            MediaPlayer mediaPlayer = new MediaPlayer(media);

            // Create a media view and add the player to it
            javafx.scene.media.MediaView mediaView = new javafx.scene.media.MediaView(mediaPlayer);

            // Create a layout and add the media view to it
            StackPane root = new StackPane();
            root.getChildren().add(mediaView);

            // Create a scene
            Scene scene = new Scene(root, 800, 600);

            // Set up the stage
            primaryStage.setScene(scene);
            primaryStage.show();

            // Start playing the video
            mediaPlayer.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
