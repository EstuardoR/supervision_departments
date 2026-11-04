/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */

package controller;

public class LoginException extends Exception {

    public LoginException(String message) {
        super(message);
    }

    public LoginException(String message, Throwable cause) {
        super(message, cause);
    }
}
