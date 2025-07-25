import javax.swing.*;
import java.awt.*;
import java.util.*;


public class Interface extends JFrame {
    private boolean agentRandomSpawn = true;
    private boolean selectFittestByRank = true;
    private boolean crossoverElitism = true;
    private double mutationStepSize = 0.01;
    private boolean seeOthers = true;
    private int foodSpawnRadius = 400;
    private float mutationRate = (float) 0.2;
    private int populationSize;
    private int numberOfInputs = 28;
    int simulationSpeed = 10;
    Simulator panel;

    private JPanel createControlsPanel() {
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new GridLayout(3, 3, 10, 10));


        JCheckBox randomSpawnCheckBox = new JCheckBox("Agent Random Spawn");
        randomSpawnCheckBox.setSelected(agentRandomSpawn);
        randomSpawnCheckBox.addActionListener(e -> agentRandomSpawn = randomSpawnCheckBox.isSelected());
        controlsPanel.add(randomSpawnCheckBox);


        JCheckBox selectFittestCheckBox = new JCheckBox("Select Fittest by Rank");
        selectFittestCheckBox.setSelected(selectFittestByRank);
        selectFittestCheckBox.addActionListener(e -> selectFittestByRank = selectFittestCheckBox.isSelected());
        controlsPanel.add(selectFittestCheckBox);


        JCheckBox elitismCheckBox = new JCheckBox("Crossover Elitism");
        elitismCheckBox.setSelected(crossoverElitism);
        elitismCheckBox.addActionListener(e -> crossoverElitism = elitismCheckBox.isSelected());
        controlsPanel.add(elitismCheckBox);


        JLabel mutationLabel = new JLabel("Mutation Step Size:");
        controlsPanel.add(mutationLabel);

        JSlider mutationSlider = new JSlider(1, 10);
        mutationSlider.setMajorTickSpacing(1);
        mutationSlider.setPaintTicks(true);
        mutationSlider.setPaintLabels(true);
        mutationSlider.addChangeListener(e -> mutationStepSize = (double) mutationSlider.getValue() /10);
        controlsPanel.add(mutationSlider);

        JLabel spawnRadiusLabel = new JLabel("Food spawn radius");
        controlsPanel.add(spawnRadiusLabel);

        JSlider spawnRadiusSlider = new JSlider(1, 10, foodSpawnRadius / 50);
        spawnRadiusSlider.setMajorTickSpacing(1);
        spawnRadiusSlider.setPaintTicks(true);
        spawnRadiusSlider.setPaintLabels(true);
        spawnRadiusSlider.addChangeListener(e -> foodSpawnRadius = spawnRadiusSlider.getValue() * 50);
        controlsPanel.add(spawnRadiusSlider);

        JLabel mutationRateLabel = new JLabel("Mutation Rate");
        controlsPanel.add(mutationRateLabel);

        JSlider mutationRateSlider = new JSlider(0, 10, (int) (mutationRate * 10));
        mutationRateSlider.setMajorTickSpacing(1);
        mutationRateSlider.setPaintTicks(true);
        mutationRateSlider.setPaintLabels(true);

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        for (int i = 0; i <= 10; i++) {
            labelTable.put(i, new JLabel(String.format("%.1f", i / 10.0)));
        }
        mutationRateSlider.setLabelTable(labelTable);

        mutationRateSlider.addChangeListener(e -> mutationRate = (float) (mutationRateSlider.getValue() / 10.0));
        controlsPanel.add(mutationRateSlider);

        // Simulation Speed Slider
        JLabel speedLabel = new JLabel("Simulation Speed");
        controlsPanel.add(speedLabel);

        JSlider speedSlider = new JSlider(1, 5, 1); // Default at medium speed (mapped to 2)
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);

// Mapping display labels
        Hashtable<Integer, JLabel> speedLabelTable = new Hashtable<>();
        speedLabelTable.put(1, new JLabel("1x"));
        speedLabelTable.put(2, new JLabel("2x"));
        speedLabelTable.put(3, new JLabel("3x"));
        speedLabelTable.put(4, new JLabel("4x"));
        speedLabelTable.put(5, new JLabel("5x"));
        speedSlider.setLabelTable(speedLabelTable);

        controlsPanel.add(speedSlider);

// Simulation speed mapping logic
        // Default speed

        speedSlider.addChangeListener(e -> {
            int sliderValue = speedSlider.getValue();
            switch (sliderValue) {
                case 1 -> simulationSpeed = 10;
                case 2 -> simulationSpeed = 5;
                case 3 -> simulationSpeed = 2;
                case 4 -> simulationSpeed = 1;
                case 5 -> simulationSpeed = 0;
            }
        });

        return controlsPanel;
    }

    public Interface() throws InterruptedException {
        populationSize = askForPopulationSize();
        Population population = new Population(populationSize, numberOfInputs);
        panel = new Simulator(population, agentRandomSpawn, foodSpawnRadius, simulationSpeed);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel controlsPanel = createControlsPanel();
        this.add(controlsPanel, BorderLayout.SOUTH);
        this.add(panel);
        panel.setSize(800, 600);
        this.pack();
        this.setSize(800, 700);
        this.setVisible(true);

        panel.simulate(500);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        while (true) {
            if (selectFittestByRank) {
                population = population.selectFitestByRank();
            } else {
                population = population.selectFitestByTournament();
            }
            population = population.crossover(crossoverElitism);
            population.mutate(mutationStepSize, mutationRate);

            Simulator newPanel = new Simulator(population, agentRandomSpawn, foodSpawnRadius, simulationSpeed);

            this.remove(panel);
            panel = newPanel;
            this.add(panel);
            panel.setSize(800, 600);
            this.setSize(800, 700);
            this.pack();

            this.revalidate();
            this.repaint();

            panel.simulate(400);
        }
    }

    private int askForPopulationSize() {
        String input = JOptionPane.showInputDialog(
                this,
                "Enter the population size:",
                "Population Size",
                JOptionPane.QUESTION_MESSAGE
        );

        try {
            int size = Integer.parseInt(input);
            if (size > 0) {
                return size;
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid input. Please enter a positive integer.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return askForPopulationSize();
        }
    }
}