package upm.lssp.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import upm.lssp.Config;
import upm.lssp.View;
import upm.lssp.exceptions.QuitException;
import upm.lssp.exceptions.RegistrationException;

import java.io.IOException;

public class ChatUIController extends UIController {
    private static final String FXML = "/chat.fxml";
    public String username;



    public void quit() {
        if (Config.DEBUG) System.out.println("Quit request for: " + username);
        boolean status = false;
        try {
            status = View.quit(username);
        } catch (QuitException e) {
            this.showError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (status) {
            if (!Config.AUTOLOGIN) this.showInfo("You have been successfully disconnected. See you!");
            View.setController(new UserUIController());
        }

    }
    public void setUsername(String username){
        this.username=username;
    }



    @Override
    public void setScene() throws IOException {
        super.activateScene((Parent) FXMLLoader.load(getClass().getResource(FXML)), 868, 626);
    }
}
