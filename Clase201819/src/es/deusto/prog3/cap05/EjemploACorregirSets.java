package es.deusto.prog3.cap05;

import java.util.*;

public class EjemploACorregirSets {
	static HashSet<Fantasma> hS;
	static TreeSet<Fantasma> hSS;
	public static void main(String[] args) {
		hS = new HashSet<>();
		hS.add( new Fantasma( "casper", 1, 2) );
		hS.add( new Fantasma( "opera phantom", 4, 2) );
		hS.add( new Fantasma( "casper", 1, 2) );
		hS.add( new Fantasma( "myrtle", 5, 10) );
		System.out.println( hS );
		hSS = new TreeSet<>();
		hSS.add( new Fantasma( "casper", 1, 2) );
		hSS.add( new Fantasma( "opera phantom", 4, 2) );
		hSS.add( new Fantasma( "casper", 1, 2) );
		hSS.add( new Fantasma( "myrtle", 5, 10) );
		System.out.println( hSS );
	}
	
	private static class Fantasma {
		String nombre;
		int x;
		int y;
		public Fantasma(String nombre, int x, int y) {
			super();
			this.nombre = nombre;
			this.x = x;
			this.y = y;
		}	
		@Override
		public String toString() {
			return nombre + " (" + x + "," + y + ")";
		}
	}
	
}
