package geometry;

/**
 * Class which represents a point in a three-dimensional space.
 */
public class Point {

    /**
     * x-coordinate of the point
     */
    private double x;

    /**
     * y-coordinate of the point
     */
    private double y;

    /**
     * z-coordinate of the point
     */
    private double z;

    /**
     * extra flag for the point.
     * 0 means not tagged, 1 tagged and all integers > 1 define the membership of a specific cluster.
     */
    private int flag;

    /**
     * Constructor.
     * @param x x-coordinate of the point
     * @param y y-coordinate of the point
     * @param z z-coordinate of the point
     */
    public Point(double x, double y, double z) {
        setX(x);
        setY(y);
        setZ(z);
        setFlag(0);
    }

    /**
     * Constructor.
     * @param x x-coordinate of the point
     * @param y y-coordinate of the point
     * @param z z-coordinate of the point
     * @param flag extra flag for the point
     */
    public Point(double x, double y, double z, int flag) {
        setX(x);
        setY(y);
        setZ(z);
        setFlag(flag);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        if (flag < 0) {
            this.flag = 0;
        } else {
            this.flag = flag;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        if (Double.compare(point.getX(), getX()) != 0) return false;
        if (Double.compare(point.getY(), getY()) != 0) return false;
        if (Double.compare(point.getZ(), getZ()) != 0) return false;
        return getFlag() == point.getFlag();
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(getX());
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getY());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getZ());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + getFlag();
        return result;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", flag=" + flag +
                '}';
    }
}
