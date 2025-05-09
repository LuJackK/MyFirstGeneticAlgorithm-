import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Stack;

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
            Stack<EventData> events = new Stack<>();
            for(Point p : foods){
                if(calculateDistance(a.getCordinates(), p) < visionRadius){
                    events.push(new EventData(Event.FOOD_NEARBY, p));
                }
            }
            for(Agent b : population){
                if(b == a){
                    continue;
                }
                if(calculateDistance(a.getCordinates(), b.getCordinates()) < visionRadius){
                    if(agentSeeOthers){
                        events.push(new EventData(Event.PERSON_NEARBY, b.getCordinates()));
                    }
                }
            }
            events.add(new EventData(Event.NONE, new Point(a.getCordinates().x, a.getCordinates().y)));
            int c =0;
            while(rand.nextBoolean()){
                c++;
            }
            for(int i=0; i<c; i++){
                //events.add(new EventData(Event.NONE, new Point(a.getCordinates().x, a.getCordinates().y)));
            }
            evaluateAction(a, a.evaluateBehavior(events));
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
        }
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
                Thread.sleep(10);
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