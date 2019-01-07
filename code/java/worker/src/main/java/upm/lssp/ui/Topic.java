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

import static java.util.stream.Collectors.toList;

public class Topic {
    public static FlowPane uizeMessage(MessageWrapper wMessage, String thisUsername) {
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

    public static List<FlowPane> uizeMessages(ArrayList<MessageWrapper> wMessages, String thisUsername) {
        return wMessages.stream().map(wMessage -> uizeMessage(wMessage, thisUsername)).collect(toList());

    }
}
