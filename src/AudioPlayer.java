import javafx.application.Application;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AudioPlayer extends Application {

    private ProgressBar progressBar;
    private Button playButton;
    private Button pauseButton;
    private Button resumeButton;

    private MediaPlayer mediaPlayer;
    private boolean isPlaying;
    private File temp = File.createTempFile("temp", ".mp3");
    private ReadOnlyDoubleWrapper progress;

    public AudioPlayer() throws IOException {
    }

    public static void playAudio(byte[] audio) throws IOException {
        System.out.println("HI");

        File temp = File.createTempFile("temp", ".mp3");
        FileOutputStream fos = new FileOutputStream(temp);
        fos.write(audio);
        fos.close();
        launch(null);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();

        progress = new ReadOnlyDoubleWrapper(0);

        progressBar = new ProgressBar();
        progressBar.setMinWidth(300);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.progressProperty().bind(progress);

        playButton = new Button("Play");
        playButton.setOnAction(e -> playAudio("test.mp3"));

        pauseButton = new Button("Pause");
        pauseButton.setDisable(true);
        pauseButton.setOnAction(e -> pauseAudio());

        resumeButton = new Button("Resume");
        resumeButton.setDisable(true);
        resumeButton.setOnAction(e -> resumeAudio());

        VBox buttonsVBox = new VBox(10);
        buttonsVBox.getChildren().addAll(playButton, pauseButton, resumeButton);
        buttonsVBox.setMaxWidth(Double.MAX_VALUE);
        buttonsVBox.setAlignment(Pos.CENTER);

        root.setTop(progressBar);
        root.setCenter(buttonsVBox);

        Scene scene = new Scene(root, 400, 150);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Audio Player");
        primaryStage.show();
    }

    private void playAudio(String mp3FilePath) {

        Media hit = new Media(temp.toURI().toString());
        mediaPlayer = new MediaPlayer(hit);

        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer.getTotalDuration() != null) {
                double currentTime = mediaPlayer.getCurrentTime().toSeconds();
                double totalDuration = mediaPlayer.getTotalDuration().toSeconds();
                progress.set(currentTime / totalDuration);
            }
        });

        mediaPlayer.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == MediaPlayer.Status.PLAYING) {
                playButton.setDisable(true);
                pauseButton.setDisable(false);
                resumeButton.setDisable(true);
            } else if (newValue == MediaPlayer.Status.PAUSED) {
                playButton.setDisable(true);
                pauseButton.setDisable(true);
                resumeButton.setDisable(false);
            } else {
                playButton.setDisable(false);
                pauseButton.setDisable(true);
                resumeButton.setDisable(true);
            }
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.seek(mediaPlayer.getStartTime());
            progressBar.setProgress(0.0);
        });

        mediaPlayer.play();
    }

    private void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
        }
    }

    private void resumeAudio() {
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
            mediaPlayer.play();
        }
    }

    private static byte[] convertMP3ToByteArray(String mp3FilePath) throws IOException {
        Path path = Paths.get(mp3FilePath);
        return Files.readAllBytes(path);
    }
}
