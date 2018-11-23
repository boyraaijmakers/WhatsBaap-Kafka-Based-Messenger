package upm.lssp.ui;


import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import upm.lssp.Config;
import upm.lssp.UIController;
import upm.lssp.exceptions.RegistrationException;

public class ActionController implements UI {
    private final UIController controller = new UIController(this);
    public TextField username;


    public void login(){
        if(Config.DEBUG) System.out.println("Login request for: "+username.getText());
        try {
            controller.login(username.getText());
        } catch (RegistrationException e) {
            this.showError(e.getMessage());
        }
    }

    public void showError(String message) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setHeaderText("An error occurred");
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }

    public void showInfo(String message) {
        Alert errorAlert = new Alert(Alert.AlertType.INFORMATION);
        errorAlert.setHeaderText("Information");
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }
}
