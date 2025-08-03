import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class Simulator extends JPanel {
    private static final int scale = 2;
    private static boolean paused = false;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int foodAmmount = 90;
    public static final int agentSpeed = 1 * scale;
    private static int centerX = WIDTH / 2;
    private static int centerY = HEIGHT / 2;
    private static int spawnRadius = 300;
    private static int visionRadius = 30 * scale;
    public static int agentSize = 5 * scale;
    private static int foodSize = 3 * scale;
    private static int simulationSpeed=10;
    private static long startTime;
    private static int genNo;
    private final ArrayList<Point> foods = new ArrayList<>();
    private Population population;
    private  Random rand = new Random();
    public Simulator(Population population,boolean AgentRandomSpawn, int foodSpawnRadius, int simulationSpeed, long startTime, int genNO) {
        this.genNo = genNO;
        this.startTime = startTime;
        setBackground(Color.WHITE);
        this.simulationSpeed = simulationSpeed;
        this.population = population;
        setPreferredSize(new Dimension(WIDTH,HEIGHT));
        setBackground(Color.BLACK);
        this.population = population;
        this.spawnRadius = foodSpawnRadius;
        initializeFood();
        if(AgentRandomSpawn) {
            initializeAgentsRandom();
        }
        else{
            initializeAgentsFixed();
        }

    }

    private void initializeFood(){
        for (int i = 0; i < foodAmmount; i++) {
            double angle = rand.nextDouble() * 2 * Math.PI;
            double distance = rand.nextDouble() * spawnRadius;

            int x = centerX + (int) (distance * Math.cos(angle));
            int y = centerY + (int) (distance * Math.sin(angle));

            foods.add(new Point(x, y));
        }
    }
    private void drawFoods(Graphics2D g){
        g.setColor(Color.ORANGE);
        synchronized (foods) {
            for(Point p : foods){
                g.drawOval(p.x-5, p.y-5, foodSize, foodSize);
                g.fillOval(p.x-5, p.y-5, foodSize, foodSize);
            }
        }
    }
    private void initializeAgentsRandom(){
        for(Agent a : population){
            int x = rand.nextInt(WIDTH);
            int y = rand.nextInt(HEIGHT);
            Point p = new Point(x, y);
            a.setCordinates(p);
        }
    }
    private void initializeAgentsFixed() {
        int radius = 50;
        int centerX = 750;
        int centerY = 50;

        for (Agent a : population) {
            double angle = rand.nextDouble() * 2 * Math.PI;
            double r = radius * Math.sqrt(rand.nextDouble());
            int x = centerX + (int) (r * Math.cos(angle));
            int y = centerY + (int) (r * Math.sin(angle));
            a.setCordinates(new Point(x, y));
        }
    }
    private void drawStats(Graphics2D g){
        String time = "Time: " + (System.currentTimeMillis()-startTime);
        Font font = new Font("Arial", Font.BOLD, 14);
        String iterNo = "Generation: " + genNo;
        g.setFont(font);
        g.setColor(Color.BLACK);

        FontMetrics metrics = g.getFontMetrics(font);
        int textWidth = metrics.stringWidth(time);
        int x = WIDTH - textWidth - 20;
        int y = 20;
        g.drawString(time, x, y);
        y=y+20;
        g.drawString(iterNo, WIDTH-metrics.stringWidth(iterNo)-20, y);
    }
    private void drawAgents(Graphics2D g){
        synchronized (population){
            for(Agent a : population){
                if(a.getStatus()==AgentState.ALIVE)
                {
                    g.setColor(Color.BLACK);
                    Point cords = a.getCordinates();
                    int size = agentSize+a.getSize();
                    g.drawOval(cords.x - size / 2, cords.y - size / 2, size, size);
                    g.fillOval(cords.x - size / 2, cords.y - size / 2, size, size);
                }
            }
        }
    }
    private void checkVisionSynchronized(ForkJoinPool executor) throws InterruptedException {
        int cores = Runtime.getRuntime().availableProcessors();
        int batchSize = Math.max(1, population.size() / cores);

        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < population.size(); i += batchSize) {
            int from = i;
            int to = Math.min(i + batchSize, population.size());

            tasks.add(() -> {
                for (int j = from; j < to; j++) {
                    Agent a = population.get(j);
                    if (a.getStatus() == AgentState.DEAD) continue;

                    Point agentPos = a.getCordinates();

                    // Process Food
                    List<Point> seenFoods = foods.stream()
                            .sorted(Comparator.comparingDouble(p -> calculateDistance(agentPos, p)))
                            .limit(5)
                            .toList();

                    int[][] foodInputs = new int[5][2];
                    for (int k = 0; k < seenFoods.size(); k++) {
                        Point f = seenFoods.get(k);
                        foodInputs[k][0] = f.x - agentPos.x;
                        foodInputs[k][1] = f.y - agentPos.y;
                    }

                    // Process other agents
                    List<Agent> seenPlayers = population.stream()
                            .filter(b -> b != a && b.getStatus() == AgentState.ALIVE)
                            .sorted(Comparator.comparingDouble(b -> calculateDistance(agentPos, b.getCordinates())))
                            .limit(5)
                            .toList();

                    int[][] playerInputs = new int[5][3];
                    for (int k = 0; k < seenPlayers.size(); k++) {
                        Agent b = seenPlayers.get(k);
                        Point bp = b.getCordinates();
                        playerInputs[k][0] = bp.x - agentPos.x;
                        playerInputs[k][1] = bp.y - agentPos.y;
                        playerInputs[k][2] = b.getSize();
                    }

                    evaluateAction(a, a.evaluateBehavior(foodInputs, playerInputs));
                }
                return null;
            });
        }

        executor.invokeAll(tasks);
    }

    private void checkVision() {
        for(Agent a : population){
            if(a.getStatus()==AgentState.DEAD){continue;}
            ArrayList<Point> seenFoods = new ArrayList<>();
            for(Point p : foods){
                double currentDist = calculateDistance(a.getCordinates(), p);
                if(currentDist < visionRadius){
                }
                seenFoods.add(p);
            }
            seenFoods.sort(Comparator.comparingDouble(p -> calculateDistance(a.getCordinates(), p)));
            int[][] foodInputs = new int[5][2];
            for (int i = 0; i < 5; i++) {
                foodInputs[i][0] = (seenFoods.get(i).x-a.getCordinates().x);
                foodInputs[i][1] = seenFoods.get(i).y-a.getCordinates().y;
            }
            ArrayList<Agent> seenPlayers = new ArrayList<>();
            for(Agent b : population){
                if(b == a){
                    continue;
                }
                double currentDist = calculateDistance(a.getCordinates(), b.getCordinates());
                if( currentDist < visionRadius){
                }
                seenPlayers.add(b);
            }
            seenPlayers.sort(Comparator.comparingDouble(p -> calculateDistance(a.getCordinates(), p.getCordinates())));
            int[][] playerInputs = new int[5][3];
            for (int i = 0; i < 5; i++) {
                playerInputs[i][0] = seenPlayers.get(i).getCordinates().x-a.getCordinates().x;
                playerInputs[i][1] = seenPlayers.get(i).getCordinates().y-a.getCordinates().y;
                playerInputs[i][2] = seenPlayers.get(i).getSize();
            }
            evaluateAction(a, a.evaluateBehavior(foodInputs, playerInputs));
        }
    }
    private void evaluateAction(Agent a, Action action){
        switch(action){
            case MOVEUP -> a.translateCordinates(0, agentSpeed);
            case MOVEDOWN -> a.translateCordinates(0, -agentSpeed);
            case MOVELEFT -> a.translateCordinates(-agentSpeed, 0);
            case MOVERIGHT -> a.translateCordinates(agentSpeed, 0);
            case MOVEUPLEFT -> a.translateCordinates(-agentSpeed, agentSpeed);
            case MOVEUPRIGHT -> a.translateCordinates(agentSpeed, agentSpeed);
            case MOVEDOWNLEFT -> a.translateCordinates(-agentSpeed, -agentSpeed);
            case MOVEDOWNRIGHT -> a.translateCordinates(agentSpeed, -agentSpeed);
        }
    }
    private void checkCollisionSynchronized(ExecutorService executor) throws InterruptedException {
        List<Callable<Void>> tasks = new ArrayList<>();
        for (Agent a : population) {
            tasks.add(() -> {
                if (a.getStatus() == AgentState.DEAD) {
                    return null;
                }
                synchronized (foods) {
                    for (Point p : new ArrayList<>(foods)) {
                        if (calculateDistance(a.getCordinates(), p) < foodSize + a.getSize()) {
                            a.foodEat();
                            foods.remove(p);
                        }
                    }
                }
                synchronized (population){
                    for (Agent other : population) {
                        if (other == a || other.getStatus() == AgentState.DEAD){continue;}
                        if (calculateDistance(a.getCordinates(), other.getCordinates()) < (double) (a.getSize() + agentSize * 2 + other.getSize()) / 2) {
                            if (a.getSize() > other.getSize() && other.getStatus() == AgentState.ALIVE) {
                                other.setStatus(AgentState.DEAD);
                                a.eatEnemy(other);
                            }
                        }
                    }
                }

                if (isOutOfBounds(a)) {
                    a.punish();
                } else {
                    a.setFoodProximty(closestFoodToAgent(a));
                }
                return null;
            });
        }
        executor.invokeAll(tasks);
    }
    private void checkCollision() {
        for (Agent a : population) {
            if (a.getStatus() == AgentState.DEAD) {
                continue;
            }
            synchronized (foods) {
                for (Point p : new ArrayList<>(foods)) {
                    if (calculateDistance(a.getCordinates(), p) < foodSize + a.getSize()) {
                        a.foodEat();
                        foods.remove(p);
                    }
                }
            }
            synchronized (population){
                for (Agent other : population) {
                    if (other == a || other.getStatus() == AgentState.DEAD){continue;}
                    if (calculateDistance(a.getCordinates(), other.getCordinates()) < (double) (a.getSize() + agentSize * 2 + other.getSize()) / 2) {
                        if (a.getSize() > other.getSize() && other.getStatus() == AgentState.ALIVE) {
                            other.setStatus(AgentState.DEAD);
                            a.eatEnemy(other);
                        }
                    }
                }
            }

            if (isOutOfBounds(a)) {
                a.punish();
            } else {
                a.setFoodProximty(closestFoodToAgent(a));
            }
        }
    }
    public double closestFoodToAgent(Agent a) {
        double distanceFromClosestPoint = Double.MAX_VALUE;
        for(Point p : foods){
            if(calculateDistance(a.getCordinates(), p) < distanceFromClosestPoint){
                distanceFromClosestPoint = calculateDistance(a.getCordinates(), p);
            }
        }
        return distanceFromClosestPoint;
    }
    private boolean isOutOfBounds(Agent a){
        int x = a.getCordinates().x;
        int y = a.getCordinates().y;
        return x < 0 || x > WIDTH || y < 0 || y > HEIGHT;
    }
    public void simulate (int genLength) {
        for(int i=0; i<genLength; i++){
            checkVision();
            checkCollision();
            repaint();

            try {
                Thread.sleep(simulationSpeed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    public void simulateSynchronized(int genLength) throws InterruptedException {
        ForkJoinPool executor = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

        for (int i = 0; i < genLength; i++) {
            checkVisionSynchronized(executor);
            checkCollisionSynchronized(executor);
            repaint();

            try {
                Thread.sleep(simulationSpeed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            while (paused) {
                Thread.sleep(10);  // Avoid CPU burning
            }
        }

        executor.shutdown();
    }
    public void simulateDistrubuted(int genLength, int noOfWorkers) throws Exception {
        ForkJoinPool executor = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

        DistributedManager manager = new DistributedManager(8888);

        manager.startServer(noOfWorkers);
        for (int i = 0; i < genLength; i++) {
            population = new Population(manager.distributeAndCollect(population, foods));
            checkCollisionSynchronized(executor);
            repaint();
        }
        executor.shutdown();
    }
    @Override
    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        Graphics2D g2d = (Graphics2D) g;
        drawFoods(g2d);
        drawAgents(g2d);
        drawStats(g2d);
    }
    private double calculateDistance(Point p1, Point p2){
        return Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
    }

    public void togglePause() {
        paused = !paused;
    }

    public Population getPopulation() {
       return this.population;
    }
}
