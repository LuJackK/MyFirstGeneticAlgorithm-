import javax.swing.*;
import java.awt.*;
import java.util.*;

public class Interface extends JFrame {
    private boolean agentRandomSpawn = false;
    private boolean selectFittestByRank = true;
    private boolean crossoverElitism = true;
    private boolean crossoverPointFixed = true;
    private int mutationStepSize = 1;
    private boolean seeOthers = false;
    private int foodSpawnRadius = 100;
    private float mutationRate = (float) 0.4;
    private int populationSize;
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


        JCheckBox fixedCrossoverCheckBox = new JCheckBox("Fixed Crossover Point");
        fixedCrossoverCheckBox.setSelected(crossoverPointFixed);
        fixedCrossoverCheckBox.addActionListener(e -> crossoverPointFixed = fixedCrossoverCheckBox.isSelected());
        controlsPanel.add(fixedCrossoverCheckBox);


        JLabel mutationLabel = new JLabel("Mutation Step Size:");
        controlsPanel.add(mutationLabel);

        JSlider mutationSlider = new JSlider(1, 10, mutationStepSize);
        mutationSlider.setMajorTickSpacing(1);
        mutationSlider.setPaintTicks(true);
        mutationSlider.setPaintLabels(true);
        mutationSlider.addChangeListener(e -> mutationStepSize = mutationSlider.getValue());
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


        JCheckBox seeOthersCheckBox = new JCheckBox("See Others");
        seeOthersCheckBox.setSelected(seeOthers);
        seeOthersCheckBox.addActionListener(e -> seeOthers = seeOthersCheckBox.isSelected());
        controlsPanel.add(seeOthersCheckBox);

        return controlsPanel;
    }

    public Interface() {
        populationSize = askForPopulationSize();
        Population population = new Population(20);
        panel = new Simulator(population, agentRandomSpawn, foodSpawnRadius, seeOthers);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.add(panel);
        JPanel controlsPanel = createControlsPanel();
        this.add(controlsPanel, BorderLayout.SOUTH);
        this.pack();
        this.setSize(800, 700);
        this.setVisible(true);

        panel.simulate(300);

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
            population = population.crossover(crossoverElitism, crossoverPointFixed);
            population.mutate(mutationStepSize, mutationRate);

            Simulator newPanel = new Simulator(population, agentRandomSpawn, foodSpawnRadius, seeOthers);

            this.remove(panel);
            panel = newPanel;
            this.add(panel, BorderLayout.CENTER);

            this.add(panel);
            this.pack();

            this.revalidate();
            this.repaint();

            panel.simulate(300);
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