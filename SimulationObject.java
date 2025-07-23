import java.io.Serializable;

public abstract class SimulationObject implements Serializable {

    /**
     * The x and y coordinates of this object
     */
    protected PVector position;

    /**
     * The velocity of this object
     */
    protected PVector direction;

    public SimulationObject(double x, double y, PVector direction) {
        this.position = new PVector(x,y);
        this.direction = direction;
    }

    public void setDirection(PVector direction) {
        this.direction = direction;
    }

    public PVector getDirection() {
        return this.direction;
    }

    public PVector getPosition() {
        return this.position;
    }

}
