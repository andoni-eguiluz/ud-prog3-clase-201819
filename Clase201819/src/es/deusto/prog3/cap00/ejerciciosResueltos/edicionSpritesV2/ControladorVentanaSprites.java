package es.deusto.prog3.cap00.ejerciciosResueltos.edicionSpritesV2;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

/** Clase controladora de la ventana de sprites
 * @author andoni.eguiluz @ ingenieria.deusto.es
 */
public class ControladorVentanaSprites {

	// Ventana asociada
	private VentanaEdicionSprites miVentana;  // Ventana controlada
	
	// Atributos para gesti�n de los gr�ficos
	JLabelGraficoAjustado lPreview = null;
	JLabelGraficoAjustado lAnim = null;
	
	// Atributos para la estructura de datos asociada a la secuencia
	int spriteActual = -1;  // N�mero de sprite seleccionado en la secuencia actualmente
	ArrayList<SpriteSec> datosSecuencia = new ArrayList<>();
	private static class SpriteSec {  // Datos asociados a cada fotograma de la secuencia
		int zoom = 100;
		int rotacion = 0;
		int offsetX = 0;
		int offsetY = 0;
		int duracionMsegs = 100;
	}
	// Nota: La otra manera de gestionar esto (m�s recomendable desde el punto de vista del dise�o)
	// ser�a hacer la JList de secuencia como una JList<SpriteSec> y definir el SpriteSec
	// para que incluya tambi�n el File (bien heredando de File, bien incorpor�ndolo como atributo)
	
	
	/** Constructor de controlador de ventana de edici�n de sprites
	 * @param vent	Ventana a controlar
	 */
	public ControladorVentanaSprites( VentanaEdicionSprites vent ) {
		miVentana = vent;
	}

		// (1) y (5) Objetos privados usados para visualizar las listas de ficheros
		private static JPanel panelSprite = initPanel();
		private static JLabelGraficoAjustado dibujitoSprite;
		private static JLabel textoSprite;
		private static JPanel initPanel() {
			JPanel ret = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			ret.setPreferredSize( new Dimension( 300, 25 ) );
			dibujitoSprite = new JLabelGraficoAjustado( "angle.png", 25, 25 );
			textoSprite = new JLabel( " " );
			ret.add( dibujitoSprite );
			ret.add( textoSprite );
			return ret;
		}
		private static ListCellRenderer<File> rendererSprite = new ListCellRenderer<File>() {
			@Override
			public Component getListCellRendererComponent(JList<? extends File> list, 
					File value, int index, boolean isSelected, boolean cellHasFocus) {
				// Cambio del contenido del panel para que se dibuje el fichero value
				if (value==null) {
					dibujitoSprite.setImagen( (URL) null );
					textoSprite.setText( "Error" );
				} else {
					try {
						dibujitoSprite.setImagen( value.toURI().toURL() );
					} catch (MalformedURLException e) {}
					textoSprite.setText( value.getName() );
				}
				// Cambio de fondo del panel si est� seleccionado
				if (isSelected) panelSprite.setBackground( Color.GRAY );
				else panelSprite.setBackground( Color.LIGHT_GRAY );
				return panelSprite;
			}
		};
		
	/** Inicializaci�n adicional de ventana (llamar a este m�todo cuando la ventana est� ya creada, antes de mostrarse)
	 */
	public void init() {
		// Mejoramos la visualizaci�n de los ficheros gr�ficos en la JList (1)
		// cambiando el renderer.
		// (una manera m�s f�cil y limitada es hacer que el tipo no sea File sino otro
		//  por ejemplo un MiFile extends File 
		//  en el que cambie el toString() - por defecto lo que saca JList es el
		//  toString del objeto en cuesti�n en un JLabel que es lo que se dibuja
		//  en cada fila de la lista)
		miVentana.lSprites.setCellRenderer( rendererSprite );
		miVentana.lSecuencia.setCellRenderer( rendererSprite );
		// Inicializamos el label de la preview
		lPreview = new JLabelGraficoAjustado( "img/angle.png", 200, 200 );  // Imagen inicial (luego se cambia)
		lPreview.setLocation( 0, 0 );
		lPreview.setBorder( BorderFactory.createLineBorder( Color.green ));  // Ponemos borde para que se vea en la preview d�nde acaba el gr�fico
		lPreview.setVisible( false );  // De momento lo ponemos no visible
		miVentana.pPreview.add( lPreview );
		// Inicializamos el label de la animaci�n
		lAnim = new JLabelGraficoAjustado( "img/angle.png", 200, 200 );  // Imagen inicial (luego se cambia)
		lAnim.setLocation( 0, 0 );
		lAnim.setVisible( false );  // De momento lo ponemos no visible
		miVentana.pArena.add( lAnim );
		// Inicializamos el hilo de animaci�n
		lanzarHilo();
	}
	
		// Atributo para guardar la �ltima carpeta seleccionada
		private File ultimaCarpeta = null;
	/** Click sobre el bot�n buscar */
	public void clickBBuscar() {
		// Saca un di�logo de b�squeda de fichero con JFileChooser
		JFileChooser f = new JFileChooser();
		if (ultimaCarpeta!=null) // Si se ha buscado antes otra carpeta, la reutiliza
			f.setCurrentDirectory(ultimaCarpeta);
		f.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
		int cod = f.showOpenDialog( miVentana );
		if (cod==JFileChooser.APPROVE_OPTION) {
			File dir = f.getSelectedFile();
			cargaFicherosGraficosOrdenados( dir );
			// Carga la lista de ficheros lSprites (1) a trav�s de su modelo
			miVentana.lCarpetaSel.setText( dir.getAbsolutePath() );
			// Vac�a la lista de secuencia
			if (enMovimiento) {
				paraAnimacion();
				try { Thread.sleep( 50 ); } catch (Exception e) {}  // Espera unas cent�simas para darle tiempo al hilo de animaci�n a parar
			}
			miVentana.mSecuencia.clear();
			datosSecuencia.clear();  // Se deben limpiar los datos asociados a la vez 
			// Memoriza esta carpeta para usarla desde el inicio la pr�xima vez
			ultimaCarpeta = dir;
		}
	}
	
		/** Carga los ficheros gr�ficos png de la carpeta indicada
		 * @param dir	Carpeta en la que buscar ficheros. Si es nula o no existe, se deja la lista de secuencia vac�a
		 */
		void cargaFicherosGraficosOrdenados(File dir) {
			miVentana.mSprites.clear();
			if (dir==null || !dir.exists()) return;
			File[] fics = dir.listFiles(); // Ficheros de la carpeta
			Arrays.sort( fics, new Comparator<File>() {  // Ordena los ficheros por nombre
				@Override
				public int compare(File o1, File o2) {
					return o1.getName().compareTo( o2.getName() );
				}
			});
			for (File f2 : fics) { // Recorre los ficheros y a�ade los pngs
				if (f2.getName().toLowerCase().endsWith("png"))  // A�adir m�s extensiones si procede
					miVentana.mSprites.addElement( f2 );
			}
			// O lo que ser�a lo mismo....
			// for (File f2 : dir.listFiles( new FilenameFilter() {
			// 	@Override
			// 	public boolean accept(File dir, String name) {
			// 		return (name.toLowerCase().endsWith("png"));  // A�adir m�s extensiones si procede
			// 	}
			// 	})) {
			// 	miVentana.mSprites.addElement( f2 );
			// }
		}

	/** Doble click en lista de sprites (1) */
	public void dobleClickListaSprites() {
		File f = miVentana.lSprites.getSelectedValue();
		if (f!=null) {
			anyadirSpriteASecuencia( f );
		}
	}
	
	/** Llamar a este m�todo cuando se quiera a�adir un sprite a la secuencia
	 * @param f
	 */
	public void anyadirSpriteASecuencia( File f ) {
		miVentana.mSecuencia.addElement( f );
		SpriteSec ss = new SpriteSec();  // datos asociados por defecto
		datosSecuencia.add( ss ); // En paralelo modelo y datos asociados
		// Y cambiar modificadores en funci�n de los datos asociados (por defecto)
		miVentana.slZoom.setValue( ss.zoom );
		miVentana.slRotacion.setValue( ss.rotacion );
		miVentana.tfoffsetX.setText( "" + ss.offsetX );
		miVentana.tfoffsetY.setText( "" + ss.offsetY );
		miVentana.tfDuracion.setText( "" + ss.duracionMsegs );
	}
	
	/** Click en bot�n de subir gr�fico en la secuencia */
	public void clickBSubir() {
		int sel = miVentana.lSecuencia.getSelectedIndex();
		if (sel>0) {  // Si hay alg�n elemento seleccionado y no es el primero
			File f = miVentana.mSecuencia.remove( sel );  // Quitamos el seleccionado
			miVentana.mSecuencia.add( sel-1, f );  // Y lo ponemos una posici�n arriba
			SpriteSec ss = datosSecuencia.remove( sel ); // En paralelo modelo y datos asociados 
			datosSecuencia.add( sel-1, ss );  
			miVentana.lSecuencia.setSelectedIndex( sel-1 );  // Lo volvemos a seleccionar ya movido
		}
	}
	
	/** Click en bot�n de bajar gr�fico en la secuencia */
	public void clickBBajar() {
		int sel = miVentana.lSecuencia.getSelectedIndex();
		if (sel>=0 && sel<miVentana.mSecuencia.size()-1) {  // Si hay alg�n elemento seleccionado y no es el �ltimo
			File f = miVentana.mSecuencia.remove( sel );  // Quitamos el seleccionado
			miVentana.mSecuencia.add( sel+1, f );  // Y lo ponemos una posici�n abajo
			SpriteSec ss = datosSecuencia.remove( sel ); // En paralelo modelo y datos asociados 
			datosSecuencia.add( sel+1, ss );  
			miVentana.lSecuencia.setSelectedIndex( sel+1 );  // Lo volvemos a seleccionar ya movido
		}
	}
	
	/** Cambio de slider
	 * @param sl	Slider que cambia
	 * @param tf	Textfield asociado a ese slider
	 * @param multiplicador	Opcional - si se indica funciona como multiplicador del valor real del slider al mostrado
	 */
	public void sliderStateChanged( JSlider sl, JTextField tf,  double...multiplicador ) {
		// Cuando cambia un slider hay que cambiar el tf asociado
		if (multiplicador.length==0)
			tf.setText( sl.getValue()+"" );
		else 
			// tf.setText( (sl.getValue() * multiplicador[0])+"" ); // en vez de sacarlo por defecto...
			tf.setText( String.format( "%.1f", sl.getValue() * multiplicador[0]) ); // ... mejor maquetar con un decimal - si no pueden salir muchos .0000001, .999999 y cosas as�
	}

	/** Cambio de texto
	 * @param sl	Slider asociado a ese textfield
	 * @param tf	Textfield que cambia
	 * @param min	Valor m�nimo
	 * @param max	Valor m�ximo
	 * @param multiplicador	Opcional - si se indica funciona como multiplicador del valor real del slider al mostrado
	 */
	public void textFieldFocusLost( JSlider sl, JTextField tf, int min, int max, double...multiplicador ) {
		// Cuando cambia un textfield hay que comprobar el valor...
		if (multiplicador.length==0) {
			try {
				int val = Integer.parseInt( tf.getText() );
				if (val<min || val>max) throw new NullPointerException(); // Error por fuera de rango
				sl.setValue( val ); // Si es correcto se pone el valor en el slider
			} catch (Exception e) {  
				// Si no es un valor correcto (no entero o fuera de rango), se anula con feedback
				JOptionPane.showMessageDialog( miVentana, "Valor incorrecto - debe estar entre " + min + " y " + max, "Error en valor", JOptionPane.ERROR_MESSAGE );
				tf.setText( "" + min ); // Lo ponemos a valor por defecto (m�nimo v�lido)
				tf.requestFocus(); // Y volvemos a forzar el foco en ese cuadro
			}
		} else {
			try {
				double val = Double.parseDouble( tf.getText() );
				if (val<min*multiplicador[0] || val>max*multiplicador[0]) throw new NullPointerException(); // Error por fuera de rango
				sl.setValue( (int) Math.round(val/multiplicador[0]) ); // Si es correcto se pone el valor en el slider
			} catch (Exception e) {  
				// Si no es un valor correcto (no entero o fuera de rango), se anula con feedback
				JOptionPane.showMessageDialog( miVentana, "Valor incorrecto - debe estar entre " + (min*multiplicador[0]) + " y " + (max*multiplicador[0]), "Error en valor", JOptionPane.ERROR_MESSAGE );
				tf.setText( "" + min ); // Lo ponemos a valor por defecto (m�nimo v�lido)
				tf.requestFocus(); // Y volvemos a forzar el foco en ese cuadro
			}
		}
	}
	
	/** Cambio en la selecci�n de lista de secuencia */
	public void lSecuenciaSelectionChanged() {
		spriteActual = miVentana.lSecuencia.getSelectedIndex();  // Marcar el sprite seleccionado actualmente
		File fSel = miVentana.lSecuencia.getSelectedValue();
		if (fSel!=null) {  // Si hay un elementoseleccionado, mostrarlo en el gr�fico
			// Cambiar modificadores en funci�n de los datos asociados
			if (spriteActual>=0) {
				SpriteSec ss = datosSecuencia.get( spriteActual );
				miVentana.slZoom.setValue( ss.zoom );
				miVentana.slRotacion.setValue( ss.rotacion );
				miVentana.tfoffsetX.setText( "" + ss.offsetX );
				miVentana.tfoffsetY.setText( "" + ss.offsetY );
				miVentana.tfDuracion.setText( "" + ss.duracionMsegs );
			}
			try {  // Poner la imagen en el label de preview
				lPreview.setImagen( fSel.toURI().toURL() );
			} catch (MalformedURLException e) {}
			colocarPreview(); // Colocarlo en su tama�o y sitio  (lo hacemos con un m�todo porque lo necesitamos en varios puntos)
			lPreview.setVisible( true ); // Hacerlo visible
		} else {  // Si no lo hay, quitamos el gr�fico
			lPreview.setVisible( false );
		}
	}
	
		// Colocar el sprite de preview en su tama�o y lugar
		private void colocarPreview() {
			int anchura = 200;
			int altura = 200;
			try {
				anchura = Integer.parseInt( miVentana.tfAncho.getText() );
				altura = Integer.parseInt( miVentana.tfAlto.getText() );
			} catch (Exception e) {}
			int offsetX = 0;
			int offsetY = 0;
			try {
				offsetX = Integer.parseInt( miVentana.tfoffsetX.getText() );
				offsetY = Integer.parseInt( miVentana.tfoffsetY.getText() );
			} catch (NumberFormatException e) {}
			lPreview.setSize( anchura, altura ); // Actualizar el tama�o
			lPreview.setLocation( offsetX + (miVentana.pPreview.getWidth() - anchura)/2,
				offsetY + (miVentana.pPreview.getHeight() - altura)/2 ); // Posicionarlo (con respecto al centro del panel)
		}
		
	/** Redimensi�n del panel de preview */
	public void pPreviewResized() {
		colocarPreview();
	}
	
	/** P�rdida de foco de cualquier textfield de tama�o */
	public void tfTamanyoFocusLost( ) {
		// Comprobar valor de tama�o
		try {
			int valAncho = Integer.parseInt( miVentana.tfAncho.getText() );
			int valAlto = Integer.parseInt( miVentana.tfAlto.getText() );
			if (valAncho<0 || valAlto<0) throw new NullPointerException(); // Error por fuera de rango
			// Recolocar
			colocarPreview();
		} catch (Exception e) {  
			// Si no es un valor correcto (no entero o fuera de rango), se da feedback y se reinician
			miVentana.tfAncho.setText( "200" );
			miVentana.tfAlto.setText( "200" );
			JOptionPane.showMessageDialog( miVentana, "Valor de tama�o incorrecto - debe ser positivo", "Error", JOptionPane.ERROR_MESSAGE );
		}
	}
	
	/** Cambio de slider de zoom */
	public void slZoomStateChanged() {
		lPreview.setZoom( miVentana.slZoom.getValue()/100.0 ); // A porcentaje
		// Modificaci�n del dato asociado
		if (spriteActual>=0) {
			SpriteSec ss = datosSecuencia.get( spriteActual );
			ss.zoom = miVentana.slZoom.getValue();
		}
	}
	
	/** Cambio de slider de rotaci�n */
	public void slRotacionStateChanged() {
		lPreview.setRotacion( miVentana.slRotacion.getValue()/180.0*Math.PI );  // Grados a radianes
		// Modificaci�n del dato asociado
		if (spriteActual>=0) {
			SpriteSec ss = datosSecuencia.get( spriteActual );
			ss.rotacion = miVentana.slRotacion.getValue();
		}
	}
	
	/** Cambio de cuadro de texto de offset X */
	public void tfOffsetXFocusLost() {
		int ox = 0;
		try {
			ox = Integer.parseInt( miVentana.tfoffsetX.getText() );
		} catch (NumberFormatException e) {
			// Si no es un valor correcto, se anula con feedback
			JOptionPane.showMessageDialog( miVentana, "Valor incorrecto - debe ser entero", "Error en valor", JOptionPane.ERROR_MESSAGE );
			miVentana.tfoffsetX.setText( "0" );
			miVentana.tfoffsetX.requestFocus();
		}
		colocarPreview();
		// Modificaci�n del dato asociado
		if (spriteActual>=0) {
			SpriteSec ss = datosSecuencia.get( spriteActual );
			ss.offsetX = ox;
		}
	}
	
	/** Cambio de cuadro de texto de offset Y */
	public void tfOffsetYFocusLost() {
		int oy = 0;
		try {
			oy = Integer.parseInt( miVentana.tfoffsetY.getText() );
		} catch (NumberFormatException e) {
			// Si no es un valor correcto, se anula con feedback
			JOptionPane.showMessageDialog( miVentana, "Valor incorrecto - debe ser entero", "Error en valor", JOptionPane.ERROR_MESSAGE );
			miVentana.tfoffsetY.setText( "0" );
			miVentana.tfoffsetY.requestFocus();
		}
		colocarPreview();
		// Modificaci�n del dato asociado
		if (spriteActual>=0) {
			SpriteSec ss = datosSecuencia.get( spriteActual );
			ss.offsetY = oy;
		}
	}
	
	/** Cambio de cuadro de texto de duraci�n */
	public void tfDuracionFocusLost() {
		int dur = 100;
		try {
			dur = Integer.parseInt( miVentana.tfDuracion.getText() );
			if (dur<1) {
				dur = 100;
				throw new NullPointerException();  // Fuerza error
			}
		} catch (Exception e) {
			// Si no es un valor correcto, se anula con feedback
			JOptionPane.showMessageDialog( miVentana, "Valor incorrecto - debe ser positivo", "Error en valor", JOptionPane.ERROR_MESSAGE );
			miVentana.tfDuracion.setText( "100" );
			miVentana.tfDuracion.requestFocus();
		}
		// Modificaci�n del dato asociado
		if (spriteActual>=0) {
			SpriteSec ss = datosSecuencia.get( spriteActual );
			ss.duracionMsegs = dur;
		}
	}
	
	/** Click en bot�n de play secuencia */
	public void clickBPlaySecuencia() {
		enMovimiento = true;
		enSecuencia = true;
		enAnim = false;
		iniciarAnimacion();
	}
	
	/** Click en bot�n de play anim */
	public void clickBPlayAnim() {
		enMovimiento = true;
		enSecuencia = false;
		enAnim = true;
		iniciarAnimacion();
	}
	
	/** Click en bot�n de play anim+secuencia */
	public void clickBPlayAnimSecuencia() {
		enMovimiento = true;
		enSecuencia = true;
		enAnim = true;
		iniciarAnimacion();
	}

	// Atributos, hilo y m�todos de control de animaci�n
	private boolean enMovimiento = false;   // Animaci�n en marcha
	private boolean enSecuencia = false;    // Cuando hay que animar la secuencia
	private boolean enAnim = false;         // Cuando hay que animar el movimiento
	private int fotogramaSecuencia = -1;    // Fotograma en curso de la secuencia
	private long milisFotogramaActual = -1; // Duraci�n de fotograma actual
	private double posX = 0;
	private double posY = 0;
	private double vel = 0.0;
	private double angVel = 0.0;
	private double gravedad = 0.0;
	private double rot = 0.0;
	private double zoom = 1.0;
	private double rotPorFotograma = 0.0;
	private double zoomPorFotograma = 1.0;
	private final long MILIS_POR_FRAME = 20; // Unas 50 veces por segundo (M�ximo! Luego medimos para ver los fps -frames por segundo- reales)
		// medici�n de fps (por curiosidad de ver cu�l es el rendimiento de nuestro sistema)
		int numFrames = 0;
	
	private void lanzarHilo() {
		Thread t = new Thread( new Runnable() {
			long initTime = System.currentTimeMillis();
			public void run() {
				long lastTime = System.currentTimeMillis();
				while( true ) {  // Lo ponemos forever y lo definiremos demon para que se pare solo
					numFrames++;
					try { Thread.sleep( MILIS_POR_FRAME ); } catch (Exception e) {}  
					long tiempoReal = System.currentTimeMillis() - lastTime;  // Tiempo real transcurrido entre ejecuci�n y ejecuci�n del bucle
						// En el algoritmo usamos tiempoReal en lugar de MILIS_POR_FRAME. Ser�n muy parecidos pero seg�n la complejidad del c�digo
						// ocurrir� que entre bucle y bucle a veces pasen m�s milisegundos de los esperados. Es m�s preciso siempre usar el clock
						double fps = numFrames * 1000.0 / (lastTime-initTime);
						if (tiempoReal!=MILIS_POR_FRAME) miVentana.setTitle( "Edici�n de sprites - Prog. III - " + String.format( "%.1f" , fps) + " FPS (P�rdida de " + (tiempoReal-MILIS_POR_FRAME) + " msg.)" ); // Mostramos cu�ntos milis se pierden y c�mo afecta eso a los frames por segundo
					lastTime = System.currentTimeMillis();
					if (enMovimiento && fotogramaSecuencia!=-1) {
						if (enAnim) {
							double vx = vel * Math.cos(angVel);
							double vy = vel * Math.sin(angVel);
							posX += (vx/1000.0*tiempoReal);  // Sx = Sx + Vx*t 
							posY += (vy/1000.0*tiempoReal);  // Sy = Sy + Vy*t 
							vy += (gravedad/1000.0*tiempoReal);  // Vy = Vy + G*t, Vx = Vx (no hay rozamiento)
							vel = Math.sqrt( vx*vx + vy*vy );   // V = hipotenusa
							angVel = Math.atan2( vy, vx );      // ang = arco tangente (atan2 ya hace el c�lculo de cuadrante)
							zoom *= zoomPorFotograma;
							rot += rotPorFotograma;
							if (posX > miVentana.pArena.getWidth() || posY > miVentana.pArena.getHeight()) {  // Cuando se sale por abajo o por la derecha se para el ciclo de animaci�n
								if (miVentana.cbCicloAnim.isSelected()) {
									iniciarAnimacion();
								} else if (miVentana.cbRetornoAnim.isSelected()) {
									enAnim = false;
									iniciarAnimacion();
								} else {
									enAnim = false;
								}
							}
						}
						if (enSecuencia) {
							milisFotogramaActual -= tiempoReal;  // Decrementa milis de la secuencia
							if (milisFotogramaActual<=0) {  // Pasa al siguiente fotograma
								fotogramaSecuencia++;
								if (fotogramaSecuencia>=datosSecuencia.size()) {
									if (miVentana.cbCiclo.isSelected()) // Si hay ciclo vuelve a empezar
										fotogramaSecuencia = 0;
									else  // Si no se queda en la �ltima
										fotogramaSecuencia = datosSecuencia.size()-1;
								}
								milisFotogramaActual = datosSecuencia.get( fotogramaSecuencia ).duracionMsegs;
								ponFotograma();
							}
						}
						mueveImagen();
					}
				}
			}
		} );
		t.setDaemon( true );  // Se pone a daemon para que la JVM se acabe sola cuando no haya m�s hilos principales
		t.start();
	}
	
	/** Inicia la animaci�n en la arena (si no hay ning�n gr�fico no hace nada)
	 */
	public void iniciarAnimacion() {
		if (datosSecuencia.isEmpty()) return;  // Si no hay secuencia de animaci�n no hay nada que animar (ni un solo gr�fico)
		if (enSecuencia)
			fotogramaSecuencia = 0;
		else
			fotogramaSecuencia = miVentana.lSecuencia.getSelectedIndex();
		if (fotogramaSecuencia==-1) return; // Si no hay animaci�n de secuencia y no hay fotograma seleccionado no hay nada que animar
		ponFotograma();
		lAnim.setVisible( true );  // Visualiza el gr�fico
		milisFotogramaActual = datosSecuencia.get( fotogramaSecuencia ).duracionMsegs;
		posX = Integer.parseInt( miVentana.tfOrigenX.getText() );
		posY = Integer.parseInt( miVentana.tfOrigenY.getText() );
		vel = Integer.parseInt( miVentana.tfVelocidad.getText() );
		angVel = -Integer.parseInt( miVentana.tfAngulo.getText() )/180.0*Math.PI;  // Cambiado de signo porque en pantalla la Y baja en vez de subir
		gravedad = Double.parseDouble( miVentana.tfGravedad.getText() );
		rotPorFotograma = Integer.parseInt( miVentana.tfRotacionAnim.getText() )/180.0*Math.PI/1000.0*MILIS_POR_FRAME;
		zoomPorFotograma =
			Math.pow( Integer.parseInt( miVentana.tfZoomAnim.getText() )/100.0,
					1.0/(1000.0/MILIS_POR_FRAME) );
		rot = 0;
		zoom = 1.0;
	}
	
	private void ponFotograma() {
		if (fotogramaSecuencia<0 || fotogramaSecuencia>=datosSecuencia.size()) return;
		try {
			lAnim.setImagen( miVentana.mSecuencia.getElementAt( fotogramaSecuencia ).toURI().toURL() );
		} catch (MalformedURLException e) {}
		lAnim.setZoom( datosSecuencia.get(fotogramaSecuencia).zoom/100.0 );
		lAnim.setRotacion( datosSecuencia.get(fotogramaSecuencia).zoom/180.0*Math.PI );
	}
	
	private void mueveImagen() {
		int x = (int) Math.round( posX );
		int y = (int) Math.round( posY );
		double zoom2 = zoom * datosSecuencia.get( fotogramaSecuencia ).zoom / 100.0;
		double rot2 = rot + datosSecuencia.get( fotogramaSecuencia ).rotacion / 180.0 * Math.PI;
		x += datosSecuencia.get( fotogramaSecuencia ).offsetX;
		y += datosSecuencia.get( fotogramaSecuencia ).offsetY;
		lAnim.setLocation( x, y );
		lAnim.setZoom( zoom2 );
		lAnim.setRotacion( rot2 );
	}
	
	private void paraAnimacion() {
		enAnim = false;
		enSecuencia = false;
		enMovimiento = false;
	}
	
}
