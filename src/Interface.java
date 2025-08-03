import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Hashtable;

public class Interface extends JFrame {
    // Simulation configuration variables
    private boolean agentRandomSpawn = true;
    private boolean selectFittestByRank = true;
    private boolean crossoverElitism = true;
    private double mutationStepSize = 0.1;

    private int foodSpawnRadius = 400;
    private float mutationRate = 0.2f;
    private int populationSize;
    private int numberOfInputs = 28;
    private int simulationSpeed = 0;
    private boolean isParallel = false;
    private boolean isDistrubuted = false;
    private boolean loadedPopulation = false;
    private int simulationLength = 500;
    private static long startTime;
    private int generation = 0;
    private int noOfWokers=1;

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel startPanel;
    private JPanel modePanel;
    private JPanel controlPanel;
    private Simulator simPanel;
    private JPanel connectPanel;

    public Interface() {
        setTitle("Simulation Setup");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 300); // Initial window for setup
        setLocationRelativeTo(null);

        initPanels();
        setVisible(true);
    }

    private void initPanels() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // --- Start Panel ---
        startPanel = new JPanel();
        startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.Y_AXIS));
        startPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JButton newButton = new JButton("Start New Population");
        JButton loadButton = new JButton("Load Existing Population");
        JButton connectToMasterButton = new JButton("Connect to Master");

        newButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectToMasterButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        newButton.addActionListener(e -> {
            loadedPopulation = false;
            cardLayout.show(mainPanel, "MODE");
        });
        loadButton.addActionListener(e -> {
            loadedPopulation = true;
            cardLayout.show(mainPanel, "MODE");

        });
        connectToMasterButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "CONNECT");
        });

        startPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        startPanel.add(newButton);
        startPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        startPanel.add(loadButton);
        startPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        startPanel.add(connectToMasterButton);

        // --- Mode Selection Panel ---
        modePanel = new JPanel();
        modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.Y_AXIS));
        modePanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JRadioButton sequential = new JRadioButton("Sequential Mode");
        JRadioButton parallel = new JRadioButton("Parallel Mode");
        JRadioButton distrubuted = new JRadioButton("Distributed Mode");
        ButtonGroup group = new ButtonGroup();
        group.add(sequential);
        group.add(parallel);
        group.add(distrubuted);
        sequential.setSelected(true);

        JTextField popSizeField = new JTextField("100");
        popSizeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JTextField simLengthField = new JTextField("500");
        simLengthField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JTextField noOfWorkersField = new JTextField("1");
        noOfWorkersField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JButton continueButton = new JButton("Continue");
        continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        continueButton.addActionListener((ActionEvent e) -> {
            isParallel = parallel.isSelected();
            isDistrubuted = distrubuted.isSelected();
            try {
                noOfWokers = Integer.parseInt(noOfWorkersField.getText());
                populationSize = Integer.parseInt(popSizeField.getText());
                simulationLength = Integer.parseInt(simLengthField.getText());
                if (simulationLength <=0) throw new NumberFormatException();
                if (populationSize <= 0) throw new NumberFormatException();
                if (noOfWokers <= 0) throw new NumberFormatException();
                launchSimulation();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid positive integer for parameters.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        sequential.setAlignmentX(Component.CENTER_ALIGNMENT);
        parallel.setAlignmentX(Component.CENTER_ALIGNMENT);
        popSizeField.setAlignmentX(Component.CENTER_ALIGNMENT);

        modePanel.add(sequential);
        modePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        modePanel.add(parallel);
        modePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        modePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        modePanel.add(distrubuted);
        modePanel.add(new JLabel("Population Size:"));
        modePanel.add(popSizeField);
        modePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        modePanel.add(new JLabel("Simulation length: "));
        modePanel.add(simLengthField);
        modePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        modePanel.add(new JLabel("Number of workers (if distributed): "));
        modePanel.add(noOfWorkersField);
        modePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        modePanel.add(continueButton);

        JLabel hostIpLabel = new JLabel("Host IP:");
        hostIpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField hostIpField = new JTextField("127.0.0.1");
        hostIpField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        hostIpField.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton connectButton = new JButton("Connect");
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectButton.addActionListener((ActionEvent e) -> {
            try {
                connectToMaster(hostIpField.getText());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });
        connectPanel = new JPanel();
        connectPanel.setLayout(new BoxLayout(connectPanel, BoxLayout.Y_AXIS));
        connectPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        connectPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        connectPanel.add(hostIpLabel);
        connectPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        connectPanel.add(hostIpField);
        connectPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        connectPanel.add(connectButton);


        mainPanel.add(startPanel, "START");
        mainPanel.add(modePanel, "MODE");
        mainPanel.add(connectPanel, "CONNECT");


        setContentPane(mainPanel);
        cardLayout.show(mainPanel, "START");
    }

    private void connectToMaster(String Ip) throws IOException, ClassNotFoundException {
        WorkerClient worker = new WorkerClient(Ip, 8888);
        worker.start();
    }

    private void launchSimulation() {
        setSize(1200, 600);
        setLocationRelativeTo(null);
        startTime = System.currentTimeMillis();

        Population population = new Population(populationSize, numberOfInputs);
        simPanel = new Simulator(population, agentRandomSpawn, foodSpawnRadius, simulationSpeed, startTime, generation);
        simPanel.setPreferredSize(new Dimension(800, 600));
        simPanel.setMaximumSize(new Dimension(800, 600));

        controlPanel = createControlsPanel();
        controlPanel.setPreferredSize(new Dimension(400, 600)); // Wider for 4 elements/row
        controlPanel.setMaximumSize(new Dimension(400, 600));

        // === Use BorderLayout to arrange side-by-side ===
        JPanel simContainer = new JPanel(new BorderLayout());
        simContainer.add(controlPanel, BorderLayout.WEST);
        simContainer.add(simPanel, BorderLayout.CENTER);

        mainPanel.add(simContainer, "SIM");
        cardLayout.show(mainPanel, "SIM");

        new Thread(() -> runSimulationLoop(population)).start();
    }


    private void runSimulationLoop(Population population) {
        try {
            simPanel.simulate(simulationLength);
            Thread.sleep(1000);
            while (true) {
                generation++;
                if(generation==50){
                    break;
                }
                population = selectFittestByRank ? population.selectFitestByRank() : population.selectFitestByTournament();
                population = population.crossover(crossoverElitism);
                population.mutate(mutationStepSize, mutationRate);
                Simulator newPanel = new Simulator(population, agentRandomSpawn, foodSpawnRadius, simulationSpeed, startTime, generation);
                newPanel.setPreferredSize(new Dimension(800, 600));
                newPanel.setMaximumSize(new Dimension(800, 600));

                SwingUtilities.invokeAndWait(() -> {
                    simPanel.getParent().remove(simPanel);
                    simPanel = newPanel;
                    simPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    ((Container) controlPanel.getParent()).add(simPanel, 0);
                    revalidate();
                    repaint();
                });
                if(isParallel){
                    simPanel.simulateSynchronized(simulationLength);
                } else if (isDistrubuted) {
                    simPanel.simulateDistrubuted(simulationLength, noOfWokers);
                }
                else{
                    simPanel.simulate(populationSize);
                }
                {
                    simPanel.simulate(simulationLength);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel createControlsPanel() {
        JPanel controls = new JPanel(new GridBagLayout());
        controls.setPreferredSize(new Dimension(400, 600));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // === Row 0: First 2 Checkboxes ===
        gbc.gridx = 0;
        gbc.gridy = row;
        JCheckBox randomSpawnCheck = new JCheckBox("Agent Random Spawn", agentRandomSpawn);
        randomSpawnCheck.addActionListener(e -> agentRandomSpawn = randomSpawnCheck.isSelected());
        controls.add(randomSpawnCheck, gbc);

        gbc.gridx = 1;
        JCheckBox fittestCheck = new JCheckBox("Select Fittest by Rank", selectFittestByRank);
        fittestCheck.addActionListener(e -> selectFittestByRank = fittestCheck.isSelected());
        controls.add(fittestCheck, gbc);

        // === Row 1: Third Checkbox ===
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        JCheckBox elitismCheck = new JCheckBox("Crossover Elitism", crossoverElitism);
        elitismCheck.addActionListener(e -> crossoverElitism = elitismCheck.isSelected());
        controls.add(elitismCheck, gbc);


        row++;

        gbc.gridwidth = 2;
        gbc.gridx = 0;

        // -- Simulation Length --
        gbc.gridy = row++;
        controls.add(new JLabel("Simulation Length:"), gbc);
        gbc.gridx = 1;
        JTextField simLengthField = new JTextField("500");
        simLengthField.addActionListener(e -> simulationLength = Integer.parseInt(simLengthField.getText()));
        controls.add(simLengthField, gbc);

        // -- Mutation Step Size --
        gbc.gridx = 0;
        gbc.gridy = row++;
        controls.add(new JLabel("Mutation Step Size:"), gbc);

        gbc.gridy = row++;
        JSlider mutSlider = new JSlider(1, 10, Math.max(1, (int) (mutationStepSize * 10)));
        mutSlider.setMajorTickSpacing(1);
        mutSlider.setPaintTicks(true);
        mutSlider.setPaintLabels(true);
        mutSlider.addChangeListener(e -> mutationStepSize = mutSlider.getValue() / 10.0);
        controls.add(mutSlider, gbc);

        // -- Food Spawn Radius --
        gbc.gridy = row++;
        controls.add(new JLabel("Food Spawn Radius:"), gbc);

        gbc.gridy = row++;
        JSlider spawnSlider = new JSlider(1, 10, Math.max(1, foodSpawnRadius / 50));
        spawnSlider.setPreferredSize(new Dimension(200, 50));
        spawnSlider.setMajorTickSpacing(1);
        spawnSlider.setPaintTicks(true);
        spawnSlider.setPaintLabels(true);
        spawnSlider.addChangeListener(e -> foodSpawnRadius = spawnSlider.getValue() * 50);
        controls.add(spawnSlider, gbc);

        // -- Mutation Rate --
        gbc.gridy = row++;
        controls.add(new JLabel("Mutation Rate:"), gbc);

        gbc.gridy = row++;
        JSlider rateSlider = new JSlider(0, 10, (int) (mutationRate * 10));
        rateSlider.setPreferredSize(new Dimension(200, 50));
        Hashtable<Integer, JLabel> rateLabels = new Hashtable<>();
        for (int i = 0; i <= 10; i++) {
            rateLabels.put(i, new JLabel(String.format("%.1f", i / 10.0)));
        }
        rateSlider.setLabelTable(rateLabels);
        rateSlider.setMajorTickSpacing(1);
        rateSlider.setPaintTicks(true);
        rateSlider.setPaintLabels(true);
        rateSlider.addChangeListener(e -> mutationRate = rateSlider.getValue() / 10.0f);
        controls.add(rateSlider, gbc);

        // -- Simulation Speed --
        gbc.gridy = row++;
        controls.add(new JLabel("Simulation Speed:"), gbc);

        gbc.gridy = row++;
        JSlider speedSlider = new JSlider(1, 5, 5);
        speedSlider.setPreferredSize(new Dimension(200, 40));
        Hashtable<Integer, JLabel> speedLabels = new Hashtable<>();
        for (int i = 1; i <= 5; i++) {
            speedLabels.put(i, new JLabel(i + "x"));
        }
        speedSlider.setLabelTable(speedLabels);
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.addChangeListener(e -> {
            switch (speedSlider.getValue()) {
                case 1 -> simulationSpeed = 10;
                case 2 -> simulationSpeed = 5;
                case 3 -> simulationSpeed = 2;
                case 4 -> simulationSpeed = 1;
                case 5 -> simulationSpeed = 0;
            }
        });
        controls.add(speedSlider, gbc);

        // === Pause Button ===
        gbc.gridy = row++;
        JButton pauseBtn = new JButton("Pause/Unpause");
        pauseBtn.addActionListener(e -> simPanel.togglePause());
        controls.add(pauseBtn, gbc);

        return controls;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(Interface::new);
    }
}
