package upm.lssp.exceptions;

public class QuitException extends GenericException {


    public QuitException(String s) {
        super(s);
    }


    @Override
    public String toString() {
        return "QuitException: " + super.getMessage();
    }


}
