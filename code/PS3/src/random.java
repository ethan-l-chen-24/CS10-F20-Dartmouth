import java.util.ArrayList;

public class random {

    public static void main(String[] args) {
        int x = Integer.parseInt("100011", 2);
        System.out.println(x);
        System.out.println((char) x);

        ArrayList arr = new ArrayList();
        arr.add("a");
        arr.add("b");
        ArrayList arr2 = (ArrayList) arr.clone();
        System.out.println(arr2);
        arr2.add("c");
        System.out.println(arr2);
        System.out.println(arr);

        String codeString = "abc";
        for(String s : codeString.split("")) {
            System.out.println(s);
        }

        if(0=='0') {

        }
    }
}
