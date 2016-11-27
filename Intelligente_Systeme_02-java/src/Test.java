import geometry.Point;
import parsing.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonny on 26.11.16.
 */
public class Test {

    public static void main(String[] args) throws FileNotFoundException {
        List<Point> pointList = csv.getPointsFromCSV(new File("/home/jonny/data-crunchyyy/src/data1.csv"));
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

        csv.writePointstoCSV(new File("/home/jonny/rommel.csv"), dummyList, false);
    }
}
