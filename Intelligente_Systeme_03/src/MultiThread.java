import geometry.Vector;
import parsing.ReadData;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Exchanger;

/**
 * Created by Adrian on 12.01.2017.
 */
public class MultiThread implements Runnable {
    public static Map<Double, Double> relations = new HashMap<Double, Double>();
    double threshold_start;
    double threshold_end;
    List<List<Vector>> vecs_alone_t;
    List<List<Vector>> vecs_group_t;
    Thread t;

    MultiThread(double start, double end, List<List<Vector>> vecs_alone, List<List<Vector>> vecs_group) {
        this.threshold_start = start;
        this.threshold_end = end;
        this.vecs_alone_t = vecs_alone;
        this.vecs_group_t = vecs_group;
    }

    public void run(){
            if (threshold_start == 0) {
                threshold_start = 1;
            }
            while (threshold_start < threshold_end) {
                //System.out.println(Classify.relation(threshold_start, vecs_alone_t, vecs_group_t));
                try {
                    relations.put(threshold_start, Classify.relation(threshold_start, vecs_alone_t, vecs_group_t));
                }catch (Exception E){

                }
                threshold_start+=0.01;
            }
    }

    public void start () {
        t = new Thread(this);
        t.start();
        /*try {
            t.join();
        }catch (Exception e){
        }*/
    }


    public static void main(String[] args) {
        int threads = 4;
        int threshhold = 88;
        List<List<Vector>> vecs_alone = ReadData.getVectorsFromFile(new File("train_alone.txt"));
        List<List<Vector>> vecs_group = ReadData.getVectorsFromFile(new File("train_group.txt"));
        for(int i = 0;i < threads;i++){
            MultiThread thread = new MultiThread((threshhold/threads) * i,(threshhold/threads) * (i + 1),vecs_alone,vecs_group);
            thread.start();
            try {
                thread.t.join();
            }catch (Exception e){
            }
        }

        Map.Entry<Double,Double> maxEntry = null;

        for(Map.Entry<Double,Double> entry : relations.entrySet()){
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
            {
                maxEntry = entry;
            }
        }
        System.out.println("Best Threshhold: " + maxEntry);
    }

}
