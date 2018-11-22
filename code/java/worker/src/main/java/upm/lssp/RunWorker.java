package upm.lssp;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import upm.lssp.worker.ZookeeperWorker;



public class RunWorker extends Application implements Runnable {

    public void start(Stage stage) throws Exception {
       /* ZookeeperWorker zooWorker = new ZookeeperWorker();
        RunWorker rw = new RunWorker();
        rw.run();*/
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("login.fxml"));

        stage.setTitle("WhatsBaap");
        stage.setScene(new Scene(root));
        stage.show();


    }

    private static final class Lock { }
    private final Object lock = new Lock();

    public static void main(String[] args) {

        launch(args);

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
