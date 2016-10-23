import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by cru on 10/23/16.
 */
public class LockerSim {

    /**
     * Locker parameters
     */
    public static int LOCKER_NUM = 150;
    public static int LOCKER_ROWS = 2;

    /**
     * Customer behaviour
     */
    public static int RUNTIME = 3600; // 1 hour
    public static int TIME_TO_CHANGE = 30; // 5 minutes
    public static int NEW_CUSTOMER_PROBABILITY = 1;
    public static int NEW_CUSTOMER_PROBABILITY_RANGE = 10;

    public Locker[] lockers;
    public int time;
    private LockerAssign assigner;

    public LockerSim() {
        this.assigner = new LockerAssignRandom();
        this.lockers = new Locker[LOCKER_NUM];
        for (int i = 0; i < LOCKER_NUM; i++) {
            this.lockers[i] = new Locker(i, TIME_TO_CHANGE);
        }
    }


    public Boolean isNewCustomer() {
        return ThreadLocalRandom.current().nextInt(1, NEW_CUSTOMER_PROBABILITY_RANGE + 1) == NEW_CUSTOMER_PROBABILITY;
    }


    public int getReturnTime() {
        //TODO
        return ThreadLocalRandom.current().nextInt(2*TIME_TO_CHANGE, 5*TIME_TO_CHANGE);
    }

    public void newCustomer() {
        if (isNewCustomer()) {
            System.out.println("NEW_CUSTOMER");
            int locker = this.assigner.assignLocker(this.lockers);
            if (locker == -1) {
                System.out.println("NO MORE FREE LOCKERS!");
                return;
            }
            int returnTime = this.getReturnTime();
            this.lockers[locker].occupy(this.time, returnTime);
        }
    }

    public void detectEncounters() {
        //TODO
//        System.out.println("DETECT ENCOUNTERS!");
    }

    public void start() {
        this.update();
    }

    public void update() {
        System.out.format("Update! time: %d\n", this.time);
        for (Locker l : this.lockers) {
            l.update(this.time);
        }
        this.newCustomer();
        this.detectEncounters();
        this.time++;
        if (this.time < this.RUNTIME) {
            this.update();
        }
    }

    public static void main(String[] args) {
        new LockerSim().start();
    }

}
