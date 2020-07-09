package ru.geekbrains.chat.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client {
    private static final String SERVER_ADDR = "localhost";
    private static final int SERVER_PORT = 8188;
    private static Socket socket;

    public static void main(String[] args) {
        try {
            socket = new Socket(SERVER_ADDR, SERVER_PORT);

            Scanner in = new Scanner(socket.getInputStream());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            Thread t1 = new Thread (new Runnable() { // Отдельный поток для чтения сообщений от сервера
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
            t1.start();

            System.out.println("Да начнется чат: "); // В основном потоке будем отрпавлять сообщения
            Thread t2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    Scanner sc = new Scanner(System.in);
                    while (true) {
                        out.println(sc.nextLine());
                    }
                }
            });

            t2.setDaemon(true);
            t2.start();
            try {
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
