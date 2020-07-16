package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller {
    String n;
//    @FXML
//    TextArea textArea;

    @FXML
    TextField textField;

    @FXML
    TextField nickField;

    @FXML
    HBox bottomPanel;

    @FXML
    HBox upperPanel;

    @FXML
    TextField loginField;

    @FXML
    TextField passwordField;

    @FXML
    VBox VboxChat;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    final String IP_ADDR = "localhost";
    final int PORT = 8188;
    private boolean isAuthorized;
    private String myNick;

    public void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
        if (!isAuthorized) { // Если не авторизовались
            upperPanel.setVisible(true); // То показываем только панель с авторизацией
            upperPanel.setManaged(true);
            bottomPanel.setManaged(false);
            bottomPanel.setVisible(false);
        } else {
            upperPanel.setVisible(false); // То показываем только панель с авторизацией
            upperPanel.setManaged(false);
            bottomPanel.setManaged(true);
            bottomPanel.setVisible(true);
        }
    }

    public void saveNick() {
        Nickname n = new Nickname(nickField.getText());
        Stage stage = (Stage) nickField.getScene().getWindow();
        stage.close();
    }

    public void setNick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("nicknameWindow.fxml"));
        Parent root1 = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Enter nickname");
        stage.setScene(new Scene(root1));
        stage.show();
    }

    public void handleExitAction() {
        System.out.println("Stop");
        Platform.exit();
    }

    public void sendMsg() {
        try {
            out.writeUTF(textField.getText()); // Отправляем данные в исходящий поток
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void connect() {
        try {
            socket = new Socket(IP_ADDR, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/authok")) {
                                setAuthorized(true);
                                break;
                            } else {
                                Platform.runLater(new Runnable() { // Изменение GUI должно делаться в отдельном потоке
                                    @Override
                                    public void run() {
                                        String[] s = str.split(":");
                                        Label label = new Label(str + "\n");
                                        VBox vBox = new VBox();
                                        vBox.getChildren().add(label);
                                        VboxChat.getChildren().add(vBox);
                                    }
                                });
                                //textArea.appendText(str + "\n");
                            }
                        }

                        while (true) {
                            if (isAuthorized) {
                                String str = in.readUTF(); // Считываем данные из входящего поток
                                if (str.startsWith("/mynick")) {
                                    String[] s = str.split(" ");
                                    myNick = s[1];
                                } else {
                                    if (str.equalsIgnoreCase("/serverClosed")) {
                                        break;
                                    }
                                    Platform.runLater(new Runnable() { // Изменение GUI должно делаться в отдельном потоке
                                        @Override
                                        public void run() {
                                            String[] s = str.split(":"); // Будем определять по нику, в какую сторону прижать сообщение
                                            Label label = new Label(str + "\n");
                                            VBox vBox = new VBox();
                                            if(s[0].startsWith(myNick)) {
                                                vBox.setAlignment(Pos.TOP_LEFT);
                                            } else {
                                                vBox.setAlignment(Pos.TOP_RIGHT);
                                            }
                                            vBox.getChildren().add(label);
                                            VboxChat.getChildren().add(vBox);
                                        }
                                    });
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        setAuthorized(false);
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
