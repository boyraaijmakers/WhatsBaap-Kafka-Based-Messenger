package upm.lssp;

import javafx.stage.Stage;
import upm.lssp.exceptions.ConnectionException;
import upm.lssp.exceptions.GenericException;
import upm.lssp.ui.UIController;
import upm.lssp.worker.ZookeeperWorker;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class View {

    private static Stage stage;
    private static ZookeeperWorker zooWorker;
    private static UIController uiController;

    private static String username;
    private static boolean logged;
    private static boolean status;

    public static Stage getStage() {
        return stage;
    }

    public static void setStage(Stage stage) {
        View.stage = stage;
    }

    public static String getUsername() {
        return username;
    }


    public static void setController(UIController uiController) {
        View.uiController = uiController;

        uiController.setStage(stage);

        try {
            uiController.setScene();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Config.DEBUG) System.out.println("Controller " + uiController.getClass() + " set");
    }

    public static void setZooWorker(ZookeeperWorker zooWorker) {
        View.zooWorker = zooWorker;
    }

    /* INCOMING */
    public static boolean login(String username) throws GenericException {
        View.username = username;
        return zooWorker.register(username);
    }
    public static boolean quit(String username) throws GenericException {
        View.username = null;
        return zooWorker.quit(username);
    }

    public static boolean goOnline(String username) throws ConnectionException {
        return zooWorker.goOnline(username);
    }

    public static boolean goOffline(String username) throws ConnectionException, InterruptedException {
        return zooWorker.goOffline(username);
    }

    public static HashMap<Status, List<String>> retrieveUserList() {
        return zooWorker.retrieveUserList();
    }


    /* Callback methods - OUTGOING */
    public static void error(String message) {
        uiController.showError(message);
    }

    private static void info(String message) {
        uiController.showInfo(message);
    }

}
