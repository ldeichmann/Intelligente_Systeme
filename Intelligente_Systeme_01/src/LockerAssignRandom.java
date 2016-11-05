import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by cru on 10/23/16.
 */
public class LockerAssignRandom implements LockerAssign{

    /**
     * calls getRandomFreelocker
     * @param lockers
     * @return randome free locker
     */
    public int assignLocker(Locker[] lockers) {
        return getRandomFreeLocker(lockers);
    }


    /**
     * checks for a random locker
     * returns this locker if locker is free
     * @param lockers
     * @return free locker
     */
    public int getRandomFreeLocker(Locker[] lockers) {
        int randomNumber = ThreadLocalRandom.current().nextInt(0, lockers.length);
        if (!lockers[randomNumber].occupied) {
            return randomNumber;
        } else {
            if (Locker.occupiedLockers == lockers.length) {
                return -1;
            }
            return getRandomFreeLocker(lockers);
        }
    }
}
