package AguileraBalderas;

import tools.Vector2d;

public class Nodo {
	public int coste_g;
	public int estimacion_h;
	public Vector2d posicion;
	public Vector2d padre;
	
	public Nodo(int coste,int heuristica,Vector2d pos,Vector2d nodo_padre) {
		coste_g = coste;
		estimacion_h = heuristica;
		posicion = pos;
		padre = nodo_padre;
	}
	
	
	
}
