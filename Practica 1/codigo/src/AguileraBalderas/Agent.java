package AguileraBalderas;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import AguileraBalderas.ResolutorTareas;

import java.util.ArrayList;
import java.util.HashSet;

public class Agent extends AbstractPlayer {
	//Factores de escala para conversión del mundo
    private int fescalaX;
    private int fescalaY;
    
    //Lista de acciones a realizar
    private ArrayList<Types.ACTIONS> lista_acciones;
    
    //Lista de gemas que se pueden coger si interaccionar con piedras ni bichos
    private ArrayList<Gema> lista_gemas_faciles;
    
    //Lista de gemas que se pueden coger fácil interaccionando con piedras
    private ArrayList<Gema> lista_gemas_faciles_piedras;
    
    //Lista de gemas que se pueden coger fácil interaccionando con bichos
    private ArrayList<Gema> lista_gemas_faciles_bichos;
    
    //Resolutor de tareas, nos da caminos y rutinas de acciones
    private ResolutorTareas resolutor;
    
    //Valor booleano que nos dice si hemos acabado la partida
    private boolean acabado;
    
    //Dimensiones del mundo
    int alto, ancho;
    
    //Coordenadas del portal, el punto inicial y un punto alejado a los dos primeros (se usan para escapar de forma planificada)
    int fila_portal, col_portal;
    int fila_inicial, col_inicial;
    int tercer_punto_fila, tercer_punto_col;
    
    //Valor booleano que nos indica si estamos escapando
    boolean escapando;
    
    //Número de veces que hemos escapado (para escoger de forma aleatoria el punto inicial, el portal o el tercer punto)
    int veces_escapadas;
    
    //Valor booleano para saber si estamos escpando de forma reactiva
    boolean escapando_reactivo;
    
    //Número de veces que hemos estado yendo a por la misma gema o que nos hemos quedado en la misma posicion, se usan para desbloquear al avatar
    int bloqueado = 0;
    //Gema objetivo, se usa para ver si llevamos mucho tiempo yendo a por la misma gema.
    Gema gema_objetivo = null;
    
    //Set de posiciones que no se pueden tocar porque hay bichos
    HashSet<Vector2di> contornos_bichos;
    
    //Booleano que controla si estamos en el primer act.
    boolean primer_act = false;
    //Booleano que controla que los primeros movimientos sean nulos para que caigan las piedras
    boolean nils;
    
    //Booleano que controla el cálculo de las gemas que requieren mover piedras
    boolean calcula_gemas_piedras;
            
    /**
     * Función que calcula la distancia manhattan entre dos puntos
     * @param fila1 Fila del primer punto
     * @param col1 Columna del primer punto
     * @param fila2 Fila del segundo punto
     * @param col2 Columna del segundo punto
     * @return Devuelve un valor entero con la distancia entre los dos puntos
     */
    private int distanciaManhattan(int fila1, int col1, int fila2, int col2) {
		return Math.abs(fila1-fila2) + Math.abs(col1 - col2);
	}
    
    /**
     * Constructor de la clase Agent
     * @param stateObs Estado actual del mundo
     * @param elapsedTimer Objeto para controlar el tiempo
     */
    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
    	//Activamos el cálculo de las gemas que involucran piedras
    	this.calcula_gemas_piedras=true;
    	//Calculamos las dimensiones del mundo
    	ancho = stateObs.getObservationGrid().length;
        alto = stateObs.getObservationGrid()[0].length;
    	
        //Inicializamos las listas de gemas de piedras y bichos
    	this.lista_gemas_faciles_bichos=new ArrayList<Gema>();
    	this.lista_gemas_faciles_piedras = new ArrayList<Gema>();
    	
    	//El siguiente act es el primero
    	this.primer_act=true;
    	//Avanzamos el mundo para simular que han caido las piedras
    	for(int i = 0; i<100 ; ++i)
    		stateObs.advance(Types.ACTIONS.ACTION_NIL);
    	
    	//Inicializamos el los contornos de los bichos
    	this.contornos_bichos = new HashSet<Vector2di>();
    	
    	//Inicializamos el estado
    	this.escapando_reactivo=false;
    	veces_escapadas=0;
    	escapando=false;
    	acabado = false;
 	
    	//Inicializamos la lista de acciones
    	lista_acciones = new ArrayList<Types.ACTIONS>();

    	//Calculamos los factores de escala del mundo
        this.fescalaX = stateObs.getWorldDimension().width / stateObs.getObservationGrid().length;
        this.fescalaY = stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length;
        
        //Inicializamos el objeto ResolutorTareas
        resolutor = new ResolutorTareas(stateObs.getObservationGrid(), stateObs.getObservationGrid().length, stateObs.getObservationGrid()[0].length,stateObs, this.fescalaX, this.fescalaY);
        //Obetenemos los contornos de los bichos
        this.contornos_bichos = resolutor.obtenRegionesBichos(stateObs);
        //Reseteamos el resolutor y colocamos los parámetros actuales
        resolutor.reset();
        resolutor.setParametros(stateObs, this.contornos_bichos);
        
        //Calculamos la lista de gemas fáciles y establecemos la gema objetivo como la actual
        lista_gemas_faciles = new ArrayList<Gema>();
        lista_gemas_faciles = obtenListaGemasFacilesSinBichos(stateObs,elapsedTimer);
        gema_objetivo = lista_gemas_faciles.size()>0 ? lista_gemas_faciles.get(0) : new Gema();
        
        //Calculamos la lista de gemas que involucran piedras
        lista_gemas_faciles_piedras = obtenListaGemasFacilesPiedras(stateObs);
        
        //Calculamos la lista de gemas que involucran bichos
        lista_gemas_faciles_bichos = obtenListaGemasConBichos(stateObs, elapsedTimer);
        
        //Obtenemos la matriz del mundo
        ArrayList<Observation>[][] mundo = stateObs.getObservationGrid();
        
        
        //Calculamos todas las posiciones accesibles por el algoritmo AEstrella
        ArrayList<Vector2di> posiciones_accesibles = new ArrayList<Vector2di>();
        for(int i=0; i < ancho;++i)
        	for(int j = 0; j < alto; j++) {
        		resolutor.reset();
        		resolutor.setParametros(stateObs, this.contornos_bichos);
        		if(isAccesible(mundo, i, j)) {
	        		if(resolutor.obtenCamino(i, j, elapsedTimer, true).get(0)!=Types.ACTIONS.ACTION_NIL)
	        			posiciones_accesibles.add(new Vector2di(i,j));
        		}
        	}
        
        //Guardamos la posición del portal
        col_portal = (int) Math.round(stateObs.getPortalsPositions()[0].get(0).position.x / this.fescalaX);
    	fila_portal = (int) Math.round(stateObs.getPortalsPositions()[0].get(0).position.y / this.fescalaY);
    	
    	//Guardamos la posicion inicial
    	col_inicial = (int) Math.round(stateObs.getAvatarPosition().x / this.fescalaX);
    	fila_inicial = (int) Math.round(stateObs.getAvatarPosition().y / this.fescalaY);
    	
    	//Calculamos un tercer punto para escapar de los bichos que es, de los accesibles, el más lejano al portal y la posición inicial
    	if(!posiciones_accesibles.isEmpty()) {
    		Vector2di max_dist = posiciones_accesibles.get(0);
    		for(Vector2di pos : posiciones_accesibles) {
    			if(distanciaManhattan(max_dist.x, max_dist.y, col_inicial, fila_inicial)<distanciaManhattan(pos.x, pos.y, col_inicial, fila_inicial))
    				if(distanciaManhattan(max_dist.x, max_dist.y, col_portal, fila_portal)<distanciaManhattan(pos.x, pos.y, col_portal, fila_portal))
    					max_dist = pos;
    		}
                
    		tercer_punto_col = max_dist.x;
    		tercer_punto_fila = max_dist.y;
    	}
        
    }

    /**
     * Función que calcula la lista de gemas que no involucran bichos ni piedras
     * @param stateObs Estado actual del mundo
     * @param timer Objeto para medir el tiempo
     * @return Devuelve un ArrayList con las gemas fáciles
     */
    private ArrayList<Gema> obtenListaGemasFacilesSinBichos(StateObservation stateObs, ElapsedCpuTimer timer) {
    	//Posición del avatar
    	int col_actual = (int) Math.round(stateObs.getAvatarPosition().x / this.fescalaX);
    	int fila_actual = (int) Math.round(stateObs.getAvatarPosition().y / this.fescalaY);
    	//Matriz del mundo
    	ArrayList<Observation>[][] mundo = stateObs.getObservationGrid();
    	//Generamos un resolutor de tareas auxiliar
		ResolutorTareas resolutor_aux = new ResolutorTareas(mundo, mundo.length, mundo[0].length, stateObs, fescalaX, fescalaY);
		//Inicializamos los ArrayList
		ArrayList<Gema> gemas_faciles = new ArrayList<Gema>();
		ArrayList<Gema> gemas = new ArrayList<Gema>();
		
		//Obtenemos las posiciones de las gemas y las ponemos en el ArrayList gemas
		ArrayList<Observation>[] posiciones_gemas = stateObs.getResourcesPositions();
		for(Observation o : posiciones_gemas[0]) {
			Gema gema = new Gema();
			gema.coordenadas.x = (int) Math.round(o.position.x / fescalaX);
			gema.coordenadas.y = (int) Math.round(o.position.y / fescalaY);
			if(!contornos_bichos.contains(gema.coordenadas))
				gemas.add(gema);
		}
		//Calculamos cuáles de esas son accesibles por AEstrella
		ArrayList<Gema> gemas9 = new ArrayList<Gema>();
		while(gemas.size()>0) {
			gemas_faciles = new ArrayList<Gema>();
			for(Gema gema : gemas) {
				resolutor_aux.reset();
				resolutor_aux.setParametros(stateObs, this.contornos_bichos);
				for(int j = 0; j < 20; j++) {
					if(resolutor_aux.obtenCamino2(col_actual,fila_actual,gema.coordenadas.x, gema.coordenadas.y, timer,true).get(0)!=Types.ACTIONS.ACTION_NIL) {
						gema.distancia_actual = resolutor_aux.cantidad_pasos;
						gemas_faciles.add(gema);
					}
				}
			}
			//Las ordenamos por distancia entre ellas para escoger el camino con menor distancia que pase por todas
			if(gemas_faciles.size()>0) {
				Gema min = gemas_faciles.get(0);
				for(int j = 1; j<gemas_faciles.size();j++) {
					if(gemas_faciles.get(j).distancia_actual < min.distancia_actual)
						min = gemas_faciles.get(j);
				}
				gemas.remove(min);
				gemas9.add(min);
				col_actual = gemas9.get(gemas9.size()-1).coordenadas.x;
				fila_actual = gemas9.get(gemas9.size()-1).coordenadas.y;
			}
			else
				return gemas9;
			
		}
		return gemas9;
	}
    
    /**
     * Función que obtiene la lista de gemas que involucran bichos
     * @param stateObs Estado actual del mundo
     * @param timer Objeto para medir el tiempo
     * @return Devuelve un ArrayList con las gemas
     */
    private ArrayList<Gema> obtenListaGemasConBichos(StateObservation stateObs, ElapsedCpuTimer timer) {
    	//Posición actual del avatar
    	int col_actual = (int) Math.round(stateObs.getAvatarPosition().x / this.fescalaX);
    	int fila_actual = (int) Math.round(stateObs.getAvatarPosition().y / this.fescalaY);
    	//Matriz del mundo
    	ArrayList<Observation>[][] mundo = stateObs.getObservationGrid();
    	//Obtenemos un resolutor auxiliar
		ResolutorTareas resolutor_aux = new ResolutorTareas(mundo, mundo.length, mundo[0].length, stateObs, fescalaX, fescalaY);
		ArrayList<Gema> gemas_faciles = new ArrayList<Gema>();
		ArrayList<Gema> gemas = new ArrayList<Gema>();
		
		//Calculamos la posición de las gemas.
		ArrayList<Observation>[] posiciones_gemas = stateObs.getResourcesPositions();
		for(Observation o : posiciones_gemas[0]) {
			Gema gema = new Gema();
			gema.coordenadas.x = (int) Math.round(o.position.x / fescalaX);
			gema.coordenadas.y = (int) Math.round(o.position.y / fescalaY);
			if(!contornos_bichos.contains(gema.coordenadas))
				gemas.add(gema);
		}
		
		//Calculamos aquellas que son accesibles
		ArrayList<Gema> gemas9 = new ArrayList<Gema>();
		while(gemas.size()>0) {
			gemas_faciles = new ArrayList<Gema>();
			for(Gema gema : gemas) {
				resolutor_aux.reset();
				//Aquí no colocamos los contornos de los bichos si no un set vacío para no tenerlos en cuenta
				resolutor_aux.setParametros(stateObs, new HashSet<Vector2di>());
				for(int j = 0; j < 20; j++) {
					if(resolutor_aux.obtenCamino2(col_actual,fila_actual,gema.coordenadas.x, gema.coordenadas.y, timer,true).get(0)!=Types.ACTIONS.ACTION_NIL) {
						gema.distancia_actual = resolutor_aux.cantidad_pasos;
						gemas_faciles.add(gema);
					}
				}
			}
			//Las ordenamos por distancia
			if(gemas_faciles.size()>0) {
				Gema min = gemas_faciles.get(0);
				for(int j = 1; j<gemas_faciles.size();j++) {
					if(gemas_faciles.get(j).distancia_actual < min.distancia_actual)
						min = gemas_faciles.get(j);
				}
				gemas.remove(min);
				gemas9.add(min);
				col_actual = gemas9.get(gemas9.size()-1).coordenadas.x;
				fila_actual = gemas9.get(gemas9.size()-1).coordenadas.y;
			}
			else
				return gemas9;
			
		}
		return gemas9;
	}
    
    /**
     * Función que obtiene las gemas que son accesibles moviendo piedras
     * @param stateObs Estado actual del mundo
     * @return Devuelve un ArrayList con las gemas que son accesibles moviendo piedras
     */
    private ArrayList<Gema> obtenListaGemasFacilesPiedras(StateObservation stateObs) {
    	//Matriz del mundo
    	ArrayList<Observation>[][] mundo = stateObs.getObservationGrid();
		ArrayList<Gema> gemas = new ArrayList<Gema>();
		
		//Obtenemos las posiciones de las gemas
		ArrayList<Observation>[] posiciones_gemas = stateObs.getResourcesPositions();
		for(Observation o : posiciones_gemas[0]) {
			Gema gema = new Gema();
			gema.coordenadas.x = (int) Math.round(o.position.x / fescalaX);
			gema.coordenadas.y = (int) Math.round(o.position.y / fescalaY);
			if(!contornos_bichos.contains(gema.coordenadas) && !lista_gemas_faciles.contains(gema))
				gemas.add(gema);
		}
		
		//Vamos a comprobar que las gemas concuerdan con los tipos que hemos tenido en cuenta y explicado en la documentación
		ArrayList<Gema> gemas_piedras = new ArrayList<Gema>();
		for(Gema gema : gemas) {
			//Comprobamos el primer tipo de gema con piedras
			if(esPiedra(mundo,gema.coordenadas.x, gema.coordenadas.y-1) && !contornos_bichos.contains(new Vector2di(gema.coordenadas.x, gema.coordenadas.y-1))){
					if(isAccesibleSinPiedraArriba(mundo, gema.coordenadas.x+1, gema.coordenadas.y-1) && isAccesibleSinPiedraArriba(mundo, gema.coordenadas.x+1, gema.coordenadas.y)) {
						gema.posiciones_a_ir.add(new Vector2di(gema.coordenadas.x+1, gema.coordenadas.y));
						gema.orientaciones.add(new Vector2d(-1.0,0.0));
						gema.posiciones_a_ir.add(gema.coordenadas);
						gema.orientaciones.add(null);
						gema.tipo_gema_piedra=1;
						gemas_piedras.add(gema);
					}
					else if(isAccesibleSinPiedraArriba(mundo, gema.coordenadas.x-1, gema.coordenadas.y-1) && isAccesibleSinPiedraArriba(mundo, gema.coordenadas.x-1, gema.coordenadas.y)) {
						gema.posiciones_a_ir.add(new Vector2di(gema.coordenadas.x-1, gema.coordenadas.y));
						gema.orientaciones.add(new Vector2d(1.0,0.0));
						gema.posiciones_a_ir.add(gema.coordenadas);
						gema.orientaciones.add(null);
						gema.tipo_gema_piedra=1;
						gemas_piedras.add(gema);
					}
			}
			else if(isAccesibleSinPiedraArriba(mundo, gema.coordenadas.x, gema.coordenadas.y+2)) {
				if(isAccesibleSinPiedraArriba(mundo, gema.coordenadas.x, gema.coordenadas.y+3))
					if(esPiedra(mundo,gema.coordenadas.x, gema.coordenadas.y+1) && !contornos_bichos.contains(new Vector2di(gema.coordenadas.x, gema.coordenadas.y+1)))
						if(!isAccesibleSinPiedraArriba(mundo, gema.coordenadas.x-1, gema.coordenadas.y))
							if(!isAccesibleSinPiedraArriba(mundo, gema.coordenadas.x+1, gema.coordenadas.y))
								if(!esPiedra(mundo, gema.coordenadas.x, gema.coordenadas.y-1)) {
									if(isAccesible(mundo, gema.coordenadas.x-1, gema.coordenadas.y+2)) {
										gema.posiciones_a_ir.add(new Vector2di(gema.coordenadas.x-1, gema.coordenadas.y+2));
										gema.orientaciones.add(new Vector2d(1.0,0.0));
									}
									else if(isAccesible(mundo, gema.coordenadas.x+1, gema.coordenadas.y+2)) {
										gema.posiciones_a_ir.add(new Vector2di(gema.coordenadas.x+1, gema.coordenadas.y+2));
										gema.orientaciones.add(new Vector2d(-1.0,0.0));
									}
									if(isAccesible(mundo, gema.coordenadas.x-1, gema.coordenadas.y+3)) {
										gema.posiciones_a_ir.add(new Vector2di(gema.coordenadas.x-1, gema.coordenadas.y+3));
										gema.orientaciones.add(new Vector2d(1.0,0.0));
									}
									else if(isAccesible(mundo, gema.coordenadas.x+1, gema.coordenadas.y+3)) {
										gema.posiciones_a_ir.add(new Vector2di(gema.coordenadas.x+1, gema.coordenadas.y+3));
										gema.orientaciones.add(new Vector2d(-1.0,0.0));
									}
									if(gema.posiciones_a_ir.size()==2) {
										gema.posiciones_a_ir.add(gema.coordenadas);
										gema.orientaciones.add(null);
										gema.tipo_gema_piedra=0;
										gemas_piedras.add(gema);
									}
								}
			}
			//Añadiendo caso de dos piedras debajo de una gema para pasar el mapa 0
			else if(isAccesibleSinPiedraArriba(mundo,gema.coordenadas.x,gema.coordenadas.y+3)) {
				if(isAccesibleSinPiedraArriba(mundo,gema.coordenadas.x,gema.coordenadas.y+4)) {
					if(esPiedra(mundo,gema.coordenadas.x,gema.coordenadas.y+1) && 
							esPiedra(mundo,gema.coordenadas.x,gema.coordenadas.y+2) && !contornos_bichos.contains(new Vector2di(gema.coordenadas.x,gema.coordenadas.y+1))
							&& !contornos_bichos.contains(new Vector2di(gema.coordenadas.x,gema.coordenadas.y+2))){
						if(!esPiedra(mundo,gema.coordenadas.x,gema.coordenadas.y-1)) {
							if(isAccesible(mundo,gema.coordenadas.x-1,gema.coordenadas.y+3)) {
								gema.posiciones_a_ir.add(new Vector2di(gema.coordenadas.x-1,gema.coordenadas.y+3));
								gema.orientaciones.add(new Vector2d(1.0,0.0));
							}
							else if(isAccesible(mundo,gema.coordenadas.x+1,gema.coordenadas.y+3)) {
								gema.posiciones_a_ir.add(new Vector2di(gema.coordenadas.x+1,gema.coordenadas.y+3));
								gema.orientaciones.add(new Vector2d(-1.0,0.0));
							}
							if(isAccesible(mundo,gema.coordenadas.x-1,gema.coordenadas.y+4)) {
								gema.posiciones_a_ir.add(new Vector2di(gema.coordenadas.x-1, gema.coordenadas.y+4));
								gema.orientaciones.add(new Vector2d(1.0,0.0));
							}
							else if(isAccesible(mundo, gema.coordenadas.x+1, gema.coordenadas.y+4)) {
								gema.posiciones_a_ir.add(new Vector2di(gema.coordenadas.x+1, gema.coordenadas.y+4));
								gema.orientaciones.add(new Vector2d(-1.0,0.0));
							}
							if(gema.posiciones_a_ir.size()==2) {
								gema.posiciones_a_ir.add(gema.coordenadas);
								gema.orientaciones.add(null);
								gema.tipo_gema_piedra=0;
								gemas_piedras.add(gema);
							}
						}
					}
				}
			}
			
		}
		return gemas_piedras;
	}
    
	@Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		//Calculamos los contornos de los bichos si lo necesitamos
		if(lista_gemas_faciles.size()>0 || lista_gemas_faciles_piedras.size()>0)
			this.contornos_bichos = resolutor.obtenRegionesBichos(stateObs);	
		
		//Colocamos los contornos de los bichos si estamos buscando gemas fáciles o gemas que involucran piedras, si son gemas de bichos entonces no
		if(this.lista_gemas_faciles.size()==0 && this.lista_gemas_faciles_piedras.size()==0 && this.lista_gemas_faciles_bichos.size()>0)
    		resolutor.setParametros(stateObs, new HashSet<Vector2di>());
		else
			resolutor.setParametros(stateObs, this.contornos_bichos);
    	
		//Si tenemos que recalcular la lista de gemas de piedras la recalculamos
    	if(this.calcula_gemas_piedras && lista_gemas_faciles.size()==0) {
    		lista_gemas_faciles_piedras = obtenListaGemasFacilesPiedras(stateObs);
			this.calcula_gemas_piedras=false;
    	}
    	
    	//si estamos bloqueados quitamos gemas no accesibles por culpa de los bichos
    	if(this.nils && lista_acciones.isEmpty()) {
			this.nils=false;
			if(!lista_gemas_faciles.isEmpty()) {
				Gema gem = lista_gemas_faciles.get(0);
				resolutor.reset();
				if(resolutor.obtenCamino(gem.coordenadas.x, gem.coordenadas.y, elapsedTimer, false).get(0)==Types.ACTIONS.ACTION_NIL)
					lista_gemas_faciles.remove(0);
			}
		}
		
    	//Si es el primer act hacemos 20 acciones nulas
		if(this.primer_act) {
			for(int i = 0; i<20; ++i)
				lista_acciones.add(Types.ACTIONS.ACTION_NIL);
			this.primer_act=false;
			this.nils=true;
		}
		
		//Si ya tenemos las gemas que necesitamos ponemos el estado a acabado
		if(stateObs.getAvatarResources().size()>0)
    		if(stateObs.getAvatarResources().get(6)>=9) {
    			if(!acabado)
    				resolutor.reset();
    			acabado=true;
    		}
		
		//Si hemos acabado vamos hacia el portal
		if(this.acabado) {
			if(lista_acciones.isEmpty())
				lista_acciones = resolutor.salirPortal(elapsedTimer);
			else {
				Types.ACTIONS accion = lista_acciones.get(0);
				lista_acciones.remove(0);
				return accion;
			}
		}
		//Si hay gemas fáciles vamos a por ellas
		else if(!this.lista_gemas_faciles.isEmpty()) {
			return actGemasFaciles(stateObs, elapsedTimer);
		}
		//Si no hay gemas fáciles y si gemas de piedras entonces vamos a por ellas
		else if(this.lista_gemas_faciles.isEmpty() && !this.lista_gemas_faciles_piedras.isEmpty()) {
			return actGemasPiedras(stateObs, elapsedTimer);
		}
		//Si no hay ni gemas fáciles ni de piedras vamos a por las de los bichos
		else if(this.lista_gemas_faciles.isEmpty() && this.lista_gemas_faciles_piedras.isEmpty() && !this.lista_gemas_faciles_bichos.isEmpty())
			return actGemasBichos(stateObs, elapsedTimer);
		//Si no hay de ningún tipo nos vamos al portal
		else{
			if(lista_acciones.isEmpty()) {
				resolutor.reset();
				lista_acciones = resolutor.salirPortal(elapsedTimer);
			}
			else {
				Types.ACTIONS accion = lista_acciones.get(0);
				lista_acciones.remove(0);
				return accion;
			}
		}
		return Types.ACTIONS.ACTION_NIL;
    	
    }
	
	/**
	 * Función que controla el comportamiento cuando estamos yendo a por las gemas de los bichos
	 * @param stateObs Estado actual del mundo
	 * @param elapsedTimer Objeto para controlar el tiempo
	 * @return Devuelve una acción a realizar
	 */
	private ACTIONS actGemasBichos(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		//Posición actual del avatar
		int col_start = (int) Math.round(stateObs.getAvatarPosition().x / fescalaX);
    	int fila_start = (int) Math.round(stateObs.getAvatarPosition().y / fescalaY);
    	//Colocamos los parámetros sin contornos de bichos
    	resolutor.setParametros(stateObs, new HashSet<Vector2di>());
    	
    	//Si ya tenemos las gemas que queremos actualizamos el estado y señalamos que hemos acabado
    	if(stateObs.getAvatarResources().size()>0 && !acabado)
    		if(stateObs.getAvatarResources().get(6)==9) {
    			resolutor.reset();
    			acabado=true;
    			return Types.ACTIONS.ACTION_NIL;
    		}
    	
    	//Actualizamos el contador de bloqueado para ver si llevamos mucho tiempo persiguiendo una gema
    	if(lista_gemas_faciles_bichos.size()>0 && !acabado)
	    	if(gema_objetivo.equals(lista_gemas_faciles_bichos.get(0)))
	    		bloqueado+=1;
	    	else {
	    		gema_objetivo = lista_gemas_faciles_bichos.get(0);
	    		bloqueado=0;
	    	}
    	
    	//Si nos hemos quedado bloqueados entonces pasamos a la siguiente gema y colocamos esta al final
    	if(bloqueado>50) {
    		if(lista_gemas_faciles_bichos.size()>0) {
    			Gema gem = lista_gemas_faciles_bichos.get(0);
    			lista_gemas_faciles_bichos.remove(0);
    			resolutor.reset();
    			lista_gemas_faciles_bichos.add(gem);
    		}
    		bloqueado = 0;
    	}
    	
    	//Si hemos vaciado la lista de acciones actualizamos el estado
    	if(lista_acciones.size()==0) {
    		this.escapando_reactivo=false;
    		resolutor.reset();
    		escapando=false;
    	}
    	
    	//Si no tenemos acciones y quedan gemas vamos a por ellas
    	if(lista_acciones.size()==0 && lista_gemas_faciles_bichos.size()>0) {
    		//Si no hemos llegado a la gema calculamos el camino
    		if(col_start != lista_gemas_faciles_bichos.get(0).coordenadas.x || fila_start != lista_gemas_faciles_bichos.get(0).coordenadas.y) {    		
    			lista_acciones = resolutor.obtenCamino(lista_gemas_faciles_bichos.get(0).coordenadas.x, lista_gemas_faciles_bichos.get(0).coordenadas.y,elapsedTimer,false);
    		}
    		//Si hemos llegado la eliminamos
    		else {
    			lista_gemas_faciles_bichos.remove(0);
    			resolutor.reset();
    		}
    	}
    	//Si tenemos acciones por hacer
    	if(lista_acciones.size()>0) {
    		//Vemos si tenemos peligro de bichos y estamos huyendo
    		if(this.escapando && hayPeligroBicho(stateObs, lista_acciones) && !this.escapando_reactivo) {
    			this.escapando_reactivo = true;
    			//Escapamos de forma reactiva
    			lista_acciones = escapaReactivo(stateObs, lista_acciones);
    		}
    		//Vemos si tenemos peligro de bichos y no estamos huyendo
    		else if(hayPeligroBicho(stateObs, lista_acciones) && !this.acabado) {
    			escapando=true;
    			//Escapamos de forma deliberativa a alguno de los 3 puntos que tenemos
    			lista_acciones = esquivaBicho(stateObs,lista_acciones, elapsedTimer);
    		}
    		//Si la lista de acciones es vacía devolvemos nil
    		if(lista_acciones.size()==0)
    			return Types.ACTIONS.ACTION_NIL;
    		//Si no sacamos la siguiente accion y la ejecutamos
	    	Types.ACTIONS accion = lista_acciones.get(0);
	    	stateObs.advance(accion);
	    	lista_acciones.remove(0);
	    	return(accion);
    	}
    	stateObs.advance(Types.ACTIONS.ACTION_NIL);
    	return Types.ACTIONS.ACTION_NIL;
	}

	/**
	 * Función que calcula la siguiente acción cuando estamos yendo a por gemas de piedras 
	 * @param stateObs Estado actual del mundo
	 * @param elapsedTimer Objeto para controlar el tiempo
	 * @return Devuelve la siguiente acción a realizar si estamos persiguiendo gemas de piedras
	 */
	private Types.ACTIONS actGemasPiedras(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		//Posición actual del avatar
		int col_start = (int) Math.round(stateObs.getAvatarPosition().x / fescalaX);
    	int fila_start = (int) Math.round(stateObs.getAvatarPosition().y / fescalaY);
    	//Colocamos los parámetros del resolutor
    	resolutor.setParametros(stateObs, this.contornos_bichos);
    	
    	//Si ya tenemos las gemas actualizamos el estado y señalamos que hemos ganado
    	if(stateObs.getAvatarResources().size()>0 && !acabado)
    		if(stateObs.getAvatarResources().get(6)==9) {
    			resolutor.reset();
    			acabado=true;
    			return Types.ACTIONS.ACTION_NIL;
    		}
    	
    	//Controlamos si llevamos mucho tiempo persiguiendo una gema
    	if(lista_gemas_faciles_piedras.size()>0 && !acabado)
	    	if(gema_objetivo.equals(lista_gemas_faciles_piedras.get(0)))
	    		bloqueado+=1;
	    	else {
	    		gema_objetivo = lista_gemas_faciles_piedras.get(0);
	    		bloqueado=0;
	    	}
    	
    	//Si nos hemos quedado bloqueados
    	if(bloqueado>100) {
    		if(lista_gemas_faciles_piedras.size()>0) {
    			Gema gem = lista_gemas_faciles_piedras.get(0);
    			lista_gemas_faciles_piedras.remove(0);
    			resolutor.reset();
    			//Si somos capaces de seguir la rutina para quitar la gema entonces la ponemos al final, si no, no la añadimos de nuevo
    			if(resolutor.obtenCamino(gem.posiciones_a_ir.get(0).x, gem.posiciones_a_ir.get(0).y, elapsedTimer, false).get(0)!=Types.ACTIONS.ACTION_NIL)
    				lista_gemas_faciles_piedras.add(gem);
    		}
    		bloqueado = 0;
    	}
    	//Si no tenemos acciones a realizar y nos quedan gemas vamos a por ellas
    	if(lista_acciones.size()==0 && lista_gemas_faciles_piedras.size()>0) {  
    		//Si hemos llegado a la gema la quitamos de la lista
    		if(lista_gemas_faciles_piedras.get(0).posiciones_a_ir.size()==0 && col_start == lista_gemas_faciles_piedras.get(0).coordenadas.x && fila_start == lista_gemas_faciles_piedras.get(0).coordenadas.y) {    		
    			lista_gemas_faciles = obtenListaGemasFacilesSinBichos(stateObs, elapsedTimer);
    			lista_gemas_faciles_piedras.remove(0);
    			resolutor.reset();
    		}
    		//Si no vamos a ver cuál es la siguiente posición a ir
    		else {
    			Gema gema = lista_gemas_faciles_piedras.get(0);
    			//Si hemos llegado a la siguiente posición a ir
    			if(col_start==gema.posiciones_a_ir.get(0).x && fila_start==gema.posiciones_a_ir.get(0).y) {
    				int tam = lista_gemas_faciles_piedras.get(0).posiciones_a_ir.size();
    				lista_gemas_faciles_piedras.get(0).posiciones_a_ir.remove(0);
    				//La última posición a ir es la propia gema, por lo tanto si quedan más de una posición a la que ir
    				if(tam>1) {
    					//Añadimos la acción USE o bien corregimos la orientación si es necesario
    					lista_acciones.add(orientaAvatar(lista_gemas_faciles_piedras.get(0).orientaciones.get(0), stateObs));
    					if(gema.tipo_gema_piedra==0)
    						lista_acciones.add(Types.ACTIONS.ACTION_USE);
    					else if(gema.tipo_gema_piedra==1) {
    						if(orientaAvatar(lista_gemas_faciles_piedras.get(0).orientaciones.get(0), stateObs)!=Types.ACTIONS.ACTION_NIL)
    							lista_acciones.add(orientaAvatar(lista_gemas_faciles_piedras.get(0).orientaciones.get(0), stateObs));
    						else
    							lista_acciones.add(mismoMovimiento(stateObs));
    					}
    				}
    				lista_gemas_faciles_piedras.get(0).orientaciones.remove(0);
    				resolutor.reset();
    			}
    			//Si no hemos llegado
    			else {
    				//Avanzamos el mundo 20 nils
    				for(int i = 0; i< 20; ++i)
    					stateObs.advance(Types.ACTIONS.ACTION_NIL);
    				//Vamos a la siguiente posición
    				lista_acciones = resolutor.obtenCamino(gema.posiciones_a_ir.get(0).x, gema.posiciones_a_ir.get(0).y, elapsedTimer, false);
    				//Si solo queda una posición (la propia de la gema) esperamos 20 nils
    				if(gema.posiciones_a_ir.size()==1)
    					for(int i = 0; i<20; ++i)
    						lista_acciones.add(0, Types.ACTIONS.ACTION_NIL);
    			}
    		}
    	}
    	
    	//Si tenemos acciones por hacer las hacemos
    	if(lista_acciones.size()>0) {
	    	Types.ACTIONS accion = lista_acciones.get(0);
	    	stateObs.advance(accion);
	    	lista_acciones.remove(0);
	    	return(accion);
    	}
    	stateObs.advance(Types.ACTIONS.ACTION_NIL);
    	return Types.ACTIONS.ACTION_NIL;
    	
    	
	}

	/**
	 * Función que devuelve la misma acción que realizó anteriormente basándose en la orientación del avatar
	 * @param stateObs Estado actual del mundo
	 * @return Devuelve una acción
	 */
	private ACTIONS mismoMovimiento(StateObservation stateObs) {
		//Calculamos la orientación
		Vector2d orientacion_avatar = stateObs.getAvatarOrientation();
		//En función de la orientación devolvemos la acción que corresponda
		if(orientacion_avatar.equals(new Vector2d(1.0,0.0)))
			return Types.ACTIONS.ACTION_RIGHT;
		else if(orientacion_avatar.equals(new Vector2d(-1.0,0.0)))
			return Types.ACTIONS.ACTION_LEFT;
		else if(orientacion_avatar.equals(new Vector2d(0.0,1.0)))
			return Types.ACTIONS.ACTION_DOWN;
		else
			return Types.ACTIONS.ACTION_UP;
	}

	/**
	 * Función que devuelve una acción para cambiar la orientación del avatar a la deseada
	 * @param orientacion_final Orientación que queremos que tenga el avatar
	 * @param stateObs Estado del mundo
	 * @return Devuelve una acción
	 */
	private ACTIONS orientaAvatar(Vector2d orientacion_final, StateObservation stateObs) {
		//Calculamos la orientación actual
		Vector2d orientacion_avatar = stateObs.getAvatarOrientation();
		//Si es la misma devolvemos nil
		if(orientacion_avatar.equals(orientacion_final))
			return Types.ACTIONS.ACTION_NIL;
		//En caso contrario ajustamos el movimiento
		else {
			if(orientacion_final.equals(new Vector2d(1.0,0.0)))
				return Types.ACTIONS.ACTION_RIGHT;
			if(orientacion_final.equals(new Vector2d(-1.0,0.0)))
				return Types.ACTIONS.ACTION_LEFT;
			if(orientacion_final.equals(new Vector2d(0.0,1.0)))
				return Types.ACTIONS.ACTION_DOWN;
			if(orientacion_final.equals(new Vector2d(0.0,-1.0)))
				return Types.ACTIONS.ACTION_UP;
		}
		return Types.ACTIONS.ACTION_NIL;
	}

	/**
	 * Función que controla el comportamiento cuando estamos yendo a por gemas fáciles
	 * @param stateObs Estado actual del mundo
	 * @param elapsedTimer Objeto para controlar el tiempo
	 * @return Devuelve la siguiente acción a realizar
	 */
	private Types.ACTIONS actGemasFaciles(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		//Posición actual del avatar
    	int col_start = (int) Math.round(stateObs.getAvatarPosition().x / fescalaX);
    	int fila_start = (int) Math.round(stateObs.getAvatarPosition().y / fescalaY);
    	//Colocamos los parámetros actuales
    	resolutor.setParametros(stateObs, this.contornos_bichos);
    	
    	//Si ya tenemos las gemas actualizamos el estado y señalamos que hemos acabado
    	if(stateObs.getAvatarResources().size()>0 && !acabado)
    		if(stateObs.getAvatarResources().get(6)==9) {
    			resolutor.reset();
    			acabado=true;
    			return Types.ACTIONS.ACTION_NIL;
    		}
    	
    	//Comprobamos si nos estamos quedando bloqueados
    	if(lista_gemas_faciles.size()>0 && !acabado)
	    	if(gema_objetivo.equals(lista_gemas_faciles.get(0)))
	    		bloqueado+=1;
	    	else {
	    		gema_objetivo = lista_gemas_faciles.get(0);
	    		bloqueado=0;
	    	}
    	
    	//Si nos bloqueamos
    	if(bloqueado>50) {
    		if(lista_gemas_faciles.size()>0) {
    			Gema gem = lista_gemas_faciles.get(0);
    			lista_gemas_faciles.remove(0);
    			resolutor.reset();
    			//Comprobamos si la gema es accesible y si no lo es no la añadimos
    			if(resolutor.obtenCamino(gem.coordenadas.x, gem.coordenadas.y, elapsedTimer, false).get(0)!=Types.ACTIONS.ACTION_NIL)
    				lista_gemas_faciles.add(gem);
    		}
    		bloqueado = 0;
    	}
    	
    	//Si ya no tenemos más acciones actualizamos el estado
    	if(lista_acciones.size()==0) {
    		this.escapando_reactivo=false;
    		resolutor.reset();
    		escapando=false;
    	}
    	
    	//Si quedan gemas
    	if(lista_acciones.size()==0 && lista_gemas_faciles.size()>0) {
    		//Si no estamos en la gema vamos a por ella
    		if(col_start != lista_gemas_faciles.get(0).coordenadas.x || fila_start != lista_gemas_faciles.get(0).coordenadas.y) {    		
    			lista_acciones = resolutor.obtenCamino(lista_gemas_faciles.get(0).coordenadas.x, lista_gemas_faciles.get(0).coordenadas.y,elapsedTimer,false);
    		}
    		//Si estamos la quitamos
    		else {
    			if(lista_gemas_faciles_bichos.contains(lista_gemas_faciles.get(0)))
    				lista_gemas_faciles_bichos.remove(lista_gemas_faciles.get(0));
    			lista_gemas_faciles.remove(0);
    			resolutor.reset();
    		}
    	}
    	
    	//Si quedan acciones por hacer las hacemos
    	if(lista_acciones.size()>0) {
    		if(lista_acciones.size()==0)
    			return Types.ACTIONS.ACTION_NIL;
	    	Types.ACTIONS accion = lista_acciones.get(0);
	    	stateObs.advance(accion);
	    	lista_acciones.remove(0);
	    	return(accion);
    	}
    	stateObs.advance(Types.ACTIONS.ACTION_NIL);
    	return Types.ACTIONS.ACTION_NIL;
	}
    
	/**
	 * Método que devuelve una lista de acciones para escapar de forma reactiva de un bicho
	 * @param obs Estado actual del mundo
	 * @param lista_acciones2 Lista de acciones previas que tenía el avatar
	 * @return Devuelve una nueva lista de acciones para esquivar al bicho
	 */
    private ArrayList<ACTIONS> escapaReactivo(StateObservation obs, ArrayList<ACTIONS> lista_acciones2) {
    	//Obtenemos la matriz del mundo
    	ArrayList<Observation>[][] mundo = obs.getObservationGrid();
    	ArrayList<Types.ACTIONS> lista_acciones = new ArrayList<Types.ACTIONS>();
    	//Obtenemos la posición del avatar
    	int col_start = (int) Math.round(obs.getAvatarPosition().x / fescalaX);
    	int fila_start = (int) Math.round(obs.getAvatarPosition().y / fescalaY);
    	
    	//Si la última acción era hacia la izquierda y hemos entrado en peligro hacemos la acción contraria si podemos o si no cualquier otra
    	if(lista_acciones2.get(0)==Types.ACTIONS.ACTION_LEFT) {
			if(obs.getAvatarOrientation().x==1.0) {
				if(isAccesible(mundo, col_start+1, fila_start)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_RIGHT);
					lista_acciones.add(0,Types.ACTIONS.ACTION_RIGHT);
				}
				else if(isAccesible(mundo, col_start, fila_start+1)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_DOWN);
					lista_acciones.add(0,Types.ACTIONS.ACTION_DOWN);
				}
				else if(isAccesible(mundo, col_start, fila_start-1)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_UP);
					lista_acciones.add(0,Types.ACTIONS.ACTION_UP);
				}
			}
			else {
				if(isAccesible(mundo, col_start+1, fila_start)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_RIGHT);
					lista_acciones.add(0,Types.ACTIONS.ACTION_RIGHT);
				}
				else if(isAccesible(mundo, col_start, fila_start+1)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_DOWN);
					lista_acciones.add(0,Types.ACTIONS.ACTION_DOWN);
				}
				else if(isAccesible(mundo, col_start, fila_start-1)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_UP);
					lista_acciones.add(0,Types.ACTIONS.ACTION_UP);
				}
			}
    	}
    	//Igual si nos estabamos moviendo hacia la derecha
		else if(lista_acciones2.get(0)==Types.ACTIONS.ACTION_RIGHT) {
			if(obs.getAvatarOrientation().x==-1.0) {
				if(isAccesible(mundo, col_start-1, fila_start)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_LEFT);
					lista_acciones.add(0,Types.ACTIONS.ACTION_LEFT);
				}
				else if(isAccesible(mundo, col_start, fila_start-1)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_UP);
					lista_acciones.add(0,Types.ACTIONS.ACTION_UP);
				}
				else if(isAccesible(mundo, col_start, fila_start+1)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_DOWN);
					lista_acciones.add(0,Types.ACTIONS.ACTION_DOWN);
				}
			}
			else {
				if(isAccesible(mundo, col_start-1, fila_start)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_LEFT);
					lista_acciones.add(0,Types.ACTIONS.ACTION_LEFT);
				}
				else if(isAccesible(mundo, col_start, fila_start-1)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_UP);
					lista_acciones.add(0,Types.ACTIONS.ACTION_UP);
				}
				else if(isAccesible(mundo, col_start, fila_start+1)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_DOWN);
					lista_acciones.add(0,Types.ACTIONS.ACTION_DOWN);
				}
			}
		}
    	//Igual si nos estabamos moviendo hacia arriba
		else if(lista_acciones2.get(0)==Types.ACTIONS.ACTION_UP) {
			if(obs.getAvatarOrientation().y==1.0) {
				if(isAccesible(mundo, col_start, fila_start+1)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_DOWN);
					lista_acciones.add(0,Types.ACTIONS.ACTION_DOWN);
				}
				else if(isAccesible(mundo, col_start-1, fila_start)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_LEFT);
					lista_acciones.add(0,Types.ACTIONS.ACTION_LEFT);
				}
				else if(isAccesible(mundo, col_start+1, fila_start)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_RIGHT);
					lista_acciones.add(0,Types.ACTIONS.ACTION_RIGHT);
				}
			}
			else {
				if(isAccesible(mundo, col_start, fila_start+1)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_DOWN);
					lista_acciones.add(0,Types.ACTIONS.ACTION_DOWN);
				}
				else if(isAccesible(mundo, col_start-1, fila_start)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_LEFT);
					lista_acciones.add(0,Types.ACTIONS.ACTION_LEFT);
				}
				else if(isAccesible(mundo, col_start+1, fila_start)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_RIGHT);
					lista_acciones.add(0,Types.ACTIONS.ACTION_RIGHT);
				}
			}
		}
    	//Igual si nos estabamos moviendo hacia abajo
		else {
			if(obs.getAvatarOrientation().y==-1.0) {
				if(isAccesible(mundo, col_start, fila_start-1)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_UP);
					lista_acciones.add(0,Types.ACTIONS.ACTION_UP);
				}
				else if(isAccesible(mundo, col_start-1, fila_start)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_LEFT);
					lista_acciones.add(0,Types.ACTIONS.ACTION_LEFT);
				}
				else if(isAccesible(mundo, col_start+1, fila_start)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_RIGHT);
					lista_acciones.add(0,Types.ACTIONS.ACTION_RIGHT);
				}
			}
			else {
				if(isAccesible(mundo, col_start, fila_start-1)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_UP);
					lista_acciones.add(0,Types.ACTIONS.ACTION_UP);
				}	
				else if(isAccesible(mundo, col_start-1, fila_start)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_LEFT);
					lista_acciones.add(0,Types.ACTIONS.ACTION_LEFT);
				}
				else if(isAccesible(mundo, col_start+1, fila_start)) {
					lista_acciones.add(0,Types.ACTIONS.ACTION_RIGHT);
					lista_acciones.add(0,Types.ACTIONS.ACTION_RIGHT);
				}
			}
		}
    	//Añadimos además un giro para esquivar al bicho o al menos aumentar la distancia manhattan entre el avatar y el bicho
    	if(lista_acciones.size()>0) {
    	if(lista_acciones.get(0)==Types.ACTIONS.ACTION_RIGHT){
    		if(isAccesible(mundo, col_start+1, fila_start-1)) {
	    		lista_acciones.add(Types.ACTIONS.ACTION_UP);
	    		lista_acciones.add(Types.ACTIONS.ACTION_UP);
    		}
    	}
    	else if(lista_acciones.get(0)==Types.ACTIONS.ACTION_UP) {
    		if(isAccesible(mundo, col_start-1, fila_start-1)) {
    			lista_acciones.add(Types.ACTIONS.ACTION_LEFT);
    			lista_acciones.add(Types.ACTIONS.ACTION_LEFT);
    		}
    	}
    	else if(lista_acciones.get(0)==Types.ACTIONS.ACTION_LEFT) {
    		if(isAccesible(mundo, col_start-1, fila_start+1)) {
				lista_acciones.add(Types.ACTIONS.ACTION_DOWN);
				lista_acciones.add(Types.ACTIONS.ACTION_DOWN);
    		}
	    }
    	else {
	    	if(isAccesible(mundo, col_start+1, fila_start-1)) {	
	    		lista_acciones.add(Types.ACTIONS.ACTION_RIGHT);
	    		lista_acciones.add(Types.ACTIONS.ACTION_RIGHT);
	    	}
    	}
    	}
		return lista_acciones;
	}
    
    /**
     * Función que indica si una casilla es accesible sin comprobar la condición de que tenga una piedra arriba
     * @param mundo Matriz del mundo
     * @param columna Columna de la posición a comprobar
     * @param fila Fila de la posición a comprobar
     * @return Devuelve un valor booleano que indica si la posición es o no accesible.
     */
    private boolean isAccesibleSinPiedraArriba(ArrayList<Observation>[][] mundo, int columna, int fila) {
    	if(columna<0 || columna>=this.ancho || fila<0 || fila>=this.alto)
			return false;
    	if(mundo[columna][fila].size()>0)
			return mundo[columna][fila].get(0).itype==4 || mundo[columna][fila].get(0).itype==6;
    	else
    		return true;
    }

    /**
     * Función que indica si una posición es accesible
     * @param mundo Matriz del mundo
     * @param columna Columna de la posición a comprobar
     * @param fila Fila de la posición a comprobar
     * @return Devuelve un valor booleano que indica si la posición es accesible
     */
	private boolean isAccesible(ArrayList<Observation>[][] mundo, int columna, int fila) {
		if(columna<0 || columna>=this.ancho || fila<0 || fila>=this.alto)
			return false;
		// Si el nodo está vacío es accesible
		boolean vacio = mundo[columna][fila].size()==0;
		if(!vacio) {
			// Comprueba si hay un bicho
			boolean bicho = mundo[columna][fila].get(0).itype==11 || mundo[columna][fila].get(0).itype==10;
			// Comprueba si hay un muro
			boolean muro = mundo[columna][fila].get(0).itype==0;
			// Comprueba si hay una piedra
			boolean piedra = mundo[columna][fila].get(0).itype==7;
			// Comprueba si la casilla tiene una piedra encima
			boolean piedra_arriba = false;
			if(fila>0 && mundo[columna][fila-1].size()>0)
				piedra_arriba = mundo[columna][fila-1].get(0).itype==7;
			// Comprueba si hay un monstruo arriba, abajo, a la izquierda o a la derecha
			boolean monstruo_alrededores = false;
			if(fila-1>=0)
				if(mundo[columna][fila-1].size()>0)
					monstruo_alrededores = monstruo_alrededores || mundo[columna][fila-1].get(0).itype==11 || mundo[columna][fila-1].get(0).itype==10;
			if(fila+1<alto)
				if(mundo[columna][fila+1].size()>0)
					monstruo_alrededores = monstruo_alrededores || mundo[columna][fila+1].get(0).itype==11 || mundo[columna][fila+1].get(0).itype==10;
			if(columna-1>=0)
				if(mundo[columna-1][fila].size()>0)
					monstruo_alrededores = monstruo_alrededores || mundo[columna-1][fila].get(0).itype==11 || mundo[columna-1][fila].get(0).itype==10;
			if(columna+1<ancho)
				if(mundo[columna+1][fila].size()>0)
					monstruo_alrededores = monstruo_alrededores || mundo[columna+1][fila].get(0).itype==11 || mundo[columna+1][fila].get(0).itype==10;
			// Si no hay un bicho ni un muro ni una piedra ni una piedra encima entonces es una casilla accesible
			boolean condicion = !bicho && !muro && !piedra && !piedra_arriba && !monstruo_alrededores;
			return condicion;
		}
		return true;
	}
    
    /**
     * Función que escapa de un bicho de forma deliberativa
     * @param obs Estado actual del mundo
     * @param lista_acciones2 Lista de acciones previas
     * @param timer Objeto para controlar el paso del tiempo
     * @return Devuelve una lista de acciones para escapar del bicho
     */
    private ArrayList<ACTIONS> esquivaBicho(StateObservation obs,ArrayList<ACTIONS> lista_acciones2, ElapsedCpuTimer timer) {
    	//Mediante esta variable controlamos a qué punto huimos
    	this.veces_escapadas+=1;
    	ArrayList<Types.ACTIONS> lista_acciones = new ArrayList<Types.ACTIONS>();
    	//Posición del avatar
    	int col_start = (int) Math.round(obs.getAvatarPosition().x / fescalaX);
    	int fila_start = (int) Math.round(obs.getAvatarPosition().y / fescalaY);
    	
    	//Añadimos los puntos de huida que son el porta, la posición inicial y el punto más alejado a estos dos que sea accesible 
    	ArrayList<Vector2di> puntos_huida = new ArrayList<Vector2di>();
    	puntos_huida.add(new Vector2di(this.col_inicial, this.fila_inicial));
    	puntos_huida.add(new Vector2di(this.col_portal, this.fila_portal));
    	puntos_huida.add(new Vector2di(this.tercer_punto_col, this.tercer_punto_fila));
    	
    	
    	resolutor.reset();
    	resolutor.setParametros(obs, this.contornos_bichos);
    	
    	boolean aleatorio = Math.random() < 0.25;
    	
    	//Si se da la condición tomamos aquella casilla que está más lejos de todos los bichos
    	if(aleatorio) {
	    	ArrayList<Integer> minima_distancia_bicho = new ArrayList<Integer>();
	    	    	
	    	for(Vector2di punto : puntos_huida) {
	    		int minima_distancia = Integer.MAX_VALUE;
	    		for(ArrayList<Observation> ob : obs.getNPCPositions(new Vector2d(col_start, fila_start))) {
	    			int col_bicho =(int) Math.round(ob.get(0).position.x / fescalaX);
	    			int fila_bicho =(int) Math.round(ob.get(0).position.y / fescalaY);
	    			if(distanciaManhattan(col_bicho, fila_bicho, punto.x, punto.y)<minima_distancia)
	    				minima_distancia = distanciaManhattan(col_bicho, fila_bicho, punto.x, punto.y);
	    		}
	    		minima_distancia_bicho.add(minima_distancia);
	    	}
	    	
	    	int maximo = 0;
	    	for(int i = 0; i < minima_distancia_bicho.size();++i)
	    		if(minima_distancia_bicho.get(i)>minima_distancia_bicho.get(maximo))
	    			maximo = i;
	    	
	    	lista_acciones = resolutor.obtenCamino(puntos_huida.get(maximo).x, puntos_huida.get(maximo).y, timer, false);
    	}
    	//En caso contrario tomamos alguna de las 3 posiciones calculadas (portal, inicial y tercer punto)
    	else {
			if(this.veces_escapadas%3==0)
				lista_acciones = resolutor.obtenCamino(this.col_inicial, this.fila_inicial, timer, false);
			else if(this.veces_escapadas%3==1)
				lista_acciones = resolutor.obtenCamino(this.tercer_punto_col, this.tercer_punto_fila, timer, false);
			else
				lista_acciones = resolutor.obtenCamino(this.col_portal, this.fila_portal, timer, false);
    	}
    	return lista_acciones;
	}
    
    /**
     * Función que comprueba si en una casilla no hay un muro ni una piedra
     * @param col Columna de la posición a comprobar
     * @param fila Fila de la posición a comprobar
     * @param mundo Matriz con el estado del mundo
     * @return Devuelve un valor booleano indicando si no hay ni muros ni piedras
     */
    public boolean noHayMuroPiedra(int col, int fila, ArrayList<Observation>[][] mundo) {
    	boolean no_muro_piedra = false;
    	if(col>=0 && fila>=0 && col<ancho && fila<alto) {
    		if(mundo[col][fila].size()>0) {
    			Observation ob = mundo[col][fila].get(0);
    			if(ob.itype!=7 && ob.itype!=0)
    				no_muro_piedra=true;
    		}
    		else
    			no_muro_piedra=true;
    	}
    	return no_muro_piedra;
    }

    /**
     * Función que implementa la detección del peligro por proximidad a un bicho
     * @param obs Estado actual del mundo
     * @param lista_acciones Lista previa de acciones
     * @return Devuelve un valor booleano indicando si hay peligro por un bicho o no
     */
    public boolean hayPeligroBicho(StateObservation obs, ArrayList<Types.ACTIONS> lista_acciones) {
    	//Posición actual del avatar
    	int col_start = (int) Math.round(obs.getAvatarPosition().x / fescalaX);
    	int fila_start = (int) Math.round(obs.getAvatarPosition().y / fescalaY);
    	//Matriz del mundo
    	ArrayList<Observation>[][] mundo = obs.getObservationGrid();
    	
    	boolean hay_bicho = false;
    	ArrayList<Vector2di> posiciones = new ArrayList<Vector2di>();
    	
    	Types.ACTIONS accion = lista_acciones.get(0);
    	//Si la siguiente acción es hacia abajo comprobamos el cono como hemos indicado en la documentación hacia abajo
    	if(accion == Types.ACTIONS.ACTION_DOWN) {
    		
    		if(noHayMuroPiedra(col_start, fila_start-1, mundo))
    			posiciones.add(new Vector2di(col_start, fila_start-1));
    		
    		if(noHayMuroPiedra(col_start, fila_start+1, mundo))
    			posiciones.add(new Vector2di(col_start, fila_start+1));
    		if(noHayMuroPiedra(col_start-1, fila_start+1, mundo))
    			posiciones.add(new Vector2di(col_start-1, fila_start+1));
    		if(noHayMuroPiedra(col_start+1, fila_start+1, mundo))
    			posiciones.add(new Vector2di(col_start+1, fila_start+1));
    		
    		if(noHayMuroPiedra(col_start-1, fila_start, mundo))
    			posiciones.add(new Vector2di(col_start-1, fila_start));
    		if(noHayMuroPiedra(col_start+1, fila_start, mundo))
    			posiciones.add(new Vector2di(col_start+1, fila_start));
    		
    		//Comprobamos primero las casillas de la izquierda y derecha
    		if(posiciones.contains(new Vector2di(col_start-1, fila_start)) && noHayMuroPiedra(col_start-2, fila_start, mundo))
    			posiciones.add(new Vector2di(col_start-2, fila_start));
    		if(posiciones.contains(new Vector2di(col_start+1, fila_start)) && noHayMuroPiedra(col_start+2, fila_start, mundo))
    			posiciones.add(new Vector2di(col_start+2, fila_start));
    		
    		//Compruebo la primera y segunda diagonal
    		Vector2di cmenosuno_f = new Vector2di(col_start-1, fila_start);
    		Vector2di c_fmasuno = new Vector2di(col_start, fila_start+1);
    		Vector2di cmenosuno_fmasuno = new Vector2di(col_start-1, fila_start+1);
    		Vector2di cmasuno_f = new Vector2di(col_start+1, fila_start);
    		Vector2di cmasuno_fmasuno = new Vector2di(col_start+1, fila_start+1);
    		Vector2di c_fmasdos = new Vector2di(col_start, fila_start+2);
    		
    		if(((posiciones.contains(cmenosuno_f) && posiciones.contains(cmenosuno_fmasuno)) || (posiciones.contains(cmenosuno_fmasuno) && posiciones.contains(c_fmasuno))) && noHayMuroPiedra(col_start-2, fila_start+1, mundo))
    			posiciones.add(new Vector2di(col_start-2, fila_start+1));
    		if(((posiciones.contains(cmasuno_f) && posiciones.contains(cmasuno_fmasuno)) || (posiciones.contains(cmasuno_fmasuno) && posiciones.contains(c_fmasuno))) && noHayMuroPiedra(col_start+2, fila_start+1, mundo))
    			posiciones.add(new Vector2di(col_start+2, fila_start+1));
    		
    		//Comprobamos dos posiciones enfrente
    		if(noHayMuroPiedra(col_start, fila_start+2, mundo) && posiciones.contains(new Vector2di(col_start, fila_start+1)))
    			posiciones.add(new Vector2di(col_start, fila_start+2));
 
    		//Comprobamos la segunda diagonal
    		if(noHayMuroPiedra(col_start-1, fila_start+2, mundo) && ((posiciones.contains(cmenosuno_fmasuno) && posiciones.contains(cmenosuno_f)) || (posiciones.contains(cmenosuno_fmasuno) && posiciones.contains(c_fmasuno)) || (posiciones.contains(c_fmasdos) && posiciones.contains(c_fmasuno))))
    			posiciones.add(new Vector2di(col_start-1, fila_start+2));
    		if(noHayMuroPiedra(col_start+1, fila_start+2, mundo) && ((posiciones.contains(cmasuno_fmasuno) && posiciones.contains(c_fmasuno)) || (posiciones.contains(cmasuno_fmasuno) && posiciones.contains(cmasuno_f)) || (posiciones.contains(c_fmasdos) && posiciones.contains(c_fmasuno))))
    			posiciones.add(new Vector2di(col_start+1, fila_start+2));
    		
    		//Comprobamos dos posiciones tres posiciones enfrente
    		if(noHayMuroPiedra(col_start, fila_start+3, mundo) && posiciones.contains(new Vector2di(col_start, fila_start+2)))
    			posiciones.add(new Vector2di(col_start, fila_start+3));
    		
    	}
    	//Si la siguiente acción es hacia arriba comprobamos el cono como hemos indicado en la documentación hacia arriba
    	else if(accion == Types.ACTIONS.ACTION_UP) {
    		if(noHayMuroPiedra(col_start, fila_start+1, mundo))
    			posiciones.add(new Vector2di(col_start, fila_start+1));
    		
    		if(noHayMuroPiedra(col_start, fila_start-1, mundo))
    			posiciones.add(new Vector2di(col_start, fila_start-1));
    		if(noHayMuroPiedra(col_start-1, fila_start-1, mundo))
    			posiciones.add(new Vector2di(col_start-1, fila_start-1));
    		if(noHayMuroPiedra(col_start+1, fila_start-1, mundo))
    			posiciones.add(new Vector2di(col_start+1, fila_start-1));
    		
    		if(noHayMuroPiedra(col_start-1, fila_start, mundo))
    			posiciones.add(new Vector2di(col_start-1, fila_start));
    		if(noHayMuroPiedra(col_start+1, fila_start, mundo))
    			posiciones.add(new Vector2di(col_start+1, fila_start));
    		
    		//Comprobamos primero las casillas de la izquierda y derecha
    		if(posiciones.contains(new Vector2di(col_start-1, fila_start)) && noHayMuroPiedra(col_start-2, fila_start, mundo))
    			posiciones.add(new Vector2di(col_start-2, fila_start));
    		if(posiciones.contains(new Vector2di(col_start+1, fila_start)) && noHayMuroPiedra(col_start+2, fila_start, mundo))
    			posiciones.add(new Vector2di(col_start+2, fila_start));
    		
    		//Compruebo la primera y segunda diagonal
    		Vector2di cmenosuno_f = new Vector2di(col_start-1, fila_start);
    		Vector2di c_fmenosuno = new Vector2di(col_start, fila_start-1);
    		Vector2di cmenosuno_fmenosuno = new Vector2di(col_start-1, fila_start-1);
    		Vector2di cmasuno_f = new Vector2di(col_start+1, fila_start);
    		Vector2di cmasuno_fmenosuno = new Vector2di(col_start+1, fila_start-1);
    		Vector2di c_fmenosdos = new Vector2di(col_start, fila_start-2);
    		
    		if(((posiciones.contains(cmenosuno_f) && posiciones.contains(cmenosuno_fmenosuno)) || (posiciones.contains(cmenosuno_fmenosuno) && posiciones.contains(c_fmenosuno))) && noHayMuroPiedra(col_start-2, fila_start-1, mundo))
    			posiciones.add(new Vector2di(col_start-2, fila_start-1));
    		if(((posiciones.contains(cmasuno_f) && posiciones.contains(cmasuno_fmenosuno)) || (posiciones.contains(cmasuno_fmenosuno) && posiciones.contains(c_fmenosuno))) && noHayMuroPiedra(col_start+2, fila_start-1, mundo))
    			posiciones.add(new Vector2di(col_start+2, fila_start-1));
    		
    		//Comprobamos dos posiciones enfrente
    		if(noHayMuroPiedra(col_start, fila_start-2, mundo) && posiciones.contains(new Vector2di(col_start, fila_start-1)))
    			posiciones.add(new Vector2di(col_start, fila_start-2));
 
    		//Comprobamos la segunda diagonal
    		if(noHayMuroPiedra(col_start-1, fila_start-2, mundo) && ((posiciones.contains(cmenosuno_fmenosuno) && posiciones.contains(cmenosuno_f)) || (posiciones.contains(cmenosuno_fmenosuno) && posiciones.contains(c_fmenosuno)) || (posiciones.contains(c_fmenosdos) && posiciones.contains(c_fmenosuno))))
    			posiciones.add(new Vector2di(col_start-1, fila_start-2));
    		if(noHayMuroPiedra(col_start+1, fila_start-2, mundo) && ((posiciones.contains(cmasuno_fmenosuno) && posiciones.contains(c_fmenosuno)) || (posiciones.contains(cmasuno_fmenosuno) && posiciones.contains(cmasuno_f)) || (posiciones.contains(c_fmenosdos) && posiciones.contains(c_fmenosuno))))
    			posiciones.add(new Vector2di(col_start+1, fila_start-2));
    		
    		//Comprobamos dos posiciones tres posiciones enfrente
    		if(noHayMuroPiedra(col_start, fila_start-3, mundo) && posiciones.contains(new Vector2di(col_start, fila_start-2)))
    			posiciones.add(new Vector2di(col_start, fila_start-3));
    		
    	}
    	//Si la siguiente acción es hacia la izquierda comprobamos el cono como hemos indicado en la documentación hacia la izquierda
    	else if(accion == Types.ACTIONS.ACTION_LEFT) {
    		if(noHayMuroPiedra(col_start+1, fila_start, mundo))
    			posiciones.add(new Vector2di(col_start+1, fila_start));
    		
    		if(noHayMuroPiedra(col_start-1, fila_start, mundo))
    			posiciones.add(new Vector2di(col_start-1, fila_start));
    		if(noHayMuroPiedra(col_start-1, fila_start-1, mundo))
    			posiciones.add(new Vector2di(col_start-1, fila_start-1));
    		if(noHayMuroPiedra(col_start-1, fila_start+1, mundo))
    			posiciones.add(new Vector2di(col_start-1, fila_start+1));
    		
    		if(noHayMuroPiedra(col_start, fila_start-1, mundo))
    			posiciones.add(new Vector2di(col_start, fila_start-1));
    		if(noHayMuroPiedra(col_start, fila_start+1, mundo))
    			posiciones.add(new Vector2di(col_start, fila_start+1));
    		
    		//Comprobamos primero las casillas de la izquierda y derecha
    		if(posiciones.contains(new Vector2di(col_start, fila_start-1)) && noHayMuroPiedra(col_start, fila_start-2, mundo))
    			posiciones.add(new Vector2di(col_start, fila_start-2));
    		if(posiciones.contains(new Vector2di(col_start, fila_start+1)) && noHayMuroPiedra(col_start, fila_start+2, mundo))
    			posiciones.add(new Vector2di(col_start, fila_start+2));
    		
    		//Compruebo la primera y segunda diagonal
    		Vector2di c_fmenosuno = new Vector2di(col_start, fila_start-1);
    		Vector2di cmenosuno_f = new Vector2di(col_start-1, fila_start);
    		Vector2di cmenosuno_fmenosuno = new Vector2di(col_start-1, fila_start-1);
    		Vector2di c_fmasuno = new Vector2di(col_start, fila_start+1);
    		Vector2di cmenosuno_fmasuno = new Vector2di(col_start-1, fila_start+1);
    		Vector2di cmenosdos_f = new Vector2di(col_start-2, fila_start);
    		
    		if(((posiciones.contains(c_fmenosuno) && posiciones.contains(cmenosuno_fmenosuno)) || (posiciones.contains(cmenosuno_fmenosuno) && posiciones.contains(cmenosuno_f))) && noHayMuroPiedra(col_start-1, fila_start-2, mundo))
    			posiciones.add(new Vector2di(col_start-1, fila_start-2));
    		if(((posiciones.contains(c_fmasuno) && posiciones.contains(cmenosuno_fmasuno)) || (posiciones.contains(cmenosuno_fmasuno) && posiciones.contains(cmenosuno_f))) && noHayMuroPiedra(col_start-1, fila_start+2, mundo))
    			posiciones.add(new Vector2di(col_start-1, fila_start+2));
    		
    		//Comprobamos dos posiciones enfrente
    		if(noHayMuroPiedra(col_start-2, fila_start, mundo) && posiciones.contains(new Vector2di(col_start-1, fila_start)))
    			posiciones.add(new Vector2di(col_start-2, fila_start));
 
    		//Comprobamos la segunda diagonal
    		if(noHayMuroPiedra(col_start-2, fila_start-1, mundo) && ((posiciones.contains(cmenosuno_fmenosuno) && posiciones.contains(c_fmenosuno)) || (posiciones.contains(cmenosuno_fmenosuno) && posiciones.contains(cmenosuno_f)) || (posiciones.contains(cmenosdos_f) && posiciones.contains(cmenosuno_f))))
    			posiciones.add(new Vector2di(col_start-2, fila_start-1));
    		if(noHayMuroPiedra(col_start-2, fila_start+1, mundo) && ((posiciones.contains(cmenosuno_fmasuno) && posiciones.contains(cmenosuno_f)) || (posiciones.contains(cmenosuno_fmasuno) && posiciones.contains(c_fmasuno)) || (posiciones.contains(cmenosdos_f) && posiciones.contains(cmenosuno_f))))
    			posiciones.add(new Vector2di(col_start-2, fila_start+1));
    		
    		//Comprobamos dos posiciones tres posiciones enfrente
    		if(noHayMuroPiedra(col_start-3, fila_start, mundo) && posiciones.contains(new Vector2di(col_start-2, fila_start)))
    			posiciones.add(new Vector2di(col_start-3, fila_start));
    	}
    	//Si la siguiente acción es hacia la derecha comprobamos el cono como hemos indicado en la documentación hacia la derecha
    	else if(accion == Types.ACTIONS.ACTION_RIGHT) {
    		if(noHayMuroPiedra(col_start-1, fila_start, mundo))
    			posiciones.add(new Vector2di(col_start-1, fila_start));
    		
    		if(noHayMuroPiedra(col_start+1, fila_start, mundo))
    			posiciones.add(new Vector2di(col_start+1, fila_start));
    		if(noHayMuroPiedra(col_start+1, fila_start-1, mundo))
    			posiciones.add(new Vector2di(col_start+1, fila_start-1));
    		if(noHayMuroPiedra(col_start+1, fila_start+1, mundo))
    			posiciones.add(new Vector2di(col_start+1, fila_start+1));
    		
    		if(noHayMuroPiedra(col_start, fila_start-1, mundo))
    			posiciones.add(new Vector2di(col_start, fila_start-1));
    		if(noHayMuroPiedra(col_start, fila_start+1, mundo))
    			posiciones.add(new Vector2di(col_start, fila_start+1));
    		
    		//Comprobamos primero las casillas de la izquierda y derecha
    		if(posiciones.contains(new Vector2di(col_start, fila_start-1)) && noHayMuroPiedra(col_start, fila_start-2, mundo))
    			posiciones.add(new Vector2di(col_start, fila_start-2));
    		if(posiciones.contains(new Vector2di(col_start, fila_start+1)) && noHayMuroPiedra(col_start, fila_start+2, mundo))
    			posiciones.add(new Vector2di(col_start, fila_start+2));
    		
    		//Compruebo la primera y segunda diagonal
    		Vector2di c_fmenosuno = new Vector2di(col_start, fila_start-1);
    		Vector2di cmasuno_f = new Vector2di(col_start+1, fila_start);
    		Vector2di cmasuno_fmenosuno = new Vector2di(col_start+1, fila_start-1);
    		Vector2di c_fmasuno = new Vector2di(col_start, fila_start+1);
    		Vector2di cmasuno_fmasuno = new Vector2di(col_start+1, fila_start+1);
    		Vector2di cmasdos_f = new Vector2di(col_start+2, fila_start);
    		
    		if(((posiciones.contains(c_fmenosuno) && posiciones.contains(cmasuno_fmenosuno)) || (posiciones.contains(cmasuno_fmenosuno) && posiciones.contains(cmasuno_f))) && noHayMuroPiedra(col_start+1, fila_start-2, mundo))
    			posiciones.add(new Vector2di(col_start+1, fila_start-2));
    		if(((posiciones.contains(c_fmasuno) && posiciones.contains(cmasuno_fmasuno)) || (posiciones.contains(cmasuno_fmasuno) && posiciones.contains(cmasuno_f))) && noHayMuroPiedra(col_start+1, fila_start+2, mundo))
    			posiciones.add(new Vector2di(col_start+1, fila_start+2));
    		
    		//Comprobamos dos posiciones enfrente
    		if(noHayMuroPiedra(col_start+2, fila_start, mundo) && posiciones.contains(new Vector2di(col_start+1, fila_start)))
    			posiciones.add(new Vector2di(col_start+2, fila_start));
 
    		//Comprobamos la segunda diagonal
    		if(noHayMuroPiedra(col_start+2, fila_start-1, mundo) && ((posiciones.contains(cmasuno_fmenosuno) && posiciones.contains(c_fmenosuno)) || (posiciones.contains(cmasuno_fmenosuno) && posiciones.contains(cmasuno_f)) || (posiciones.contains(cmasdos_f) && posiciones.contains(cmasuno_f))))
    			posiciones.add(new Vector2di(col_start+2, fila_start-1));
    		if(noHayMuroPiedra(col_start+2, fila_start+1, mundo) && ((posiciones.contains(cmasuno_fmasuno) && posiciones.contains(cmasuno_f)) || (posiciones.contains(cmasuno_fmasuno) && posiciones.contains(c_fmasuno)) || (posiciones.contains(cmasdos_f) && posiciones.contains(cmasuno_f))))
    			posiciones.add(new Vector2di(col_start+2, fila_start+1));
    		
    		//Comprobamos dos posiciones tres posiciones enfrente
    		if(noHayMuroPiedra(col_start+3, fila_start, mundo) && posiciones.contains(new Vector2di(col_start+2, fila_start)))
    			posiciones.add(new Vector2di(col_start+3, fila_start));
    	}
    	
    	//Para las posiciones que hemos añadido comprobamos si son un bicho
    	for(Vector2di pos : posiciones)
			if(pos.x>=0 && pos.x<ancho && pos.y>=0 && pos.y<alto)
				if(mundo[pos.x][pos.y].size()!=0) {
					hay_bicho = hay_bicho || mundo[pos.x][pos.y].get(0).itype==11 || mundo[pos.x][pos.y].get(0).itype==10;
				}
    	
    	//Si en alguna de las posiciones había un bicho entonces devolvemos true, si no false
    	return hay_bicho;
    }

    /**
     * Función que comprueba si una posición es una piedra
     * @param mundo Matriz del mundo
     * @param columna Columna de la posición a comprobar
     * @param fila Fila de la posición a comprobar
     * @return Devuelve un valor booleano que indica si hay una piedra en la posición o no
     */
    public boolean esPiedra(ArrayList<Observation>[][] mundo, int columna, int fila) {
    	if(columna<0 || fila<0 || columna>=ancho || fila>=alto)
    		return false;
    	if(mundo[columna][fila].size()==0)
    		return false;
    	else {
    		if(mundo[columna][fila].get(0).itype==7)
    			return true;
    	}
    	return false;
    }
}
