import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

/**
 * @author Seth McNevin, Brian Mbawa, Gladwin Ngobeni
 *
 * SimPanelFrame class for initiating the Bacteria, Starting the animation, updating the bacteria, and repainting the simulation
 *
 */

public class SimPanel extends JPanel implements Runnable{

    /**
     * The maximum boundaries of the simulation environment
     */
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension screenSize = toolkit.getScreenSize();
    protected static final double ENVIRONMENT_SIZE = 600;
    protected final double DISPLAY_HEIGHT = screenSize.height;
    protected final double DISPlAY_WIDTH = screenSize.width;

    /*
     Offset from the left edge of the panel
    */
    protected static final int offsetX = 50;

    /*
     Center vertically
     */
    //protected static final int offsetY = (int) (DISPLAY_HEIGHT - ENVIRONMENT_SIZE) / 2;

    /**
    Time in seconds
     */
    protected static double currentTime = 0.0;

    /**
    Time step in seconds
     */
    private static final double TIME_STEP = 0.005;

    /**
     * {@link Bacterium}s in the simulation
     */
    private final ArrayList<Bacterium> bacteria;

    /**
     * {@link EPS} particles in the simulation
     */
    private final ArrayList<EPS> eps;

    /**
     * Grid tracking {@link Psl} deposits
     */
    private Grid grid;

    /**
     * Boolean for keeping tracking of the {@link Simulation}'s running state
     */
    protected static Boolean paused = true;

    /**
     * Adds  the {@link Bacterium}'s to the arraylist and sets the background
     */
    public SimPanel(){
        setBackground(Color.WHITE);
        bacteria = new ArrayList<>();
        grid = new Grid(1);
        eps = new ArrayList<>();

        setLayout(new BorderLayout());
        startAni();
    }

    /**
     * Updates the simulation and increments the time step
     */
    public void startAni() {
        Timer aniTime = new Timer(0, e -> {
            if (paused.equals(false)) {
                updateSim();
                currentTime += TIME_STEP;
            }
        });
        aniTime.start();
    }

    /**
     * Updates the BIOFILM {@link Simulation} by calling the move method of {@link Bacterium}s
     */
    private void updateSim(){
        /*
        Lists to track changes
         */
        ArrayList<Bacterium> toRemove = new ArrayList<>();
        ArrayList<Bacterium> toAdd = new ArrayList<>();

        /*
        Iterate through the bacteria
         */
        for (Bacterium bacterium : bacteria) {
            /*
            Updates the position of the bacteria in the simulation
             */
            bacterium.move(currentTime,50, (int) DISPlAY_WIDTH-200, 50, (int) DISPLAY_HEIGHT-100 , bacteria, grid, eps);
            /*
            Checks for father bacteria that have reproduced
             */
            handleReproduction(bacterium, toAdd, toRemove);
        }

        /*
        Iterate through the bacteria
         */
        for (EPS epsParticle : eps) {
            /*
            Updates the position of the bacteria in the simulation
             */
            epsParticle.move(currentTime, eps, bacteria);
        }

        if (currentTime > EPS.BOND_CHECK_TIME) {
            EPS.checkForBond(currentTime);
        }

        /*
        Add daughters and remove fathers
         */
        bacteria.addAll(toAdd);
        bacteria.removeAll(toRemove);
    }

    /**
     * Removes father bacterium and adds daughter bacterias
     *
     * @param bacterium the reproducing {@link Bacterium}
     * @param toAdd list to add the daughters of the reproducing bacterium
     * @param toRemove list to add the reproducing bacterium to be removed
     */
    private void handleReproduction(Bacterium bacterium, ArrayList<Bacterium> toAdd, ArrayList<Bacterium> toRemove) {
        if (bacterium.isReproduced() && !bacterium.getDaughters().isEmpty()) {
            toAdd.addAll(bacterium.getDaughters());
            toRemove.add(bacterium);
        }
    }

    @Override
    public void run() {
        while (true) {
            repaint();
            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        /*
        Defining the clipping area (boundary box)
         */
        Shape oldClip = g2d.getClip();
        Shape boundaryBox = new Rectangle(50,50 , (int) DISPlAY_WIDTH-250, (int) DISPLAY_HEIGHT-150);
        g2d.setClip(boundaryBox);

        /*
        Draw the boundary box
         */
        drawBoundaryBox(g2d);

        /*
        Draw PSL particles and bacteria within teh boundary
         */
        drawPSL(g2d);
        drawEPS(g2d);
        drawBacteria(g2d);

        /*
        Restore the old clipping area
         */
        g2d.setClip(oldClip);
    }

    /**
     * Draws a boundary box on the panel
     *
     * @param g2d Graphics2D object used for rendering
     */
    private void drawBoundaryBox(Graphics2D g2d) {

        /*
        Set the color and stroke for the boundary box
         */
        g2d.setColor(Color.BLACK);
        /*

        Line thickness
         */
        g2d.setStroke(new BasicStroke(4));

        /*
        Draw the rectangle (box) on the panel
         */
        g2d.drawRect(offsetX, 50, (int) DISPlAY_WIDTH-250, (int) DISPLAY_HEIGHT-150);
    }

    /**
     * draws EPS particles on screen
     *
     * @param g2d Graphics2D object used for rendering
     */
    private void drawPSL (Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double cellSize = grid.getCellSize();

        for (PVector particle : grid.getVisitCount().keySet()) {
            //int visitCount = grid.getPslParticle(particle.getX() / cellSize, particle.getY() / cellSize).getCount();
            //double intensity = Math.min(1.0f, visitCount / 10.0f);
            g2d.setColor(new Color(255,0,0, (int) 100));
            g2d.fill(new Ellipse2D.Double(particle.getX(), particle.getY(), cellSize * 2, cellSize * 2));
        }
    }

    /**
     * draws {@link Bacterium}s on screen
     *
     * @param g2d Graphics2D object used for rendering
     */
    private void drawBacteria(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Prepare drawing data in parallel
        class DrawData {
            Color color;
            Ellipse2D.Double shape;
            DrawData(Color color, Ellipse2D.Double shape) {
                this.color = color;
                this.shape = shape;
            }
        }
        java.util.List<DrawData> drawList = java.util.Collections.synchronizedList(new java.util.ArrayList<>());

        bacteria.parallelStream().forEach(bacterium -> {
            Color color = bacterium.getColor();
            double x;
            double y;
            double dotSize = Bacterium.TRANSVERSE_WIDTH;
            for (Particle particle : bacterium.getParticles()) {
                x = particle.getPosition().getX();
                y = particle.getPosition().getY();
                dotSize = Bacterium.TRANSVERSE_WIDTH;
                Ellipse2D.Double shape = new Ellipse2D.Double(x - dotSize / 2, y - dotSize / 2, dotSize, dotSize);
                drawList.add(new DrawData(color, shape));
            }
        });

        // Paint on EDT
        for (DrawData data : drawList) {
            g2d.setColor(data.color);
            g2d.fill(data.shape);
        }
    }

    /**
     * draws {@link EPS} on screen
     *
     * @param g2d Graphics2D object used for rendering
     */
    private void drawEPS(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double x;   
        double y;
        double dotSize;
        for(EPS epsParticle: eps) {
            g2d.setColor(Color.BLUE);
            x = epsParticle.getPosition().getX();
            y = epsParticle.getPosition().getY();
            dotSize = (Bacterium.TRANSVERSE_WIDTH) / 2;
            g2d.fill(new Ellipse2D.Double(x - dotSize / 2, y - dotSize / 2, dotSize, dotSize));
        }
    }

    public void loadBacteria(ArrayList<Bacterium> loadedBac){
        bacteria.addAll(loadedBac);
    }

    public void loadEPS(ArrayList<EPS> loadEPS){
        eps.addAll(loadEPS);
    }

    public void loadGrid(Grid loadGrid){
        grid = loadGrid;
    }

    public void clearBacteria(){
        bacteria.clear();
    }

    public void clearEps () {
        eps.clear();
    }

    public void clearPsl () {
        grid.getVisitCount().clear();
    }

    public void setBacteria(ArrayList<Bacterium> newBac ){
        bacteria.addAll(newBac);
    }

    public ArrayList<Bacterium> getBacteria(){
        return bacteria;
    }

    public Grid getGrid(){
        return grid;
    }
    public ArrayList<EPS> getEPS(){
        return eps;
    }

    public static void setPause(Boolean pause){
        paused = pause;
    }

    public static Boolean getPause(){
        return paused;
    }

    public static void setTime(double time){
        currentTime = time;
    }
}
