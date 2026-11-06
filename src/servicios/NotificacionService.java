/*
 * Autor: María José Pelicó López
 * Carné: 9490-25-11528
 */
package servicios;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;

/**
 * Envío de correos. Uso desde cualquier botón:
 *
 *     NotificacionService.enviar("a@x.com, b@x.com", "Asunto", "Texto");
 *
 * Requiere jakarta.mail-2.0.1.jar y jakarta.activation-2.0.1.jar en Libraries.
 * Con Gmail se usa una "contraseña de aplicación", no la normal.
 */
public class NotificacionService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String REMITENTE = "supervisionesdci@gmail.com";
    private static final String CLAVE_APP = "dqjbwkclrxdgcgmf";

    private NotificacionService() {
    }

    /**
     * @param destinos uno o varios correos separados por coma
     * @return true si se envió, false si falló (el motivo sale en consola)
     */
    public static boolean enviar(String destinos, String asunto, String texto) {
        if (destinos == null || destinos.trim().isEmpty()) {
            System.err.println("Correo no enviado: no hay destinatarios.");
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(REMITENTE, CLAVE_APP);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(REMITENTE));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinos));
            msg.setSubject(asunto);
            msg.setText(texto);
            Transport.send(msg);
            return true;
        } catch (Exception e) {
            System.err.println("Error al enviar correo: " + e.getMessage());
            return false;
        }
    }

    /** Igual que enviar(String, ...), pero con una lista de correos. */
    public static boolean enviar(List<String> destinos, String asunto, String texto) {
        return enviar(String.join(",", destinos), asunto, texto);
    }
}