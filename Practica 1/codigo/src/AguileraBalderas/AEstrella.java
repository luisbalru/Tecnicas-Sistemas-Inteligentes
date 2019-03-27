package AguileraBalderas;

import AguileraBalderas.Nodo;
import tools.Vector2d;

import java.lang.*;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class AEstrella {
	private Nodo nodo_inicial;
	private Nodo nodo_objetivo;
	private PriorityQueue<Nodo> abiertos;
	private Set<Nodo> cerrados;
	private ArrayList<ArrayList<Character> > mundo;
	
	public AEstrella(Nodo start, Nodo end,ArrayList<ArrayList<Character> >mundo) {
		this.nodo_inicial = start;
		this.nodo_objetivo = end;
		this.mundo = mundo;
	}
	
	private double g(int fila, int columna) {
		return Math.abs(fila - nodo_inicial.fila) + Math.abs(columna - nodo_inicial.columna);
	}
	
	private double h(int fila, int columna) {
		return Math.abs(fila - nodo_objetivo.fila) + Math.abs(columna - nodo_objetivo.columna);
	}
	
	public ArrayList<Nodo> obtenerVecinos(Nodo n) {
		ArrayList<Nodo> vecinos = new ArrayList<Nodo>();
		vecinos.add(new Nodo(g(n.fila-1,n.columna), h(n.fila-1,n.columna), n.fila-1,n.columna, n,this.mundo.get(n.fila-1).get(n.columna)=='m'));
		vecinos.add(new Nodo(g(n.fila-1,n.columna), h(n.fila+1,n.columna), n.fila+1,n.columna, n,this.mundo.get(n.fila+1).get(n.columna)=='m'));
		vecinos.add(new Nodo(g(n.fila-1,n.columna), h(n.fila,n.columna-1), n.fila,n.columna-1, n,this.mundo.get(n.fila).get(n.columna-1)=='m'));
		vecinos.add(new Nodo(g(n.fila-1,n.columna), h(n.fila,n.columna+1), n.fila,n.columna+1, n,this.mundo.get(n.fila).get(n.columna+1)=='m'));
		return vecinos;
	}
	
	public List<Nodo> buscaCamino(){
		abiertos.add(nodo_inicial);
		while(!isEmpty(abiertos)) {
			Nodo nodo_actual = abiertos.poll();
			
		}
	}
	
	private boolean isEmpty(PriorityQueue<Nodo> openList) {
        return openList.size() == 0;
	}
}
