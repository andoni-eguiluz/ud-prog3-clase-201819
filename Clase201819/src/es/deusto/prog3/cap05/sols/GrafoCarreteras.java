package es.deusto.prog3.cap05.sols;

import java.util.*;

public class GrafoCarreteras {

	/* Versión 1 - correcta pero mala para buscar carrs
	private HashSet<String> ciudades;
	private ArrayList<Carretera> carreteras; */
	private HashMap<String,ArrayList<Carretera>> carreteras;
	
	public ArrayList<Carretera> getCarrs( String ciudad ) {
		return carreteras.get( ciudad );
	}
	
	public GrafoCarreteras() {
		carreteras = new HashMap<>();
	}

	/** Añade una carretera al grafo. La carretera NO debe estar antes
	 * @param ciudad1
	 * @param ciudad2
	 * @param distancia
	 */
	public void anyadirCarretera( String ciudad1, String ciudad2, int distancia ) {
		if (!carreteras.containsKey( ciudad1 ))
			carreteras.put( ciudad1, new ArrayList<>());
		carreteras.get( ciudad1 ).add( new Carretera( ciudad1, ciudad2, distancia ));
		if (!carreteras.containsKey( ciudad2 ))
			carreteras.put( ciudad2, new ArrayList<>());
		carreteras.get( ciudad2 ).add( new Carretera( ciudad2, ciudad1, distancia ));
	}
	
	public int getDistanciaMinima( String ciudad1, String ciudad2 ) {
		return getDMRec( ciudad1, ciudad2, ciudad1, 0 );
	}
	
		public int getDMRec( String c1, String c2, String camino, int distAcum ) {
			System.out.println( camino + " dist " + distAcum );
			if (c1.equals(c2)) {
				System.out.println( " ! " + camino + " - distancia " + distAcum );
				return 0;
			} else {
				int menor = Integer.MAX_VALUE;
				for (Carretera c : carreteras.get(c1)) {
					if (!camino.contains(c.ciudad2)) {
						int dist = getDMRec( c.ciudad2, c2, camino + "#" + c.ciudad2, distAcum+c.distancia );
						if (dist<Integer.MAX_VALUE) dist = dist + c.distancia;
						if (dist<menor) menor = dist;
					}
				}
				return menor;
			}
		}
	
	public class Carretera {
		private String ciudad1; // Se podría quitar si usamos un mapa
		private String ciudad2;
		private int distancia; // en km
		public Carretera(String ciudad1, String ciudad2, int distancia) {
			super();
			this.ciudad1 = ciudad1;
			this.ciudad2 = ciudad2;
			this.distancia = distancia;
		}
		public String getCiudad1() {
			return ciudad1;
		}
		public void setCiudad1(String ciudad1) {
			this.ciudad1 = ciudad1;
		}
		public String getCiudad2() {
			return ciudad2;
		}
		public void setCiudad2(String ciudad2) {
			this.ciudad2 = ciudad2;
		}
		public int getDistancia() {
			return distancia;
		}
		public void setDistancia(int distancia) {
			this.distancia = distancia;
		}
	}
	
	public static void main(String[] args) {
		GrafoCarreteras grafoEjemplo = new GrafoCarreteras();
		grafoEjemplo.anyadirCarretera( "Bilbao", "Vitoria", 61 );
		grafoEjemplo.anyadirCarretera( "Bilbao", "Donostia", 119  );
		grafoEjemplo.anyadirCarretera( "Bilbao", "Santander", 108  );
		grafoEjemplo.anyadirCarretera( "Vitoria", "Donostia", 101 );
		grafoEjemplo.anyadirCarretera( "Donostia", "Pamplona", 82 );
		grafoEjemplo.anyadirCarretera( "Pamplona", "Vitoria", 95 );
		grafoEjemplo.anyadirCarretera( "Pamplona", "Logroño", 80 );
		grafoEjemplo.anyadirCarretera( "Vitoria", "Burgos", 114 );
		grafoEjemplo.anyadirCarretera( "Burgos", "Palencia", 93 );
		grafoEjemplo.anyadirCarretera( "Burgos", "Valladolid", 117 );
		grafoEjemplo.anyadirCarretera( "Burgos", "Soria", 141 );
		grafoEjemplo.anyadirCarretera( "Logroño", "Soria", 100 );
		grafoEjemplo.anyadirCarretera( "Logroño", "Zaragoza", 172 );
		grafoEjemplo.anyadirCarretera( "Zaragoza", "Huesca", 72 );
		System.out.println( 
			grafoEjemplo.getDistanciaMinima( "Bilbao", "Vitoria" ));
	}

}
