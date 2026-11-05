/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package dao;

public class ReporteException extends Exception {

    public ReporteException(String message) {
        super(message);
    }

    public ReporteException(String message, Throwable cause) {
        super(message, cause);
    }
}
