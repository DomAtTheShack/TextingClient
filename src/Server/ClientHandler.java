package Server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler extends Thread {
    private static List<ClientHandler> clients = new ArrayList<>();
    private Socket clientSocket;
    private PrintWriter out;
    private String username;


    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            Server.blacklist.set(0, "Dom");
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            this.username = in.readLine();
            for(int i = 0;i<Server.blacklist.size();i++){
                if(this.username.equals(Server.blacklist.get(i))){
                    System.out.println(username + " is blacklisted and can't join.");
                    clientSocket.close();
                    break;
                }
            }
            System.out.println(username + " has connected.");

            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.sendMessage(username + " has joined the chat.");
                }
                clients.add(this);
            }

            String message;
            while ((message = in.readLine()) != null) {
                synchronized (clients) {
                    for (ClientHandler client : clients) {
                        client.sendMessage(username + ": " + message);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            synchronized (clients) {
                clients.remove(this);
                for (ClientHandler client : clients) {
                    client.sendMessage(username + " has left the chat.");
                }
            }
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
