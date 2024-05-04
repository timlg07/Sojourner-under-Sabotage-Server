public class CryoSleep {

    private int daysRemaining;
    private boolean isFrozen;

    public CryoSleep(int forDays) {
        daysRemaining = forDays;
        isFrozen = forDays > 0;

        System.out.println("CryoSleep started for " + daysRemaining + " days");
    }

    public void dayPassed() {
        if (isFrozen) {
            daysRemaining--;
            if (daysRemaining <= 0) {
                isFrozen = false;
                System.out.println("CryoSleep ended");
            }
        } else {
            System.out.println("CryoSleep is already over");
        }
    }

    public boolean isFrozen() {
        return isFrozen;
    }
}
