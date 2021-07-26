public class RecursionTest {

    static String s = "";

    public static void main(String[] args) {
        test(3);
        System.out.println(s);
    }

    public static void test(int x) {
        if(x == 0) {
            return;
        }
            s += "1";
            test(x-1);
            s.substring(0, s.length()-2);
            s += "2";
            test(x-1);
        s.substring(0, s.length()-2);


    }

}
