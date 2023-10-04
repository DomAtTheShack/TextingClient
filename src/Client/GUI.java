package Client;

import javax.naming.CompositeName;
import javax.naming.ContextNotEmptyException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.io.PrintStream;

public class GUI {
    private static JTextArea consoleTextArea;
    private static JScrollPane scrollPane;

    public static void main(String[] args) {
        JFrame f = new JFrame();
        JButton connect = new JButton("Connect");
        final JTextField connectIP = new JTextField();
        final JTextField connectUser = new JTextField();
        JLabel connectLabel = new JLabel("Connection IP:Port");
        JLabel usernameLabel = new JLabel("Username");
        f.setTitle("ChatterBox");

        connectIP.setBounds(120, 335, 200, 20);
        connect.setBounds(640, 335, 90, 20);
        connectLabel.setBounds(10,335,110, 20);
        usernameLabel.setBounds(335,335,200,20);
        connectUser.setBounds(410,335,150,20);

        consoleTextArea = new JTextArea();
        consoleTextArea.setEditable(false);

        // Redirect System.out and System.err to the JTextArea
        PrintStream printStream = new PrintStream(new CustomOutputStream(consoleTextArea));
        System.setOut(printStream);
        System.setErr(printStream);

        // Add the JTextArea to the existing JFrame
        scrollPane = new JScrollPane(consoleTextArea);
        scrollPane.setBounds(20, 10, 700, 260);
        f.add(scrollPane);

        f.add(connect);
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
                try {
                    if(getIpPort(connectIP.getText())[0] == null || getIpPort(connectIP.getText())[1] == null){
                        System.out.println("Invalid IP/Port");
                    }else if(connectUser.getText().length()>24 || connectUser.getText().equals("") || connectUser.getText().endsWith(" ") || connectUser.getText().substring(0,1).equals(" ")){
                        System.out.println("Invalid Username: Check Spaces and Length");
                    }else {
                        Client.connect(getIpPort(connectIP.getText()));
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            private String[] getIpPort(String text) {
                int colan;
                String[] r = new String[2];
                for(int i = 0;i<text.length()-1;i++){
                    if(text.charAt(i) == ':'){
                        colan = i+1;
                        r[0] = text.substring(0,colan-1);
                        r[1] = text.substring(colan);
                    }
                }
                return r;
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
}
