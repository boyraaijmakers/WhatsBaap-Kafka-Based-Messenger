package upm.lssp.ui;

import javafx.fxml.Initializable;
import upm.lssp.Config;

import java.net.URL;
import java.util.ResourceBundle;

public class UIController implements Initializable {

    public void initialize(URL location, ResourceBundle resources) {
        if(Config.DEBUG) System.out.println("Loading UI");
    }
}
