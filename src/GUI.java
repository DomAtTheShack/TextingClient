import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class GUI {
    private static JTextArea users = new JTextArea();
    private static String imagePath;
    private static final JFrame f = new JFrame();
    private static String videoPath;



    public static void main(String[] args) {
        JButton connect = new JButton("Connect");
        URL imageUrl = GUI.class.getResource("images/image.png");
        assert imageUrl != null;
        ImageIcon imageC = new ImageIcon(imageUrl);
        URL audioUrl = GUI.class.getResource("images/video.png");
        assert audioUrl != null;
        ImageIcon audioC = new ImageIcon(audioUrl);
        JButton image = new JButton(imageC);
        JButton video = new JButton(audioC);
        final JTextField connectIP = new JTextField();
        JTextField sendMessage = new JTextField();
        final JTextField connectUser = new JTextField();
        JButton disconnect = new JButton("Disconnect");
        JLabel connectLabel = new JLabel("Connection IP:Port");
        JLabel usernameLabel = new JLabel("Username");
        JLabel connectedUsers = new JLabel("People Connected");
        f.setTitle("ChatterBox");

        connectIP.setBounds(150, 335, 200, 20);
        connect.setBounds(620, 335, 120, 20);
        connectLabel.setBounds(10, 335, 140, 20);
        usernameLabel.setBounds(355, 335, 200, 20);
        connectUser.setBounds(430, 335, 170, 20);
        connectedUsers.setBounds(590, 10, 150, 20);
        sendMessage.setBounds(20, 285, 580, 40);
        disconnect.setBounds(620,305,120,20);
        image.setBounds(720,230,30,30);
        video.setBounds(720,195,30,30);

        JTextArea consoleTextArea = new JTextArea();
        consoleTextArea.setEditable(false);
        users = new JTextArea();
        users.setEditable(false);


        // Redirect System.out and System.err to the JTextArea
        PrintStream printStream = new PrintStream(new CustomOutputStream(consoleTextArea));
        System.setOut(printStream);
        System.setErr(printStream);

        // Add the JTextArea to the existing JFrame
        JScrollPane scrollPane = new JScrollPane(consoleTextArea);
        scrollPane.setBounds(20, 10, 550, 260);
        JScrollPane userPane = new JScrollPane(users);
        userPane.setBounds(590, 40, 120, 230);

        f.add(userPane);
        f.add(image);
        f.add(sendMessage);
        f.add(video);
        f.add(scrollPane);
        f.add(connect);
        f.add(connectedUsers);
        f.add(disconnect);
        f.add(connectUser);
        f.add(connectLabel);
        f.add(connectIP);
        f.add(usernameLabel);
        f.setSize(770, 400);
        f.setLayout(null);
        f.setVisible(true);
        f.setResizable(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        image.addActionListener(e -> {
            if(FileExplore(true,false)&& Client.isConnected()){
                try {
                    Packet.sendObjectAsync(Client.out, sendImage(imagePath));
                    imagePath = null;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });


        sendMessage.addActionListener(e -> {
            if(Client.isConnected()) {
                if(sendMessage.getText().startsWith("/room ")){
                    if(getRoom(sendMessage.getText()) != -1){
                        Client.packet = new Packet(getRoom((sendMessage.getText())), Packet.Type.RoomChange, Client.getUsername(), Client.getRoom());
                    }
                }else {
                    Client.packet = new Packet(sendMessage.getText(), Packet.Type.Message, Client.getRoom());
                }
                sendMessage.setText("");
            }else {
                System.out.println("Not Connected");
            }
        });

        connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (getIpPort(connectIP.getText())[0] == null || getIpPort(connectIP.getText())[1] == null) {
                    System.out.println("Invalid IP/Port");
                } else if (connectUser.getText().length() > 24 || connectUser.getText().equals("") || connectUser.getText().endsWith(" ") || connectUser.getText().charAt(0) == ' ') {
                    System.out.println("Invalid Username: Check Spaces and Length");
                } else if (!Client.isConnected()) {
                    new Thread(() -> {
                        try {
                            Client.connect(getIpPort(connectIP.getText()), connectUser.getText());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                } else {
                    System.out.println("Already connected!");
                }
            }

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
        });
        disconnect.addActionListener(actionEvent -> {
            try {
                Client.disconnect();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        video.addActionListener(e -> {
            if(FileExplore(false,true) && Client.isConnected()){
                try {
                    Packet.sendObjectAsync(Client.out, sendData(videoPath));
                    videoPath = null;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

    }

    private static int getRoom(String room) {
        int index = room.indexOf("/room ");
        if(index != -1){
            try {
                return Integer.parseInt(room.substring(index + "/room ".length()));
            }catch (NumberFormatException e){
                return -1;
            }
        }
        return -1;
    }

    private static Packet sendData(String dataPath) throws IOException {
        byte[] sendData = loadAudioFileToByteArray(dataPath);

        // Create the message with the audio data
        return new Packet(sendData, Packet.Type.Video,users.getText(), Client.getRoom());
    }
    private static byte[] loadAudioFileToByteArray(String filePath) {
        File audioFile = new File(filePath);

        try (FileInputStream fis = new FileInputStream(audioFile);
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
    private static class CustomOutputStream extends OutputStream {
        private final JTextArea textArea;

        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) {
            textArea.append(String.valueOf((char) b));
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }

    public static void openData(byte[] data, String userSent, String WhatIsSent) {
        JFrame frame = new JFrame("Display " + WhatIsSent);
        JLabel user = new JLabel("An " + WhatIsSent + " was sent by " + userSent);
        JButton yes = new JButton("Yes");
        JButton no = new JButton("No");

        user.setBounds(90, 20, 300, 40);
        yes.setBounds(100, 100, 80, 30);
        no.setBounds(200, 100, 80, 30);

        frame.setLayout(null);  // Use null layout for manual positioning
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);

        frame.add(user);
        frame.add(yes);
        frame.add(no);
        frame.setVisible(true);

        yes.addActionListener(e -> {
            frame.dispose();
            JFrame f = new JFrame();
            f.setTitle(WhatIsSent + " Display from Byte Array");
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            if(WhatIsSent.equals("Image")) {
                // Convert byte[] to ImageIcon
                ImageIcon imageIcon = new ImageIcon(data);
                Image image = imageIcon.getImage();

                // Create a BufferedImage from the Image
                BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
                Graphics g = bufferedImage.createGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();

                // Create a JLabel to hold the image
                JLabel label = new JLabel(new ImageIcon(bufferedImage));

                // Add the label to the JFrame
                f.getContentPane().add(label);

                // Set frame properties
                f.pack(); // Adjusts the frame size to fit the image
                f.setLocationRelativeTo(null); // Centers the frame on the screen
                f.setVisible(true);
            }else {
                try {
                    playVideo(data);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        no.addActionListener(e -> frame.dispose());
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
    public static void clear(){
        users.setText("");
    }
    public static void addText(String x) throws InterruptedException {
        String str = x.replace("null","");
        users.append(str);
    }
    public static Packet sendImage(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] imageByteArray = baos.toByteArray();

        // Create the message with the image data
        return new Packet(imageByteArray, Packet.Type.Image, Client.getUsername(), Client.getRoom());
    }

    private static boolean FileExplore(boolean image, boolean audio){
        JFrame frame = new JFrame("File Explorer Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION && image) {
            imagePath = fileChooser.getSelectedFile().getAbsolutePath();
        } else if (result == JFileChooser.APPROVE_OPTION && audio){
            videoPath = fileChooser.getSelectedFile().getAbsolutePath();
        }
        frame.setVisible(true);
        frame.dispose();
        return (imagePath != null || videoPath != null) && Client.isConnected();
    }
    public static void playSound() {
        if(!f.isFocused()) {
            java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
            toolkit.beep();  // Play a beep sound
            new Thread(GUI::playWav).start();
            f.toFront();  // Bring the frame to the front
            toolkit.beep();  // Play a beep sound
        }
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