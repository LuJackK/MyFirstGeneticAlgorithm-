import javax.swing.*;
import java.awt.*;
import java.util.*;

public class Simulator extends JPanel {
    private static final int scale = 2;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int foodAmmount = 50;
    private static final int agentSpeed = 1 * scale;
    private static int centerX = WIDTH / 2;
    private static int centerY = HEIGHT / 2;
    private static int spawnRadius = 300;
    private static int visionRadius = 15 * scale;
    private static int agentSize = 5 * scale;
    private static int foodSize = 3 * scale;
    private static int popSize;
    private boolean agentSeeOthers;
    private ArrayList<Point> foods = new ArrayList<>();
    private Population population;
    private  Random rand = new Random();
    public Simulator(Population population,boolean AgentRandomSpawn, int foodSpawnRadius, boolean agentSeeOthers) {
        setBackground(Color.WHITE);
        popSize = population.size();
        this.population = population;
        setPreferredSize(new Dimension(WIDTH,HEIGHT));
        setBackground(Color.BLACK);
        this.population = population;
        this.spawnRadius = foodSpawnRadius;
        this.agentSeeOthers = agentSeeOthers;
        intializeFood();
        if(AgentRandomSpawn) {
            initializeAgentsRandom();
        }
        else{
            initializeAgentsFixed();
        }

    }
    private void intializeFood(){
        for (int i = 0; i < foodAmmount; i++) {
            double angle = rand.nextDouble() * 2 * Math.PI;
            double distance = rand.nextDouble() * spawnRadius;

            int x = centerX + (int) (distance * Math.cos(angle));
            int y = centerY + (int) (distance * Math.sin(angle));

            foods.add(new Point(x, y));
        }
    }
    private void drawFoods(Graphics2D g){
        for(Point p : foods){
            g.setColor(Color.ORANGE);
            g.drawOval(p.x-5, p.y-5, foodSize, foodSize);
            g.fillOval(p.x-5, p.y-5, foodSize, foodSize);
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

    private void drawAgents(Graphics2D g){
        for(Agent a : population){
            g.setColor(Color.BLACK);
            Point cords = a.getCordinates();
            g.drawOval(cords.x-5, cords.y-5, agentSize, agentSize);
            g.fillOval(cords.x-5, cords.y-5, agentSize, agentSize);
        }
    }
    private void checkVision(){
        for(Agent a : population){
            ArrayList<Point> seenFoods = new ArrayList<>();
            for(Point p : foods){
                double currentDist = calculateDistance(a.getCordinates(), p);
                if(currentDist < visionRadius){
                   seenFoods.add(p);
                }
            }
            seenFoods.sort(Comparator.comparingDouble(p -> calculateDistance(a.getCordinates(), p)));
            int nFoodInputs = Math.min(5, seenFoods.size());
            int[][] foodInputs = new int[5][2];
            for (int i = 0; i < nFoodInputs; i++) {
                foodInputs[i][0] = seenFoods.get(i).x;
                foodInputs[i][1] = seenFoods.get(i).y;
            }
            for (int i = nFoodInputs; i < 5; i++) {
                foodInputs[i][0] = -1;
                foodInputs[i][1] = -1;
            }
            ArrayList<Point> seenPlayers = new ArrayList<>();
            for(Agent b : population){
                if(b == a){
                    continue;
                }
                double currentDist = calculateDistance(a.getCordinates(), b.getCordinates());
                if( currentDist < visionRadius){
                    seenPlayers.add(b.getCordinates());
                }
            }
            seenPlayers.sort(Comparator.comparingDouble(p -> calculateDistance(a.getCordinates(), p)));
            int nPlayerInputs = Math.min(5, seenPlayers.size());
            int[][] playerInputs = new int[5][2];
            for (int i = 0; i < nPlayerInputs; i++) {
                playerInputs[i][0] = seenPlayers.get(i).x;
                playerInputs[i][1] = seenPlayers.get(i).y;
            }
            for (int i = nPlayerInputs; i < 5; i++) {
                playerInputs[i][0] = -1;
                playerInputs[i][1] = -1;
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
private void checkCollision(){
    for(Agent a : population) {
        Iterator<Point> iterator = foods.iterator();
        while (iterator.hasNext()) {
            Point p = iterator.next();
            if (calculateDistance(a.getCordinates(), p) < foodSize + agentSize) {
                iterator.remove();
                a.foodEat();
            }
        }
        if(isOutOfBounds(a)){
            a.punish();
        }
        else {
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
public void simulate (int genLength){
    for(int i=0; i<genLength; i++){
        checkVision();
        checkCollision();
        repaint();
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
@Override
public void paint(Graphics g) {
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, getWidth(), getHeight());
    Graphics2D g2d = (Graphics2D) g;
    drawFoods(g2d);
    drawAgents(g2d);
}

private double calculateDistance(Point p1, Point p2){
    return Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
}
}
