package sample.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients;


    public Server() {
        Socket socket = null; //сокет для общения
        ServerSocket server = null; // серверсокет
        try {
            AuthService.connect();
            server = new ServerSocket(8188);
            System.out.println("Сервер запущен!");
            clients = new Vector<>();

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился!");
                new ClientHandler(this, socket);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }

    public void subscribe(ClientHandler c) {
        clients.add(c);
    }

    public void unsubscribe(ClientHandler c) {
        clients.remove(c);
    }

    public boolean checkClientAuth (String Nickname) { // Так как всех авторизованных клиентов добавляем в clients, то проверяем наличие авторизации по факту наличия в массиве
        for (ClientHandler c: clients) {
            if (c.getNick().equals(Nickname)) return false;
        }
        return true;
    }

    public ArrayList<String> getAllClientNicks () { // Получим ники всех авторизованных пользователей
        ArrayList<String> arr = new ArrayList();
        for (ClientHandler cl: clients) {
            arr.add(cl.getNick());
        }
        return arr;
    }

    public void broadcastMsg(ClientHandler fromClient, String msg) {
        for (ClientHandler cl: clients) {
            if (!cl.checkUserInBlacklist(fromClient.getId())) // Не отправляем сообщение кому не надо
                cl.sendMsg(msg);
        }
    }

    public void whisperMsg(ClientHandler fromClient, String toNickname, String originalMessage) {
        String fromNickname = fromClient.getNick();
        String msg = originalMessage.substring(originalMessage.indexOf(' '));
        for (ClientHandler cl: clients) {
            if (cl.getNick().equals(toNickname) || cl.getNick().equals(fromNickname) ) {
                if (!cl.checkUserInBlacklist(fromClient.getId())) {
                    cl.sendMsg("* " + fromNickname + ": " + msg);
                }
            }
        }
    }
}
