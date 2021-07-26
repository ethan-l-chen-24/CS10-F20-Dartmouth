public class StoreTwo implements Comparable<StoreTwo> {
    private Character key;
    private Integer val;
    public StoreTwo(Character k, Integer v){
        key=k;
        val=v;
    }
    public Character getkey(){
        return key;
    }
    public Integer getVal(){
        return val;
    }

    @Override
    public String toString() {
        return "StoreTwo{" +
                "key=" + key +
                ", val=" + val +
                '}';
    }

    @Override
    public int compareTo(StoreTwo o) {
        return 0;
    }
}
