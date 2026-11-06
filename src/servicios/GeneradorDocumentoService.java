/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package servicios;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Genera los documentos del sistema (Anexos I-IV y Matriz de seguimiento) como un archivo
 * HTML real dentro de expedientes/{idSupervision}/, y permite abrirlo con la aplicación
 * predeterminada del sistema operativo. No es un PDF con firma digital real: es el documento
 * "generado" que la especificación pide, en el formato más simple que se puede producir de
 * forma confiable sin agregar una librería de PDF nueva.
 */
public class GeneradorDocumentoService {

    private static final DateTimeFormatter FORMATO_ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private GeneradorDocumentoService() {
    }

    /**
     * Genera el documento HTML con el título y cuerpo dados, lo guarda en
     * expedientes/{idSupervision}/ y devuelve la ruta del archivo creado.
     */
    public static String generar(int idSupervision, String nombreDocumento, String titulo, String cuerpoHtml)
            throws IOException {
        Path carpeta = Paths.get("expedientes", String.valueOf(idSupervision));
        Files.createDirectories(carpeta);

        String nombreArchivo = nombreDocumento.replaceAll("[^A-Za-z0-9]+", "_")
                + "_" + LocalDateTime.now().format(FORMATO_ARCHIVO) + ".html";
        Path destino = carpeta.resolve(nombreArchivo);

        String html = "<html><head><meta charset=\"UTF-8\"><title>" + escapar(titulo) + "</title>"
                + "<style>body{font-family:Arial,sans-serif;margin:40px;}"
                + "h1{font-size:18px;border-bottom:2px solid #333;padding-bottom:8px;}"
                + "table{border-collapse:collapse;width:100%;margin-top:12px;}"
                + "td,th{border:1px solid #999;padding:6px 8px;text-align:left;font-size:13px;}"
                + ".firma{margin-top:30px;padding-top:10px;border-top:1px dashed #666;font-size:13px;}</style>"
                + "</head><body><h1>" + escapar(titulo) + "</h1>" + cuerpoHtml + "</body></html>";

        Files.write(destino, html.getBytes(StandardCharsets.UTF_8));
        return destino.toAbsolutePath().toString();
    }

    /** Abre el documento con la aplicación predeterminada (normalmente el navegador). */
    public static boolean abrir(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.isEmpty()) {
            return false;
        }
        File archivo = new File(rutaArchivo);
        if (!archivo.exists() || !Desktop.isDesktopSupported()) {
            return false;
        }
        try {
            Desktop.getDesktop().open(archivo);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static String escapar(String texto) {
        return texto == null ? "" : texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
