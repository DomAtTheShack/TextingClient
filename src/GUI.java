import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class GUI {
    private static JTextArea consoleTextArea;
    private static JScrollPane scrollPane;
    private static final JTextField connectUser = new JTextField();
    private static JTextArea users = new JTextArea();
    private static JScrollPane userPane;


    public static void main(String[] args) {
        JFrame f = new JFrame();
        JButton connect = new JButton("Connect");
        final JTextField connectIP = new JTextField();
        JTextField sendMessage = new JTextField();
        final JTextField connectUser = new JTextField();
        JButton disconnect = new JButton("Disconnect");
        JLabel connectLabel = new JLabel("Connection IP:Port");
        JLabel usernameLabel = new JLabel("Username");
        JLabel connectedUsers = new JLabel("People Connected");
        f.setTitle("ChatterBox");

        sendMessage.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e){
                if(Client.isConnected()) {
                    Client.message = sendMessage.getText();
                    sendMessage.setText("");
                }else {
                    System.out.println("Not Connected");
                }
            }});

        connectIP.setBounds(150, 335, 200, 20);
        connect.setBounds(620, 335, 140, 20);
        connectLabel.setBounds(10, 335, 140, 20);
        usernameLabel.setBounds(355, 335, 200, 20);
        connectUser.setBounds(430, 335, 150, 20);
        connectedUsers.setBounds(600, 10, 120, 20);
        sendMessage.setBounds(20, 285, 580, 40);
        disconnect.setBounds(620,305,140,20);

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
        f.add(sendMessage);
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
}
