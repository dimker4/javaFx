package sample.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Server server;

    public String getNick() {
        return nick;
    }

    private String nick;

    public ClientHandler(Server server, Socket socket) {
        this.socket = socket;
        this.server = server;

        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) { // Цикл для авторизации
                            String str = in.readUTF();
                            if (str.startsWith("/auth")) {
                                String[] tokens = str.split(" "); // Разбиваем строку по пробелу, что бы получить login и password
                                String newNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);

                                if (newNick != null && server.checkClientAuth(newNick)) {
                                    sendMsg("/authok");
                                    nick = newNick;
                                    server.subscribe(ClientHandler.this); // Подписываем на все сообщения
                                    break;
                                } else {
                                    sendMsg(!server.checkClientAuth(newNick) ? "Пользователь уже залогинен" : "Неверный логин/пароль");
                                }
                            }
                        }

                        while (true) { // Цикл для отправки сообщений
                            String str = in.readUTF();
                            if (str.equals("/end")) {
                                out.writeUTF("/serverClosed");
                                break;
                            }

                            String[] msgPart = str.split(" "); // Разобъем сообщение по пробелу
                            if (msgPart[0].startsWith("/")) { // Если начинается с / то проверим, что дальше идет ник
                                if (server.getAllClientNicks().contains(msgPart[0].substring(1))) {
                                    server.whisperMsg(nick, msgPart[0].substring(1), str);
                                }
                            } else {
                                server.broadcastMsg(nick + ": " + str);
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        server.unsubscribe(ClientHandler.this);
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg (String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean socketIsClose() {
        return socket.isClosed();
    }
}
