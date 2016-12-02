import analysis.Score;
import geometry.Point;
import parsing.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static double distance(Point a, Point b) {

        return Math.abs(a.getX() - b.getX()); // Using taxicab geometry for the best performance
//        double x = a.getX() - b.getX();
//        double y = a.getY() - b.getY();
//
//        return Math.sqrt((Math.pow(x, 2) + Math.pow(y, 2)));
    }

    public static void main(String[] args) throws FileNotFoundException {
        Point[][] pointList = csv.getPointsFromCSV(new File("data0.csv"));
        List<Point> labelList = csv.getLabelsFromCSV(new File("label0.csv"), pointList);
        List<Double> bodenList = Point.calcAverageOnXAxis(pointList);

        for (int x = 0; x < pointList.length; x++) {
            for (int y = 0; y < pointList[0].length; y++) {
                if (pointList[x][y].getZ() > bodenList.get((int) pointList[x][y].getX()) + bodenList.get((int) pointList[x][y].getX()) * 0.037) {
                    pointList[x][y].setFlag(1);
                }
            }
        }

        System.out.println("Read points");

        List<List<Point>> clusterList;
        Point biggestPoint = null;
        List<Point> dummyList = new ArrayList<>();
        clusterList = Point.getAllCluster(pointList);
        int numSmallClusters = 0;
        for (List<Point> cluster : clusterList) {

            if (cluster.size() < 20) {
                numSmallClusters++;
                continue;
            }

            for (int i = 0; i < cluster.size(); i++) {
                if (i == 0) {
                    biggestPoint = cluster.get(i);
                } else {
                    if (biggestPoint.getZ() < cluster.get(i).getZ()) {
                        biggestPoint = cluster.get(i);
                    }
                }
            }
            dummyList.add(biggestPoint);
        }

        System.out.println("Got all Clusters");
        System.out.println("Got " + numSmallClusters + " too small clusters");

        double max_dist = 50.0;
        double max_diff = 0.5;

        List<Point> removeList = new ArrayList<>();
        // look at neighbours heights
        for (int i = 0; i < dummyList.size(); i++) {
            for (int j = i+1; j < dummyList.size(); j++) {
                if (distance(dummyList.get(i), dummyList.get(j)) <= max_dist) {

                    Point a = dummyList.get(i);
                    Point b = dummyList.get(j);

                    double height_a = a.getZ() - bodenList.get((int) a.getX());
                    double height_b = b.getZ() - bodenList.get((int) b.getX());

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

        dummyList.removeAll(removeList);

        Score s = new Score(pointList, labelList, dummyList);

        s.calculateScore();
        System.out.println("Recall: " + s.getRecall());
        System.out.println("Precision: " + s.getPrecision());
        System.out.println("F-Score: " + s.getF_score());

        csv.writePointstoCSV(new File("rommel.csv"), dummyList, false);
    }
}
