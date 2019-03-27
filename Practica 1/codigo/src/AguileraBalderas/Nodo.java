package AguileraBalderas;


public class Nodo {
	public double coste_g;
	public double estimacion_h;
	public double f;
	public int fila;
	public int columna;
	public Nodo padre;
	public boolean esMuro;
	
	public Nodo(double d,double e,int fila,int columna,Nodo n,boolean muro) {
		coste_g = d;
		estimacion_h = e;
		this.fila = fila;
		this.columna = columna;
		padre = n;
		f = coste_g + estimacion_h;
		esMuro = muro;
	}
	
	@Override
    public boolean equals(Object arg0) {
        Nodo nodo = (Nodo) arg0;
        return this.fila == nodo.fila && this.columna == nodo.columna;
	}
	
}
