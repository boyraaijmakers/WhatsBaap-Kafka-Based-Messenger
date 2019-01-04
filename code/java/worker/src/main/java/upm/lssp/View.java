package upm.lssp;

import javafx.stage.Stage;
import upm.lssp.exceptions.GenericException;
import upm.lssp.ui.UIController;
import upm.lssp.worker.ZookeeperWorker;

import java.io.IOException;

public class View {

    private static Stage stage;
    private static ZookeeperWorker zooWorker;
    private static UIController uiController;

    public static Stage getStage() {
        return stage;
    }

    public static void setStage(Stage stage) {
        View.stage = stage;
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
        return zooWorker.register(username);
    }
    /* INCOMING */
    public static boolean quit(String username) throws GenericException {
        return zooWorker.quit(username);
    }


    /* Callback methods - OUTGOING */
    public static void error(String message) {
        uiController.showError(message);
    }

    private static void info(String message) {
        uiController.showInfo(message);
    }

}
