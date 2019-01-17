package upm.lssp.exceptions;

public class RegistrationException extends GeneralException {


    /**
     * Error thrown when the registration
     * process doesn't go though
     *
     * @param s error string
     */
    public RegistrationException(String s) {
        super(s);
    }


    @Override
    public String toString() {
        return "RegistrationException: " + super.getMessage();
    }


}
