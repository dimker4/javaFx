package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    String n;
    @FXML
    TextArea textArea;

    @FXML
    TextField textField;

    @FXML
    TextField nickField;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    final String IP_ADDR = "localhost";
    final int PORT = 8188;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            socket = new Socket(IP_ADDR, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        while (true) {
                            String str = in.readUTF(); // Считываем данные из входящего поток
                            if (str.equalsIgnoreCase("/serverClosed")) {
                                break;
                            }
                            textArea.appendText(str + "\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
