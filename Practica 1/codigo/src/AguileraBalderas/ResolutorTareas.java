package AguileraBalderas;

import java.util.ArrayList;

import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import AguileraBalderas.AEstrella;
import core.game.Observation;

public class ResolutorTareas {
	
	private ArrayList<Observation>[][] mundo;
	int ancho, alto;
	
	public ResolutorTareas(ArrayList<Observation>[][] mundo, int ancho, int alto) {
		this.mundo = mundo;
		this.ancho = ancho;
		this.alto = alto;
	}
	
	private int distanciaManhattan(int fila1, int col1, int fila2, int col2) {
		return Math.abs(fila1-fila2) + Math.abs(col1 - col2);
	}
	
	public ArrayList<Types.ACTIONS> obtenCamino(int fila_actual,int col_actual,int fila_obj, int col_obj, ElapsedCpuTimer timer){
		Nodo inicio = new Nodo(0, distanciaManhattan(fila_actual, col_actual, fila_obj, col_obj), fila_actual, col_actual, null);
		Nodo fin = new Nodo(distanciaManhattan(fila_actual, col_actual, fila_obj, col_obj), 0, fila_obj, col_obj, null);
		AEstrella aestrella = new AEstrella(inicio, fin, mundo);
		aestrella.buscaCamino(timer);
		return aestrella.devuelveAcciones();
	}
	
	public ArrayList<Types.ACTIONS> salirPortal(int fila, int columna, ElapsedCpuTimer timer){
		int fila_portal = 0;
		int columna_portal = 0;
		for(int i = 0; i < ancho; ++i) {
			for(int j = 0; j < alto; ++j) {
				boolean puerta = mundo[i][j].size()>0;
				if(puerta)
					puerta = mundo[i][j].get(0).itype==5; 
				if(puerta) {
					fila_portal = i;
					columna_portal = j;
				}
			}
		}
		ArrayList<Types.ACTIONS> acciones = obtenCamino(fila, columna, fila_portal, columna_portal, timer);
		acciones.add(Types.ACTIONS.ACTION_ESCAPE);
		return(acciones);
	}

}