public class Foo {
    public static int add(int a, int b) {
        int s = a, x = b;
        while (x > 0) {
            System.out.println(x);
            x = x - 1;
            s = s + 1;
        }
        return s;
    }
}
