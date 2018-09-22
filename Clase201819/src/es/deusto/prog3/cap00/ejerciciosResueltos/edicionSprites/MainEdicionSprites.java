package es.deusto.prog3.cap00.ejerciciosResueltos.edicionSprites;

import javax.swing.UIManager;  // Para usar look and feels distintos al estándar
import javax.swing.UIManager.*;

/** Clase principal de edición de sprites
 * @author andoni.eguiluz @ ingenieria.deusto.es
 */
public class MainEdicionSprites {

	/** Método principal, crea una ventana de edición y la lanza 
	 * @param args
	 */
	public static void main(String[] args) {
		try { // Cambiamos el look and feel (se tiene que hacer antes de crear la GUI
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) { } // Si Nimbus no está disponible, se usa el l&f por defecto
		VentanaEdicionSprites v = new VentanaEdicionSprites();
		v.setVisible( true );
	}

}
