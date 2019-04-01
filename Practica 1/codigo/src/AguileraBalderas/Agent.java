/*package AguileraBalderas;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import java.util.Random;

public class Agent extends AbstractPlayer{

	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		
	}
	
	public void init(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		
	}
	
	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		Random aleatorio = new Random(System.currentTimeMillis());
		while(true) {
			int intAleatorio = aleatorio.nextInt(4);
			switch (intAleatorio) {
			case 0:
				return Types.ACTIONS.ACTION_LEFT;
			case 1:
				return Types.ACTIONS.ACTION_RIGHT;
			case 2:
				return Types.ACTIONS.ACTION_UP;
			case 3:
				return Types.ACTIONS.ACTION_DOWN;
			default:
				break;
			}
		}
	}
	
}*/
package AguileraBalderas;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import tools.pathfinder.Node;
import tools.pathfinder.PathFinder;

import AguileraBalderas.ResolutorTareas;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Agent extends AbstractPlayer {
    //Objeto de clase Pathfinder
    private PathFinder pf;
    private int fescalaX;
    private int fescalaY;
    
    private ArrayList<Types.ACTIONS> lista_acciones;
    private ArrayList<Vector2di> lista_gemas_faciles;
        
    
    private int distanciaManhattan(int fila1, int col1, int fila2, int col2) {
		return Math.abs(fila1-fila2) + Math.abs(col1 - col2);
	}
    
    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {    	
    	lista_acciones = new ArrayList<Types.ACTIONS>();
        //Creamos una lista de IDs de obstaculos
        ArrayList<Integer> tiposObs = new ArrayList();
        tiposObs.add(0); //<- Muros
        tiposObs.add(7); //<- Piedras

        //Se inicializa el objeto del pathfinder con las ids de los obstaculos
        pf = new PathFinder(tiposObs);
        pf.VERBOSE = false; // <- Activa o desactiva el modo la impresión del log

        //Se lanza el algoritmo de pathfinding para poder ser usado en la función ACT
        pf.run(stateObs);

        fescalaX = stateObs.getWorldDimension().width / stateObs.getObservationGrid().length;
        fescalaY = stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length;
        
        lista_gemas_faciles = new ArrayList<Vector2di>();
        lista_gemas_faciles.add(new Vector2di(1,4));
        lista_gemas_faciles.add(new Vector2di(7,9));
        lista_gemas_faciles.add(new Vector2di(9,10));
        lista_gemas_faciles.add(new Vector2di(16,9));
        lista_gemas_faciles.add(new Vector2di(18,9));
        lista_gemas_faciles.add(new Vector2di(20,3));
        //lista_gemas_faciles.add(new Vector2di(6,11));
    }


    @Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
    	int col_start = (int) Math.round(stateObs.getAvatarPosition().x / fescalaX);
    	int fila_start = (int) Math.round(stateObs.getAvatarPosition().y / fescalaY);
    	
    	ResolutorTareas resolutor = new ResolutorTareas(stateObs.getObservationGrid(), stateObs.getWorldDimension().width, stateObs.getWorldDimension().height,stateObs, fescalaX, fescalaY);
    	
    	//System.out.println(lista_gemas_faciles);
    	
    	if(lista_acciones.size()==0 && lista_gemas_faciles.size()>0) {
    		if(col_start != lista_gemas_faciles.get(0).x || fila_start != lista_gemas_faciles.get(0).y) {    		
    			lista_acciones = resolutor.obtenCamino(lista_gemas_faciles.get(0).x, lista_gemas_faciles.get(0).y,elapsedTimer);
    		}
    		else
    			lista_gemas_faciles.remove(0);
    	}
    	if(lista_acciones.size()>0) {
	    	Types.ACTIONS accion = lista_acciones.get(0);
	    	//System.out.println(accion.toString());
	    	lista_acciones.remove(0);
	    	return(accion);
    	}
    	if(lista_gemas_faciles.size()==0) {
    		lista_acciones = resolutor.salirPortal(elapsedTimer);
    	}
    	return Types.ACTIONS.ACTION_NIL;
    	
    }


}
