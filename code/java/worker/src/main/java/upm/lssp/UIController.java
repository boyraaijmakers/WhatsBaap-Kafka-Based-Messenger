package upm.lssp;

import upm.lssp.exceptions.ConnectionException;
import upm.lssp.exceptions.RegistrationException;
import upm.lssp.ui.UI;
import upm.lssp.worker.ZookeeperWorker;

public class UIController {
    private ZookeeperWorker zooWorker;
    private UI scene;

    public UIController(UI scene) {
        this.scene = scene;
        try {
            zooWorker = new ZookeeperWorker();
        } catch (ConnectionException e) {
            error(e.getMessage());
        }

    }

    /* INCOMING */
    public void login(String username) throws RegistrationException {
        zooWorker.register(username);
    }


    /* Callback methods - OUTGOING */
    private void error(String message) {
        scene.showError(message);
    }

    private void info(String message) {
        scene.showInfo(message);
    }

}
