/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */
package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Maneja la copia de archivos adjuntos a la carpeta de expedientes del
 * proyecto (expedientes/{id_supervision}/).
 */
public class ArchivoService {

    private ArchivoService() {
    }

    /** Copia el archivo dado a expedientes/{idSupervision}/ y devuelve la ruta final. */
    public static String copiarAExpediente(int idSupervision, File origen) throws IOException {
        Path carpeta = Paths.get("expedientes", String.valueOf(idSupervision));
        Files.createDirectories(carpeta);
        Path destino = carpeta.resolve(origen.getName());
        Files.copy(origen.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        return destino.toString();
    }
}
