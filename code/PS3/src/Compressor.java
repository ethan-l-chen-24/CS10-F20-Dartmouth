
import java.io.*;
import java.util.*;

public class Compressor {
    private Map<Character, Integer> f_Table;
    private BufferedReader reader;
    private PriorityQueue q;
    private Map<Character, ArrayList<Character>> code;
    private ArrayList<Character> allChars;
    private BinaryTree<StoreTwo> mainTree;

    public class TreeComparator implements Comparator<BinaryTree<StoreTwo>> {
        public int compare(BinaryTree<StoreTwo> o1, BinaryTree<StoreTwo> o2) {
            if (o1.getData().getVal() < o2.getData().getVal()) {
                return -1;
            } else if (o1.getData().getVal() == o2.getData().getVal()) {
                return 0;
            } else {
                return 1;
            }

        }

    }

    public Compressor() {
        f_Table = new HashMap<Character, Integer>();
        code = new HashMap<Character, ArrayList<Character>>();
        allChars = new ArrayList<Character>();
    }


    public void readFile(String fileName) throws IOException {
        try {
            reader = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        //if (reader.read() == -1) {
        //    System.err.println("Empty File Found");
        //}
        int charNum = reader.read();
        while (charNum != -1) {
            Character store = (char) (charNum);
            allChars.add(store);
            if (f_Table.containsKey(store)) {
                int val = f_Table.get(store) + 1;
                f_Table.put(store, val);
            } else {
                f_Table.put(store, 1);
                //System.out.println(store);
            }
            charNum = reader.read();

        }

        reader.close();
    }

    public void pQueue() {
        //ArrayList<BinaryTree<StoreTwo>> trees = new ArrayList<BinaryTree<StoreTwo>>();
        Iterator<Character> i = f_Table.keySet().iterator();
        q = new PriorityQueue<BinaryTree<StoreTwo>>(f_Table.size(), new TreeComparator());

        while (i.hasNext()) {
            Character c = i.next();
            q.add(new BinaryTree<StoreTwo>(new StoreTwo(c, f_Table.get(c)))); //add directly to q, the list is just an extra step
        }
        //q.addAll(trees);
    }

    public void createTree() {
        BinaryTree<StoreTwo> t1;
        BinaryTree<StoreTwo> t2;
        while (q.size() > 1) {
            t1 = (BinaryTree<StoreTwo>) q.remove();
            t2 = (BinaryTree<StoreTwo>) q.remove();
            BinaryTree<StoreTwo> t = new BinaryTree<StoreTwo>(new StoreTwo(null, t1.getData().getVal() + t2.getData().getVal()), t1, t2);
            q.add(t);
        }

        //System.out.println(q);
    }

    public void createMap(BinaryTree<StoreTwo> root, ArrayList<Character> path) {
        if (root == null) {
            return;
        }
        if (root.isLeaf()) {
            code.put(root.getData().getkey(), (ArrayList<Character>) path.clone());
        } else {
            path.add('0');
            createMap(root.getLeft(), path);
            path.remove(path.size() - 1);

            path.add('1');
            createMap(root.getRight(), path);
            path.remove(path.size() - 1);//            if (root.hasLeft() && root.hasRight()) {
//                createMap(root.getLeft(), path + "0");
//            } else if (root.hasRight()) {
//                path += "1";
//                createMap(root.getRight(), path);
//            }
            mainTree = (BinaryTree<StoreTwo>) q.peek();
        }

    }
//    public void compress(String fname) throws IOException {
//        readFile(fname);
//        pQueue();
//        createTree();
//        createMap((BinaryTree<StoreTwo>) q.peek(), new ArrayList<Character>());
//
//        BufferedBitWriter bitoutput = new BufferedBitWriter(fname += "_compressed");
//
//        Iterator<Character> iter = code.keySet().iterator();
//        while (iter.hasNext()) {
//            Character curr = iter.next();
//            for(int i=0; i<code.get(curr).size(); i++){
//                if (code.get(curr).get(i) == '0') {
//                    //System.out.println(c);
//                    bitoutput.writeBit(false);
//
//                } else if (code.get(curr).get(i) == '1') {
//                    bitoutput.writeBit(true);
//                }
//            }
//
//        }
//        System.out.println(f_Table);
//        System.out.println(q);
//        bitoutput.close();
//    }

    public void compress(String fname) throws IOException {
        readFile(fname);
        pQueue();
        createTree();
        createMap((BinaryTree<StoreTwo>) q.peek(), new ArrayList<Character>());
        System.out.println(code);
        ArrayList<Character> temp = new ArrayList<Character>();
        BufferedBitWriter bitoutput = new BufferedBitWriter(fname += "_compressed");

        for (int i = 0; i < allChars.size(); i++) {
            Character store = allChars.get(i);

            temp = (code.get(store));
         //   System.out.println(temp.size());
            for (int j = 0; j < temp.size(); j++) {
                if (temp.get(j) == '0') {
                    bitoutput.writeBit(false);
                } else {
                    bitoutput.writeBit(true);
                }
            }
        }

        bitoutput.close();
    }
//    public void compress(String fname) throws IOException {
//        readFile(fname);
//        pQueue();
//        createTree();
//        createMap((BinaryTree<StoreTwo>) q.peek(), new ArrayList<Character>());
//        ArrayList<Character> temp = new ArrayList<Character>();
//        BufferedBitWriter bitoutput = new BufferedBitWriter(fname += "_compressed");
//        BufferedReader t = new BufferedReader(new FileReader("PS3/test2"));
//        int charNum = t.read();
//        System.out.println(charNum);
//        while (charNum != -1) {
//            Character store = (char) (charNum);
//
//            temp = (code.get(store));
//            System.out.println(temp.size());
//            for (int i = 0; i < temp.size(); i++) {
//                if (temp.get(i) == '0') {
//                    bitoutput.writeBit(false);
//                } else {
//                    bitoutput.writeBit(true);
//                }
//            }
//            charNum = t.read();
//        }
//
//        bitoutput.close();
//        reader.close();
//    }

    public void decompress(String fname) throws IOException {

        BufferedBitReader bitInput = new BufferedBitReader(fname + "_compressed");
        BufferedWriter output = new BufferedWriter(new FileWriter(fname + "_decompressed"));

        BinaryTree<StoreTwo> curr = mainTree;
//
        while (bitInput.hasNext()) {
            boolean bit = bitInput.readBit();

            if (bit == false)
                curr = curr.getLeft();
            else
                curr = curr.getRight();

            if (curr.isLeaf()) {
                char c = curr.getData().getkey();
                output.write(c);
                curr = (BinaryTree<StoreTwo>) q.peek();
            }
        }

        bitInput.close();
        output.close();
    }

    public BinaryTree<StoreTwo> getTree() {
        return (BinaryTree<StoreTwo>) q.peek();
    }

    public PriorityQueue<BinaryTree<StoreTwo>> getq() {
        return q;
    }

    public ArrayList<Character> getAllChars() {
        return allChars;
    }

    public static void main(String[] args) throws IOException {
        Compressor test1 = new Compressor();
        //test1.readFile("PS3/test
        // 1");
//        test1.pQueue();
//        PriorityQueue<BinaryTree<StoreTwo>> t= new PriorityQueue<BinaryTree<StoreTwo>>();
//        PriorityQueue<BinaryTree<StoreTwo>> t2= new PriorityQueue<BinaryTree<StoreTwo>>();
//        t=test1.getq();
//        test1.createTree();
//        test1.createMap();
//        while(!t.isEmpty()){
//            System.out.println(t.remove().getData().getkey());
//        }
//        test1.compress("PS3/test1");
//        test1.decompress("PS3/test1");
        test1.compress("inputs/USConstitution.txt");
        test1.decompress("inputs/USConstitution.txt");
//        test1.compress("PS3/USConstitution.txt");
//        System.out.println(test1.getAllChars());
//
//        test1.decompress("PS3/USConstitution.txt");
        // test1_compressed");
    }
}