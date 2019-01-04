package upm.lssp.ui;

import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class UIController {

    private Stage stage;
    private String username;

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setTitle("WhatsBaap");
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


    public void setUsername(String username) {
        this.username=username;
    }

    protected String getUsername() {
        return username;
    }
}
