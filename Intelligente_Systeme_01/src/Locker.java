import java.util.HashMap;
import java.util.Map;

/**
 * Created by cru on 10/23/16.
 */
public class Locker {

    public static int occupiedLockers = 0;

    public int id;
    public int state;
    public Boolean occupied;
    public Boolean inUse;
    public Boolean hadEncounter;
    public Boolean focusPerson;
    private int returnTime;
    private int occupyTime;
    public float encounterProbability;

    private int time_to_change;

    /**
     * Constructor
     * @param id
     * @param time_to_change
     */
    public Locker(int id, int time_to_change) {
        this.id = id;
        this.time_to_change = time_to_change;
        this.returnTime = -1;
        this.state = 0;
        this.occupied = false;
        this.inUse = false;
        this.focusPerson = false;
        this.encounterProbability = 0;
    }

    /**
     * Setter for occupy time
     * @param time
     * @param returnTime
     */
    public void occupy(int time, int returnTime) {
        //System.out.format("Locker %d occupied, free at %d\n", id, returnTime);
        this.returnTime = returnTime;
        this.occupyTime = time;
        this.occupied = true;
        this.inUse = true;
        this.state++;
        Locker.occupiedLockers++;
        this.hadEncounter = false;
    }

    /**
     * reset for locker
     */
    public void free () {
        //System.out.format("Locker %d freed\n", id);
        this.occupied = false;
        this.inUse = false;
        this.state++;
        Locker.occupiedLockers--;
        this.hadEncounter = false;
        this.focusPerson = false;
    }

    /**
     * update on locker
     * check if locker is still inUse(costumer is changing)
     * @param time
     * @param distributionMap
     */
    public void update(int time, Map<Integer, Float> distributionMap) {
        // customer returns
        if (time == this.returnTime-time_to_change) {
            this.inUse = true;
            this.hadEncounter = false;
        // customer leaves
        } else if (time == this.returnTime) {
            this.free();
        // customer works out
        } else if (time == this.occupyTime+time_to_change) {
            this.inUse = false;
        }
        this.update_probability(time, distributionMap);
    }

    /**
     * Sets the probability
     * 0 if not occupied
     * 1 if inUse
     * gets the encounterProbability from the distributionMap
     * @param time
     * @param distributionMap
     */
    public void update_probability(int time, Map<Integer, Float> distributionMap){
        if (!this.occupied) {
            this.encounterProbability = 0;
        }
        else if (this.inUse){
            this.encounterProbability = 1;
        }
        else {
            if(distributionMap.containsKey(time - this.occupyTime)){
                this.encounterProbability = distributionMap.get(time - this.occupyTime);
            }
        }
    }
}
