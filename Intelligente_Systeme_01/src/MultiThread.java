import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
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
    public List<Integer> customer;
    public List<Integer> encounters;
    public List<Integer> focusEncounters;
    public static final int threads = 4;
    public static final int runs = 250;

    public MultiThread(List<Integer> stats) {
        this.stats = stats;
        this.customer = new LinkedList<>();
        this.encounters = new LinkedList<>();
        this.focusEncounters = new LinkedList<>();
    }

    public void start() {
        t = new Thread(this, "test");
        t.start();
    }
    public void run() {
        for(int i = 0; i < runs; i++) {
            LockerSim sim = new LockerSim(stats);
            sim.update();
            customer.add(sim.customers);
            encounters.add(sim.encounters);
            focusEncounters.add(sim.focusEncounter);
            //System.out.format("sim1 encounters: %d\nsim1 customers: %d\n", sim.encounters, sim.customers);
        }
        //System.out.format("sim1 encounters: %d\nsim1 customers: %d\n", this.encounters/1000, this.customers/1000);
    }

    public static List<Integer> readStats() {
        Path path = Paths.get("Belegungszeiten.txt");
        List<Integer> list = new LinkedList<>();
        try (Stream<String> lines = Files.lines(path)) {
            lines.skip(1).forEach(s -> {
//                System.out.println(s);
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

    public static void main(String[] args) throws Exception{
        List<Integer> stats = readStats();
        List<MultiThread> threadlist = new LinkedList();
        double DCustomer = 0;
        double DEncounters = 0;
        double DFocusEncounters = 0;
        long millis1 = System.currentTimeMillis();
        for(int i = 0; i < threads ; i++) {
            MultiThread mult = new MultiThread(stats);
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
        //System.out.format("sim1 encounters: %d\nsim1 customers: %d\n", sim1.encounters, sim1.customers);
        long millis2 = System.currentTimeMillis();
        System.out.format("Laufzeit : %d\n", (millis2 - millis1));
        System.out.format("Durchschnittliche Kunden : %f\nDurchschnittliche Begegnungen : %f\nDurchschnittliche Begegnungen der Fokusperson : %f\n", DCustomer,DEncounters,DFocusEncounters);
    }
}
