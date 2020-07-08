package ru.geekbrains.chat.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    private static Socket clientSocket; //сокет для общения
    private static ServerSocket server; // серверсокет


    public static void main(String[] args)
    {
        try {
            server = new ServerSocket(8188);
            System.out.println("Сервер запущен!");

            clientSocket = server.accept();
            System.out.println("Клиент подключился!");

            Scanner in = new Scanner(clientSocket.getInputStream());
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        String str = in.nextLine();
                        if (str.equals("/end")) {
                            break;
                        }
                        System.out.println("Client: " + str);
                        out.println("echo: " + str);
                    }
                }
            }).start();

            Scanner sc = new Scanner(System.in);
            while (true) {
                out.println(sc.nextLine());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
