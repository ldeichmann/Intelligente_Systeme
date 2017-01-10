package geometry;

public class DifferenceVector {

    State state;

    Vector vec_a;
    Vector vec_b;

    public double getDiff_x() {
        return diff_x;
    }

    public void setDiff_x(double diff_x) {
        this.diff_x = diff_x;
    }

    public double getDiff_y() {
        return diff_y;
    }

    public void setDiff_y(double diff_y) {
        this.diff_y = diff_y;
    }

    double diff_x;
    double diff_y;

    public DifferenceVector(Vector a, Vector b) {
        this.vec_a = a;
        this.vec_b = b;

        this.diff_x = b.getX() - a.getX();
        this.diff_y = b.getY() - a.getY();
    }

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
