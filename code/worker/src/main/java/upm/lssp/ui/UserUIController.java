package upm.lssp.ui;


import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import upm.lssp.Config;
import upm.lssp.View;
import upm.lssp.exceptions.RegistrationException;
import upm.lssp.messages.Message;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

public class UserUIController extends UIController {

    private static final String FXML = "/login.fxml";


    public TextField username;


    /**
     * Handles the login phase
     */
    public void login() {
        if (Config.DEBUG) System.out.println("Login request for: " + username.getText());
        boolean status = false;
        try {
            status = View.login(username.getText());

        } catch (RegistrationException e) {
            this.showError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (status) {
            if (!Config.AUTOLOGIN) this.showInfo("Welcome " + username.getText() + "!");
            View.setController(new ChatUIController());

        }
    }


    @Override
    public void setScene() throws IOException {
        if (Config.AUTOLOGIN) {
            this.username = new TextField();
            this.username.setText("Test" + new Timestamp(System.currentTimeMillis()).getTime());
            login();
        } else {
            super.activateScene(FXMLLoader.load(getClass().getResource(FXML)), 400, 450);
        }
    }

    @Override
    public void receiveMessage(List<Message> message) {
        showError("Invalid call of the receiveMessage");
    }


}
