package AguileraBalderas;


public class Vector2di {
	/**
     * X-coordinate of the vector.
     */
    public int x;

    /**
     * Y-coordinate of the vector.
     */
    public int y;

    /**
     * Default constructor.
     */
    public Vector2di() {
        this(0, 0);
    }

    /**
     * Checks if a vector and this are the same.
     * @param o the other vector to check
     * @return true if their coordinates are the same.
     */
    @Override
	public boolean equals(Object o) {
        if (o instanceof Vector2di) {
            Vector2di v = (Vector2di) o;
            return x == v.x && y == v.y;
        } else {
            return false;
        }
    }

    /**
     * Builds a vector from its coordinates.
     * @param x x coordinate
     * @param y y coordinate
     */
    public Vector2di(int x, int y) {
        this.x = x;
        this.y = y;
    }


    /**
     * Builds a vector from another vector.
     * @param v Vector to copy from.
     */
    public Vector2di(Vector2di v) {
        this.x = v.x;
        this.y = v.y;
    }

    /**
     * Creates a copy of this vector
     * @return a copy of this vector
     */
    public Vector2di copy() {
        return new Vector2di(x,y);
    }

    /**
     * Sets this vector's coordinates to the coordinates of another vector.
     * @param v that other vector.
     */
    public void set(Vector2di v) {
        this.x = v.x;
        this.y = v.y;
    }

    /**
     * Sets this vector's coordinates to the coordinates given.
     * @param x x coordinate.
     * @param y y coordinate.
     */
    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Sets the vector's coordinates to (0,0)
     */
    public void zero() {
        x = 0;
        y = 0;
    }

    /**
     * Returns a representative String of this vector.
     * @return a representative String of this vector.
     */
    @Override
	public String toString() {
        return x + " : " + y;
    }

}
