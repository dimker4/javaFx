package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Controller {
    String n;
    @FXML
    TextArea textArea;

    @FXML
    TextField textField;

    @FXML
    TextField nickField;

    public void sendMsg() {
        String name = Nickname.nickname;

        if (name == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Не указан никнейм");
            alert.setContentText("Необходимо задать ник в меню");
            alert.showAndWait();
        } else {
            textArea.appendText( name +": " + textField.getText() + "\n");
            textField.clear();
            textField.requestFocus();
        }
    }

    public void saveNick() {
        Nickname n = new Nickname(nickField.getText());
        Stage stage = (Stage) nickField.getScene().getWindow();
        stage.close();
    }

    public void setNick() throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("nicknameWindow.fxml"));
        Parent root1 = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Enter nickname");
        stage.setScene(new Scene(root1));
        stage.show();
    }

    public void handleExitAction()
    {
        System.out.println("Stop");
        Platform.exit();
    }
}
