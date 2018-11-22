package upm.lssp.ui;


import javafx.scene.control.TextField;
import upm.lssp.Config;

public class LoginController {
public TextField username;

    public void login(){
        if(Config.DEBUG) System.out.println("Login request for: "+username.getText());
    }
}
