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
    public static int RUNTIME = 3600; // 10 hours
    public static int TIME_TO_CHANGE = 30; // 5 minutes
    public static int NEW_CUSTOMER_PROBABILITY = 1;
    public static int NEW_CUSTOMER_PROBABILITY_RANGE = 10;

    public Locker[] lockers;
    public int time;
    public int encounters;
    public int customers;
    private LockerAssign assigner;

    public LockerSim() {
        this.assigner = new LockerAssignRandom();
        this.lockers = new Locker[LOCKER_NUM];
        for (int i = 0; i < LOCKER_NUM; i++) {
            this.lockers[i] = new Locker(i, TIME_TO_CHANGE);
        }
        this.time = 0;
        this.encounters = 0;
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
            this.customers++;
            int locker = this.assigner.assignLocker(this.lockers);
            if (locker == -1) {
                System.out.println("NO MORE FREE LOCKERS!");
                return;
            }
            int returnTime = this.getReturnTime();
            this.lockers[locker].occupy(this.time, this.time+returnTime);
        }
    }

    public void checkLockersForEncounter(Locker a, Locker b) {
        if (a.inUse && b.inUse) {
            if (!a.encounterMap.containsKey(b.id) || !b.encounterMap.containsKey(a.id)) {
                // if this is a new encounter and we have no recollection of it for either locker, increment counter
                this.encounters++;
            }
            a.encounterMap.put(b.id, b);
            b.encounterMap.put(a.id, a);
        }
    }

    public void detectEncounters() {
        for (int i = 0; i < LOCKER_NUM; i++) {
            // if Locker is in use, let's look around
            if (this.lockers[i].inUse) {
                // look three ahead for the first one
                if (i == 0) {
                    // make sure simulation isn't useless
                    if (LOCKER_NUM > 3) {
                        checkLockersForEncounter(this.lockers[i], this.lockers[i + 1]);
                        checkLockersForEncounter(this.lockers[i], this.lockers[i + 2]);
                        checkLockersForEncounter(this.lockers[i], this.lockers[i + 3]);
                    }
                // and the second one as well
                } else if (i == 1) {
                    // make sure simulation isn't useless
                    if (LOCKER_NUM > 3) {
                        checkLockersForEncounter(this.lockers[i], this.lockers[i - 1]);
                        checkLockersForEncounter(this.lockers[i], this.lockers[i + 1]);
                        checkLockersForEncounter(this.lockers[i], this.lockers[i + 2]);
                    }

                } else if (i == LOCKER_NUM-1) {
                    if (LOCKER_NUM > 3) {
                        checkLockersForEncounter(this.lockers[i], this.lockers[i - 1]);
                        checkLockersForEncounter(this.lockers[i], this.lockers[i - 2]);
                        checkLockersForEncounter(this.lockers[i], this.lockers[i - 3]);
                    }
                } else if (i == LOCKER_NUM-2) {
                    if (LOCKER_NUM > 3) {
                        checkLockersForEncounter(this.lockers[i], this.lockers[i + 1]);
                        checkLockersForEncounter(this.lockers[i], this.lockers[i - 1]);
                        checkLockersForEncounter(this.lockers[i], this.lockers[i - 2]);
                    }
                } else if (i % 2 == 0) {
                    checkLockersForEncounter(this.lockers[i], this.lockers[i - 2]);
                    checkLockersForEncounter(this.lockers[i], this.lockers[i - 1]);
                    checkLockersForEncounter(this.lockers[i], this.lockers[i + 1]);
                    checkLockersForEncounter(this.lockers[i], this.lockers[i + 2]);
                    checkLockersForEncounter(this.lockers[i], this.lockers[i + 3]);
                } else if (i % 2 == 1) {
                    checkLockersForEncounter(this.lockers[i], this.lockers[i - 3]);
                    checkLockersForEncounter(this.lockers[i], this.lockers[i - 2]);
                    checkLockersForEncounter(this.lockers[i], this.lockers[i - 1]);
                    checkLockersForEncounter(this.lockers[i], this.lockers[i + 1]);
                    checkLockersForEncounter(this.lockers[i], this.lockers[i + 2]);
                } else {
                    System.out.format("HELP! COULDN'T CHECK FOR ENCOUNTERS FOR i=%d!\n", i);
                }
            }
        }
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
        LockerSim sim1 = new LockerSim();
        sim1.start();
        System.out.format("sim1 encounters: %d\nsim1 customers: %d\n", sim1.encounters, sim1.customers);
    }

}
