public class Method1 {

    public static void main(String[] args) {
        int a = args[0].charAt(0)-'0'; // this expression must not be constant!
        int b = args[0].charAt(0)-'0'; // this expression must not be constant!
        int c = getFirst(a, b);
        ++c;
    }

    private static int getFirst(int first, int second) {
        return first;
    }

}

