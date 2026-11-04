package dao;

public class SupervisionException extends Exception {

    public SupervisionException(String message) {
        super(message);
    }

    public SupervisionException(String message, Throwable cause) {
        super(message, cause);
    }
}
