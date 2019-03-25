package AguileraBalderas;

import AguileraBalderas.Nodo;
import tools.Vector2d;

import java.lang.*;
import java.util.ArrayList;
import java.util.List;

public class AEstrella {
	private Nodo nodo_inicial;
	private Nodo nodo_objetivo;
	
	private double g(Vector2d n) {
		return Math.abs(n.x - nodo_inicial.posicion.x) + Math.abs(n.y - nodo_inicial.posicion.y);
	}
	
	private double h(Vector2d n) {
		return Math.abs(n.x - nodo_objetivo.posicion.x) + Math.abs(n.y - nodo_objetivo.posicion.y);
	}
	
	public List<Nodo> obtenerVecinos(Nodo n) {
		List<Nodo> vecinos = new ArrayList<Nodo>();
		vecinos.add(new Nodo(g(new Vector2d(n.posicion.x-1,n.posicion.y)), h(new Vector2d(n.posicion.x-1,n.posicion.y)), new Vector2d(n.posicion.x-1,n.posicion.y), n));
		vecinos.add(new Nodo(g(new Vector2d(n.posicion.x+1,n.posicion.y)), h(new Vector2d(n.posicion.x+1,n.posicion.y)), new Vector2d(n.posicion.x+1,n.posicion.y), n));
		vecinos.add(new Nodo(g(new Vector2d(n.posicion.x,n.posicion.y-1)), h(new Vector2d(n.posicion.x,n.posicion.y-1)), new Vector2d(n.posicion.x,n.posicion.y-1), n));
		vecinos.add(new Nodo(g(new Vector2d(n.posicion.x,n.posicion.y+1)), h(new Vector2d(n.posicion.x,n.posicion.y+1)), new Vector2d(n.posicion.x,n.posicion.y+1), n));
		return vecinos;
	}
}
