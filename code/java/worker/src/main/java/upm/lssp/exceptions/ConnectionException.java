package upm.lssp.exceptions;

public class ConnectionException extends Throwable {


    public ConnectionException(String s) {
        super(s);
    }


    @Override
    public String toString() {
        return "ConnectionException: " + super.getMessage();
    }


}
