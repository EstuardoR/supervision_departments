package servicios;

import dao.UsuarioDAO;
import java.awt.Component;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import model.Usuario;
import util.Sesion;

/**
 * Firma simulada. Uso desde cualquier botón:
 *
 *     if (FirmaService.firmar()) { ... }
 *
 * Pide la contraseña del usuario que inició sesión. Si es correcta muestra
 * "Firmado" con fecha, hora y un código corto.
 */
public class FirmaService {

    private static final int MAX_INTENTOS = 3;
    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static String ultimoCodigo;
    private static String ultimaFechaHora;

    private FirmaService() {
    }

    /** Pide la contraseña y devuelve true si la firma fue exitosa. */
    public static boolean firmar() {
        return firmar(null);
    }

    /** Igual que firmar(), pero centrando la ventana sobre el formulario dado. */
    public static boolean firmar(Component parent) {
        Usuario usuario = Sesion.getInstancia().getUsuarioActual();
        if (usuario == null) {
            JOptionPane.showMessageDialog(parent, "No hay una sesión activa.",
                    "Firma", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            JPasswordField campo = new JPasswordField();
            int opcion = JOptionPane.showConfirmDialog(parent, campo,
                    "Ingrese su contraseña para firmar",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opcion != JOptionPane.OK_OPTION) {
                return false;
            }

            String password = new String(campo.getPassword());
            try {
                if (contrasenaCorrecta(usuario.getCui(), password)) {
                    ultimaFechaHora = LocalDateTime.now().format(FORMATO);
                    ultimoCodigo = "F-" + UUID.randomUUID().toString()
                            .substring(0, 8).toUpperCase();
                    JOptionPane.showMessageDialog(parent,
                            "Firmado\nFecha y hora: " + ultimaFechaHora
                            + "\nCódigo: " + ultimoCodigo,
                            "Firma", JOptionPane.INFORMATION_MESSAGE);
                    return true;
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(parent,
                        "No se pudo validar la contraseña: " + e.getMessage(),
                        "Firma", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            JOptionPane.showMessageDialog(parent,
                    "Contraseña incorrecta (intento " + intento + " de " + MAX_INTENTOS + ").",
                    "Firma rechazada", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    /** Código de la última firma exitosa (para guardarlo en la base de datos). */
    public static String getUltimoCodigo() {
        return ultimoCodigo;
    }

    /** Fecha y hora de la última firma exitosa, formato dd/MM/yyyy HH:mm:ss. */
    public static String getUltimaFechaHora() {
        return ultimaFechaHora;
    }

    private static boolean contrasenaCorrecta(String cui, String password) throws SQLException {
        Usuario guardado = new UsuarioDAO().buscarPorCui(cui);
        return guardado != null && password.equals(guardado.getContrasena());
    }
}