package upm.lssp.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import upm.lssp.Config;
import upm.lssp.Status;
import upm.lssp.View;
import upm.lssp.exceptions.ConnectionException;
import upm.lssp.exceptions.QuitException;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class ChatUIController extends UIController implements Initializable {
    private static final String FXML = "/chat.fxml";
    @FXML
    public ListView userList;
    private String username;


    @FXML
    public Button statusButton;
    @FXML
    public Circle myStatus;
    @FXML
    public Text myUsername;
    private Status status;



    public ChatUIController() {

        this.username = View.getUsername();
    }

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

    public void changeStatus() {
        if (Config.DEBUG) System.out.println("Status changed");
        if (this.status == Status.ONLINE) {
            goOffline();
        } else {
            goOnline();
        }
    }


    // Private methods below

    private void goOffline() {
        try {
            View.goOffline(username);
        } catch (ConnectionException e) {
            this.showError(e.getMessage());
        } catch (InterruptedException e) {
            this.showError(e.getMessage());
        }
        this.status = Status.OFFLINE;
        this.myStatus.setFill(Color.RED);
        this.statusButton.setText("Go online");
    }

    private void goOnline() {
        try {
            View.goOnline(username);
        } catch (ConnectionException e) {
            this.showError(e.getMessage());
        }
        this.status = Status.ONLINE;
        this.myStatus.setFill(Color.GREEN);
        this.statusButton.setText("Go offline");
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        this.myUsername.setText(this.username);
        goOnline();
        getUserList();
    }

    public void getUserList() {
        HashMap<Status, List<String>> users = View.retrieveUserList();

        for (Status status : users.keySet()) {
            for (String user : users.get(status)) {
                if (user.equals(username)) continue;
                Label userLabel = new Label();
                userLabel.setText(user);
                Circle circle = new Circle();
                if (status == Status.ONLINE) {
                    circle.setFill(Color.GREEN);
                } else if (status == Status.OFFLINE) {
                    circle.setFill(Color.RED);
                }
                circle.setRadius(5.0f);
                userLabel.setGraphic(circle);
                userList.getItems().add(userLabel);
            }
        }


    }



    @Override
    public void setScene() throws IOException {
        super.activateScene((Parent) FXMLLoader.load(getClass().getResource(FXML)), 868, 679);
    }


}
