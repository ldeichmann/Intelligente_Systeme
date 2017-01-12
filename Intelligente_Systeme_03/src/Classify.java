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

    public static String classifyDifferenceVector(double value, double threshold) throws Exception {

        if (value < (-1 * threshold)) {
            return "A";
        } else if ((-1 * threshold) <= value && value <= threshold) {
            return "B";
        } else if (threshold < value) {
            return "C";
        }
        throw new Exception("Cannot classify!");
    }

    public static void classifyDifferenceVectors(List<DifferenceVector> vecs, double threshold) throws Exception {

        for (DifferenceVector v: vecs) {
            String tmp = new String();

            tmp = tmp.concat(classifyDifferenceVector(v.getDiff_x(), threshold));
            tmp = tmp.concat(classifyDifferenceVector(v.getDiff_y(), threshold));

            v.setState(State.valueOf(tmp));
        }

    }

    public static Map<String, Double> countClassifications(List<DifferenceVector> vecs) {
        Map<String, Double> pairMap = new HashMap<>();
        Map<State, Double> countMap = new HashMap<>();

        // set everything to 1
        for (State s: State.values()) {
            for (State s2: State.values()) {
                String pair = s.name() + s2.name();
                pairMap.put(pair, 1.0);
            }
            countMap.put(s, 1.0);
        }
//        System.out.println(pairMap);
//        System.out.println(countMap);

        for (int i = 0; i < vecs.size()-1; i++) {
            DifferenceVector vec = vecs.get(i);
            DifferenceVector vec2 = vecs.get(i+1);
            String pair = new String();
            pair = pair.concat(vec.getState().name());
            pair = pair.concat(vec2.getState().name());

            countMap.putIfAbsent(vec.getState(), 0.0);
            countMap.put(vec.getState(), countMap.get(vec.getState())+1);
            pairMap.putIfAbsent(pair, 0.0);
            pairMap.put(pair, pairMap.get(pair)+1);
            // This is so terrible, I can't stop laughing
//            switch (vec.getState().name().charAt(0)) {
//                case 'A':
//                    a++;
//                    break;
//                case 'B':
//                    b++;
//                    break;
//                case 'C':
//                    c++;
//                    break;
//            }
        }

        pairMap.forEach( (k,v) -> pairMap.put(k, v / countMap.get( State.valueOf(k.substring(2)) )) );

//        for (State s : State.values()) {
//            switch (s.name().charAt(0)) {
//                case 'A':
//                    countMap.put(s, countMap.get(s)/a);
//                    break;
//                case 'B':
//                    countMap.put(s, countMap.get(s)/b);
//                    break;
//                case 'C':
//                    countMap.put(s, countMap.get(s)/c);
//                    break;
//            }
//        }

        return pairMap;
    }

    public static void main(String[] args) throws Exception {

        double threshold = 10;

        List<List<Vector>> vecs_alone = ReadData.getVectorsFromFile(new File("train_alone.txt"));
        List<List<Vector>> vecs_group = ReadData.getVectorsFromFile(new File("train_group.txt"));
        List<DifferenceVector> diff_vecs_alone = new ArrayList<>();
        List<DifferenceVector> diff_vecs_group = new ArrayList<>();

        for (List<Vector> v : vecs_alone) {
            for (int i = 1; i < v.size(); i++) {
                diff_vecs_alone.add(new DifferenceVector(v.get(i-1), v.get(i)) );
            }
        }
        for (List<Vector> v : vecs_group) {
            for (int i = 1; i < v.size(); i++) {
                diff_vecs_group.add(new DifferenceVector(v.get(i-1), v.get(i)) );
            }
        }

        classifyDifferenceVectors(diff_vecs_alone, threshold);
        classifyDifferenceVectors(diff_vecs_group, threshold);

        Map<String, Double> counted_alone = countClassifications(diff_vecs_alone);
        Map<String, Double> counted_group = countClassifications(diff_vecs_group);

        System.out.println(counted_alone);

        List<List<Vector>> eval_vecs = ReadData.getVectorsFromFile(new File("eval_group.txt"));

        int count_alone = 0;
        int count_group = 0;

        for (List<Vector> eval_v : eval_vecs) {

            List<DifferenceVector> eval_diff_vecs = new ArrayList<>();

            for (int i = 1; i < eval_v.size(); i++) {
                eval_diff_vecs.add(new DifferenceVector(eval_v.get(i - 1), eval_v.get(i)));
            }
            classifyDifferenceVectors(eval_diff_vecs, threshold);

            double tmp_alone = 0;
            double tmp_group = 0;

//            for (DifferenceVector eval_vec : eval_diff_vecs) {
            for (int i = 0; i < eval_diff_vecs.size()-1; i++) {
                DifferenceVector eval_vec = eval_diff_vecs.get(i);
                DifferenceVector eval_vec2 = eval_diff_vecs.get(i+1);
                String state = eval_vec.getState().name() + eval_vec2.getState().name();
                tmp_alone += -Math.log(counted_alone.get(state));
                tmp_group += -Math.log(counted_group.get(state));
            }

            if (tmp_alone >= tmp_group) {
                count_group++;
            } else {
                count_alone++;
            }

//            System.out.println(tmp_alone + " - " + tmp_group);

        }
        System.out.println("Alone: " + count_alone + "\nGroup: " + count_group);
    }

}
