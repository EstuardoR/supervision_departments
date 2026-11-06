/*
 * Autor: Joan Estuardo Rodas
 * Carné: 94902514130
 */




package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class clsConnection {

    // CAMBIAR EL USER Y PASSWORD POR LAS CREDENCIALES QUE TENGAS CONFIGURADAS EN TU LOCAL
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=supervision_db";
    private static final String USER = "sa";
    private static final String PASSWORD = "Bruno2701";

    public static Connection ConnectionDB() {
        Connection con = null;
        try {
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexión establecida con exito :)!!");
        } catch (SQLException e) {
            System.out.println("Ocurrio un error al establecer conexión" + e.getMessage());
        }
        return con;

    }

}
