package upm.lssp.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import upm.lssp.Config;
import upm.lssp.Status;
import upm.lssp.View;
import upm.lssp.exceptions.ConnectionException;
import upm.lssp.exceptions.GenericException;
import upm.lssp.messages.DailySeparator;
import upm.lssp.messages.Message;
import upm.lssp.messages.MessageWrapper;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class ChatUIController extends UIController implements Initializable {
    private static final String FXML = "/chat.fxml";
    @FXML
    public VBox topicAndTextView;
    @FXML
    public ListView userList;
    @FXML
    public Button statusButton;
    @FXML
    public Circle myStatus;
    @FXML
    public Text topicUsername;
    @FXML
    public TextField textBox;
    @FXML
    public ListView topicView;
    @FXML
    public ScrollPane scrollTopicView;
    private String username;
    private Status status;
    private String openedTopicWith;
    private Message lastMessageSentOrReceived;
    private ArrayList<Message> incomingMessageQueue;


    public ChatUIController() {
        this.username = View.getUsername();
    }

    /**
     * Called when clicked on quit button
     */
    public void quit() {
        if (Config.DEBUG) System.out.println("Quit request for: " + username);
        boolean status = false;
        try {
            status = View.quit(username);
        } catch (GenericException e) {
            this.showError(e.getMessage());
        }
        if (status) {
            if (!Config.AUTOLOGIN) this.showInfo("You have been successfully disconnected. See you!");
            View.setController(new UserUIController());
        }

    }

    /**
     * Called when clicked on "Go Offline" or "Go online" button
     */
    public void changeStatus() {
        if (Config.DEBUG) System.out.println("Status changed");
        if (this.status == Status.ONLINE) {
            goOffline();
        } else {
            goOnline();
        }
    }


    /**
     * Private method to deal with offline requests
     */
    private void goOffline() {
        if (Config.DEBUG) System.out.println("GoOffline request");
        try {
            View.goOffline();
        } catch (ConnectionException | InterruptedException e) {
            this.showError(e.getMessage());
        }
        this.status = Status.OFFLINE;
        this.myStatus.setFill(Color.RED);
        this.statusButton.setText("Go Online");
    }

    /**
     * Private method to deal with online request. Resets the incoming message queue
     */
    private void goOnline() {
        if (Config.DEBUG) System.out.println("GoOnline request");
        try {
            View.goOnline(username);
        } catch (ConnectionException e) {
            this.showError(e.getMessage());
        }
        this.status = Status.ONLINE;
        this.myStatus.setFill(Color.GREEN);
        this.statusButton.setText("Go Offline");
        this.incomingMessageQueue = new ArrayList<>();
    }


    /**
     * Sets the visibility of the input box, the topic view and the topic name (username of
     * the topic's counterpart)
     *
     * @param condition
     */
    private void setTopicViewVisibility(boolean condition) {
        textBox.setVisible(condition);
        topicAndTextView.setVisible(condition);
        topicUsername.setVisible(condition);
    }


    /**
     * Refresh user list by calling  View.retrieveUserList(). Then,
     * separate the online users from the offline ones (and place the status
     * circle). Checks whether there are unread messages in the incoming messages queue
     * and in case shows a blue circle for notification.
     */
    private void refreshUserList() {

        HashMap<Status, List<String>> users = View.retrieveUserList();
        ArrayList<Label> toList = new ArrayList<>();


        for (Status status : Arrays.asList(Status.ONLINE, Status.OFFLINE)) {
            for (String user : users.get(status)) {
                if (!Config.DEBUG && user.equals(username)) continue;
                Label userLabel = new Label();
                userLabel.setText(user);

                Circle statusCircle = new Circle();
                statusCircle.setRadius(5.0f);

                userLabel.setGraphic(statusCircle);
                if (status == Status.ONLINE) {
                    statusCircle.setFill(Color.GREEN);

                } else if (status == Status.OFFLINE) {
                    statusCircle.setFill(Color.RED);
                }


                HBox hb = new HBox();

                hb.getChildren().add(statusCircle);

                long incomingMessagesToRead = incomingMessageQueue.stream()
                        .filter(message -> message.getSender().equals(user)).count();

                if (incomingMessagesToRead > 0) {
                    Circle notificationCircle = new Circle();
                    notificationCircle.setRadius(5.0f);
                    notificationCircle.setFill(Color.BLUEVIOLET);

                    hb.getChildren().add(notificationCircle);
                    HBox.setMargin(notificationCircle, new Insets(0, 0, 0, 4));

                }

                userLabel.setGraphic(hb);
                toList.add(userLabel);
            }

        }


        userList.getItems().clear();
        userList.getItems().addAll(toList);

        userList.setOnMouseClicked(event -> {
            String userClicked = ((Label) userList.getSelectionModel().getSelectedItem()).getText();

            if (Config.DEBUG) System.out.println("ListView user clicked on: " + userClicked);
            getTopic(userClicked);
        });


    }

    /**
     * Method called when the UI is loaded
     *
     * @param location
     * @param resources
     */
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        goOnline();
        setTopicViewVisibility(false);
        topicView.setFocusTraversable(false);
        userList.setFocusTraversable(false);
        refreshUserList();
    }


    /**
     * Method called when the return key on the text box is pressed
     */
    public void sendMessage() {
        String receiver = openedTopicWith;

        String text = textBox.getText();

        Message newMessage = new Message(username, receiver, text);

        sendReceiverUIHandler(newMessage);
        textBox.setText("");
    }

    /**
     * Method called when a new messages is received
     *
     * @param newMessage
     */
    public void receiveMessage(Message newMessage) {
        //If the user view is on that chat I'll show it, otherwise I notify
        if (newMessage.getSender().equals(openedTopicWith)) {
            sendReceiverUIHandler(newMessage);
        } else {
            incomingMessageQueue.add(newMessage);
            refreshUserList();
        }

    }


    /**
     * Handles the UI transformation of a Message object (either
     * incoming or outgoing)
     *
     * @param newMessage
     */
    private void sendReceiverUIHandler(Message newMessage) {
        //We need to create a list, which may contain a separator
        ArrayList<MessageWrapper> newMessageWrapperList = new ArrayList<>();
        newMessageWrapperList.add(newMessage);


        if (Topic.checkIfDailySeparatorIsNeeded(lastMessageSentOrReceived, newMessage)) {
            newMessageWrapperList.add(new DailySeparator());
        }
        newMessageWrapperList.sort(Comparator.comparing(MessageWrapper::getTime));

        topicView.getItems().addAll(Topic.uizeMessages(newMessageWrapperList, username));
        lastMessageSentOrReceived = newMessage;
        scrollTopicView.setVvalue(1D);
    }


    /**
     * Removes from incoming message queue the notification of a new message
     *
     * @param participant
     */
    private void removeIncomingNotification(String participant) {
        incomingMessageQueue = (ArrayList<Message>) incomingMessageQueue.stream().filter(message -> !message.getSender().equals(participant)).collect(toList());
        refreshUserList();
    }

    /**
     * Retrieve topic messages
     *
     * @param participant
     */
    private void getTopic(String participant) {
        setTopicViewVisibility(false);
        textBox.requestFocus();
        textBox.positionCaret(0);
        lastMessageSentOrReceived = null;
        ArrayList<MessageWrapper> messages = new ArrayList<>();
        int i = 0;
/*        messages.add(new Message("i: " + i, participant, new Date(), i + " Is this a message? I don't think so but let's see eventually it is bla bla bla bla bla bla bla bla bla bla"));
        messages.add(new Message(participant, "i: 000" + i, new Date(), "Yes"));
        messages.add(new Message("i: " + i, participant, new Date(), i + " Is this a message? I don't think so but let's see eventually it is bla bla bla bla bla bla bla bla bla bla"));
        messages.add(new Message("i: " + i, participant, new Date(), i + " Is this a message? I don't think so but let's see eventually it is bla bla bla bla bla bla bla bla bla bla"));
        */
        String dateString = "10/15/2015 09:30:0" + i;
        SimpleDateFormat sdf = new SimpleDateFormat("mm/dd/yyyy hh:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        messages.add(new Message(username, participant, date, i + " Is this a message? I don't think so but let's see eventually it is bla bla bla bla bla bla bla bla bla bla"));
        messages.add(new Message(participant, username, date, "Yes"));


        topicView.getItems().clear();

        messages.sort(Comparator.comparing(MessageWrapper::getTime));
        //Adding date separator
        messages.addAll(Topic.addDailySeparator(messages));
        //Re-sort with all the Wrappers
        messages.sort(Comparator.comparing(MessageWrapper::getTime));
        //Saving the last message sent/received
        lastMessageSentOrReceived = (Message) messages.get(messages.size() - 1);
        //Merging them all together into the viewlist
        topicView.getItems().addAll(Topic.uizeMessages(messages, username));


        this.openedTopicWith = participant;
        this.topicUsername.setText(participant);
        setTopicViewVisibility(true);
        scrollTopicView.setVvalue(1D);

        removeIncomingNotification(participant);
    }


    @Override
    public void setScene() throws IOException {
        super.activateScene(FXMLLoader.load(getClass().getResource(FXML)), 868, 679);
    }


}
