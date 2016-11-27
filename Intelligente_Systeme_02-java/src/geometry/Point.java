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
     * Returns a list containing points which are connected to this point regarding to x- and y-coordinates
     * @param step  the layer of the connected points
     * @return  the list containing the points which are connected to this point
     */
    public List<Point> getNearPoints(int step) {
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
            returnList.add(new Point(this.getX() + outerList.get(i), this.getY() + outerList.get(i), this.getZ()));
            returnList.add(new Point(this.getX() + outerList.get(i), this.getY() - outerList.get(i), this.getZ()));
            for (int k = 0; k < innerList.size(); k++) {
                returnList.add(new Point(this.getX() + outerList.get(i), this.getY() + innerList.get(k), this.getZ()));
                returnList.add(new Point(this.getX() + innerList.get(k), this.getY() + outerList.get(i), this.getZ()));
            }
        }

        return returnList;
    }

    /**
     * Adds all tagged points belonging to the same cluster as this point to the current cluster
     * @param taggedPoints a list containing all tagged points
     * @param currentCluster a list containing all points of the current cluster
     */
    private void getClusterRecurr(List<Point> taggedPoints, List<Point> currentCluster) {
        List<Point> nearPoints = this.getNearPoints(1);
        List<Point> newPoints = new ArrayList<>();
        for (Point point : nearPoints) {
            if (!currentCluster.contains(point) && taggedPoints.contains(point)) {
                newPoints.add(point);
            }
        }
        if (newPoints.size() != 0) {
            for (Point point : newPoints) {
                currentCluster.add(point);
            }
            for (Point point : newPoints) {
                point.getClusterRecurr(taggedPoints, currentCluster);
            }
        }
    }

    /**
     * Returns the tagged points divided into clusters
     * @param taggedPoints  a list containing the tagged points
     * @return a list of lists containing the clusters of the tagged points
     */
    public static List<List<Point>> getAllCluster(List<Point> taggedPoints) {
        List<List<Point>> returnList = new ArrayList<>();
        List<Point> newTaggedPoints = new ArrayList<>();
        for (Point point : taggedPoints) {
            newTaggedPoints.add(point);
        }

        while (newTaggedPoints.size() != 0) {
            List<Point> clusterList = new ArrayList<>();
            clusterList.add(newTaggedPoints.get(0));
            newTaggedPoints.get(0).getClusterRecurr(newTaggedPoints, clusterList);
            returnList.add(clusterList);
            for (Point point : clusterList) {
                newTaggedPoints.remove(point);
            }
        }

        return returnList;
    }

    /**
     * Calculates the average z-values for the x-axis
     * @param pointList a list containing all points
     * @return a list containing the average z-values for the x-axis
     */
    public static List<Double> calcAverageOnXAxis(List<Point> pointList) {
        Map<Double, List<Double>> dummyMap = new HashMap<>();
        List<Double> dummyList;
        for (Point point : pointList) {
            if (dummyMap.keySet().contains(point.getX())) {
                dummyList = dummyMap.get(point.getX());
                dummyList.set(0, dummyList.get(0) + 1);
                dummyList.set(1, dummyList.get(1) + point.getZ());
            } else {
                dummyList = new ArrayList<>();
                dummyList.add((double) 1);
                dummyList.add(point.getZ());
                dummyMap.put(point.getX(), dummyList);
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
