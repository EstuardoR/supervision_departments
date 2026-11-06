package view;

import dao.UsuarioDAO;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import util.Sesion;

/**
 * Clase temporal para ver las pantallas sin pasar por el login ni el menú.
 * Clic derecho > Run File. Bórrala antes del commit final.
 */
public class PruebaPantallas {

    // 1000000060606 = JEFE_OFICINA (para FrmAcciones)
    // 1000000040404 = SUPERVISOR_DCI (para FrmMatrizSeguimiento)
    private static final String CUI = "1000000060606";

    public static void main(String[] args) throws Exception {
        Sesion.getInstancia().setUsuarioActual(new UsuarioDAO().buscarPorCui(CUI));

        JDesktopPane escritorio = new JDesktopPane();
        JFrame ventana = new JFrame("Prueba de pantallas");
        ventana.setContentPane(escritorio);
        ventana.setSize(1100, 700);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setVisible(true);

        // Quita el // de la pantalla que quieras ver (solo una a la vez)
        FrmAcciones pantalla = new FrmAcciones();
        // FrmMatrizSeguimiento pantalla = new FrmMatrizSeguimiento();

        escritorio.add(pantalla);
        pantalla.setVisible(true);
    }
}