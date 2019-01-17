package upm.lssp.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import upm.lssp.messages.DailySeparator;
import upm.lssp.messages.Message;
import upm.lssp.messages.MessageWrapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class Topic {

    /**
     * Make a MessageWrapper (either a message or a daily separator) as
     * box to be later shown in the viewlist
     *
     * @param wMessage a message that can be a Daily separator or an actual message
     * @param thisUsername the username
     * @return the layout of the message
     */
    private static FlowPane uizeMessage(MessageWrapper wMessage, String thisUsername) {
        FlowPane wrapper = new FlowPane();
        if (wMessage instanceof DailySeparator) {
            DailySeparator sep = (DailySeparator) wMessage;

            wrapper.setAlignment(Pos.CENTER);

            Text time = new Text();
            time.setText(new SimpleDateFormat("dd/MM/yyyy").format(sep.getTime()));
            time.setFont(Font.font("Verdana", 14));
            time.setWrappingWidth(100);
            time.setTextAlignment(TextAlignment.CENTER);
            HBox hbox = new HBox();
            String hbStyle = "-fx-background-radius: 20; -fx-padding: 8; -fx-background-color: rgb(210,239,125);";

            hbox.setMaxWidth(100);

            hbox.setAlignment(Pos.CENTER);

            HBox.setMargin(time, new Insets(1, 5, 1, 7));
            hbox.setStyle(hbStyle);
            hbox.getChildren().add(time);
            wrapper.getChildren().add(hbox);


        } else {
            Message message = (Message) wMessage;


            Text text = new Text();
            text.setText(message.getText());
            text.setFont(Font.font("Verdana", 14));
            text.setWrappingWidth(250);


            Text time = new Text();
            time.setText(new SimpleDateFormat("HH:mm").format(message.getTime()));
            time.setFont(Font.font("Verdana", 11));


            HBox hbox = new HBox();
            String hbStyle = "-fx-background-radius: 20; -fx-padding: 8; ";

            hbox.setMaxWidth(250);
            hbox.setPrefWidth(50);
            hbox.setAlignment(Pos.BOTTOM_LEFT);
            HBox.setMargin(text, new Insets(1, 5, 1, 7));

            if (message.getSender().equals(thisUsername)) {
                wrapper.setAlignment(Pos.CENTER_RIGHT);
                hbStyle = hbStyle.concat("-fx-background-color: rgb(211, 239, 190);");

            } else {
                wrapper.setAlignment(Pos.CENTER_LEFT);
                hbStyle = hbStyle.concat("-fx-background-color: rgb(220, 220, 220);");
            }
            hbox.setStyle(hbStyle);


            hbox.getChildren().addAll(text, time);
            wrapper.getChildren().add(hbox);

        }
        return wrapper;
    }

    /**
     * Given an ArrayList of wrapping messages, calls uizeMessage for each
     *
     * @param wMessages
     * @param thisUsername
     * @return
     */
    static List<FlowPane> uizeMessages(ArrayList<MessageWrapper> wMessages, String thisUsername) {
        return wMessages.stream().map(wMessage -> uizeMessage(wMessage, thisUsername)).collect(toList());

    }

    /**
     * Using a sliding window detects when a Daily separator need to be added
     *
     * @param messages
     * @return
     */
    static ArrayList<MessageWrapper> addDailySeparator(ArrayList<MessageWrapper> messages) {
        ArrayList<MessageWrapper> list = (ArrayList<MessageWrapper>)
                sliding(messages, 2)
                        .filter(Topic::checkIfDailySeparatorIsNeeded)
                        .map(twoMessages -> (MessageWrapper) new DailySeparator(twoMessages.get(1).getTime()))
                        .collect(toList());

        //Adding the separator of the first message
        if (messages.size() != 0) {
            list.add(new DailySeparator(messages.get(0).getTime()));
        }
        return list;
    }

    /**
     * Return true when the daily separator is needed
     *
     * @param twoMessages
     * @return
     */
    static boolean checkIfDailySeparatorIsNeeded(List<MessageWrapper> twoMessages) {
        return checkIfDailySeparatorIsNeeded(twoMessages.get(0), twoMessages.get(1));
    }

    /**
     * Return true when the daily separator is needed (overloaded)
     *
     * @param first
     * @param second
     * @return
     */
    static boolean checkIfDailySeparatorIsNeeded(MessageWrapper first, MessageWrapper second) {
        if (first == null || second == null) return true;
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return !fmt.format(first.getTime()).equals(fmt.format(second.getTime()));
    }

    /**
     * A sliding window script to cope with Java8's lack
     *
     * @param list
     * @param size
     * @param <T>
     * @return
     */
    private static <T> Stream<List<T>> sliding(List<T> list, int size) {
        if (size > list.size())
            return Stream.empty();
        return IntStream.range(0, list.size() - size + 1)
                .mapToObj(start -> list.subList(start, start + size));
    }


}
