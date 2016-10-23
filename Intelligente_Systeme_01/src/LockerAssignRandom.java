import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by cru on 10/23/16.
 */
public class LockerAssignRandom implements LockerAssign{

    public int assignLocker(Locker[] lockers) {
        return getRandomFreeLocker(lockers);
    }


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
