package AguileraBalderas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import core.game.Observation;

import ontology.Types;
import tools.ElapsedCpuTimer;

public class AEstrella {
	private Nodo nodo_inicial;
	private Nodo nodo_objetivo;
	private PriorityQueue<Nodo> abiertos;
	private Set<Nodo> abiertos_set;
	private Set<Nodo> cerrados;
	private ArrayList<Observation>[][] mundo;
	private List<Nodo> camino;
	
	public AEstrella(Nodo start, Nodo end,ArrayList<Observation>[][] mundo) {
		this.nodo_inicial = start;
		this.nodo_objetivo = end;
		this.mundo = mundo;
		this.cerrados = new HashSet<Nodo>();
		this.abiertos_set = new HashSet<Nodo>();
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
		vecinos.add(new Nodo(g(n.fila-1,n.columna), h(n.fila-1,n.columna), n.fila-1,n.columna, n));
		vecinos.add(new Nodo(g(n.fila+1,n.columna), h(n.fila+1,n.columna), n.fila+1,n.columna, n));
		vecinos.add(new Nodo(g(n.fila,n.columna-1), h(n.fila,n.columna-1), n.fila,n.columna-1, n));
		vecinos.add(new Nodo(g(n.fila,n.columna+1), h(n.fila,n.columna+1), n.fila,n.columna+1, n));
		return vecinos;
	}

	public int distanciaManhattan(Nodo n1, Nodo n2) {
		return Math.abs(n1.fila-n2.fila) + Math.abs(n1.columna-n2.columna);
	}
	
	public List<Nodo> buscaCamino(ElapsedCpuTimer timer){
		List<Nodo> path = new ArrayList<Nodo>();
		abiertos.add(nodo_inicial);
		abiertos_set.add(nodo_inicial);
		Nodo mejor_nodo = nodo_inicial;
		while(!isEmpty(abiertos) && timer.elapsedMillis() < 35) {
			long tiempo = timer.elapsedMillis();
			Nodo nodo_actual = abiertos.poll();
			if(nodo_actual.f < mejor_nodo.f) {
				mejor_nodo = nodo_actual;
			}
			abiertos_set.remove(nodo_actual);
			if(nodo_actual.equals(nodo_objetivo)) {
				while(!nodo_actual.equals(nodo_inicial)) {
					path.add(nodo_actual);
					nodo_actual = nodo_actual.padre;
				}
				path.add(nodo_actual);
				Collections.reverse(path);
				camino = path;
				return path;
			}
			List<Nodo> vecinos = obtenerVecinos(nodo_actual);
			for(int i=0; i < vecinos.size(); i++) {
				int g = g(nodo_actual.fila,nodo_actual.columna) + distanciaManhattan(nodo_actual,vecinos.get(i));
				if(abiertos_set.contains(vecinos.get(i))) {
					if(g(vecinos.get(i).fila,vecinos.get(i).columna) <= g)
						continue;
				}
				else if(cerrados.contains(vecinos.get(i))){
					if(g(vecinos.get(i).fila,vecinos.get(i).columna) <= g)
						continue;
					cerrados.remove(vecinos.get(i));
				}
				boolean accesible = isAccesible(mundo,vecinos.get(i));
				if(accesible) {
					vecinos.get(i).padre = nodo_actual;
					vecinos.get(i).coste_g = g;
					abiertos.add(vecinos.get(i));
					abiertos_set.add(vecinos.get(i));
				}
			}
		}
		while(!mejor_nodo.equals(nodo_inicial)) {
			path.add(mejor_nodo);
			mejor_nodo = mejor_nodo.padre;
		}
		path.add(mejor_nodo);
		Collections.reverse(path);
		camino = path;
		return path;
	}
	
	private boolean isAccesible(ArrayList<Observation>[][] mundo, Nodo nodo) {
		int fila = nodo.fila;
		int columna = nodo.columna;
		boolean vacio = mundo[fila][columna].size()==0;
		if(!vacio) {
			boolean bicho = mundo[fila][columna].get(0).itype==11 || mundo[fila][columna].get(0).itype==10;
			boolean muro = mundo[fila][columna].get(0).itype==0;
			boolean piedra = mundo[fila][columna].get(0).itype==7;
			boolean piedra_arriba = false;
			if(fila>0 && mundo[fila-1][columna].size()>0)
				piedra_arriba = mundo[fila-1][columna].get(0).itype==7;
			boolean condicion = !bicho && !muro && !piedra && !piedra_arriba;
			return condicion;
		}
		return true;
	}

	private boolean isEmpty(PriorityQueue<Nodo> openList) {
        return openList.size() == 0;
	}
	
	public ArrayList<Types.ACTIONS> devuelveAcciones(){
		Nodo nodo_actual = nodo_inicial;
		System.out.println(this.camino.toString());
		ArrayList<Types.ACTIONS> acciones = new ArrayList<Types.ACTIONS>();
		for(int i=1; i < camino.size(); i++) {
			//System.out.printf("Nodo actual: Fila %d, Columna %d\n",nodo_actual.fila, nodo_actual.columna);
			//System.out.printf("Nodo del camino: Fila %d, Columna %d\n\n",camino.get(i).fila, camino.get(i).columna);
			if(camino.get(i).fila > nodo_actual.fila) 
				acciones.add(Types.ACTIONS.ACTION_DOWN);
			
			else if(camino.get(i).fila < nodo_actual.fila) 
				acciones.add(Types.ACTIONS.ACTION_UP);
			
			else if(camino.get(i).columna > nodo_actual.columna) 
				acciones.add(Types.ACTIONS.ACTION_RIGHT);
			
			else if(camino.get(i).columna < nodo_actual.columna)
				acciones.add(Types.ACTIONS.ACTION_LEFT);
			
			else
				acciones.add(Types.ACTIONS.ACTION_NIL);
			nodo_actual = camino.get(i);
		}
		return acciones;
	}
}
