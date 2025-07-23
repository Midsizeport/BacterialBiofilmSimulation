import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Brian Mbawa, Seth McNevin, Gladwin Ngobeni
 *
 * Grid class for tracking {@link Bacterium} cell visit counts.
 * Uses Vector to represent grid cells.
 */
public class Grid implements Serializable {

    /*
    Size of each grid cell
     */
    private final int cellSize;

    /*
    Map to store visit counts
     */
    private final Map<PVector, Psl> visitCount;

    /**
     * Constructor for the Grid class.
     *
     * @param cellSize The size of each grid cell.
     */
    public Grid(int cellSize) {
        this.cellSize = cellSize;
        this.visitCount = new HashMap<>();
    }

    /**
     * Records a visit to a specific (x, y) position.
     *
     * @param x The x-coordinate of the position.
     * @param y The y-coordinate of the position.
     */
    public void recordVisit(double x, double y, PVector velocity) {
        PVector particlePosition = new PVector((int) x,(int) y);
        Psl PslParticle;
        if (this.visitCount.containsKey(particlePosition)) {
            PslParticle = getPslParticle((int) x, (int) y);
            PslParticle.incrementCount();
        } else {
            PslParticle = new Psl((int) x, (int) y, velocity);
        }
        this.visitCount.put(particlePosition, PslParticle);
    }

    /**
     * Gets the visit count for a specific (x, y) position.
     *
     * @param x The x-coordinate of the position.
     * @param y The y-coordinate of the position.
     * @return The visit count for the cell.
     */
    public Psl getPslParticle(double x, double y) {
        PVector particlePosition = new PVector((int) x, (int) y);
        return visitCount.get(particlePosition);
    }

    /**
     * Gets the size of each grid cell.
     *
     * @return The cell size.
     */
    public int getCellSize() {
        return cellSize;
    }

    public Map<PVector, Psl> getVisitCount() {
        return this.visitCount;
    }
}
