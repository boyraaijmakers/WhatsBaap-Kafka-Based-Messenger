package upm.lssp;

import javafx.application.Application;
import javafx.stage.Stage;
import upm.lssp.exceptions.ConnectionException;
import upm.lssp.ui.UserUIController;
import upm.lssp.worker.ZookeeperWorker;


public class RunWorker extends Application implements Runnable {

    public void start(Stage stage) {

        if (Config.DEBUG) System.out.println("Welcome! Loading UI...");
        View.setStage(stage);
        try {
            View.setZooWorker(new ZookeeperWorker());
        } catch (ConnectionException e) {
            View.error(e.getMessage());
        }

        View.setController(new UserUIController());


        if (Config.DEBUG) System.out.println("UIController Loaded");

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
