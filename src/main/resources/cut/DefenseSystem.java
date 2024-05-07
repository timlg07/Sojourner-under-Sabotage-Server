public class DefenseSystem {

    private static final int MIN_RANGE = 10;
    private double currentPosX;
    private double currentPosY;

    public void move(double deltaX, double deltaY) {
        currentPosX += deltaX;
        currentPosY += deltaY;
    }

    public double getCurrentPosX() {
        return currentPosX;
    }

    public double getCurrentPosY() {
        return currentPosY;
    }

    public double distanceTo(double x, double y) {
        double deltaX = x - currentPosX;
        double deltaY = y - currentPosY;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    public boolean isWithinRange(double x, double y) {
        return distanceTo(x, y) < MIN_RANGE;
    }

    private double angleTo(double x, double y) {
        double deltaX = x - currentPosX;
        double deltaY = y - currentPosY;
        return Math.atan2(deltaY, deltaX);
    }

    public void fleeFrom(double x, double y) {
        if (isWithinRange(x, y)) {
            double angle = angleTo(x, y);
            double tooClose = MIN_RANGE - distanceTo(x, y);
            move(
                    -Math.cos(angle) * tooClose,
                    -Math.sin(angle) * tooClose
            );
        }
    }
}
