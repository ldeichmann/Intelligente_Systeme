import analysis.Score;
import geometry.Point;
import parsing.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static double distance(Point a, Point b) {

        return Math.abs(a.getX() - b.getX());
//        double x = a.getX() - b.getX();
//        double y = a.getY() - b.getY();
//
//        return Math.sqrt((Math.pow(x, 2) + Math.pow(y, 2)));
    }

    public static void main(String[] args) throws FileNotFoundException {
        List<Point> pointList = csv.getPointsFromCSV(new File("/home/cru/Downloads/ISys_02/data1.csv"));
        List<Point> labelList = csv.getLabelsFromCSV(new File("/home/cru/Downloads/ISys_02/label1.csv"), pointList);
        List<Double> bodenList = Point.calcAverageOnXAxis(pointList);
        List<Point> taggedPoints = new ArrayList<>();
        for (Point point : pointList) {
            if (point.getZ() > bodenList.get((int) point.getX()) + bodenList.get((int) point.getX()) * 0.04) {
                taggedPoints.add(point);
            }
        }

        List<List<Point>> clusterList;
        Point biggestPoint = null;
        List<Point> dummyList = new ArrayList<>();
        clusterList = Point.getAllCluster(taggedPoints);
        for (List<Point> cluster : clusterList) {

            if (cluster.size() < 20) {
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

        // this data cleanup has the following effect using 0.04, 20, max_dist = 50, max_diff = 0.5;
        // old:
        //  data0:
        //   Recall: 0.8489208633093526
        //   Precision: 0.7515923566878981
        //   F-Score: 0.7972972972972974
        //  data1:
        //   Recall: 0.8672566371681416
        //   Precision: 0.875
        //   F-Score: 0.8711111111111112
        // new:
        //  data0:
        //   Recall: 0.8489208633093526
        //   Precision: 0.7972972972972973
        //   F-Score: 0.8222996515679443
        //  data1:
        //  Recall: 0.8672566371681416
        //  Precision: 0.9423076923076923
        //  F-Score: 0.9032258064516129

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
//                            System.out.println((height_b * max_diff) + " > " + height_a);
//                            System.out.println("Removing point at x: " + a.getX() + " y: " + a.getY());
                            removeList.add(a);
//                            break;
                        }
                    } else if (height_b < height_a) {
                        if ((height_a * max_diff) > height_b) {
//                            System.out.println((height_a * max_diff) + " > " + height_b);
//                            System.out.println("Removing point at x: " + b.getX() + " y: " + b.getY());
                            removeList.add(b);
//                            break;
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

        csv.writePointstoCSV(new File("/home/cru/Downloads/ISys_02/rommel.csv"), dummyList, false);
    }
}
