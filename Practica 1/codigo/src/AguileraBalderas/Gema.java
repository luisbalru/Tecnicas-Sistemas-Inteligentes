package AguileraBalderas;

public class Gema {
	Vector2di coordenadas;
	int distancia_actual;
	
	public Gema() {
		this.coordenadas = new Vector2di(-1,-1);
		this.distancia_actual = -1;
	}
	
	@Override
	public String toString() {
		return "||" + coordenadas.toString() + ", Distancia actual: " + distancia_actual + "||";
	}
	
	@Override
    public boolean equals(Object arg0) {
        Gema gema = (Gema) arg0;
        return this.coordenadas.x == gema.coordenadas.x && this.coordenadas.y == gema.coordenadas.y;
	}
}
