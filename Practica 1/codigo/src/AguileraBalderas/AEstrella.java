package AguileraBalderas;

import AguileraBalderas.Nodo;
import java.lang.*;

public class AEstrella {
	private Nodo nodo_inicial;
	private Nodo nodo_objetivo;
	
	private g(Nodo n) {
		return Math.abs(n.posicion.x - nodo_inicial.posicion.x) + Math.abs(n.posicion.y - nodo_inicial.posicion.y);
	}
	
	private h(Nodo n) {
		return Math.abs(n.posicion.x - nodo_objetivo.posicion.x) + Math.abs(n.posicion.y - nodo_objetivo.posicion.y);
	}
}
