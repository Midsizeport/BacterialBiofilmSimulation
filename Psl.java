import java.io.Serializable;
import java.util.Objects;

/**
 * A small dot that acting as a glue in bacterial model, applying a directional torque
 *
 * @author Brian Mbawa, Seth McNevin, Gladin Ngobeni
 */
public class Psl extends SimulationObject implements Serializable {

    private int visitCount;

    /**
     *  Creates a psl particle at given position on the screen
     *
     * @param x The x coordinate of the psl particle
     * @param y The y coordinate of the psl particle
     * @param direction The direction in which the particle was secreted
     */
    public Psl(double x, double y, PVector direction) {
        super(x, y, direction);
        this.visitCount = 1;
    }

    public void incrementCount () {
        this.visitCount++;
    }

    public int getCount() {
        return this.visitCount;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Psl psl = (Psl) o;
        return Double.compare(psl.getPosition().getX(), getPosition().getX()) == 0 &&
                Double.compare(psl.getPosition().getY(), getPosition().getY()) == 0 &&
                Double.compare(psl.getPosition().getZ(), getPosition().getZ()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPosition().getX(), getPosition().getY(), getPosition().getZ());
    }

}
