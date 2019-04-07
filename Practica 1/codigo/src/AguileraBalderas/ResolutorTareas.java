package AguileraBalderas;

import java.util.ArrayList;

import java.util.List;

import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import AguileraBalderas.AEstrella;
import core.game.Observation;
import core.game.StateObservation;

public class ResolutorTareas {
	
	// Grid del mundo
	private ArrayList<Observation>[][] mundo;
	// Dimensiones del grid del mundo
	int ancho, alto;
	// Estado del mundo en el tick actual
	StateObservation obs;
	// Factores de escala para las coordenadas
	double fescalaX, fescalaY;
	// Variable para encontrar el mejor camino inteligente
	public int cantidad_pasos;
	
	AEstrella aestrella;
	
	/**
	 * Constructor de ResolutorTareas
	 * @param mundo Grid del mundo obtenido mediante un objeto de tipo {@link StateObservation}
	 * @param ancho Tamaño en ancho del grid
	 * @param alto Tamaño en alto del grid
	 * @param obs Objeto de tipo {@link StateObservation} que nos da el estado actual del mundo
	 * @param fescalaX Factor de escala para las columnas
	 * @param fescalaY Factor de escala para las filas
	 */
	public ResolutorTareas(ArrayList<Observation>[][] mundo, int ancho, int alto, StateObservation obs, double fescalaX, double fescalaY) {
		this.mundo = mundo;
		this.ancho = ancho;
		this.alto = alto;
		this.obs = obs;
		this.fescalaX = fescalaX;
		this.fescalaY = fescalaY;
		this.aestrella=new AEstrella(ancho, alto);
		this.cantidad_pasos = 0;
	}
	
	/**
	 * Función que implementa la distancia Manhattan entre dos puntos
	 * @param fila1 Fila del primer punto del mapa
	 * @param col1 Columna del primer punto del mapa
	 * @param fila2 Fila del segundo punto del mapa
	 * @param col2 Columna del segundo punto del mapa
	 * @return Devuelve un entero que representa la diferencia en valor absoluto entre las filas y columnas sumadas.
	 */
	private int distanciaManhattan(int fila1, int col1, int fila2, int col2) {
		return Math.abs(fila1-fila2) + Math.abs(col1 - col2);
	}
	
	/**
	 * Función que toma la posición del avatar e inicia una instancia de AEstrella para llevar el avatar a la posición dada por col_obj y fila_obj
	 * @param col_obj Columna del objetivo a ir
	 * @param fila_obj Fila del objetivo a ir
	 * @param timer Objeto de tipo {@link ElapsedCpuTimer} para controlar el tiempo consumido en el AEstrella
	 * @return Devuelve una lista de acciones a realizar para ir hacia el objetivo
	 */
	public ArrayList<Types.ACTIONS> obtenCamino(int col_obj, int fila_obj, ElapsedCpuTimer timer, boolean notime){
		// Tomamos las coordenadas actuales del avatar
		int col_actual = (int) Math.round(this.obs.getAvatarPosition().x / this.fescalaX);
    	int fila_actual = (int) Math.round(this.obs.getAvatarPosition().y / this.fescalaY);
    	// Inicializamos el nodo de inicio con la posición del avatar y el final con la posición del objetivo
		Nodo inicio = new Nodo(0, distanciaManhattan(fila_actual, col_actual, fila_obj, col_obj), col_actual, fila_actual, null, obs.getAvatarOrientation());
		Nodo fin = new Nodo(distanciaManhattan(fila_actual, col_actual, fila_obj, col_obj), 0, col_obj, fila_obj, null, obs.getAvatarOrientation());
		aestrella.setParametros(inicio, fin, mundo);
		List<Nodo> camino = aestrella.buscaCamino(timer,notime);
		
		if(camino.get(camino.size()-1).columna!=col_obj || camino.get(camino.size()-1).fila!=fila_obj) {
			ArrayList<Types.ACTIONS> idle = new ArrayList<Types.ACTIONS>();
			idle.add(Types.ACTIONS.ACTION_NIL);
			return idle;
		}
		ArrayList<Types.ACTIONS> acciones = aestrella.devuelveAcciones(obs);
		return acciones;
	}
	
	/**
	 * Función que recibe una posición e inicia una instancia de AEstrella para llevar el avatar a la posición dada por col_obj y fila_obj
	 * @param col_actual Columna de inicio
	 * @param fila_actual Fila de inicio
	 * @param col_obj Columna del objetivo a ir
	 * @param fila_obj Fila del objetivo a ir
	 * @param timer Objeto de tipo {@link ElapsedCpuTimer} para controlar el tiempo consumido en el AEstrella
	 * @return Devuelve una lista de acciones a realizar para ir hacia el objetivo
	 */
	public ArrayList<Types.ACTIONS> obtenCamino2(int col_actual, int fila_actual,int col_obj, int fila_obj, ElapsedCpuTimer timer, boolean notime){
    	// Inicializamos el nodo de inicio con la posición del avatar y el final con la posición del objetivo
		Nodo inicio = new Nodo(0, distanciaManhattan(fila_actual, col_actual, fila_obj, col_obj), col_actual, fila_actual, null, obs.getAvatarOrientation());
		Nodo fin = new Nodo(distanciaManhattan(fila_actual, col_actual, fila_obj, col_obj), 0, col_obj, fila_obj, null, obs.getAvatarOrientation());
		aestrella.setParametros(inicio, fin, mundo);
		List<Nodo> camino = aestrella.buscaCamino(timer,notime);
		
		if(camino.get(camino.size()-1).columna!=col_obj || camino.get(camino.size()-1).fila!=fila_obj) {
			ArrayList<Types.ACTIONS> idle = new ArrayList<Types.ACTIONS>();
			idle.add(Types.ACTIONS.ACTION_NIL);
			return idle;
		}
		ArrayList<Types.ACTIONS> acciones = aestrella.devuelveAcciones(obs);
		cantidad_pasos = acciones.size();
		return acciones;
	}
	
	/**
	 * Función que lleva el avatar hacia la posición del portal
	 * @param timer Objeto para controlar el tiempo que consume AEstrella
	 * @return Devuelve una lista de acciones a realizar para llegar hasta el portal
	 */
	public ArrayList<Types.ACTIONS> salirPortal(ElapsedCpuTimer timer){
		//Obtenemos las coordenadas del portal
		int col_portal = (int) Math.round(this.obs.getPortalsPositions()[0].get(0).position.x / fescalaX);
    	int fila_portal = (int) Math.round(this.obs.getPortalsPositions()[0].get(0).position.y / fescalaY);
    	
    	//Obtenemos las coordenadas del avatar
    	int col_avatar = (int) Math.round(this.obs.getAvatarPosition().x / fescalaX);
    	int fila_avatar = (int) Math.round(this.obs.getAvatarPosition().y / fescalaY);
    	
    	ArrayList<Types.ACTIONS> acciones = new ArrayList<Types.ACTIONS>();
    	
    	// Si ya estamos en el portal devolvemos la acción nula, en caso contrario llamamos obtenCamino para ir hacia el portal
    	if(col_portal==col_avatar && fila_portal==fila_avatar) {
    		acciones.add(Types.ACTIONS.ACTION_ESCAPE);
    		return acciones;
    	}
    	return obtenCamino(col_portal, fila_portal, timer,false);
    	
	}
	
	public ArrayList<Types.ACTIONS> moverPiedra(int col_piedra, int fila_piedra){
    	int fila_avatar = (int) Math.round(this.obs.getAvatarPosition().y / fescalaY);
		ArrayList<Types.ACTIONS> acciones = new ArrayList<Types.ACTIONS>();
		Vector2d orientacion_avatar = this.obs.getAvatarOrientation();
		boolean bien_orientados = fila_piedra == fila_avatar + orientacion_avatar.x;
		if(!bien_orientados) {
			if(orientacion_avatar.x==1.0)
				acciones.add(Types.ACTIONS.ACTION_LEFT);
			else
				acciones.add(Types.ACTIONS.ACTION_RIGHT);
		}
		acciones.add(Types.ACTIONS.ACTION_USE);
		return acciones;
	}
	
	public void reset() {
		this.aestrella.reset();
	}
	
	public void setParametros(StateObservation obs) {
		this.obs = obs;
		this.mundo = obs.getObservationGrid();
	}

}
