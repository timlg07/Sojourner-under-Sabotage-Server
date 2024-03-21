public class Demo {
    public static int add(int a, int b) {
        System.out.println("add called with a = " + a);
        if (a == 5) {
            return 5 + b;
        } else {
            return a + b;
        }
    }
}
