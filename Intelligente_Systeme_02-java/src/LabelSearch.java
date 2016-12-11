import analysis.Score;
import geometry.Point;
import parsing.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class from which to start the labeling
 */
public class LabelSearch {

    /**
     * Calculates distance using taxicab geometry
     * @param a First point
     * @param b Second point
     * @return distance
     */
    public static double distance(Point a, Point b) {

        return Math.abs(a.getX() - b.getX());
    }

    /**
     * Calculates pythagorean distance
     * @param a First point
     * @param b Second point
     * @return Distance
     */
    public static double pythagoreanDistance(Point a, Point b) {
        double x = a.getX() - b.getX();
        double y = a.getY() - b.getY();

        return Math.sqrt((Math.pow(x, 2) + Math.pow(y, 2)));
    }

    /**
     * Cleans list of maximums by comparing maximums which are close to each other
     * @param floorList list of floor averages
     * @param max_list list of all maximums
     * @param max_diff max height difference between two points
     * @param max_dist distance for which to compare two points
     * @return
     */
    public static List<Point> cleanData(List<Double> floorList, List<Point> max_list, double max_diff, double max_dist) {
        List<Point> removeList = new ArrayList<>();
        // look at neighbours heights
        for (int i = 0; i < max_list.size(); i++) {
            for (int j = i+1; j < max_list.size(); j++) {
                if (distance(max_list.get(i), max_list.get(j)) <= max_dist) {

                    Point a = max_list.get(i);
                    Point b = max_list.get(j);

                    double height_a = a.getZ() - floorList.get((int) a.getX());
                    double height_b = b.getZ() - floorList.get((int) b.getX());

                    if (height_a < height_b) {
                        if ((height_b * max_diff) > height_a) {
                            removeList.add(a);
                        }
                    } else if (height_b < height_a) {
                        if ((height_a * max_diff) > height_b) {
                            removeList.add(b);
                        }
                    }
                    // else don't care...
                }
            }
        }
        System.out.println("Filtering " + removeList.size() + " items from labels");
        return removeList;
    }

    /**
     * Get one to two cluster maximums
     * @param cluster List of Points forming cluster
     * @param size size for which to consider second maximum
     * @param distance minimal distance between two maximums
     * @return List of Maximums
     */
    public static List<Point> getClusterMaximums(Point[][] points, List<Point> cluster, int size, int distance) {
        List<Point> max = new ArrayList<>();

        Point max1 = null;
        Point max2 = null;

        for (int i = 0; i < cluster.size(); i++) {
            if (i == 0) {
                max1 = cluster.get(i);
            } else {
                if (max1.getZ() < cluster.get(i).getZ()) {
                    max1= cluster.get(i);
                }
            }
        }
        max.add(max1);

        if (cluster.size() > size) {
            for (int i = 0; i < cluster.size(); i++) {
                if (pythagoreanDistance(cluster.get(i), max1) > distance) {
                    if (max2 == null) {
                        max2 = cluster.get(i);
                    } else {
                        if (max2.getZ() < cluster.get(i).getZ()) {
                            max2 = cluster.get(i);
                        }
                    }
                }
            }
            if (max2 != null) {
                // if this isn't a local maximum, throw it out
                for (Point p: max2.getNearPoints(points, 1)) {
                    if (p.getZ() > max2.getZ()) {
                        return max;
                    }
                }
                max.add(max2);
            }
        }
        return max;
    }

    /**
     * Marks all points which are above cluster_height
     * @param points Array of all points
     * @param floorList List of floor values
     * @param cluster_height height difference for which to mark points
     */
    public static void markAllClusters(Point[][] points, List<Double> floorList, double cluster_height) {
        for (int x = 0; x < points.length; x++) {
            for (int y = 0; y < points[0].length; y++) {
                if (points[x][y].getZ() > floorList.get((int) points[x][y].getX()) + floorList.get((int) points[x][y].getX()) * cluster_height) {
                    points[x][y].setFlag(1);
                }
            }
        }
    }
    
    public static void main(String[] args) throws FileNotFoundException {

        int cluster_size = 59;
        double cluster_height = 0.031;
        double max_diff = 0.4;
        double max_dist = 99;
        int cluster_double_size = 700;
        int cluster_dist = 45;

        Point[][] points = csv.getPointsFromCSV(new File("data0.csv"));
        List<Point> labelList = csv.getLabelsFromCSV(new File("label0.csv"), points);
        List<Double> floorList = Point.calcAverageOnXAxis(points);
        System.out.println("Read points");


        List<List<Point>> clusterList;
        List<Point> dummyList = new ArrayList<>();
        //mark all clusters, then find them
        markAllClusters(points, floorList, cluster_height);
        clusterList = Point.getAllCluster(points);
        //count how many clusters we drop
        int numSmallClusters = 0;
        for (List<Point> cluster : clusterList) {

            // filter clusters by size
            if (cluster.size() < cluster_size) {
                numSmallClusters++;
                continue;
            }

            dummyList.addAll(getClusterMaximums(points, cluster, cluster_double_size, cluster_dist));
        }

        System.out.println("Got all Clusters");
        System.out.println("Got " + numSmallClusters + " too small clusters");

        //cleanup clusters further
        dummyList.removeAll(cleanData(floorList, dummyList, max_diff, max_dist));

        //score
        Score s = new Score(points, labelList, dummyList);

        s.calculateScore();
        System.out.println("Recall: " + s.getRecall());
        System.out.println("Precision: " + s.getPrecision());
        System.out.println("F-Score: " + s.getF_score());

        csv.writePointstoCSV(new File("out.csv"), dummyList, false);
    }
}
