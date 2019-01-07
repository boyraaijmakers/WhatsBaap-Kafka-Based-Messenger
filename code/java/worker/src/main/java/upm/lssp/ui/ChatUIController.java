package upm.lssp.ui;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import upm.lssp.Config;
import upm.lssp.Message;
import upm.lssp.Status;
import upm.lssp.View;
import upm.lssp.exceptions.ConnectionException;
import upm.lssp.exceptions.QuitException;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class ChatUIController extends UIController implements Initializable {
    private static final String FXML = "/chat.fxml";

    private String username;
    @FXML
    public ListView userList;


    @FXML
    public Button statusButton;
    @FXML
    public Circle myStatus;
    @FXML
    public Text myUsername;
    @FXML
    public TextField textBox;
    @FXML
    public ListView topicView;

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
        setTopicViewVisibility(false);
        topicView.setMouseTransparent(true);
        topicView.setFocusTraversable(false);
        userList.setFocusTraversable(false);
        getUserList();
    }

    private void setTopicViewVisibility(boolean condition) {
        textBox.setVisible(condition);
        topicView.setVisible(condition);
    }

    private void getUserList() {

        HashMap<Status, List<String>> users = View.retrieveUserList();
        ArrayList<Label> toList = new ArrayList<Label>();


        for (Status status : users.keySet()) {
            for (String user : users.get(status)) {
                //if (user.equals(username)) continue;
                Label userLabel = new Label();
                userLabel.setText(user);
                Circle circle = new Circle();
                circle.setRadius(5.0f);
                userLabel.setGraphic(circle);
                if (status == Status.ONLINE) {
                    circle.setFill(Color.GREEN);

                } else if (status == Status.OFFLINE) {
                    circle.setFill(Color.RED);
                }
                toList.add(userLabel);
            }
        }


        userList.getItems().clear();
        userList.getItems().addAll(toList);

        userList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String userClicked = ((Label) userList.getSelectionModel().getSelectedItem()).getText();

                if (Config.DEBUG) System.out.println("ListView user clicked on: " + userClicked);
                getTopic(username);
            }

        });


    }

    private List<FlowPane> transformMessagesToLabel(ArrayList<Message> messages) {
        return messages.stream().map(message -> {
            FlowPane wrapper = new FlowPane();

            Text text = new Text();
            text.setText(message.getText());
            text.setFont(Font.font("Verdana", 14));

            Text time = new Text();
            time.setText(new SimpleDateFormat("HH:mm").format(message.getTime()));
            time.setFont(Font.font("Verdana", 11));

            HBox hbox = new HBox();
            String hbStyle = "-fx-background-radius: 20; -fx-padding: 8; ";

            hbox.setMaxWidth(250);
            hbox.setPrefWidth(50);
            hbox.setAlignment(Pos.BOTTOM_LEFT);
            HBox.setMargin(text, new Insets(1, 5, 1, 7));

            if (message.getSender().equals(this.username)) {
                wrapper.setAlignment(Pos.CENTER_RIGHT);
                hbStyle = hbStyle.concat("-fx-background-color: rgb(211, 239, 190);");

            } else {
                wrapper.setAlignment(Pos.CENTER_LEFT);
                hbStyle = hbStyle.concat("-fx-background-color: rgb(220, 220, 220);");
            }
            hbox.setStyle(hbStyle);


            hbox.getChildren().addAll(text, time);
            wrapper.getChildren().add(hbox);
            return wrapper;
        }).collect(toList());

    }

    private void getTopic(String participant) {
        setTopicViewVisibility(false);
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new Message("phil", participant, new Date(), "Is this a message? I don't think so but let's see eventually it is"));
        messages.add(new Message(participant, "phil", new Date(), "Yes"));

        topicView.getItems().clear();
        topicView.getItems().addAll(transformMessagesToLabel(messages));


        setTopicViewVisibility(true);
    }


    @Override
    public void setScene() throws IOException {
        super.activateScene((Parent) FXMLLoader.load(getClass().getResource(FXML)), 868, 679);
    }


}
