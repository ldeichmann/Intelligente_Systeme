import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by Adrian on 27.10.2016.
 */
public class MultiThread implements Runnable{
    /**
     * Threading
     */
    private Thread t;
    public List<Integer> stats;
    public Map<Integer, Float> distributionMap;
    public List<Integer> customer;
    public List<Integer> encounters;
    public List<Integer> focusEncounters;
    //number of threads running
    public static final int threads = 4;
    //number of days each thread simulates
    public static final int runs = 250;

    /**
     * constructor
     * @param stats
     * @param distributionMap
     */
    public MultiThread(List<Integer> stats, Map<Integer, Float> distributionMap) {
        this.stats = stats;
        this.distributionMap = distributionMap;
        this.customer = new LinkedList<>();
        this.encounters = new LinkedList<>();
        this.focusEncounters = new LinkedList<>();
    }

    /**
     * start for Multithreading
     * calls run
     */
    public void start() {
        t = new Thread(this, "Simulation");
        t.start();
    }

    /**
     * runs a specified amount of simulations(each one day)
     * adds overall encounters per day, focus person encounters and overall customers per day in a list
     */
    public void run() {
        for(int i = 0; i < runs; i++) {
            LockerSim sim = new LockerSim(stats, distributionMap);
            sim.update();
            customer.add(sim.customers);
            encounters.add(sim.encounters);
            focusEncounters.add(sim.focusEncounter);
       }
    }

    /**
     * reads a statistical survey of possible occupy times and frequency of this time of customers
     * @return list of possible occupy times and frequency of this time of customers
     */
    public static List<Integer> readStats() {
        Path path = Paths.get("Belegungszeiten.txt");
        List<Integer> list = new LinkedList<>();
        try (Stream<String> lines = Files.lines(path)) {
            lines.skip(1).forEach(s -> {
                int time = Integer.parseInt(s.split(" ")[0]);
                int people = Integer.parseInt(s.split(" ")[1]);
                for (int j = 0; j < people; j++) {
                    list.add(time*6);
                }
            });
        } catch (IOException ex) {
            System.out.println(ex);
            System.out.println("File things failed, sorry.");
        }
        return list;
    }

    /**
     * gets a statistical survey of possible occupy times and frequency of this time of customers
     * rearrange the list in a map with the key being the time and value being the times of entry/overall entry
     * @return list of possible occupy times and probability of this time of customers
     */
    public static Map<Integer, Float> generateDistributionMap(List<Integer> list){
        Map<Integer, Float> distributionMap = new HashMap<>();
        int temp = list.get(0);
        float count = 0;
        for (int i: list
             ) {
            if(i != temp) {
                distributionMap.put(temp, count);
                temp = i;
            }
            count++;
        }
        distributionMap.put(temp,count);

        for(Map.Entry<Integer, Float> entry : distributionMap.entrySet()){
            entry.setValue(entry.getValue()/count);
        }

        return distributionMap;
    }
    
    public static void main(String[] args) throws Exception{
        List<Integer> stats = readStats();
        Map<Integer, Float> distributionMap = generateDistributionMap(stats);
        List<MultiThread> threadlist = new LinkedList();
        double DCustomer = 0;
        double DEncounters = 0;
        double DFocusEncounters = 0;
        long millis1 = System.currentTimeMillis();
        for(int i = 0; i < threads ; i++) {
            MultiThread mult = new MultiThread(stats,distributionMap);
            mult.start();
            threadlist.add(mult);
        }
        for (MultiThread s : threadlist
                ) {
            s.t.join();
        }
        for (MultiThread s : threadlist){
            for (Integer cust : s.customer){
                DCustomer += cust;
            }
            for (Integer enc : s.encounters){
                DEncounters += enc;
            }
            for (Integer fenc : s.focusEncounters){
                DFocusEncounters += fenc;
            }
        }
        DCustomer = DCustomer/(threads * runs);
        DEncounters = DEncounters/(threads * runs);
        DFocusEncounters = DFocusEncounters/(threads * runs);
        long millis2 = System.currentTimeMillis();
        System.out.format("Laufzeit : %d\n", (millis2 - millis1));
        System.out.format("Durchschnittliche Kunden : %f\nDurchschnittliche Begegnungen : %f\nDurchschnittliche Begegnungen der Fokusperson : %f\n", DCustomer,DEncounters,DFocusEncounters);
    }
}
