/**
 * @author Seth McNevin, Brian Mbawa, Gladwin Ngobeni
 *
 * SimPanelFrame class for creating the simulation panel and the save, pause and load functions.
 */

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class SimPanelFrame extends JFrame implements Serializable {

    /**
     * {@link Panel} on which simulation drawing will be rendered
     */
    public SimPanel simPanel;
    public int count = 0;
    private static int bacNum;
    private ArrayList<Bacterium> bacteria = new ArrayList<>();
    private ArrayList<EPS> eps = new ArrayList<>();
    private Grid grid = new Grid(1);
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension screenSize = toolkit.getScreenSize();
    private double newRunTime;
    private double newTumbleTime;
    private boolean motile = true;


    /**
     * Initialise different sections of the panel
     */
    public SimPanelFrame() {
        frameInfo();
        settings();
        panelInit();
    }

    /**
     * Initialise the panel // and start a separate thread for repainting ... not
     */
    public void panelInit(){
        simPanel = new SimPanel();
        Thread t = new Thread(simPanel);
        t.start();
        add(simPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    /**
     * Provides the information for the frame
     */
    public void frameInfo(){
        setTitle("BIOFILM");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(screenSize.width, screenSize.height);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    public void resetSimulationStructures () {
        simPanel.clearBacteria();
        simPanel.clearEps();
        simPanel.clearPsl();
        bacteria.clear();
        eps.clear();
        grid.getVisitCount().clear();
        SimPanel.setTime(0.0);
    }

    /**
     * Creates buttons and their functionality
     */
    public void settings() {
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        JFileChooser fileChooser = new JFileChooser();

        /*
        Buttons for the settings of the simulation
         */
        final JButton pauseB = new JButton("Start Sim");
        final JLabel bacNumtextfield = new JLabel("Enter number of bacteria:");
        final JLabel baclimit = new JLabel("(1-500)");

        final JTextField bacNumField = new JTextField();
        final JButton submitBacNum = new JButton("Submit");
        Dimension textFieldSize = new Dimension(50, bacNumField.getFontMetrics(bacNumField.getFont()).getHeight() + 5);
        bacNumField.setMinimumSize(textFieldSize);
        bacNumField.setMaximumSize(textFieldSize);
        bacNumField.setPreferredSize(textFieldSize);
        final JButton save = new JButton("Save");
        final JButton load = new JButton("Load");
        final JButton reset = new JButton("Reset sim");
        final JButton quit = new JButton("Quit");
        final JLabel bacteriaCount = new JLabel("Bacteria Count: " + bacteria.size());
        final JLabel simTimeSpecify = new JLabel("(in simulation seconds)");
        final JLabel simTimeSpecify1 = new JLabel("(in simulation seconds)");

        final JLabel settings = new JLabel("          SETTINGS");

        final JLabel Trun = new JLabel("  Mean Running time");     //TRUN
        final JSlider runTime = new JSlider(0, 1000, 60);
        final JLabel runTimeCurValue = new JLabel("6");

        final JLabel tumble = new JLabel("  Mean Tumble time");     //TUMBLE
        final JSlider tumbleTime = new JSlider(0, 1000, 300);
        final JLabel tumbleTimeCurValue = new JLabel("30");

        final JLabel reproLabel = new JLabel("  Reproduction");  //Reproduction toggle
        final JButton reproduction = new JButton("ON");

        final JLabel motilityLabel = new JLabel("Motility");     //Motility toggle
        final JButton motility = new JButton(" Motile ");


        settings.setAlignmentX(Component.LEFT_ALIGNMENT);
        bacNumtextfield.setAlignmentX(Component.LEFT_ALIGNMENT);
        bacNumField.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBacNum.setAlignmentX(Component.LEFT_ALIGNMENT);
        pauseB.setAlignmentX(Component.LEFT_ALIGNMENT);
        save.setAlignmentX(Component.LEFT_ALIGNMENT);
        load.setAlignmentX(Component.LEFT_ALIGNMENT);
        quit.setAlignmentX(Component.LEFT_ALIGNMENT);
        Trun.setAlignmentX(Component.LEFT_ALIGNMENT);
        runTime.setAlignmentX(Component.LEFT_ALIGNMENT);
        tumble.setAlignmentX(Component.LEFT_ALIGNMENT);
        tumbleTime.setAlignmentX(Component.LEFT_ALIGNMENT);
        reproduction.setAlignmentX(Component.LEFT_ALIGNMENT);
        motility.setAlignmentX(Component.LEFT_ALIGNMENT);

        /*
          ######################################
                      NumBac BUTTON
          ######################################
         */
        submitBacNum.addActionListener((_ -> {
            try {
                bacNum = Integer.parseInt(bacNumField.getText());
                if(bacNum>0 && bacNum<501) {
                /*
                Resets the data structures used to save and load simulation
                 */
                    resetSimulationStructures();
                    pauseB.setText("Start Sim");
                    SimPanel.setPause(true);
                    Color bacColor;

                    for (int i = 0; i < bacNum; i++) {
                        bacColor = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
                        this.bacteria.add(new Bacterium("StrainA", bacColor, motile,(float) SimPanel.offsetX + (Math.random() * (screenSize.width - 250)), 50 + (float) (Math.random() * (screenSize.height - 150)), 0.0, this.bacteria));

                        //USED FOR TESTING MOTILE AND IMMOTILE TOGETHER
                        //this.bacteria.add(new Bacterium("StrainA", new Color(0,0,150), false,(float) 400, 320, 0.0, this.bacteria));
                        //this.bacteria.add(new Bacterium("StrainA", new Color(200,0,0), true,(float) 600, 320, 0.0, this.bacteria));
                    }

                    setBac(bacteria);
                    simPanel.setBacteria(bacteria);
                }
                else {
                    JOptionPane.showMessageDialog(null, "Please enter a valid number", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            catch (NumberFormatException ex){
                JOptionPane.showMessageDialog(null, "Please enter a valid number", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }));

        /*
          ######################################
                      PAUSE BUTTON
          ######################################
         */
        //action listener and functionality for the pause function. pauses the simulation.
        pauseB.addActionListener(_ -> {
            if(!simPanel.getBacteria().isEmpty()) {
                if (SimPanel.getPause()) {
                    SimPanel.setPause(false);
                    pauseB.setText("Pause"); // added some functionality to see the state and to change Pause to Resume
                } else {
                    SimPanel.setPause(true);
                    pauseB.setText("Resume");
                }
            }
            else{
                JOptionPane.showMessageDialog(null, "Please enter number of Bacteria:", "Input Error", JOptionPane.ERROR_MESSAGE);

            }
        });

        /*
          ######################################
                      SAVE BUTTON
          ######################################
         */
        /*
        functionality for the Save button. Pauses the simulation and saves the state as "Bacteria.txt" into a project folder
         */
        save.addActionListener(_ -> {
            if(!simPanel.getBacteria().isEmpty()) {
                SimPanel.setPause(true);
                pauseB.setText("Resume");
                ArrayList<Bacterium> bacSave;
                Grid gridSave;
                ArrayList<EPS> epsSave;
                bacSave = simPanel.getBacteria();
                gridSave = simPanel.getGrid();
                epsSave = simPanel.getEPS();
                ArrayList<Object> simSave = new ArrayList<>();
                simSave.add(bacSave);
                simSave.add(gridSave);
                simSave.add(epsSave);
                String filePath = "./SimSaveData\\simSave" + count + ".txt";
                File file = new File(filePath);
                file.getParentFile().mkdirs();

                try (
                        FileOutputStream fos = new FileOutputStream(filePath);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                ) {
                    oos.writeObject(simSave);
                    count++;

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error Saving data: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            else{
                JOptionPane.showMessageDialog(null, "Simulation empty, nothing to save.", "Save Error", JOptionPane.ERROR_MESSAGE);

            }

        });

        /*
          ######################################
                       LOAD BUTTON
         ######################################
         */
        //functionality for the Load button. Pauses the simulation and gives the user the ability to choose a file to load a previously saved state
        load.addActionListener(_ -> {
            SimPanel.setPause(true);
            pauseB.setText("Resume");

            fileChooser.setDialogTitle("Select the data file:");
            int userSelection = fileChooser.showOpenDialog(null);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File loadFile = fileChooser.getSelectedFile();
                try (FileInputStream fis = new FileInputStream(loadFile);
                     ObjectInputStream ois = new ObjectInputStream(fis);
                ) {
                    ArrayList<Object> savedata = (ArrayList<Object>) ois.readObject();
                    ArrayList<Bacterium> loadBac = (ArrayList<Bacterium>) savedata.get(0);
                    Grid loadGrid = (Grid) savedata.get(1);
                    ArrayList<EPS> loadEPS = (ArrayList<EPS>) savedata.get(2);
                    resetSimulationStructures();
                    simPanel.loadBacteria(loadBac);
                    simPanel.loadEPS(loadEPS);
                    simPanel.loadGrid(loadGrid);
                    SimPanel.setTime(loadBac.get(0).getTime());


                } catch (IOException | ClassNotFoundException ex) {
                    JOptionPane.showMessageDialog(null, "Error loading data: " + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        });

        /*
          ######################################
                       RESET BUTTON
          ######################################
         */

        reset.addActionListener(_ -> {
            pauseB.setText("Start Sim");
            SimPanel.setPause(true);
            bacNumField.setText("");
            resetSimulationStructures();
            Bacterium.setMotility(true);
            motile = true;
            motility.setText(" Motile ");
            Bacterium.setReproduction(true);
            reproduction.setText("ON");
            runTime.setValue(60);
            newRunTime = runTime.getValue() / 10.0;
            tumbleTime.setValue(300);
            newTumbleTime = tumbleTime.getValue() / 10.0;

        });

        /*
          ######################################
                       QUIT BUTTON
          ######################################
         */
        /*
        Functionality for the quit button, exits the simulation.
         */
        quit.addActionListener(_ -> {
            System.exit(0);
        });

        /*
          ######################################
                      BACTERIA COUNTER
          ######################################
         */

        // Timer to update the bacteria count every millisecond

        new Timer(1, _ ->
                bacteriaCount.setText("Bacteria Count: " + simPanel.getBacteria().size())).start();

        /*
          ######################################
                      RUN SLIDER
          ######################################
         */

        runTime.addChangeListener(_ -> {
            newRunTime = runTime.getValue() / 10.0;
            runTimeCurValue.setText(String.format("%.1f", newRunTime));
            Bacterium.setRunTime(newRunTime);
        });

        /*
          ######################################
                      TUMBLE SLIDER
          ######################################
         */
        tumbleTime.addChangeListener(_ -> {
            newTumbleTime = tumbleTime.getValue() / 10.0;
            tumbleTimeCurValue.setText(String.format("%.1f", newTumbleTime));
            Bacterium.setTumbleTime(newTumbleTime);
        });



        /*
          ######################################
                    REPRODUCTION TOGGLE
          ######################################
         */

        reproduction.addActionListener(_ -> {
            if (Bacterium.getReproduction()) {
                Bacterium.setReproduction(false);
                reproduction.setText("OFF"); // added some functionality to see the state and to change Pause to Resume
            } else {
                Bacterium.setReproduction(true);
                reproduction.setText("ON");
            }

        });

        /*
          ######################################
                    MOTILITY TOGGLE
          ######################################
         */
        motility.addActionListener(_ -> {
            if (Bacterium.getMotility()) {
                Bacterium.setMotility(false);
                motile =false;
                motility.setText("Immotile"); // added some functionality to see the state and to change Pause to Resume
            } else {
                Bacterium.setMotility(true);
                motile = true;
                motility.setText(" Motile ");
            }
        });

        /*
        adds the buttons and spacing to the JPanel
         */
        buttons.setPreferredSize(new Dimension(170, screenSize.height ));
        buttons.add(Box.createVerticalStrut(10));
        buttons.add(bacNumtextfield);
        buttons.add(baclimit);
        buttons.add(bacNumField);
        buttons.add(submitBacNum);
        buttons.add(Box.createVerticalStrut(30));
        buttons.add(pauseB);
        buttons.add(Box.createVerticalStrut(10));
        buttons.add(save);
        buttons.add(Box.createVerticalStrut(10));
        buttons.add(load);
        buttons.add(Box.createVerticalStrut(10));
        buttons.add(reset);
        buttons.add(Box.createVerticalStrut(10));
        buttons.add(quit);
        buttons.add(Box.createVerticalStrut(10));
        buttons.add(bacteriaCount);
        buttons.add(Box.createVerticalStrut(50));
        buttons.add(settings);
        buttons.add(Box.createVerticalStrut(20));
        buttons.add(Trun);
        buttons.add(simTimeSpecify);
        buttons.add(runTime);
        buttons.add(runTimeCurValue);
        buttons.add(Box.createVerticalStrut(15));
        buttons.add(tumble);
        buttons.add(simTimeSpecify1);
        buttons.add(tumbleTime);
        buttons.add(tumbleTimeCurValue);
        buttons.add(Box.createVerticalStrut(15));
        buttons.add(reproLabel);
        buttons.add(reproduction);
        buttons.add(Box.createVerticalStrut(15));
        buttons.add(motilityLabel);
        buttons.add(motility);

        add(buttons, BorderLayout.EAST);
    }

    public void setBac(ArrayList<Bacterium> bacteria) {
        this.bacteria = bacteria;
    }
}
