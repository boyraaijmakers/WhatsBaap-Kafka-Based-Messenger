package upm.lssp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;



public class RunWorker extends Application implements Runnable {

    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("login.fxml"));
        stage.setTitle("WhatsBaap");
        stage.setScene(new Scene(root));
        stage.show();
        if(Config.DEBUG) System.out.println("UI Loaded");


    }

    private static final class Lock { }
    private final Object lock = new Lock();

    public static void main(String[] args) {
        launch(args);
        new RunWorker().run();
    }



    public void run() {
        synchronized (lock) {
            while (true) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
