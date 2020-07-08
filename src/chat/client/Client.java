package ru.geekbrains.chat.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client {
    private static final String SERVER_ADDR = "localhost";
    private static final int SERVER_PORT = 8188;
    private static Socket socket;
    private static Scanner sc;

    public static void main(String[] args) {
        try {
            socket = new Socket(SERVER_ADDR, SERVER_PORT);

            Scanner in = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            sc = new Scanner(System.in);

            System.out.println("Текст: ");
            String str = "";
            while (!str.equals("/end")) {
                str = sc.nextLine();
                out.println(str);
                String answer = in.nextLine();
                System.out.println(answer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Клиент был закрыт...");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
