package geometry;

import java.util.*;

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
     * Upper bound on the x-axis
     */
    private static double bound_x;

    /**
     * Upper bound on the y-axis
     */
    private static double bound_y;

    /**
     * Extra flag for the point.
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

        // x and y values are enough for us here, if in doubt
        // we'll get them from the points array
        if (Double.compare(point.getX(), getX()) != 0) return false;
        return Double.compare(point.getY(), getY()) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(getX());
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getY());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
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

    /**
     * Sets upper bounds for all points
     * @param x x-axis upper bound
     * @param y y-axis upper bound
     */
    public static void setBounds(double x, double y) {
        Point.bound_x = x;
        Point.bound_y = y;
    }

    /**
     * Checks whether given values are within bounds
     * @param x x-axis position
     * @param y y-axis position
     * @return is position in bounds
     */
    private static Boolean isInBounds(double x, double y) {
        return x < Point.bound_x && x >= 0 && y < Point.bound_y && y >= 0;
    }

    /**
     * Returns a list containing all points which are neighbours of this point
     * @param points a 2D array containing all points
     * @param step  the layer of the connected points
     * @return  the list containing the points which are connected to this point
     */
    public List<Point> getNearPoints(Point[][] points, int step) {
        List<Point> returnList = new ArrayList<>();

        if (step < 1) {
            return returnList;
        }
        List<Integer> innerList = new ArrayList<>();
        List<Integer> outerList = new ArrayList<>();
        outerList.add(step);
        outerList.add((-1) * step);
        innerList.add(0);
        for (int i = 1; i < step; i++) {
            innerList.add(i);
            innerList.add((-1) * i);
        }

        for (int i = 0; i < 2; i++) {
            if (isInBounds(this.getX() + outerList.get(i), this.getY() + outerList.get(i))) {
                returnList.add(points[(int)this.getX() + outerList.get(i)][(int)this.getY() + outerList.get(i)]);
            }
            if (isInBounds(this.getX() + outerList.get(i), this.getY() - outerList.get(i))) {
                returnList.add(points[(int)this.getX() + outerList.get(i)][(int)this.getY() - outerList.get(i)]);
            }
            for (int k = 0; k < innerList.size(); k++) {
                if (isInBounds(this.getX() + outerList.get(i), this.getY() + innerList.get(k))) {
                    returnList.add(points[(int)this.getX() + outerList.get(i)][(int)this.getY() + innerList.get(k)]);
                }
                if (isInBounds(this.getX() + innerList.get(k), this.getY() + outerList.get(i))) {
                    returnList.add(points[(int)this.getX() + innerList.get(k)][(int)this.getY() + outerList.get(i)]);
                }
            }
        }
        return returnList;
    }

    /**
     * Adds all tagged points belonging to the same cluster as this point to the current cluster
     * @param points an array containing all tagged points
     * @param numClust the current cluster number
     */
    private void getCluster(Point[][] points, int numClust) {
        List<Point> toBeChecked = new ArrayList<>();
        Point current = null;
        toBeChecked.add(this);
        while(toBeChecked.size() > 0) {
            current = toBeChecked.remove(0);
            for (Point point : current.getNearPoints(points, 1)) {
                if (point.getFlag() == 1) {
                    toBeChecked.add(point);
                    point.setFlag(numClust);
                }
            }
        }
    }

    /**
     * Returns the tagged points divided into clusters
     * @param points a 2D array containing all tagged points
     * @return a list of lists containing the clusters of the tagged points
     */
    public static List<List<Point>> getAllCluster(Point[][] points) {
        List<List<Point>> returnList = new ArrayList<>();
        List<Point> newTaggedPoints = new ArrayList<>();
        Map<Integer, List<Point>> clusterMap = new HashMap<>();

        int numClust = 2;
        for (int x = 0; x < Point.bound_x; x++) {
            for (int y = 0; y < Point.bound_y; y++) {
                if (points[x][y].getFlag() == 1) {
                    points[x][y].getCluster(points, numClust);
                    numClust++;
                }
            }
        }

        for (int x = 0; x < Point.bound_x; x++) {
            for (int y = 0; y < Point.bound_y; y++) {
                if (points[x][y].getFlag() > 1) {
                    Point p = points[x][y];
                    if (!clusterMap.containsKey(p.getFlag())) {
                        clusterMap.put(p.getFlag(), new ArrayList<>());
                    }
                    clusterMap.get(p.getFlag()).add(p);
                }
            }
        }

        for (int flag: clusterMap.keySet()) {
            returnList.add(clusterMap.get(flag));
        }

        return returnList;
    }

    /**
     * Calculates the average z-values for the x-axis
     * @param pointList a 2D array containing all points
     * @return a list containing the average z-values for the x-axis
     */
    public static List<Double> calcAverageOnXAxis(Point[][] pointList) {
        Map<Double, List<Double>> dummyMap = new HashMap<>();
        List<Double> dummyList;
        for (int x = 0; x < pointList.length; x++) {
            for (int y = 0; y < pointList[0].length; y++) {
                if (dummyMap.keySet().contains((double) x)) {
                    dummyList = dummyMap.get((double) x);
                    dummyList.set(0, dummyList.get(0) + 1);
                    dummyList.set(1, dummyList.get(1) + pointList[x][y].getZ());
                } else {
                    dummyList = new ArrayList<>();
                    dummyList.add((double) 1);
                    dummyList.add(pointList[x][y].getZ());
                    dummyMap.put((double)x, dummyList);
                }

            }
        }

        dummyList = new ArrayList<>();
        for (double x : dummyMap.keySet()) {
            dummyList.add(x);
        }
        Collections.sort(dummyList);
        List<Double> returnList = new ArrayList<>();
        for (double iter : dummyList) {
            returnList.add(dummyMap.get(iter).get(1) / dummyMap.get(iter).get(0));
        }
        return returnList;
    }

}
