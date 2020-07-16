package sample.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Server server;

    private int id;
    private String nick;

    public int getId() { return id; }
    public String getNick() {
        return nick;
    }



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
                                    id = AuthService.getId(nick);
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

                            if (str.startsWith("/blacklist")) { // Добавляет или удаляет пользователя из БЛ
                                String[] msgPart = str.split(" ");
                                if (server.getAllClientNicks().contains(msgPart[1])) { // Проверим, что добавляемый пользователь существует
                                    int id = AuthService.getId(msgPart[1]);
                                    if (checkUserInBlacklist(id)) {
                                        sendMsg(String.format("Вы удалили пользователя %s из черного списка", msgPart[1]));
                                        AuthService.removeFromBlacklist(ClientHandler.this, msgPart[1]);
                                    } else {
                                        sendMsg(String.format("Вы добавили пользователя %s в черный список", msgPart[1]));
                                        AuthService.addToBlacklist(ClientHandler.this, msgPart[1]);
                                    }
                                }
                            }

                            String[] msgPart = str.split(" "); // Разобъем сообщение по пробелу
                            if (msgPart[0].startsWith("/")) { // Если начинается с / то проверим, что дальше идет ник
                                if (server.getAllClientNicks().contains(msgPart[0].substring(1))) {
                                    server.whisperMsg(nick, msgPart[0].substring(1), str);
                                }
                            } else {
                                server.broadcastMsg(ClientHandler.this, nick + ": " + str);
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

    public boolean checkUserInBlacklist(int id) { // Проверяем на наличие пользователя в бл
        return AuthService.checkBlacklist(this.id, id) ;
    }

    public boolean socketIsClose() {
        return socket.isClosed();
    }
}
