package upm.lssp.exceptions;

public class RegistrationException extends Throwable {


    public RegistrationException(String s) {
        super(s);
    }


    @Override
    public String toString() {
        return "RegistrationException: " + super.getMessage();
    }


}
