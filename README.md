# Bacterial Biofilm Simulation

This Java project simulates the movement, interaction and reproduction of Bacteria in a biofilm.
The sim visualizes bacteria, EPS (extracellular polymeric substances) and PSL (polysaccharide) particles.
Thus modelling physical and biological processos such as motility, bonding and growth.

## Features
- **Bacteria Movement:** Each Bacterium consists of seven particles, moves via run-and-tumble mechanics and interacts with other bacteria.
- **EPS and PSL modeling:** EPS particles bond with bacteria and other EPS, while PSL particles act as trails, which influence bacterial movement.
- **Reproduction:** Bacteria grow and reproduce, generating daughter cells with inherited traits from the parent Bacterium.
- **GUI:** Control the simulation Parameters, such as the number of Bacteria, motility, reproduction, run/tumble time, as well as visualise the simulation in real time.
- **Save/Load Function:** Save and load simulation states for later analysis.

## PreReqs
- Java 8 or higher

## Running the simulation

compile all `.java` files in project directory:
```sh
javac *.java
```
Run simulations:
```sh
java Simulation
```

## Saving and Loading
Sim state can be saved or loaded using the provided buttons. The Save files are located in the `SimSaveData` folder

## Project Structure

- `Simulation.java` – Entry point for the simulation ([Simulation.java](Simulation.java))
- `SimPanelFrame.java` – Main frame and UI controls ([SimPanelFrame.java](SimPanelFrame.java))
- `SimPanel.java` – Simulation rendering and update loop ([SimPanel.java](SimPanel.java))
- `Bacterium.java` – Bacterial logic and physics ([Bacterium.java](Bacterium.java))
- `Particle.java`, `EPS.java`, `Psl.java`, `Grid.java`, `PVector.java` – Supporting simulation objects



