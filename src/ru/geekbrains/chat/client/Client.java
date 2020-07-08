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
    private static Thread t;

    public static void main(String[] args) {
        try {
            socket = new Socket(SERVER_ADDR, SERVER_PORT);

            Scanner in = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            sc = new Scanner(System.in);

            t = new Thread (new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        String str = in.nextLine();
                        if (str.equals("/end")) {
                            break;
                        }
                        System.out.println("Server: " + str);
                    }
                }
            });
            t.start();

            System.out.println("Да начнется чат: ");
            Scanner sc = new Scanner(System.in);
            while (true) {
                out.println(sc.nextLine());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Клиент был закрыт...");

            try {
                t.stop();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
