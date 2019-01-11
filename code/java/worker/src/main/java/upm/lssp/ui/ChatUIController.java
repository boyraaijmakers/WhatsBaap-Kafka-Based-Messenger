package upm.lssp.ui;

import javafx.application.Platform;
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
import upm.lssp.exceptions.GenericException;
import upm.lssp.exceptions.SendException;
import upm.lssp.exceptions.SendToOfflineException;
import upm.lssp.messages.DailySeparator;
import upm.lssp.messages.Message;
import upm.lssp.messages.MessageWrapper;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private static final List<Object> incomingMessageQueue = Collections.synchronizedList(new ArrayList<>());
    private static final List<Object> onlineUsers = Collections.synchronizedList(new ArrayList<>());
    @FXML
    public ScrollPane scrollTopicView;


    private String username;
    private Status status;
    private static final HashMap<String, ArrayList<MessageWrapper>> messages = new HashMap<>();
    private Message lastMessageSentOrReceived;
    private static String openedTopicWith;
    private final Object openedTopicLock = new Object();

    @FXML
    public Text myUsername;
    @FXML
    public ListView topicView;
    private AtomicBoolean closeThreads = new AtomicBoolean(false);



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

        shutdownRefreshUserList();

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
        } catch (GenericException e) {
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
        } catch (GenericException e) {
            this.showError(e.getMessage());
        }
        this.status = Status.ONLINE;
        this.myStatus.setFill(Color.GREEN);
        this.statusButton.setText("Go Offline");

        /*new Thread(()-> {
            while(!closeThreads.get()) {
                if(openedTopicWith==null){
                    System.err.println("NULL");
                }
            }
        }).start();*/
    }


    /**
     * Sets the visibility of the input box, the topic view and the topic name (username of
     * the topic's counterpart)
     *
     * @param condition
     */
    private void setTopicViewVisibility(boolean condition) {
        setTextBoxVisibility(condition);
        topicAndTextView.setVisible(condition);
        topicUsername.setVisible(condition);
    }

    private void setTextBoxVisibility(boolean condition) {
        textBox.setVisible(condition);
    }


    /**
     * Refresh user list by calling  View.retrieveUserList(). Then,
     * separate the online users from the offline ones (and place the status
     * circle). Checks whether there are unread messages in the incoming messages queue
     * and in case shows a blue circle for notification.
     */
    private void refreshUserList() {

        /*
        boolean toSendNotification;
        int notificationSeconds=0;
        boolean notNotified=true;
        */

        while (!closeThreads.get()) {
            //toSendNotification=false;

            HashMap<Status, List<String>> users = View.retrieveUserList();
            ArrayList<Label> toList = new ArrayList<>();

            synchronized (onlineUsers) {
                onlineUsers.clear();
                onlineUsers.addAll(users.get(Status.ONLINE));
            }


            for (Status status : Arrays.asList(Status.ONLINE, Status.OFFLINE)) {
                for (String user : users.get(status)) {
                    if (user.equals(username)) continue;
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
                    long incomingMessagesToRead;

                    synchronized (incomingMessageQueue) {
                        incomingMessagesToRead = incomingMessageQueue.stream()
                                .filter(message -> {
                                    Message m = (Message) message;
                                    return m.getSender().equals(user);
                                })
                                .count();
                    }


                    if (incomingMessagesToRead > 0) {
                        //toSendNotification=true;


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
            Platform.runLater(() -> {
                userList.getItems().clear();
                userList.getItems().addAll(toList);
                synchronized (openedTopicLock) {
                    if (!onlineUsers.contains(openedTopicWith)) {
                        setTextBoxVisibility(false);
                    } else {
                        setTextBoxVisibility(true);
                    }
                }
            });

            userList.setOnMouseClicked(event -> {
                if (userList.getSelectionModel().getSelectedItem() != null) {
                    String userClicked = ((Label) userList.getSelectionModel().getSelectedItem()).getText();
                    if (Config.DEBUG) System.out.println("ListView user clicked on: " + userClicked);
                    getTopic(userClicked);
                }
            });

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
/*            if(toSendNotification) notificationSeconds++;
            System.out.println("Notification seconds: "+notificationSeconds);

            if(notNotified && notificationSeconds>5){
                View.info("You have unread messages!");
                notNotified=false;
            }*/
        }

    }


    private void shutdownRefreshUserList() {
        closeThreads.set(true);
    }

    /**
     * Method called when the UI is loaded
     *
     * @param location
     * @param resources
     */
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        myUsername.setText(username);
        goOnline();
        setTopicViewVisibility(false);
        setTextBoxVisibility(false);
        topicView.setFocusTraversable(false);
        userList.setFocusTraversable(false);
        new Thread(this::refreshUserList).start();
        new Thread(this::liveConsumer).start();
    }


    /**
     * Method called when the return key on the text box is pressed
     */
    public void sendMessage() {
        String receiver;
        synchronized (openedTopicLock) {
            receiver = openedTopicWith;
        }
        String text = textBox.getText();

        Message newMessage = new Message(username, receiver, text);
        try {
            View.sendMessage(newMessage);
            sendReceiverUIHandler(newMessage);
            messages.computeIfAbsent(newMessage.getSender(), k -> new ArrayList<>());
            messages.get(newMessage.getReceiver()).add(newMessage);
        } catch (SendToOfflineException e) {
            showError(e.getMessage());
            setTextBoxVisibility(false);
        } catch (SendException e) {
            showError(e.getMessage());
        }


        textBox.setText("");
    }

    public void receiveMessage(List<Message> newMessages) {
        synchronized (incomingMessageQueue) {
            incomingMessageQueue.addAll(newMessages);
            incomingMessageQueue.notifyAll();
        }


    }


    /**
     * Handles the UI transformation of a Message object (either
     * incoming or outgoing)
     *
     * @param newMessage
     */
    private void sendReceiverUIHandler(Message newMessage) {
        if (Config.DEBUG) System.out.println("sendReceiverUIHandler");
        //We need to create a list, which may contain a separator
        ArrayList<MessageWrapper> newMessageWrapperList = new ArrayList<>();
        newMessageWrapperList.add(newMessage);


        if (Topic.checkIfDailySeparatorIsNeeded(lastMessageSentOrReceived, newMessage)) {
            newMessageWrapperList.add(new DailySeparator());
        }
        newMessageWrapperList.sort(Comparator.comparing(MessageWrapper::getTime));
        Platform.runLater(() -> {
            this.topicView.getItems().addAll(Topic.uizeMessages(newMessageWrapperList, username));
        });
        lastMessageSentOrReceived = newMessage;
        scrollTopicView.setVvalue(1D);
    }



    /**
     * Retrieve topic messages
     *
     * @param participant
     */
    private void getTopic(String participant) {
        boolean isParticipantOnline;
        synchronized (onlineUsers) {
            isParticipantOnline = onlineUsers.contains(participant);
        }
        setTopicViewVisibility(false);
        setTextBoxVisibility(false);
        synchronized (openedTopicLock) {
            openedTopicWith = participant;
        }

        lastMessageSentOrReceived = null;

        ArrayList<MessageWrapper> topic_messages;
        if (messages.get(participant) == null) {
            topic_messages = new ArrayList<>();
            messages.put(participant, topic_messages);
        } else {
            topic_messages = (ArrayList<MessageWrapper>) messages.get(participant).clone();
        }


        topicView.getItems().clear();

        topic_messages.sort(Comparator.comparing(MessageWrapper::getTime));
        //Adding date separator
        topic_messages.addAll(Topic.addDailySeparator(topic_messages));
        //Re-sort with all the Wrappers
        topic_messages.sort(Comparator.comparing(MessageWrapper::getTime));
        //Saving the last message sent/received
        if (topic_messages.size() > 0)
            lastMessageSentOrReceived = (Message) topic_messages.get(topic_messages.size() - 1);
        //Merging them all together into the viewlist
        topicView.getItems().addAll(Topic.uizeMessages(topic_messages, username));



        this.topicUsername.setText(participant);
        setTopicViewVisibility(true);


        if (isParticipantOnline) {
            setTextBoxVisibility(true);
            textBox.requestFocus();
            textBox.positionCaret(0);
        } else {
            setTextBoxVisibility(false);
        }


        scrollTopicView.setVvalue(1D);
    }

    private void liveConsumer() {
        while (!closeThreads.get()) {
            synchronized (incomingMessageQueue) {

                while (incomingMessageQueue.stream().noneMatch(message -> {
                    Message m = (Message) message;
                    return m.getSender().equals(openedTopicWith);
                })) {
                    try {
                        incomingMessageQueue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                List<Object> messagesToProcess = incomingMessageQueue.stream().filter(message -> {
                    Message m = (Message) message;
                    return m.getSender().equals(openedTopicWith);
                }).collect(toList());
                messagesToProcess.forEach(m -> {
                    sendReceiverUIHandler((Message) m);
                    messages.get(((Message) m).getSender()).add((Message) m);
                });

                incomingMessageQueue.removeAll(messagesToProcess);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void setScene() throws IOException {
        super.activateScene(FXMLLoader.load(getClass().getResource(FXML)), 868, 679);
    }


}
