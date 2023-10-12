import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import sun.awt.im.SimpleInputMethodWindow;

import javax.annotation.processing.FilerException;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;

public class GUI {
    private static JTextArea consoleTextArea;
    private static JScrollPane scrollPane;
    private static final JTextField connectUser = new JTextField();
    private static JTextArea users = new JTextArea();
    private static JScrollPane userPane;
    private static String imagePath;
    private static JFrame f = new JFrame();



    public static void main(String[] args) {
        JButton connect = new JButton("Connect");
        ImageIcon imageC = new ImageIcon("images/image.png");
        ImageIcon audioC = new ImageIcon("image/audio.png");
        JButton image = new JButton(imageC);
        JButton audio = new JButton(audioC);
        final JTextField connectIP = new JTextField();
        JTextField sendMessage = new JTextField();
        final JTextField connectUser = new JTextField();
        JButton disconnect = new JButton("Disconnect");
        JLabel connectLabel = new JLabel("Connection IP:Port");
        JLabel usernameLabel = new JLabel("Username");
        JLabel connectedUsers = new JLabel("People Connected");
        f.setTitle("ChatterBox");

        connectIP.setBounds(150, 335, 200, 20);
        connect.setBounds(620, 335, 140, 20);
        connectLabel.setBounds(10, 335, 140, 20);
        usernameLabel.setBounds(355, 335, 200, 20);
        connectUser.setBounds(430, 335, 170, 20);
        connectedUsers.setBounds(590, 10, 150, 20);
        sendMessage.setBounds(20, 285, 580, 40);
        disconnect.setBounds(620,305,140,20);
        image.setBounds(720,230,30,30);
        audio.setBounds(720,195,30,30);

        consoleTextArea = new JTextArea();
        consoleTextArea.setEditable(false);
        users = new JTextArea();
        users.setEditable(false);


        // Redirect System.out and System.err to the JTextArea
        PrintStream printStream = new PrintStream(new CustomOutputStream(consoleTextArea));
        System.setOut(printStream);
        System.setErr(printStream);

        // Add the JTextArea to the existing JFrame
        scrollPane = new JScrollPane(consoleTextArea);
        scrollPane.setBounds(20, 10, 550, 260);
        userPane = new JScrollPane(users);
        userPane.setBounds(590, 40, 120, 230);

        f.add(userPane);
        f.add(image);
        f.add(sendMessage);
        f.add(audio);
        f.add(scrollPane);
        f.add(connect);
        f.add(connectedUsers);
        f.add(disconnect);
        f.add(connectUser);
        f.add(connectLabel);
        f.add(connectIP);
        f.add(usernameLabel);
        f.setSize(764, 400);
        f.setLayout(null);
        f.setVisible(true);
        f.setResizable(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        image.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(FileExplore(true,false)&& Client.isConnected()){
                    try {
                        Message.sendObject(Client.out, sendImage(imagePath));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            });


        sendMessage.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e){
                if(Client.isConnected()) {
                    Client.message = new Message(sendMessage.getText(),false,false);
                    sendMessage.setText("");
                }else {
                    System.out.println("Not Connected");
                }
            }});

        connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (getIpPort(connectIP.getText())[0] == null || getIpPort(connectIP.getText())[1] == null) {
                    System.out.println("Invalid IP/Port");
                } else if (connectUser.getText().length() > 24 || connectUser.getText().equals("") || connectUser.getText().endsWith(" ") || connectUser.getText().substring(0, 1).equals(" ")) {
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
        disconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    Client.disconnect();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    public static void openImage(byte[] imageData, String userSent) throws IOException {
        final boolean[] display = {false};
        JFrame frame = new JFrame("Display Image");
        JLabel user = new JLabel("An Image was sent by " + userSent);
        JButton yes = new JButton("Yes");
        JButton no = new JButton("No");

        user.setBounds(90, 20, 300, 40);
        yes.setBounds(100, 100, 80, 30);
        no.setBounds(200, 100, 80, 30);

        frame.setLayout(null);  // Use null layout for manual positioning
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);

        yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                display[0] = true;
            }
        });

        no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                display[0] = false;
            }
        });

        frame.add(user);
        frame.add(yes);
        frame.add(no);
        frame.setVisible(true);

        yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                JFrame f = new JFrame();
                f.setTitle("Image Display from Byte Array");
                f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                // Convert byte[] to ImageIcon
                ImageIcon imageIcon = new ImageIcon(imageData);
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
            }
        });
        no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
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
    public static void clear(){
        users.setText("");
    }
    public static void addText(String x) throws InterruptedException {
        String str = x.replace("null","");
        users.append(str);
    }
    public static Message sendImage(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] imageByteArray = baos.toByteArray();

        // Create the message with the image data
        return new Message(imageByteArray,true,users.getText());
    }
    private static BufferedImage loadImageFromByteArray(byte[] imageData) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
        return ImageIO.read(bis);
    }
    private static boolean FileExplore(boolean image, boolean audio){
        JFrame frame = new JFrame("File Explorer Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            imagePath = fileChooser.getSelectedFile().getAbsolutePath();
        }
        frame.setVisible(true);
        frame.dispose();
        if(imagePath != null && Client.isConnected()) {
            return true;
        }
        return false;
    }
    public static void playSound() {
        java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
        toolkit.beep();  // Play a beep sound
        playWav();
        f.toFront();  // Bring the frame to the front

    }
    public static void playWav() {
        try
        {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new File("sounds/ping.wav")));
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