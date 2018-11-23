package upm.lssp.exceptions;

public class ConnectionException extends GenericException {


    public ConnectionException(String s) {
        super(s);
    }


    @Override
    public String toString() {
        return "ConnectionException: " + super.getMessage();
    }


}
