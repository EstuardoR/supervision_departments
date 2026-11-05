package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class clsConnection {

     public static Connection getConexion() {
        String Cadena = "jdbc:sqlserver:"   /*Driver de Conexión*/
                + "//localhost:1433;"      /*Dirección IP del servidor BD y el Puerto*/
                + "databaseName=supervision_db; " /*Nombre de la Base de Datos*/
                + "user=sa; "               /*Usuario de la Base de Datos*/
                + "password=Sofia@57; "  /*Password de la Base de Datos*/
                + "encrypt=true;"
                + "trustServerCertificate=true;"
                + "loginTimeout=30";        /*Tiempo de espera para establecer conexión*/

        try {
            Connection con = DriverManager.getConnection(Cadena);
            System.out.println("Base conectada: " + con.getCatalog());
            return con;
        } catch (SQLException ex) {
            System.out.println("ERROR:" + ex.toString());
            return null;
        }
    }

}
