package AguileraBalderas;

import java.util.ArrayList;
import java.util.HashSet;
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
	private HashSet<Vector2di> contornos_bichos;
	
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
		this.contornos_bichos = new HashSet<Vector2di>();
		this.mundo = mundo;
		this.ancho = ancho;
		this.alto = alto;
		this.obs = obs;
		this.fescalaX = fescalaX;
		this.fescalaY = fescalaY;
		this.aestrella=new AEstrella(ancho,alto);
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
	 * @param notime Booleano que controla si queremos que el tiempo se tenga o no en cuenta en el algoritmo AEstrella (usado en el constructor)
	 * @return Devuelve una lista de acciones a realizar para ir hacia el objetivo
	 */
	public ArrayList<Types.ACTIONS> obtenCamino(int col_obj, int fila_obj, ElapsedCpuTimer timer, boolean notime){
		// Tomamos las coordenadas actuales del avatar
		int col_actual = (int) Math.round(this.obs.getAvatarPosition().x / this.fescalaX);
    	int fila_actual = (int) Math.round(this.obs.getAvatarPosition().y / this.fescalaY);
    	// Inicializamos el nodo de inicio con la posición del avatar y el final con la posición del objetivo
		Nodo inicio = new Nodo(0, distanciaManhattan(fila_actual, col_actual, fila_obj, col_obj), col_actual, fila_actual, null, obs.getAvatarOrientation());
		Nodo fin = new Nodo(distanciaManhattan(fila_actual, col_actual, fila_obj, col_obj), 0, col_obj, fila_obj, null, obs.getAvatarOrientation());
		//Actualizamos los parámetros del algoritmo
		aestrella.setParametros(inicio, fin, mundo);
		//Calculamos el camino
		List<Nodo> camino = aestrella.buscaCamino(timer,notime);
		
		//Si hemos llegado al objetivo entonces devolvemos nil
		if(col_actual == col_obj && fila_actual == fila_obj) {
			ArrayList<Types.ACTIONS> idle = new ArrayList<Types.ACTIONS>();
			idle.add(Types.ACTIONS.ACTION_NIL);
			return idle;
		}
		
		//Si la última posición del camino no es la objetivo devolvemos nil porque el algoritmo AEstrella no ha acabado
		if(camino.get(camino.size()-1).columna!=col_obj || camino.get(camino.size()-1).fila!=fila_obj) {
			ArrayList<Types.ACTIONS> idle = new ArrayList<Types.ACTIONS>();
			idle.add(Types.ACTIONS.ACTION_NIL);
			return idle;
		}
		//En otro caso calculamos la lista de acciones asociadas al camino y la devolvemos.
		ArrayList<Types.ACTIONS> acciones = aestrella.devuelveAcciones(obs);
		return acciones;
	}
	
	/**
	 * Función que recibe una posición e inicia una instancia de AEstrella para llevar el avatar a la posición dada por col_obj y fila_obj desde una
	 * posición dada por col_actual y fila_actual y no por la posición del avatar.
	 * @param col_actual Columna de inicio
	 * @param fila_actual Fila de inicio
	 * @param col_obj Columna del objetivo a ir
	 * @param fila_obj Fila del objetivo a ir
	 * @param timer Objeto de tipo {@link ElapsedCpuTimer} para controlar el tiempo consumido en el AEstrella
	 * @param notime Booleano que indica si queremos que se tenga o no en cuenta el tiempo.
	 * @return Devuelve una lista de acciones a realizar para ir hacia el objetivo
	 */
	public ArrayList<Types.ACTIONS> obtenCamino2(int col_actual, int fila_actual,int col_obj, int fila_obj, ElapsedCpuTimer timer, boolean notime){
    	// Inicializamos el nodo de inicio con la posición del avatar y el final con la posición del objetivo
		Nodo inicio = new Nodo(0, distanciaManhattan(fila_actual, col_actual, fila_obj, col_obj), col_actual, fila_actual, null, obs.getAvatarOrientation());
		Nodo fin = new Nodo(distanciaManhattan(fila_actual, col_actual, fila_obj, col_obj), 0, col_obj, fila_obj, null, obs.getAvatarOrientation());
		//Establecemos los parámetros del algoritmo
		aestrella.setParametros(inicio, fin, mundo);
		//Calculamos la secuencia de nodos
		List<Nodo> camino = aestrella.buscaCamino(timer,notime);
		
		//Si no hemos llegado devolvemos nil
		if(camino.get(camino.size()-1).columna!=col_obj || camino.get(camino.size()-1).fila!=fila_obj) {
			ArrayList<Types.ACTIONS> idle = new ArrayList<Types.ACTIONS>();
			idle.add(Types.ACTIONS.ACTION_NIL);
			return idle;
		}
		//Si hemos llegado entonces calculamos la lista de acciones asociada
		ArrayList<Types.ACTIONS> acciones = aestrella.devuelveAcciones(obs);
		//Guardamos el número de pasos que tenemos que dar como costo del camino (número de ticks necesarios para llegar)
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
	
	/**
	 * Función que devuelve una lista de acciones para tirar una piedra dada
	 * @param col Columna desde la que queremos tirar la piedra
	 * @param fil Fila desde la que queremos tirar la piedra
	 * @param col_piedra Columna de la piedra
	 * @param fila_piedra Fila de la piedra
	 * @return Devuelve un ArrayList de acciones asociadas a tirar una piedra incluida la accion USE.
	 */
	public ArrayList<Types.ACTIONS> moverPiedra(int col, int fil,int col_piedra, int fila_piedra){
		//Columna del avatar
    	int columna_avatar = col;
		ArrayList<Types.ACTIONS> acciones = new ArrayList<Types.ACTIONS>();
		//Comprobamos si estamos o no en la orientación correcta
		Vector2d orientacion_avatar = this.obs.getAvatarOrientation();
		boolean bien_orientados = (col_piedra == columna_avatar + orientacion_avatar.x);
		//Corregimos la orientación si es necesario
		if(!bien_orientados) {
			if(orientacion_avatar.x==1.0)
				acciones.add(Types.ACTIONS.ACTION_LEFT);
			else
				acciones.add(Types.ACTIONS.ACTION_RIGHT);
		}
		//Añadimos la acción para excavar
		acciones.add(Types.ACTIONS.ACTION_USE);
		return acciones;
	}
	
	/**
	 * Función que resetea el estado de un objeto de tipo {@link ResolutorTareas} llamando al reset de su objeto AEstrella
	 */
	public void reset() {
		this.aestrella.reset();
	}
	
	/**
	 * Función que actualiza los parámetros del objeto {@link ResolutorTareas}
	 * @param obs Estado actual del mundo
	 * @param contornos_bichos Contorno de los bichos usado en AEstrella
	 */
	public void setParametros(StateObservation obs, HashSet<Vector2di> contornos_bichos) {
		aestrella.contornos_bichos = contornos_bichos;
		this.contornos_bichos = contornos_bichos;
		this.obs = obs;
		this.mundo = obs.getObservationGrid();
	}
	
	/**
	 * Función que obtiene los vecinos de una posición dada
	 * @param posicion Posición de la que queremos obtener los vecinos
	 * @return Devuelve un ArrayList de Vector2di con las posiciones de los vecinos si estas son válidas
	 */
	public ArrayList<Vector2di> getVecinos(Vector2di posicion){
		ArrayList<Vector2di> vecinos = new ArrayList<Vector2di>();
		if(posicion.x-1>=0 && posicion.x-1<ancho && posicion.y>=0 && posicion.y<alto)
			vecinos.add(new Vector2di(posicion.x-1, posicion.y));
		if(posicion.y-1>=0 && posicion.y-1<alto && posicion.x>=0 && posicion.x<ancho)
			vecinos.add(new Vector2di(posicion.x, posicion.y-1));
		if(posicion.x+1<ancho && posicion.x+1>=0 && posicion.y<alto && posicion.y>=0)
			vecinos.add(new Vector2di(posicion.x+1, posicion.y));
		if(posicion.y+1<alto && posicion.y+1>=0 && posicion.x>=0 && posicion.x<ancho)
			vecinos.add(new Vector2di(posicion.x, posicion.y+1));
		return vecinos;
	}
	
	/**
	 * Función que obtiene las regiones de los bichos (en concreto los contornos)
	 * @param stateObs Estado del mundo
	 * @return Devuelve un Set con las casillas que son los contornos de las áreas de los bichos.
	 */
	public HashSet<Vector2di> obtenRegionesBichos(StateObservation stateObs){
		//Obtenemos la matriz del mundo
		ArrayList<Observation>[][] mundo = stateObs.getObservationGrid();
		HashSet<Vector2di> interiores = new HashSet<Vector2di>();
		HashSet<Vector2di> contornos = new HashSet<Vector2di>();
		
		//Obtenemos las posiciones de los bichos
		ArrayList<Vector2di> posiciones_bichos = new ArrayList<Vector2di>();
		if(stateObs.getNPCPositions()!=null) {
			for(ArrayList<Observation> ob : stateObs.getNPCPositions()) {
				for(Observation obs : ob) {
					Vector2di bicho = new Vector2di((int) Math.round(obs.position.x / fescalaX), (int) Math.round(obs.position.y / fescalaY));
					posiciones_bichos.add(bicho);
				}
			}
		}
		
		//Calculamos aquellas posiciones que son accesibles para cada bicho
		ArrayList<Vector2di> desarrollando = new ArrayList<Vector2di>(posiciones_bichos);
		Vector2di actual = new Vector2di(-1,-1);
		while(!desarrollando.isEmpty()) {
			actual = desarrollando.get(0);
			desarrollando.remove(0);
			interiores.add(actual);
			ArrayList<Vector2di> vecinos_vacios = getVecinos(actual);
			for(Vector2di vecino : vecinos_vacios)
				if(!interiores.contains(vecino) && mundo[vecino.x][vecino.y].size()==0)
					desarrollando.add(vecino);
		}
		
		
		//Calculamos los contornos de las posiciones que son accesibles para los bichos
		ArrayList<Vector2di> desarrollando_interiores = new ArrayList<Vector2di>(interiores);
		actual = new Vector2di(-1,-1);
		while(!desarrollando_interiores.isEmpty()) {
			actual = desarrollando_interiores.get(0);
			desarrollando_interiores.remove(0);
			ArrayList<Vector2di> vecinos_no_vacios = getVecinos(actual);
			for(Vector2di vecino : vecinos_no_vacios) {
				if(!contornos.contains(vecino) && mundo[vecino.x][vecino.y].size()>0)
					contornos.add(vecino);
			}
		}
		
		return contornos;
	}

}
