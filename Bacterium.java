import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;

/**
 * A bacterial object that can move and rotate, secrete PSL, avoid other bacteria and
 * reproduce itself.
 *
 * @author Brian Mbawa, Seth McNevin, Gladwin Ngobeni
 */

public class Bacterium implements Serializable {

    /**
     * The strain or type of this bacterium -- not currently a feature, but may add
     */
    private String strain;

    /**
     * The color of this bacterium
     */
    private Color color;
    /**
     * This bacterium's particles
     */
    private final List<Particle> particles;

    /**
     * This bacterium's daughters -- mostly empty
     */
    private final List<Bacterium> daughters;

    /**
     * This bacterium's velocity - imparted onto each of its particles
     */
    private PVector bacteriaVelocity;

    /**
     * This bacterium's direction - imparted onto each of its particles
     */
    private PVector bacteriaDirection;

    /**
     * The transverse width of bacterium
     */
    protected static final double TRANSVERSE_WIDTH = 0.6 * 16.6667;

    /**
     * The rest length of a bacterium
     */
    private static final double LO = 3.333333;

    /**
     * Spring Harmonic parameters
     */
    private static final double EPSILON = 1;
    private static final double STIFFNESS = (250 * EPSILON / Math.pow(TRANSVERSE_WIDTH,2));

    /**
     * Angular harmonic interactions parameters
     */
    private static final double BENDING_STIFFNESS = 20 * EPSILON;
    private static final double REST_ANGLE = Math.PI;

    /**
     * Run parameters
     */
    private static final double V_RUN = 4;
    private boolean isRunning = false;
    private double runEndTime = 0.0;
    /*
    Time constant for running state in seconds
     */
    private static  double trun = 0.1 * 60;

    /**
     * Tumble parameters
     */
    private boolean isTumbling = false;
    private double tumbleEndTime = 0.0;
    /*
    Flag to determine the direction of torque -- Can be applied in both directions
     */
    private boolean clockwise = true;
    /*
    Time constant for tumble state in seconds
     */
    private static double tumble = 0.5 * 60;
    private static final Random RANDOM = new Random();

    /**
     * Viscosity parameters
     */
    private double frictionCoffecient;

    /**
     * WCA potential parameters
     */
    protected static final double SIGMA = TRANSVERSE_WIDTH;
    private static final double CUT_OFF = Math.pow(2, 1.0/6.0) * SIGMA;

    /**
     * Growth parameters
     */
    private static final double MAX_REST_LENGTH = 2 * LO;
    private static final double MEAN_TR = 3600;
    private final double birthTime;
    private final double growthRate;

    /**
     * Reproduction parameters
     */
    private boolean reproduced = false;
    private Bacterium father;

    /**
     * PSL torque parameters
     */
    private boolean PslClockwise = true;

    /**
     * EPS parameters
     */
    private double insertionRate;
    private double nextInsertionTime = 0.0;

    /**
     * current time of bacteria
     */
    private double currentTime;

    private static boolean canReproduce = true;

    /**
     * Motility parameters
     */
    private static boolean motile = true;

    /**
     * Creates a bacterium
     *
     * @param strain type of bacteria - extra feature hopefully
     * @param color color of this bacterium
     * @param x the first entry of this bacterium's position vector
     * @param y the second entry of this bacterium's position vector
     */
    public Bacterium(String strain, Color color,boolean motile, double x, double y, double currentTime, List<Bacterium> otherBacteria) {
        this.color = color;
        this.strain = strain;
        particles = new ArrayList<>();
        daughters = new ArrayList<>();
        this.frictionCoffecient = 0.1;
        // ########################### Change back
        Bacterium.motile = motile;

        final double MIN_VELOCITY_MAGNITUDE = 1.0;
        /*
        Generate a random direction vector
         */
        double randomDirectionX = Math.random() - 0.5;
        double randomDirectionY = Math.random() - 0.5;

        /*
        Normalize the direction vector
         */
        PVector direction = new PVector(randomDirectionX, randomDirectionY).normalize();
        bacteriaVelocity =  direction.multiply(MIN_VELOCITY_MAGNITUDE);
        bacteriaDirection = bacteriaVelocity.normalize();

        this.birthTime = currentTime;
        this.growthRate = generateGrowthRate();
        this.insertionRate = (1.0/60.0) * growthRate;

        initialiseParticles(x, y, currentTime, true, otherBacteria);

        /*
        Set time based parameters
         */
        startRunning(currentTime);
    }

    /**
     * Secondary constructor for daughter bacteria
     */
    public Bacterium(String strain, Color color, boolean motile, double x, double y, double currentTime, PVector bacteriaVelocity, List<Bacterium> otherBacteria, Bacterium father) {
        this.color = color;
        this.strain = strain;
        particles = new ArrayList<>();
        daughters = new ArrayList<>();
        this.frictionCoffecient = 0.1;
        this.father = father;
        // ####################### Change back
        this.motile = motile;

        /*
         Use the provided bacteriaVelocity and bacteriaDirection
         */
        this.bacteriaVelocity = bacteriaVelocity;
        this.bacteriaDirection = bacteriaVelocity.normalize(); // Ensure bacteriaDirection is normalized

        this.birthTime = currentTime;
        this.growthRate = generateGrowthRate();
        this.insertionRate = (1.0 / 60.0) * growthRate;

        initialiseParticles(x, y, currentTime, true, otherBacteria);

        /*
         Set time based parameters
         */
        startRunning(currentTime);
    }

    /**
     * Copy constructor
     *
     * @param otherBacterium other bacterium from which to copy
     */
    public Bacterium(Bacterium otherBacterium) {
        setColor(otherBacterium.getColor());
        setStrain(otherBacterium.getStrain());
        this.particles = new ArrayList<>();
        this.daughters = new ArrayList<>();
        this.isRunning = otherBacterium.isRunning;
        this.runEndTime = otherBacterium.runEndTime;
        this.isTumbling = otherBacterium.isTumbling;
        this.tumbleEndTime = otherBacterium.tumbleEndTime;
        this.bacteriaVelocity = otherBacterium.bacteriaVelocity;
        this.bacteriaDirection = otherBacterium.bacteriaDirection;
        this.clockwise = otherBacterium.clockwise;
        this.birthTime = otherBacterium.birthTime;
        this.growthRate = otherBacterium.growthRate;
        this.reproduced = otherBacterium.reproduced;
        this.PslClockwise = otherBacterium.PslClockwise;
        this.insertionRate = otherBacterium.insertionRate;
        this.nextInsertionTime = otherBacterium.nextInsertionTime;
        this.frictionCoffecient = otherBacterium.frictionCoffecient;
        this.currentTime = otherBacterium.currentTime;
        this.father = otherBacterium.father;
    }

    /**
     * Initializes the seven particles of a bacterium
     *
     * @param startX The x coordinate of the head particle
     * @param startY The y coordinate of the head particle
     */
    public void initialiseParticles(double startX, double startY, double currentTime, boolean useLO, List<Bacterium> otherBacteria) {
        double restLength = useLO ? LO : getCurrentRestLength(currentTime);
        /*
        The change in the position of particles as per angle
         */
        double dx = (restLength * Math.cos(bacteriaDirection.direction()));
        double dy = (restLength * Math.sin(bacteriaDirection.direction()));

        /*
        Initialise particles with valid positions
         */
        int n = 7;
        for (int i = 0; i < n; i++) {
            double x = startX + i * dx;
            double y = startY + i * dy;
            if (particles.size() < 7) {
                Particle particle = new Particle(x, y, bacteriaVelocity, bacteriaVelocity);
                particles.add(particle);
            } else {
                particles.get(i).getPosition().setX(x);
                particles.get(i).getPosition().setY(y);
            }
        }
    }

    /**
     * Check if the spawn area of this bacterium is valid
     *
     * @param positions The positions of this bacterium's particles
     * @param otherBacteria Other bacteria in the simulation
     * @return if this bacterium's initialization positions are valid
     */
    private boolean areAllPositionsValid(List<PVector> positions, List<Bacterium> otherBacteria) {
        for (PVector pos: positions) {
            /*
            Check against existing bacteria
             */
            for (Bacterium other : otherBacteria) {
                if (other == father) {
                    continue;
                }
                for (Particle particle : other.getParticles()) {
                    if (pos.distance(particle.getPosition()) < 10) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /**
     * Triggers bacterium into a run state
     *
     * @param currentTime Time of the simulation
     */
    public void startRunning(double currentTime) {
        isRunning = true;
        runEndTime = currentTime + -trun * Math.log(1.0 - RANDOM.nextDouble());
    }


    /**
     * Triggers bacterium into a tumble state
     *
     * @param currentTime Time of the simulation
     */
    public void startTumbling(double currentTime) {
        isTumbling = true;
        tumbleEndTime = currentTime + -tumble * Math.log(1.0 - RANDOM.nextDouble());
    }


    /**
     * Calculates the next time that bacteria will insert EPS into the environment
     *
     * @param currentTime Time of the simulation
     */
    public void nextInsertionTimeEPS(double currentTime) {
        nextInsertionTime = currentTime + insertionRate;
    }


    /**
     * Generates a growth rate (tr) from an exponential distribution with mean 1 hour
     *
     * @return growth rate
     */
    private double generateGrowthRate() {
        return -MEAN_TR * Math.log(1.0 - RANDOM.nextDouble());
    }


    /**
     * Applies net force on a single bacterium dictating how the bacterium moves
     *
     * @param currentTime {@link Simulation} time
     * @param xMin Minimum environment x value
     * @param xMax Maximum environment x value
     * @param yMin Minimum environment y value
     * @param yMax Maximum environment y value
     * @param otherBacteria Arraylist of bacteria between which forces occur
     * @param pslGrid Psl {@link Grid} keeping count of psl secreted in environment in a particular cell
     */
    public void move(double currentTime, double xMin, double xMax, double yMin, double yMax, List<Bacterium> otherBacteria, Grid pslGrid, List<EPS> EPS) {

        /*
        Applies force to particles of this bacterium
         */
        applyForces(currentTime, pslGrid);

        /*
        Calculates PSL trail interaction energies
         */
        //pslAttractiveForce(pslGrid, otherBacteria, TRANSVERSE_WIDTH / 2);

        /*
        Apply attractive and repulsive forces to particles as WCA potential
         */
        spatialApplyWCAForces(otherBacteria);

        /*
        Velocity verlet integration for each particle
         */
        for (Particle curParticle : particles) {
            /*
            update positions of bacteria particle's with time step dt = 0.005
             */
            curParticle.update(0.005);
        }

        /*
        Record visit of the central particle
         */
        if (isRunning && motile) recordVisit(pslGrid);

        /*
        Insert EPS into simulation environment
         */
        if (isRunning && motile) insertEPS(currentTime, EPS);

        /*
        This bacterium attempts to reproduce
         */
        if(canReproduce) {
            tryReproduce(currentTime, otherBacteria);
        }

        /*
        Process the bacterium when crossing environment boundaries
         */
        checkBoundaries(xMin, xMax, yMin, yMax, currentTime, otherBacteria);
    }

    /**
     * Computes the current rest length based on time
     *
     * @param currentTime The time of the simulation
     * @return rest length factoring in growth rate
     */
    private double getCurrentRestLength(double currentTime) {
        double elapsedTime =  currentTime - birthTime;
        double growth = Math.min(elapsedTime, 1.2 * growthRate);
        return Math.min(LO + growth, MAX_REST_LENGTH);
    }

    public void applyForces(double currentTime, Grid grid) {
        /*
        Harmonic Spring
         */
        PVector harmonicForce;
        PVector harmonicDisplacement;
        PVector harmonicDirection;
        double harmonicDistance;
        double currentRestLength = getCurrentRestLength(currentTime);

        /*
        Angular Harmonic
         */
        PVector angHarmForce;
        PVector angHarmDirection1, angHarmDirection2;
        double angHarmAngle, angularDisplacement;

        /*
        Determine if the torque should be clockwise or counterclockwise
         */
        boolean isClockwise = RANDOM.nextBoolean();
        setClockwise(isClockwise);
        /*
        Select the pivot particle at which torque is applied
         */
        Particle pivotParticle = particles.get(3);
        /*
        Get the position of the pivot particle
         */
        PVector pivotPosition = pivotParticle.getPosition();

        if (isRunning) {
            if (currentTime > runEndTime) { // if it should stop running
                /*
                End running state
                 */
                isRunning = false;
                /*
                Start tumbling state
                 */
                startTumbling(currentTime);
            }
        }

        if (isTumbling) {
            if (currentTime > tumbleEndTime) { // if it should stop tumbling
                /*
                End tumbling state
                 */
                isTumbling = false;
                /*
                Start running state
                 */
                startRunning(currentTime);
            }
        }

        /*
        Applies torque on bacterium to align with psl trails
         */
        boolean torqueClockwise = RANDOM.nextBoolean();
        setPslClockwise(torqueClockwise);
        boolean pslAlign = grid.getVisitCount().containsKey(new PVector((int) particles.get(0).getPosition().getX(), (int) particles.get(0).getPosition().getY())) && isRunning;
        double forceMagnitude;
        double dotProduct;
        Particle p1; 
        Particle p2;
        Particle p3;
        double magProduct;
        double cosTheta;
        double torqueMagnitude;
        PVector perpendicular;
        Psl PslParticle;
        PVector dampingForce;
        PVector r;
        PVector torque;

        for (int i = 0; i < particles.size(); i++) {
             p1 = particles.get(i);
            /*
            Harmonic Spring -- Applies elastic restoration force returning length between particles to rest(lO)
            */
            if (i < particles.size() - 1) {
                p2 = particles.get(i + 1);

                harmonicDisplacement =  PVector.sub(p2.getPosition(),p1.getPosition());
                harmonicDistance = harmonicDisplacement.magnitude();
                forceMagnitude = STIFFNESS * (harmonicDistance - currentRestLength);
                harmonicDirection = harmonicDisplacement.normalize();
                harmonicForce = harmonicDirection.multiply(-1 * forceMagnitude);

                p1.getNetForce().add(harmonicForce.multiply(-1));
                p2.getNetForce().add(harmonicForce.multiply(2100));
            }

            /*
            Applies angular restoration force returning angle between particles to rest(pi radians)
             */
            if (i < particles.size() - 2) {
                p1 = particles.get(i);
                p2 = particles.get(i + 1);
                p3 = particles.get(i + 2);

                /*
                Compute vector differences between particles
                */
                angHarmDirection1 = PVector.sub(p1.getPosition(), p2.getPosition());
                angHarmDirection2 = PVector.sub(p3.getPosition(), p2.getPosition());

                /*
                Compute the angle between the two vectors in radians
                 */
                dotProduct = angHarmDirection1.dot(angHarmDirection2);
                magProduct = (angHarmDirection1.magnitude() * angHarmDirection2.magnitude());

                cosTheta = dotProduct / magProduct;
                cosTheta = Math.max(-1.0, Math.min(1.0, cosTheta));
                angHarmAngle = Math.acos(cosTheta);

                angularDisplacement = angHarmAngle - REST_ANGLE;

                /*
                Calculate the intermolecular torque (force) to apply
                */
                torqueMagnitude = BENDING_STIFFNESS * angularDisplacement;

                /*
                Compute the perpendicular vector to apply the torque
                 */
                perpendicular = (angHarmDirection1.cross(angHarmDirection2)).normalize();

                /*
                Apply torque force to the particles to restore rest angle
                 */
                angHarmForce = perpendicular.multiply(torqueMagnitude);

                /*
                Apply the torque force to the particles to restore rest angle
                 */
                p1.getNetForce().add(angHarmForce.multiply(-1f)); // Apply force equally but opposite
                p2.getNetForce().add(angHarmForce.multiply(1f));  // Apply force to the center particle
                p3.getNetForce().add(angHarmForce.multiply(-1f)); // Apply force equally but opposite
            }

            /*
            Fixes rotational velocity (Torque) force to the particles making up the bacterium
             */
            if (isTumbling) {
                /*
                The position vector relative to the pivot point
                 */
                r = PVector.sub(p1.getPosition(), pivotPosition);
                /*
                Compute the torque as the cross product of r and the force vector
                 */
                
                if (isClockwise) {
                    torque = r.cross(new PVector(0, 0, 10));
                } else {
                    torque = r.cross(new PVector(0, 0, -10));
                }
                /*
                Apply the torque to each particle
                 */
                if (motile) {
                    p1.setVelocity(torque);
                } else {
                    p1.setVelocity(new PVector(0,0,0));
                }
            }

            /*
            Applies a run force on the particles making this bacterium
             */
            if (isRunning) {
                updateDirection();
                if (motile) {
                    p1.setVelocity(bacteriaVelocity.normalize().multiply(V_RUN/frictionCoffecient));
                } else {
                    p1.setVelocity(new PVector(0,0,0));
                }
            }

            /*
            Applies viscous damping force to each particle making up a bacterium
             */
            dampingForce = bacteriaVelocity.multiply(-frictionCoffecient);
            /*
            Apply the damping force to the particle's net force
             */
            p1.getNetForce().add(dampingForce);

            if (pslAlign) {
                PslParticle = grid.getPslParticle(particles.get(0).getPosition().getX(), particles.get(0).getPosition().getY());

                r = PVector.sub(p1.getPosition(), pivotPosition);
                if (torqueClockwise) {
                    torque = r.cross(new PVector(0, 0, PslParticle.getDirection().direction() * 5));
                } else {
                    torque = r.cross(new PVector(0, 0, -(PslParticle.getDirection().direction() * 5)));
                }
                if (motile) {
                    p1.setVelocity(torque);
                } else {
                    p1.setVelocity(new PVector(0,0,0));
                }
            }
        }
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
        double distance = r.magnitude();

        /*
        Compute the force between particles as a factor of distance
        return a 0 force if this distance is larger than the finite distance
         */
        if (distance < CUT_OFF) {
            double inverseDist = SIGMA / distance;
            double inverseDist6 = Math.pow(inverseDist, 6);
            double inverseDist12 = inverseDist6 * inverseDist6;
            double forceMagnitude = 24 * EPSILON * (2 * inverseDist12 - inverseDist6) / distance;
            return r.normalize().multiply(-2000);
        } else {
            return new PVector(0,0,0);
        }
    }

    /**
     *
     * @param otherBacteria other bacteria in the simulation
     */
    public void spatialApplyWCAForces(List<Bacterium> otherBacteria) {
        // Create a spatial grid of grid size 5
        SpatialGrid grid = new SpatialGrid(20, this.particles);

        // Process each bacterium in parallel, excluding the current one
        otherBacteria.parallelStream()
                // Ensures the WCA forces are not applied to itself
                .filter(otherBacterium -> !this.equals(otherBacterium))
                .forEach(otherBacterium -> {
                    // For each particle in the other bacterium
                    otherBacterium.getParticles().forEach(p2 -> {
                        // Get nearby particles from the spatial grid
                        List<Particle> nearbyParticles = grid.getNearbyParticles(p2);
                        // Calculate and apply WCA forces between particles
                        nearbyParticles.forEach(p1 -> {
                            PVector force = calculateWCAForce(p1, p2);
                            for (int i = 0; i < this.particles.size(); i++) {
                                this.particles.get(i).getNetForce().add(force.multiply(1));
                                otherBacterium.getParticles().get(i).getNetForce().add(force.multiply(-10));
                            }
                        });
                    });
                });
    }

    /**
     * Spatial grid class to manage particles in a grid structure
     */
    static class SpatialGrid {
        private final int gridSize;
        private final Map<Point, List<Particle>> grid;

        // Constructor to initialize the grid with particles
        public SpatialGrid(int gridSize, List<Particle> particles) {
            this.gridSize = gridSize;
            this.grid = new HashMap<>();
            Point gridPoint;
            for (Particle p : particles) {
                gridPoint = getGridPoint(p);
                grid.computeIfAbsent(gridPoint, k -> new ArrayList<>()).add(p);
            }
        }

        /**
         * Get nearby particles within the neighbouring grid cells
         *
         * @param p The particle for which to compute the nearby {@link Particle}s
         * @return collection of nearby particles
         */
        public List<Particle> getNearbyParticles(Particle p) {
            Point gridPoint = getGridPoint(p);
            List<Particle> nearbyParticles = new ArrayList<>();
            Point neighbourPoint;
            // Check the current grid cell and its neighbours
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    neighbourPoint = new Point(gridPoint.x + dx, gridPoint.y + dy);
                    nearbyParticles.addAll(grid.getOrDefault(neighbourPoint, Collections.emptyList()));
                }
            }
            return nearbyParticles;
        }

        /**
         * Convert particle position to grid point
         * @param p The particle to convert into a grid point
         * @return Particle's position as a grid point
         */
        private Point getGridPoint(Particle p) {
            int x = (int) (p.getPosition().getX() / gridSize);
            int y = (int) (p.getPosition().getY() / gridSize);
            return new Point(x, y);
        }
    }

    /**
     * This bacterium attempts to reproduce on checking if fully matured
     * daughters share their father's polarity
     *
     * @param currentTime time of reproduction
     */
    public void tryReproduce (double currentTime, List<Bacterium> otherBacteria) {
        /*
        Check if the bacterium has reached the reproduction length
         */
        if (getCurrentRestLength(currentTime) >= MAX_REST_LENGTH && frictionCoffecient < 1) {

            /*
            Set reproduce to true
             */
            setReproduced(true);

            // Get the current position of the head particle
            double parentX = getHeadParticleXPos();
            double parentY = getHeadParticleYPos();

            // Get the normalized direction vector of the parent
            PVector direction = getBacteriaDirection().normalize();

            // Compute the perpendicular vector (for 2D: (-y, x))
            PVector perp = new PVector(-direction.getY(), direction.getX()).normalize();

            // Offset distance (half the bacterium's width)
            double offset = TRANSVERSE_WIDTH;

            // Daughter 1: one side of the parent
            double d1x = parentX + perp.getX() * offset / 2;
            double d1y = parentY + perp.getY() * offset / 2;

            // Daughter 2: other side of the parent
            double d2x = parentX - perp.getX() * offset / 2;
            double d2y = parentY - perp.getY() * offset / 2;

            Bacterium daughter1 = new Bacterium(this.getStrain(), this.getColor(), Bacterium.motile, d1x, d1y, currentTime, direction, otherBacteria, this);
            Bacterium daughter2 = new Bacterium(this.getStrain(), this.getColor(), Bacterium.motile, d2x, d2y, currentTime, direction, otherBacteria, this);

            /*
            Add the daughter bacteria to the list
             */
            this.daughters.add(daughter1);
            this.daughters.add(daughter2);
        }
    }


    /**
     * Adds PSL trail energy to the total energy of the model
     *
     * @param grid PSL grid
     * @param otherBacteria list of bacteria in the simulation
     * @param sigma Half of the transverse width of a bacteria
     */
    public static void pslAttractiveForce(Grid grid, List<Bacterium> otherBacteria, double sigma) {
        double totalEnergy = 0.0;
        int visitCount;
        double distance;
        double gaussianAttractivePotential;
        PVector r;
        /*
        Iterate over all bacteria and their particles
         */
        for (Bacterium bacterium: otherBacteria) {
            for (Particle particle: bacterium.getParticles()) {
                for (PVector pslParticle: grid.getVisitCount().keySet()) {
                    /*
                    get the visit count of pslParticle
                     */
                    visitCount = grid.getPslParticle(pslParticle.getX(), pslParticle.getY()).getCount();

                    /*
                    Calculate the Gaussian potential
                     */
                    distance = pslParticle.distance(particle.getPosition());
                    gaussianAttractivePotential = (2 * (sigma) * distance * Math.exp(sigma * -Math.pow(distance,2)));

                    /*
                    Calculate total model energy contribution
                     */
                    totalEnergy += visitCount * gaussianAttractivePotential;

                    /*
                    Apply attractive force to particles of a bacterium
                     */
                    r = PVector.sub(pslParticle, particle.getPosition()).normalize();
                    particle.getNetForce().add(r.multiply(totalEnergy).multiply(0.000001));
                }
            }
            totalEnergy = 0;
        }
    }


    /**
     * Deposits psl and records trails in PSL {@link Grid}
     *
     * @param pslGrid the simulation psl {@link Grid} shared by all bacteria
     */
    private void recordVisit(Grid pslGrid) {
        Particle centreParticle = particles.get(3);
        pslGrid.recordVisit(centreParticle.getPosition().getX(), centreParticle.getPosition().getY(), centreParticle.getVelocity().normalize());
    }


    /**
     * Inserts EPS Particle into the simulation environment
     *
     * @param currentTime the time of the simulation
     * @param EPS EPS particle inserted into environment
     */
    private void insertEPS(double currentTime, List<EPS> EPS) {
        Particle centreParticle = particles.get(3);
        PVector ZERO = new PVector(0,0,0);
        EPS eps = new EPS(centreParticle.getPosition().getX(), centreParticle.getPosition().getY(),ZERO,ZERO);
        if (currentTime > nextInsertionTime && currentTime > 1 && !EPS.contains(eps)) {
            /*
            Insert EPS Particle into environment
             */
            EPS.add(eps);
            /*
            Compute next EPS insertion rate
             */
            nextInsertionTimeEPS(currentTime);
        }
    }


    /**
     * Handles the {@link Bacterium}s crossing simulation boundaries
     *
     * @param xMin Minimum environment x value
     * @param xMax Maximum environment x value
     * @param yMin Minimum environment y value
     * @param yMax Maximum environment y value
     */
    private void checkBoundaries(double xMin, double xMax, double yMin, double yMax, double currentTime, List<Bacterium> otherBacteria) {
        /*
         Check for bacterium leaving simulation boundaries
         */
        if (getHeadParticleXPos() > xMax) {
            initialiseParticles(xMin, getHeadParticleYPos(), currentTime, false, otherBacteria);
        } else if (getHeadParticleXPos() < xMin) {
            initialiseParticles(xMax, getHeadParticleYPos(), currentTime, false, otherBacteria);
        }

        if (getHeadParticleYPos() > yMax) {
            initialiseParticles(getHeadParticleXPos(), yMin, currentTime, false, otherBacteria);
        } else if (getHeadParticleYPos() < yMin) {
            initialiseParticles(getHeadParticleXPos(), yMax, currentTime, false, otherBacteria);
        }
    }


    /**
     * Check if the bacterium is valid (e.g., if particles are initialized)
     *
     * @return true if this bacterium object is valid
     */
    public boolean isValid() {
        return !particles.isEmpty();
    }


    /**
     * Computes the new direction that this bacterium is facing when called through the distance vector
     *
     * @return direction that this bacterium is facing
     */
    public PVector getBacteriumDirection() {
        /*
        Get head and tail particle of bacterium
         */
        Particle headParticle = particles.get(0);
        Particle tailParticle = particles.get(6);

        /*
        Obtain difference between these particles and normalize to get the direction
         */
        PVector direction = PVector.sub(tailParticle.getPosition(), headParticle.getPosition());
        return direction.normalize();
    }


    /**
     * Updates the direction of this bacterium
     */
    public void updateDirection() {
        /*
        Compute the new direction vector
         */
        PVector newDirection = getBacteriumDirection();

        /*
        Update the bacterium's velocity and direction
         */
        bacteriaVelocity = newDirection.multiply(bacteriaVelocity.normalize().multiply(V_RUN / frictionCoffecient).magnitude());
        bacteriaDirection = newDirection;

        //Used for testing against paper results
        //updateColorBasedOnDirection();
    }


    /**
     * Obtains the x coordinate of the head particle
     *
     * @return the x coordinate of the head {@link Particle}
     */
    public double getHeadParticleXPos() {
        return particles.get(0).getPosition().getX();
    }


    /**
     * Obtains the y coordinate of the head particle
     *
     * @return the y coordinate of the head {@link Particle}
     */
    public double getHeadParticleYPos() {
        return particles.get(0).getPosition().getY();
    }

    public List<Particle> getParticles() { return this.particles;}

    public List<Bacterium> getDaughters() { return this.daughters;}

    public PVector getBacteriaVelocity() {
        return bacteriaVelocity;
    }

    public void setBacteriaVelocity(PVector bacteriaVelocity) {
        this.bacteriaVelocity = bacteriaVelocity;
    }

    public PVector getBacteriaDirection() {
        return bacteriaDirection;
    }

    public void setBacteriaDirection(PVector bacteriaDirection) {
        this.bacteriaDirection = bacteriaDirection;
    }

    public void setStrain(String strain) {
        this.strain = strain;
    }

    public String getStrain() {
        return strain;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setClockwise(boolean clockwise) {
        this.clockwise = clockwise;
    }

    public double getFrictionCoeffecient() {
        return frictionCoffecient;
    }

    public void setFrictionCoeffecient(double frictionCoeffecient) {
        this.frictionCoffecient = frictionCoeffecient;
    }

    public boolean isClockwise() {
        return clockwise;
    }

    public boolean isReproduced() {
        return reproduced;
    }

    public void setReproduced(boolean reproduced) {
        this.reproduced = reproduced;
    }

    public boolean isPslClockwise() {
        return PslClockwise;
    }

    public void setPslClockwise(boolean pslClockwise) {
        PslClockwise = pslClockwise;
    }

    public boolean isMotile() {
        return motile;
    }

    public void setMotile(boolean motile) {
        this.motile = motile;
    }

    public double getInsertionRate() {
        return insertionRate;
    }

    public void setInsertionRate(double secretionRate) {
        this.insertionRate = secretionRate;
    }

    public double getTime(){
        return currentTime;
    }

    public static void setTumbleTime(double newTumbleTime) {
        tumble = newTumbleTime;
    }

    public static void setRunTime(double newRunTime){
        trun = newRunTime;
    }

    public static boolean getReproduction() {
        return canReproduce;
    }

    public static void setReproduction(boolean repro) {
        canReproduce = repro;
    }

    public static boolean getMotility() {
        return motile;
    }

    public static void setMotility(boolean motility) {
        motile = motility;
    }


    //Used for comparing against the Research paper
    //changes the colour based on the direction of the bacteria.
    public void updateColorBasedOnDirection() {
        /*
         Assuming bacteriaDirection is a normalized PVector
         */
        double angle = Math.atan2(bacteriaDirection.getY(), bacteriaDirection.getX()); // Angle in radians

        /*
        Normalize angle to [0, 2*PI]
         */
        if (angle < 0) {
            angle += 2 * Math.PI;
        }

        /*
         Map angle to color (hue) in HSV, where 0 is red, and 2*PI is back to red
         */
        float hue = (float) (angle / (2 * Math.PI)); // Normalize to [0, 1]

        /*
        Update the bacterium's color
         */
        this.color = Color.getHSBColor(hue, 1.0f, 1.0f);
    }

}