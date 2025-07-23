import java.io.Serializable;
import java.util.*;

/**
 * A small dot applying a bonding force between itself and others of its kind,
 * -- also forming bonds with other bacteria
 *
 * @author Brian Mbawa, Seth McNevin, Gladwin Ngobeni
 */
public class EPS extends Particle implements Serializable {

    /**
     * EPS bonding parameters and bonds
     */
    /*
    Mean bonding time -- 60 seconds
     */
    private static final Random RANDOM = new Random();
    private static final double MEAN_BOND = 0.3;
    protected static double BOND_CHECK_TIME = 0.0;

    /**
     * The particles {@link Bacterium}s and EPS, that this particle has bonded with
     */
    private final List<Particle> bondedParticles;


    /**
     * WCA potential parameters
     */
    private static final double SIGMA = 10;
    private static final double CUT_OFF = Math.pow(2, 1.0/6.0) * SIGMA;
    private static final double EPSILON = 0.5;

    /**
     * Creates an EPS particle at this position if the screen
     *
     * @param x The x coordinate of this EPS particle
     * @param y The y coordinate of this EPS particle
     * @param direction The direction the EPS particle is facing
     */
    public EPS (double x, double y, PVector velocity, PVector direction) {
        super(x,y,velocity,direction);
        bondedParticles = new ArrayList<>();
    }

    /**
     * Applies forces onto this EPS particle
     *
     * @param currentTime Simulation Time
     * @param otherEPS other EPS {@link Particle}s in the simulation
     * @param bacteria {@link Bacterium}s in the simulation
     */
    public void move(double currentTime, List<EPS> otherEPS, List<Bacterium> bacteria) {

        /*
        The thermal velocity of this particle -- almost negligible
         */
        thermalVelocity();


        /*
        Applies WCA force between this eps and other eps within CUT_OFF distance
         */
        applyWCAForces(otherEPS);


        /*
        Checks and applies a bond between EPS particles and bacteria
         */
        bacteriumEpsFormBond(currentTime, bacteria);
        epsFormBond(currentTime, otherEPS);

        /*
        Velocity verlet integration for particle
         */
        this.update(0.005);
    }

    /**
     * Checks if eps particles should attempt to bond with surrounding particles
     *
     * @param currentTime {@link Simulation} time
     */
    public static void checkForBond(double currentTime) {
        BOND_CHECK_TIME = currentTime + -MEAN_BOND * Math.log(1.0 - RANDOM.nextDouble());
    }

    /**
     * Imparts a negligible thermal velocity onto an EPS particle
     */
    public void thermalVelocity() {
        this.setVelocity(new PVector(0,0,0));
    }

    /**
     * Computes the force between the {@link Particle}s of different bacteria
     * -- negative gradient of the WCA potential
     *
     * @param p1 Particle 1 exerting force
     * @param p2 Particle 2 exerting force
     * @return force vector computed between particles
     */
    private PVector calculateWCAForce(Particle p1, Particle p2) {

        /*
        Calculate the distance between particles
         */
        PVector r = PVector.sub(p2.getPosition(), p1.getPosition());
        double distance = 20;

        /*
        Compute the force between particles as a factor of distance
        return a 0 force if this distance is larger than the finite distance
         */
        if (distance < CUT_OFF) {
            double inverseDist = SIGMA / distance;
            double inverseDist6 = Math.pow(inverseDist, 6);
            double inverseDist12 = inverseDist6 * inverseDist6;
            double forceMagnitude = (24 * EPSILON * (2 * inverseDist12 - inverseDist6)) / distance;
            return r.normalize().multiply(-forceMagnitude);
        } else {
            return new PVector(0,0,0);
        }
    }

    /**
     * Applies WCA force between EPS particles
     *
     * @param otherEps EPS particles in the simulation
     */
    public void applyWCAForces (List<EPS> otherEps) {
        for (EPS eps: otherEps) {
            if (this != eps) {
                PVector force = calculateWCAForce(this, eps);
                this.getNetForce().add(force.multiply(1));
                eps.getNetForce().add(force.multiply(-1));
            }
        }
    }

    /**
     *  Generates force to be applied between interacting EPS particles
     *
     * @param p1 first particle in bond
     * @param p2 second particle in bond
     * @return harmonic force to be applied
     */
    private PVector calculateEpsBondForce(Particle p1, Particle p2) {
        /*
        Calculate the distance between particles
         */
        PVector r = PVector.sub(p2.getPosition(), p1.getPosition());
        double distance = r.magnitude();

        double forceMagnitude = 200 * EPSILON * (distance - SIGMA);
        return r.normalize().multiply(-forceMagnitude);
    }

    /**
     * Generates force to be applied between interacting {@link Bacterium} particle and EPS particle
     *
     * @param p1 first particle in bond
     * @param p2 second particle in bond
     * @return harmonic force to be applied
     */
    private PVector calculateEpsBacteriumBondForce(Particle p1, Particle p2) {
        /*
        Calculate the distance between this EPS particle and another eps particle
         */
        PVector r = PVector.sub(p2.getPosition(), p1.getPosition());
        double distance = r.magnitude();

        double forceMagnitude = 100 * EPSILON * (2 * distance - SIGMA - Bacterium.SIGMA);
        return r.normalize().multiply(-forceMagnitude);
    }

    /**
     * Forms bonds between eps particles
     *
     * @param currentTime Simulation time
     * @param otherEPS other eps particles in the simulation
     */
    public void epsFormBond(double currentTime, List<EPS> otherEPS) {
        double distance;
        for (EPS epsParticle: otherEPS) {
            if (this != epsParticle) {

                PVector r = PVector.sub(this.getPosition(), epsParticle.getPosition());
                distance = r.magnitude();

                if (currentTime > BOND_CHECK_TIME && currentTime > 2) {
                    if (distance < CUT_OFF && getProbability(0.1) && !bondedParticles.contains(epsParticle)) {
                        bondedParticles.add(epsParticle);
                    }
                }

                if(distance < CUT_OFF && bondedParticles.contains(epsParticle)) {
                    PVector force = calculateEpsBondForce(this, epsParticle);

                    this.getNetForce().add(force.multiply(1));
                    epsParticle.getNetForce().add(force.multiply(-1));
                }
            }
        }
    }

    /**
     * Forms bonds between eps particles and {@link Bacterium}s
     *
     * @param currentTime Simulation time
     * @param bacteria {@link Bacterium}s in the simulation
     */
    public void bacteriumEpsFormBond(double currentTime, List<Bacterium> bacteria) {
        double distance;
        for (Bacterium bacterium: bacteria) {
            for (Particle particle: bacterium.getParticles()) {

                PVector r = PVector.sub(this.getPosition(), particle.getPosition());
                distance = r.magnitude();

                if (currentTime > BOND_CHECK_TIME && currentTime > 2) {
                    if (distance < CUT_OFF && getProbability(0.1) && !bondedParticles.contains(particle)) {
                        bondedParticles.add(particle);
                    }
                }

                if(distance < CUT_OFF && bondedParticles.contains(particle)) {
                    PVector force = calculateEpsBacteriumBondForce(this, particle);

                    this.getNetForce().add(force.multiply(1));

                    for (Particle particle1 : bacterium.getParticles()) {
                        particle1.getNetForce().add(force.multiply(-1));
                    }
                    bacterium.setFrictionCoeffecient(bacterium.getFrictionCoeffecient() + 0.00001);
                }
            }
        }
    }

    /**
     * Returns true with probability p.
     *
     * @param p Probability of returning true, between 0.0 and 1.0.
     * @return true with probability p, false otherwise.
     */
    public static boolean getProbability(double p) {
        if (p < 0.0 || p > 1.0) {
            throw new IllegalArgumentException("Probability must be between 0.0 and 1.0");
        }
        return RANDOM.nextDouble() < p;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EPS eps = (EPS) o;
        return Double.compare(eps.getPosition().getX(), getPosition().getX()) == 0 &&
                Double.compare(eps.getPosition().getY(), getPosition().getY()) == 0 &&
                Double.compare(eps.getPosition().getZ(), getPosition().getZ()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPosition().getX(), getPosition().getY(), getPosition().getZ());
    }

}
