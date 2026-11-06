package servicios;

import dao.UsuarioDAO;
import java.io.File;
import java.nio.file.Files;
import model.Usuario;
import util.ArchivoService;
import util.Sesion;

/**
 * Clase temporal para probar los servicios. Bórrala antes del commit final.
 * La prueba de firma necesita SQL Server encendido.
 */
public class PruebaServicios {

    // Pon aquí el CUI de un usuario que exista en dbo.Usuarios
    private static final String CUI_PRUEBA = "CUI";
    // Cambia a true cuando configures el correo en NotificacionService
    private static final boolean PROBAR_CORREO = false;

    public static void main(String[] args) throws Exception {

        // ---------- 1. FirmaService ----------
        System.out.println("=== PRUEBA FIRMA ===");
        Usuario u = new UsuarioDAO().buscarPorCui(CUI_PRUEBA);
        if (u == null) {
            System.out.println("No existe el CUI " + CUI_PRUEBA + ". Cámbialo y vuelve a ejecutar.");
            return;
        }
        Sesion.getInstancia().setUsuarioActual(u);
        boolean firmado = FirmaService.firmar();
        System.out.println("Firmado: " + firmado);
        if (firmado) {
            System.out.println("Código: " + FirmaService.getUltimoCodigo()
                    + " | " + FirmaService.getUltimaFechaHora());
        }

        // ---------- 2. ArchivoService (el de util, de tus compañeros) ----------
        System.out.println("\n=== PRUEBA ARCHIVO ===");
        File temporal = File.createTempFile("prueba", ".txt");
        Files.write(temporal.toPath(), "contenido de prueba".getBytes());
        String ruta = ArchivoService.copiarAExpediente(999, temporal);
        System.out.println("Guardado en: " + ruta);
        System.out.println("Existe: " + new File(ruta).exists());

        // ---------- 3. NotificacionService ----------
        System.out.println("\n=== PRUEBA CORREO ===");
        if (PROBAR_CORREO) {
            boolean enviado = NotificacionService.enviar(
                    "tu_correo@gmail.com", "Prueba DCI", "Correo de prueba del sistema.");
            System.out.println("Correo enviado: " + enviado);
        } else {
            System.out.println("Prueba de correo omitida (PROBAR_CORREO = false)");
        }
    }
}