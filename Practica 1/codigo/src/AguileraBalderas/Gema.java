package AguileraBalderas;

import java.util.ArrayList;

import AguileraBalderas.Vector2di;
import tools.Vector2d;

public class Gema {
	Vector2di coordenadas;
	int distancia_actual;
	//Es tipo 0 si está rodeada de piedras menos en la posición de arriba
	//Es de tipo 1 si tiene piedras arriba pero no en alguno de los dos lados
	int tipo_gema_piedra;
	//Posiciones por las que tiene que pasar para coger la gema si hay una piedra, la ultima será la propia gema
	ArrayList<Vector2di> posiciones_a_ir;
	//Orientaciones que tiene que tener al final de ir a casa posición.
	ArrayList<Vector2d> orientaciones;
	
	public Gema() {
		this.orientaciones = new ArrayList<Vector2d>();
		this.posiciones_a_ir = new ArrayList<Vector2di>();
		this.coordenadas = new Vector2di(-1,-1);
		this.distancia_actual = -1;
	}
	
	@Override
	public String toString() {
		return "||" + coordenadas.toString() + ", Distancia actual: " + distancia_actual + "||" + ", Posiciones a ir: " + posiciones_a_ir + "||" + ", Orientaciones: " + orientaciones + "||";
	}
	
	@Override
    public boolean equals(Object arg0) {
        Gema gema = (Gema) arg0;
        return this.coordenadas.x == gema.coordenadas.x && this.coordenadas.y == gema.coordenadas.y;
	}
}
