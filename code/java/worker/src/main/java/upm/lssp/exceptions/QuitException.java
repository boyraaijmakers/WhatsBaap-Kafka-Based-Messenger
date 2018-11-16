package upm.lssp.exceptions;

public class QuitException extends Throwable {


    public QuitException(String s) {
        super(s);
    }


    @Override
    public String toString() {
        return "QuitException: " + super.getMessage();
    }


}
