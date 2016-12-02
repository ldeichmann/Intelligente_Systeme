package analysis;

import geometry.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for evaluating detected labels
 */
public class Score {

    /**
     * Precision of found labels
     */
    private double precision;

    /**
     * Recall of found labels
     */
    private double recall;

    /**
     * F-Score of found labels
     */
    private double f_score;

    /**
     * Number of correct labels
     */
    private double correctLabels;

    /**
     * 2D Array of all points
     */
    private Point[][] points;

    /**
     * List of expected labels
     */
    private List<Point> labels;

    /**
     * List of found labels
     */
    private List<Point> foundLabels;

    /**
     * List of List of acceptable Points around a label
     */
    private List<List<Point>> labelCluster;

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getF_score() {
        return f_score;
    }

    /**
     * Creates a Score instance
     * @param points 2D array of all points
     * @param labels List of labels within points
     * @param foundLabels List of labels to be evaluated
     */
    public Score(Point[][] points, List<Point> labels, List<Point> foundLabels) {
        this.points = points;
        this.labels = labels;
        this.foundLabels = foundLabels;
    }


    /**
     * Finds acceptable points around a given label
     * @param label the label from which to span the cluster
     * @return a list containing the points
     */
    private List<Point> spanClusterForLabel(Point label) {
        List<Point> returnList = new ArrayList<>();
        List<Point> neighbours = new ArrayList<>();

        returnList.add(label);
        for (int i = 1; i <= 5; i++) {
            neighbours.addAll(label.getNearPoints(points, i));
        }
        for (Point x : neighbours){
            if (!returnList.contains(x) && x.getZ() >= label.getZ())
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
}