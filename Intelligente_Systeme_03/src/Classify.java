import geometry.DifferenceVector;
import geometry.State;
import geometry.Vector;
import parsing.ReadData;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Classify {

    public static String classifyDifferenceVector(double value, double threshold) {

        if (value < (-1 * threshold)) {
            return "A";
        } else if ((-1 * threshold) <= value && value <= threshold) {
            return "B";
        } else if (threshold < value) {
            return "C";
        }
        return "F";
    }

    public static void classifyDifferenceVectors(List<DifferenceVector> vecs, double threshold) {

        for (DifferenceVector v: vecs) {
            String tmp = new String();

            tmp = tmp.concat(classifyDifferenceVector(v.getDiff_x(), threshold));
            tmp = tmp.concat(classifyDifferenceVector(v.getDiff_y(), threshold));

            v.setState(State.valueOf(tmp));
        }

    }

    public static Map<State, Double> countClassifications(List<DifferenceVector> vecs) {
        Map<State, Double> countMap = new HashMap<>();

        double a = 0;
        double b = 0;
        double c = 0;

        for (DifferenceVector vec : vecs) {
            countMap.putIfAbsent(vec.getState(), 0.0);
            countMap.put(vec.getState(), countMap.get(vec.getState())+1);
            // This is so terrible, I can't stop laughing
            switch (vec.getState().name().charAt(0)) {
                case 'A':
                    a++;
                    break;
                case 'B':
                    b++;
                    break;
                case 'C':
                    c++;
                    break;
            }
        }

        for (State s : State.values()) {
            switch (s.name().charAt(0)) {
                case 'A':
                    countMap.put(s, countMap.get(s)/a);
                    break;
                case 'B':
                    countMap.put(s, countMap.get(s)/b);
                    break;
                case 'C':
                    countMap.put(s, countMap.get(s)/c);
                    break;
            }
        }

        return countMap;
    }

    public static void main(String[] args) {

        double threshold = 3;

        List<Vector> vecs = ReadData.getVectorsFromFile(new File("train_alone.txt"));
        List<DifferenceVector> diff_vecs = new ArrayList<>();

        for (int i = 1; i < vecs.size(); i++) {
            diff_vecs.add(new DifferenceVector(vecs.get(i-1), vecs.get(i)) );
        }

        classifyDifferenceVectors(diff_vecs, threshold);

        Map<State, Double> counted = countClassifications(diff_vecs);

        System.out.println(counted);

    }

}
