package AguileraBalderas;

import AguileraBalderas.Nodo;
import java.util.*;
import tools.Vector2d;

import java.lang.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
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
		this.cerrados = new HashSet<Nodo>();
		this.abiertos = new PriorityQueue<Nodo>(new Comparator<Nodo>() {
			@Override
			public int compare(Nodo nodo1, Nodo nodo2) {
				return Integer.compare(nodo1.f, nodo2.f);
			}
		});
	}
	
	private int g(int fila, int columna) {
		return Math.abs(fila - nodo_inicial.fila) + Math.abs(columna - nodo_inicial.columna);
	}
	
	private int h(int fila, int columna) {
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

	private int distanciaManhattan(Nodo n1, Nodo n2) {
		return Math.abs(n1.fila-n2.fila) + Math.abs(n1.columna-n2.columna);
	}
	
	public List<Nodo> buscaCamino(){
		List<Nodo> camino = new ArrayList<Nodo>();
		abiertos.add(nodo_inicial);
		while(!isEmpty(abiertos)) {
			Nodo nodo_actual = abiertos.poll();
			if(nodo_actual.equals(nodo_objetivo)) {
				while(!nodo_actual.equals(nodo_inicial)) {
					camino.add(nodo_actual);
					nodo_actual = nodo_actual.padre;
				}
				camino.add(nodo_actual);
				Collections.reverse(camino);
				return camino;
			}
			List<Nodo> vecinos = obtenerVecinos(nodo_actual);
			for(int i=0; i < vecinos.size(); i++) {
				int g = g(nodo_actual.fila,nodo_actual.columna) + distanciaManhattan(nodo_actual,vecinos.get(i));
				if(abiertos.contains(vecinos.get(i))) {
					if(g(vecinos.get(i).fila,vecinos.get(i).columna) <= g)
						continue;
				}
				else if(cerrados.contains(vecinos.get(i))){
					if(g(vecinos.get(i).fila,vecinos.get(i).columna) <= g)
						continue;
					cerrados.remove(vecinos.get(i));
				}
				vecinos.get(i).padre = nodo_actual;
				vecinos.get(i).coste_g = g;
				abiertos.add(vecinos.get(i));
			}
		}
		List<Nodo> camino_vacio = new ArrayList<Nodo>();
		return camino_vacio;
	}
	
	private boolean isEmpty(PriorityQueue<Nodo> openList) {
        return openList.size() == 0;
	}
}
