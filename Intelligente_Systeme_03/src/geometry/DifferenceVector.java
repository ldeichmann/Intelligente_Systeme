package geometry;

public class DifferenceVector {

    /**
     * Classification for the DifferenceVector
     */
    State state;

    /**
     * Vectors which were used to calculate DifferenceVector
     * Currently not used, but kept because why not.
     */
    Vector vec_a;
    Vector vec_b;

    public double getDiff_x() {
        return diff_x;
    }

    public double getDiff_y() {
        return diff_y;
    }

    /**
     * Differences between vec_b and vec_a
     */
    double diff_x;
    double diff_y;

    /**
     * Creates a DifferenceVector using two {@link Vector}s
     * @param a
     * @param b
     */
    public DifferenceVector(Vector a, Vector b) {
        this.vec_a = a;
        this.vec_b = b;

        this.diff_x = b.getX() - a.getX();
        this.diff_y = b.getY() - a.getY();
    }

    /**
     * Sets the DifferenceVectors state
     * @param state
     */
    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return this.state;
    }

    @Override
    public String toString() {
        return this.diff_x + "," + this.diff_y + " - " + this.state;
    }
}
