import java.util.ArrayList;
import java.util.List;

public class ReactorLog {

    private final List<Integer> temperatures;

    public ReactorLog(int initialTemperature) {
        temperatures = new ArrayList<>(1);
        temperatures.add(initialTemperature);
    }

    public void logTemperature(int temperature) {
        temperatures.add(temperature);
    }

    public int getCurrentTemperature() {
        return temperatures.get(temperatures.size() - 1);
    }

    public int getMaximumTemperature() {
        int max = getCurrentTemperature();
        for (int temperature : temperatures) {
            if (temperature > max) {
                max = temperature;
            }
        }
        return max;
    }

    public int logsSinceMaxTemperature() {
        int max = temperatures.get(0);
        int maxIndex = 0;
        for (int i = 1; i < temperatures.size(); i++) {
            if (temperatures.get(i) >= max) {
                max = temperatures.get(i);
                maxIndex = i;
            }
        }
        return temperatures.size() - maxIndex - 1;
    }

}
