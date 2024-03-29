package AguileraBalderas;

import tools.Vector2d;
import java.lang.Double;

public class Nodo {
	// Coste asociado al nodo
	public int coste_g;
	// Valor de la heurística en el nodo
	public int estimacion_h;
	// Suma del coste y la heurística
	public int f;
	// Posición en las filas
	public int fila;
	// Posición en las columnas
	public int columna;
	// Padre del nodo 
	public Nodo padre;
	// Orientación (-1,0)IZQ, (1,0)DER, (0,1)ABJ, (0,-1)ARRI
	public Vector2d orientacion;
	
	/**
	 * Constructor del nodo
	 * @param d Distancia o coste del nodo
	 * @param e Estimación o valor de la heurística
	 * @param columna Valor de la columna 
	 * @param fila Valor de la fila
	 * @param n Nodo padre
	 * @param orientacion Orientación del nodo (esto está asociado a un camino y sólo se usa en AEstrella)
	 */
	public Nodo(int d,int e,int columna,int fila,Nodo n, Vector2d orientacion) {
		coste_g = d;
		estimacion_h = e;
		this.fila = fila;
		this.columna = columna;
		padre = n;
		f = coste_g + estimacion_h;
		this.orientacion = orientacion;
	}
	
	@Override
    public boolean equals(Object arg0) {
        Nodo nodo = (Nodo) arg0;
        return this.fila == nodo.fila && this.columna == nodo.columna;
	}
	
	@Override
	public String toString() {
		return "Fila: " + this.fila + ", Columna: " + this.columna + ", f: " + this.f + ", g: " + this.coste_g + ", h: " + this.estimacion_h + ", padre: " + this.padre;
	}
	
	@Override
	public int hashCode() {
		return Double.valueOf((this.columna/Math.exp(this.fila))).hashCode();
	}
	
}
