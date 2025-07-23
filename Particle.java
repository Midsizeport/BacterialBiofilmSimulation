import java.io.Serializable;

/**
 * An object with velocity, acceleration and direction onto which a Net force can be applied
 *
 * @author Brian Mbawa, Seth McNevin, Gladwin Ngobeni
 */

public class Particle extends SimulationObject implements Serializable {

    /**
     * The speed of this particle
     */
    private PVector velocity;

    /**
     * The acceleration of this particle
     */
    private PVector acceleration;

    /**
     * The total force acting on this particle
     * reset to 0 after every update after time-step dt in velocity verlet
     */
    private PVector netForce;

    /**
     * The mass of this particle
     * necessary for force calculations in velocity verlet
     */
    private static final double MASS = 1;

    /**
     * Creates a Particle
     *
     * @param x the x coordinate of this particle on the screen
     * @param y the y coordinate of this particle on the screen
     * @param velocity the speed of this particle
     */
    public Particle(double x, double y, PVector velocity, PVector direction) {
        super(x , y, direction);
        this.velocity = velocity;
        this.acceleration = new PVector(0,0,0);
        this.netForce = new PVector(0,0,0);
    }

    /**
     * Solution to the kinematic equation for the motion of the bacteria
     * aka Velocity Verlet algorithm
     *
     * @param dt time-step
     */
    public void update (double dt) {
        /*
         Preserve the current position in case it is needed in future
         */
        PVector oldPosition = position.copy(); // debug

        /*
        Update the position - obtain x(t + dt)
         */
        position.add((velocity.multiply(dt)).addVector(acceleration.multiply(0.5f * dt * dt)));

        /*
        Compute forces based on the updated position
         */
        PVector newAcceleration = netForce.multiply(1.0f / MASS);

        /*
        Update the velocity using the average of the old and new acceleration
         */
        velocity.add((acceleration.addVector(newAcceleration)).multiply(0.5f * dt));

        /*
        Update the acceleration for future calculations and set netForce to ZERO
         */
        acceleration = newAcceleration;
        netForce = new PVector(0,0,0);
    }

    public void setDirection() {
        getVelocity().normalize();
    }

    public PVector getNetForce() {return this.netForce;}

    public void setNetForce(PVector netForce) {this.netForce = netForce;}

    public PVector getPosition() {
        return this.position;
    }

    public void setPosition(PVector position) {
        this.position = position;
    }

    public PVector getVelocity() {
        return this.velocity;
    }

    public void setVelocity (PVector velocity) {
        this.velocity = velocity;
    }

    public PVector getAcceleration() {
        return this.acceleration;
    }

    public void setAcceleration (PVector acceleration) {
        this.acceleration = acceleration;
    }

}
