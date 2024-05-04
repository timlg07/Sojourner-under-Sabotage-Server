public class GreenHouse {
    public enum Plant {
        EMPTY, GROWING, READY, DEAD
    }

    private Plant[] plants;

    public GreenHouse(int capacity) {
        plants = new Plant[capacity];
        for (int i = 0; i < capacity; i++) {
            plants[i] = Plant.EMPTY;
        }
    }

    public void plant(int index) {
        if (index < 0 || index >= plants.length) {
            throw new IllegalArgumentException("Invalid index");
        }
        if (plants[index] != Plant.EMPTY) {
            throw new IllegalStateException("Cannot plant here, there is already a plant");
        }
        plants[index] = Plant.GROWING;
    }

    public void water(int index) {
        if (index < 0 || index >= plants.length) {
            throw new IllegalArgumentException("Invalid index");
        }
        if (plants[index] == Plant.GROWING) {
            plants[index] = Plant.READY;
        }
    }

    public void harvest(int index) {
        if (index < 0 || index >= plants.length) {
            throw new IllegalArgumentException("Invalid index");
        }
        if (plants[index] != Plant.READY) {
            throw new IllegalStateException("Nothing to harvest here, the plant is not ready");
        }
        plants[index] = Plant.EMPTY;
    }

    public void automatic() {
        for (int i = 0; i <= plants.length - 1; i++) {
            switch (plants[i]) {
                case GROWING:
                    water(i);
                    break;
                case READY:
                    harvest(i);
                    break;
                case DEAD:
                    plants[i] = Plant.EMPTY;
                    break;
                case EMPTY:
                    plant(i);
                    break;
            }
        }
    }

    public Plant[] getPlantInfo() {
        return plants;
    }

    public void setPlantInfo(Plant[] plants) {
        this.plants = plants;
    }
}
