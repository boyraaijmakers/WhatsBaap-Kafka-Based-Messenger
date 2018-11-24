package upm.lssp.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import upm.lssp.Config;
import upm.lssp.View;

import java.io.IOException;

public class ChatUIController extends UIController {
    private static final String FXML = "/chat.fxml";


    public void quit() {
        if (!Config.AUTOLOGIN) this.showInfo("You have been successfully disconnected. See you!");
        View.setController(new UserUIController());
    }

    @Override
    public void setScene() throws IOException {
        super.activateScene((Parent) FXMLLoader.load(getClass().getResource(FXML)), 868, 626);
    }
}
