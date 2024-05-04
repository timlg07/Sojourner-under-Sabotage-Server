/**
 * The engine of the rocket. It burns methane (CH4) and oxygen (O2) to generate thrust.
 *
 * One CH4 molecule will completely react with two O2 molecules.
 * As CH4 has a molecular weight of 16 and O2 has a molecular weight of 32,
 * the engine needs 4x as much O2 by weight.
 * As hot oxygen is a powerful oxidizer, the engine uses a 78:22 O2:CH4 ratio to ensure
 * the engines safety.
 */
public class Engine {

    private static final double o2toCh4Ratio = 78d / 22d;

    private boolean isInShutdown = false;


    public Engine() {
        System.out.println("Engine started");
    }

    public void shutdown() {
        isInShutdown = true;
        System.out.println("Engine shutdown");
    }

    public double getO2(double ch4) {
        if (isInShutdown) {
            /*
             * The engine is in shutdown mode and does not provide any oxygen.
             * This is a safety feature to prevent engine rich exhaust.
             */
            return 0;
        }

        return ch4 * o2toCh4Ratio;
    }
}
