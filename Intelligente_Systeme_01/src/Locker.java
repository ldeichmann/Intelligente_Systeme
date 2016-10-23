/**
 * Created by cru on 10/23/16.
 */
public class Locker {

    public static int occupiedLockers = 0;

    public int id;
    public int state;
    public Boolean occupied;
    public Boolean inUse;
    private int returnTime;
    private int occupyTime;

    private int time_to_change;

    public Locker(int id, int time_to_change) {
        this.id = id;
        this.time_to_change = time_to_change;
        this.returnTime = -1;
        this.state = 0;
        this.occupied = false;
        this.inUse = false;
    }

    public void occupy(int time, int returnTime) {
        System.out.format("Locker %d occupied\n", id);
        this.returnTime = returnTime;
        this.occupyTime = time;
        this.occupied = true;
        this.inUse = true;
        this.state++;
        Locker.occupiedLockers++;
    }

    public void free () {
        System.out.format("Locker %d freed\n", id);
        this.occupied = false;
        this.inUse = false;
        this.state++;
        Locker.occupiedLockers--;
    }

    public void update(int time) {
        if (time == this.returnTime-time_to_change) {
            this.inUse = true;
        } else if (time == this.returnTime) {
            this.free();
        } else if (time == this.occupyTime+time_to_change) {
            this.inUse = false;
        }
    }

}
