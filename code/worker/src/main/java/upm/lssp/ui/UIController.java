package upm.lssp.ui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import upm.lssp.messages.Message;

import java.io.IOException;
import java.util.List;

public abstract class UIController {

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setTitle("WhatsBaap");
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
    }

    public void showError(String message) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setHeaderText("An error occurred");
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }

    public void activateScene(Parent root, double width, double height) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - width) / 2);
        stage.setY((screenBounds.getHeight() - height) / 2);
        stage.setScene(new Scene(root, width, height));
        stage.show();
    }

    public void showInfo(String message) {
        Alert errorAlert = new Alert(Alert.AlertType.INFORMATION);
        errorAlert.setHeaderText("Information");
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }


    public abstract void setScene() throws IOException;


    public abstract void receiveMessage(List<Message> message);
}
