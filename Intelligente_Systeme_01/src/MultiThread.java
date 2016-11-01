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

    public MultiThread(List<Integer> stats) {
        this.stats = stats;
        this.customer = new LinkedList<>();
        this.encounters = new LinkedList<>();
    }

    public void start() {
        t = new Thread(this, "test");
        t.start();
    }
    public void run() {
        for(int i = 0; i < 1000; i++) {
            LockerSim sim = new LockerSim(stats);
            sim.update();
            customer.add(sim.customers);
            encounters.add(sim.encounters);
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
        long millis1 = System.currentTimeMillis();
        List<Integer> stats = readStats();
        List<MultiThread> threads = new LinkedList();
        double DCustomer = 0;
        double DEncounters = 0;
        for(int i = 0; i < 10 ; i++) {
            MultiThread mult = new MultiThread(stats);
            mult.start();
            threads.add(mult);
        }
        for (MultiThread s : threads
                ) {
            s.t.join();
        }
        for (MultiThread s : threads){
            for (Integer cust : s.customer){
                DCustomer += cust;
            }
            for (Integer enc : s.encounters){
                DEncounters += enc;
            }
        }
        DCustomer = DCustomer/10000;
        DEncounters = DEncounters/10000;
        //System.out.format("sim1 encounters: %d\nsim1 customers: %d\n", sim1.encounters, sim1.customers);
        long millis2 = System.currentTimeMillis();
        System.out.format("Laufzeit : %d\n", (millis2 - millis1));
        System.out.format("Durchschnittliche Kunden : %f\n Durchschnittliche Begegnungen : %f\n", DCustomer,DEncounters);
    }
}
