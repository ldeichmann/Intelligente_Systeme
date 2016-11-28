package analysis;

import geometry.Point;
import parsing.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for evaluating detected labels
 */
public class Score {

    private double precision;
    private double recall;
    private double f_score;

    private double correctLabels;

    private List<Point> points;
    private List<Point> labels;
    private List<Point> foundLabels;
    private List<List<Point>> labelCluster;

    /**
     * Creates a Score instance
     * @param points List of all points
     * @param labels List of labels within points
     * @param foundLabels List of labels to be evaluated
     */
    public Score(List<Point> points, List<Point> labels, List<Point> foundLabels) {
        this.points = points;
        this.labels = labels;
        this.foundLabels = foundLabels;
    }

    /**
     * Finds acceptable points around a given label
     * @param label the label from which to span the cluster
     * @return  a list containing the points
     */
    private List<Point> spanClusterForLabel(Point label) {
        List<Point> returnList = new ArrayList<>();
        List<Point> neighbours = new ArrayList<>();

        returnList.add(label);
        for (int i = 1; i <= 5; i++) {
            neighbours.addAll(label.getNearPoints(i));
        }
        for (Point x : neighbours){
            int index = this.points.indexOf(x);
            if (index >= 0 && !returnList.contains(x) && this.points.get(index).getZ() >= label.getZ())
                returnList.add(x);
        }
        return returnList;
    }

    /**
     * Finds all acceptable points for all labels
     */
    private void spanClusterForLabels() {
        List<List<Point>> returnList = new ArrayList<>();
        for (int i = 0; i < this.labels.size(); i++) {
            returnList.add(spanClusterForLabel(this.labels.get(i)));
            System.out.println("Label " + (i+1) + "/" + this.labels.size());
        }
        this.labelCluster = returnList;
    }

    /**
     * Calculates number of correct labels without duplicates
     */
    private void calculateCorrectLabels() {
        List<List<Point>> dummyList = new ArrayList<>(this.labelCluster);
        for (Point p : this.foundLabels) {
            for (List<Point> cluster: this.labelCluster) {
                if (cluster.contains(p) && dummyList.contains(cluster)) {
                    correctLabels++;
                    dummyList.remove(cluster);
                    continue;
                }
            }
        }
    }

    /**
     * Calculates precision from label clusters and found labels
     */
    private void calculatePrecision() {
        this.precision = this.correctLabels / (double)this.foundLabels.size();
    }

    /**
     * Calculates recall from label clusters and found labels
     */
    private void calculateRecall() {
        this.recall = this.correctLabels / (double)this.labels.size();
    }

    /**
     * Calculates the F-Score for the given data
      * @return f-score
     */
    public double calculateScore() {
        this.spanClusterForLabels();
        this.calculateCorrectLabels();
        this.calculatePrecision();
        this.calculateRecall();
        this.f_score = (2.0 * this.recall * this.precision)/(this.recall + this.precision);
        return this.f_score;
    }

    public static void main(String[] args) throws FileNotFoundException {
        List<Point> pointList = csv.getPointsFromCSV(new File("/home/cru/Downloads/ISys_02/data0.csv"));
        List<Point> labelList = csv.getLabelsFromCSV(new File("/home/cru/Downloads/ISys_02/label0.csv"), pointList);
        List<Point> calcLabel = csv.getLabelsFromCSV(new File("/home/cru/Downloads/ISys_02/rommel.csv"), pointList);

        Score s = new Score(pointList, labelList, calcLabel);

        s.calculateScore();
        System.out.println("Recall: " + s.recall);
        System.out.println("Precision: " + s.precision);
        System.out.println("F-Score: " + s.f_score);
//        csv.writePointstoCSV(new File("/home/cru/Downloads/ISys_02/clustertest.csv"), s.labelCluster, false);
    }
}