import java.awt.*;
import java.util.*;

public class Agent {
    private ArrayList<Behavior> genome;
    private static int numberOfMoveActions = 9;
    private int foodEaten;
    private Point cordinates;
    private static int stepSize = 1;

    public Agent(ArrayList<Behavior> genome) {
        this.genome = genome;
        foodEaten = 0;
    }
    private Action evaluateNearbyBehavior(Behavior b, Point p) {
        ArrayList<Action> actions = b.getActions();
        int weight = b.getWeight()*10;
        int x = this.cordinates.x, y = this.cordinates.y;
        double sqrt2Over2 = Math.sqrt(2) / 2;
        int xPrime = (int) (x * sqrt2Over2 - y * sqrt2Over2), yPrime = (int) (x * sqrt2Over2 + y * sqrt2Over2);

        if (x >= p.x && y >= p.y) {
            if(Math.abs(xPrime-x)>weight) {
                return actions.get(0);
            }
            else{
                return actions.get(1);
            }
        }
        if (x < p.x && y >= p.y) {
            if(Math.abs(yPrime-y)>weight) {
                return actions.get(2);
            }
            else{
                return actions.get(3);
            }
        }
        if (x >= p.x) {
            if(Math.abs(yPrime-y)>weight) {
                return actions.get(4);
            }
            else{
                return actions.get(5);
            }
        }
        else {
            if(Math.abs(xPrime-x)>weight) {
                return actions.get(6);
            }
            else{
                return actions.get(7);
            }
        }
    }

    public Action evaluateBehavior(Stack<EventData> events){
        ArrayList<Action> resultActions = new ArrayList<Action>(numberOfMoveActions);
        int[] actionWeights = new int[numberOfMoveActions];
        while(!events.empty()) {
            EventData event = events.pop();
            for (Behavior b : genome) {
                if (b.getCondition().getType() == event.getEventType()) {
                    Action result =(evaluateNearbyBehavior(b, (Point) event.getData()));
                    if(resultActions.contains(result)) {
                        actionWeights[resultActions.indexOf(result)]++;
                    }
                    else{
                        resultActions.add(result);
                        actionWeights[resultActions.indexOf(result)]++;
                    }

                }
            }
        }
        int maxValue = 0;
        int index = -1;
        for(int i = 0; i < numberOfMoveActions; i++) {
            if(actionWeights[i] > maxValue) {
                maxValue = actionWeights[i];
                index = i;
            }
        }
        if(index > -1){
            return resultActions.get(index);
        }
        else{
            return Action.NONE;
        }
    }

    public void foodEat(){
        foodEaten++;
    }
    public int getStats(){
        return foodEaten;
    }
    public Point getCordinates() {
        return cordinates;
    }
    public void setCordinates(Point cordinates) {
        this.cordinates = cordinates;
    }
    public void translateCordinates(int dx, int dy) {
        this.cordinates.x += dx;
        this.cordinates.y += dy;
    }
    public Agent agentfixedPointCrossover(Agent a, Agent b) {
        ArrayList<Behavior> newGenome = new ArrayList<>();
        for(int i = 0; i < a.genome.size()/2; i++) {
            newGenome.add(a.genome.get(i));
        }
        for(int i = a.genome.size()/2; i < b.genome.size(); i++) {
            newGenome.add(b.genome.get(i));
        }
        return new Agent(newGenome);
    }
    public void  mutateAgent(){
        Population pop = new Population(1);
        Random rand = new Random();
        int noOfMutations =1;
        while(rand.nextBoolean()){
            noOfMutations++;
        }
        for(int i = 0; i < noOfMutations; i++) {
            if(rand.nextBoolean()){
                this.genome.get(rand.nextInt(genome.size())).setWeight(stepSize);
            }
            else{
                this.genome.get(rand.nextInt(genome.size())).setWeight(-stepSize);
            }
            for(int j=0; j< rand.nextInt(stepSize); i++){
                Behavior b = this.genome.get(rand.nextInt(genome.size()));
                b.getActions().removeFirst();
                b.getActions().add(pop.randomAction());
            }


        }
    }

    public Agent agentRandomPointCrossover(Agent a, Agent b) {
        ArrayList<Behavior> newGenome = new ArrayList<>();
        Random rand = new Random();
        int crossoverPoint = rand.nextInt(a.genome.size());
        newGenome.addAll(a.genome.subList(0, crossoverPoint));

        newGenome.addAll(b.genome.subList(crossoverPoint, b.genome.size()));

        return new Agent(newGenome);
    }
    public void punish(){
        foodEaten--;
    }
    public void setMutateStep(int stepSize1){
        this.stepSize = stepSize1;
    }
}
