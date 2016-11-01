import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by cru on 10/23/16.
 */
public class LockerAssignDistributed implements LockerAssign{

    public int assignLocker(Locker[] lockers) {
        return getBestLocker(lockers);
    }

    public int getBestLocker(Locker[] lockers) {
        int min_pos = 0;
        float min_val = 200;
        for (int i = 0; i < lockers.length; i++) {
            // if Locker is in use, let's look around
            if (!lockers[i].occupied) {
                float sum_prob = 0;
                // look three ahead for the first one
                if (i == 0) {
                    // make sure simulation isn't useless
                    if (lockers.length > 3) {
                        sum_prob += lockers[i + 1].encounterProbability;
                        sum_prob += lockers[i + 2].encounterProbability;
                        sum_prob += lockers[i + 3].encounterProbability;
                    }
                    // and the second one as well
                } else if (i == 1) {
                    // make sure simulation isn't useless
                    if (lockers.length > 3) {
                        sum_prob += lockers[i - 1].encounterProbability;
                        sum_prob += lockers[i + 1].encounterProbability;
                        sum_prob += lockers[i + 2].encounterProbability;
                    }

                } else if (i == lockers.length-1) {
                    if (lockers.length > 3) {
                        sum_prob += lockers[i - 1].encounterProbability;
                        sum_prob += lockers[i - 2].encounterProbability;
                        sum_prob += lockers[i - 3].encounterProbability;
                    }
                } else if (i == lockers.length-2) {
                    if (lockers.length > 3) {
                        sum_prob += lockers[i + 1].encounterProbability;
                        sum_prob += lockers[i - 1].encounterProbability;
                        sum_prob += lockers[i - 2].encounterProbability;
                    }
                } else if (i % 2 == 0) {
                    sum_prob += lockers[i - 2].encounterProbability;
                    sum_prob += lockers[i - 1].encounterProbability;
                    sum_prob += lockers[i + 1].encounterProbability;
                    sum_prob += lockers[i + 2].encounterProbability;
                    sum_prob += lockers[i + 3].encounterProbability;
                } else if (i % 2 == 1) {
                    sum_prob += lockers[i - 3].encounterProbability;
                    sum_prob += lockers[i - 2].encounterProbability;
                    sum_prob += lockers[i - 1].encounterProbability;
                    sum_prob += lockers[i + 1].encounterProbability;
                    sum_prob += lockers[i + 2].encounterProbability;
                } else {
                    System.out.format("HELP! COULDN'T CHECK FOR LOCKERS FOR i=%d!\n", i);
                }
                if (sum_prob == 0){
                    return i;
                } else if (sum_prob <= min_val ){
                    min_pos = i;
                    min_val = sum_prob;
                }
            }
        }
        return min_pos;
    }

}
