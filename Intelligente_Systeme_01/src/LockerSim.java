import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class LockerSim{


    /**
     * Locker parameters
     */
    public static int LOCKER_NUM = 150;
    public static int LOCKER_ROWS = 2;
    public static boolean EXIT_AFTER_FOCUS = false;

    /**
     * Customer behaviour
     */
    public static int RUNTIME = 4320; // 10 hours
    public static int TIME_TO_CHANGE = 30; // 5 minutes
    public static int NEW_CUSTOMER_PROBABILITY = 1;
    public static int NEW_CUSTOMER_PROBABILITY_RANGE = 10;
    public static int FOCUS_BEGIN = 1770;
    public static int FOCUS_END = 1830;

    //Array of lockers, because the size of this structure is static for the runtime
    public Locker[] lockers;
    public int time;
    public int encounters;
    public int customers;
    private LockerAssign assigner;
    public List<Integer> stats;
    public int focusId;
    public int focusEncounter;
    public Map<Integer, Float> distributionMap;

    /**
     * Constructor
     * Initializes Assigner (Possible Strategies: LockerAssignDistributed() or LockerAssignRandom())
     * @param stats
     * @param map
     */
    public LockerSim(List<Integer> stats, Map<Integer, Float> map) {
        this.stats = stats;
        this.distributionMap = map;
        this.assigner = new LockerAssignDistributed();
        this.lockers = new Locker[LOCKER_NUM];
        for (int i = 0; i < LOCKER_NUM; i++) {
            this.lockers[i] = new Locker(i, TIME_TO_CHANGE);
        }
        this.time = 0;
        this.encounters = 0;
        this.focusEncounter = 0;
        this.focusId = -1;
    }

    /**
     * Declares if a new customer is arriving
     * Edit NEW_CUSTOMER_PROBABILITY_RANGE to increase or decrease probability
     * @return boolean
     */
    public Boolean isNewCustomer() {
        return ThreadLocalRandom.current().nextInt(1, NEW_CUSTOMER_PROBABILITY_RANGE + 1) == NEW_CUSTOMER_PROBABILITY;
    }

    /**
     * Gets a random time frame from the list of possible time frames of a customer
     * @return time a customer occupies a locker
     */
    public int getReturnTime() {
        return this.stats.get(ThreadLocalRandom.current().nextInt(0, this.stats.size()));
    }

    /**
     * Assigns arriving customers to a locker
     * Assigns focus person to a locker
     */
    public void newCustomer() {
        if (isNewCustomer()) {
            //System.out.println("NEW_CUSTOMER");
            this.customers++;
            int locker = this.assigner.assignLocker(this.lockers);
            if (locker == -1) {
                System.out.println("NO MORE FREE LOCKERS!");
                return;
            }
            int returnTime = this.getReturnTime();
            this.lockers[locker].occupy(this.time, this.time+returnTime);
            if(FOCUS_BEGIN <= this.time  && this.time <= FOCUS_END && this.focusId == -1){
                this.focusId = locker;
                this.lockers[this.focusId].focusPerson = true;
            }
        }
    }

    /**
     * Checking for encounters on both lockers (for customer and focus person)
     * @param a
     * @param b
     */
    public void checkLockersForEncounter(Locker a, Locker b) {
        if (a.inUse && b.inUse) {
            if (!a.hadEncounter || !b.hadEncounter) {
                // if this is a new encounter and we have no recollection of it for either locker, increment counter
                this.encounters++;
                a.hadEncounter = true;
                b.hadEncounter = true;
                if(a.focusPerson || b.focusPerson){
                    focusEncounter++;
                }
            }
        }
    }

    /**
     * Checks which lockers have to be checked for encounters
     * List of lockers is going to be iterated and if one locker is currently used(locker is inUse, if customer is changing)
     * check for every possible scenario (scenarios: locker is one of the first 2/ locker is one of the last 2/ locker is one the first or second row)
     */
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
    }

    /**
     * start for Sim
     */
    public void start() {
            this.update();
    }

    /**
     * Updater
     * updates every locker, checks if customer is arriving and calls method to check for encounters
     * increments time by 1(10 seconds)
     */
    public void update() {
        //System.out.format("Update! time: %d\n", this.time);
        for (Locker l : this.lockers) {
            l.update(this.time, distributionMap);
        }
        this.newCustomer();
        this.detectEncounters();
        this.time++;
        /*
        if EXIT_AFTER_FOCUS {
            if i > FOCUS_END && focus_locker_id != -1 {
                if !locker_array[focus_locker_id as usize].focus {
                    return (i, customers, encounters, focus_encounters);
                }
            }
        }
         */
        if(EXIT_AFTER_FOCUS) {
            if (this.time > FOCUS_END && focusId != -1) {
                if (!lockers[focusId].focusPerson) {
                    return;
                }
            }
        }
        if (this.time < this.RUNTIME) {
            this.update();
        }
    }
}
