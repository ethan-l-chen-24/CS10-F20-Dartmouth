/**
 * A blob with a new method to jump to a random position.
 */
public class Teleporter extends Blob {
    private int xmax, ymax;	// size of teleporting area

    public Teleporter(double x, double y, int xmax, int ymax) {
        super(x, y);
        this.xmax = xmax; this.ymax = ymax;
    }

    /**
     * Moves the blob to a random new position
     */
    public void step() {
        x = Math.random()*xmax;
        y = Math.random()*ymax;
    }

}