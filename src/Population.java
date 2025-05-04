import java.util.ArrayList;
import java.util.Random;

public class Population extends ArrayList<Agent> {
    private static final int genomeSize = 6;
    private Random rand = new Random();
    public Population(int n) {
        for(int i = 0; i < n; i++) {
            this.add(randomAgent());
        }
    }
    public Population(ArrayList<Agent> a) {
        for(int i = 0; i < a.size(); i++) {
            this.add(a.get(i));
        }
    }
    private Agent randomAgent() {

        ArrayList<Behavior> behaviors = new ArrayList<>();
        for(int i=0; i<genomeSize/2; i++){

            Condition condition = new Condition(randomEvent());
            ArrayList<Action> actions = new ArrayList<>();
            for(int j=0; j<8; j++){
                actions.add(randomAction());
            }
            Behavior b = new Behavior(condition, actions, rand.nextInt(10));
            behaviors.add(b);
        }
        //one of each
        ArrayList<Action> none = new ArrayList<Action>();
        ArrayList<Action> person = new ArrayList<Action>();
        ArrayList<Action> food = new ArrayList<Action>();
        for(int j=0; j<8; j++){
            none.add(randomAction());
            person.add(randomAction());
            food.add(randomAction());
        }
        behaviors.add(new Behavior(new Condition(Event.NONE), none, rand.nextInt(10)));
        behaviors.add(new Behavior(new Condition(Event.PERSON_NEARBY), food, rand.nextInt(10)));
        behaviors.add(new Behavior(new Condition(Event.FOOD_NEARBY), person, rand.nextInt(10)));
        return new Agent(behaviors);
    }
    private Event randomEvent(){
        int random = rand.nextInt(0,3);
        switch (random){
            case 0 ->{return Event.FOOD_NEARBY;}
            case 1 ->{return Event.PERSON_NEARBY;}
            case 2 ->{return Event.NONE;}
        }
        return null;
    }
    public Action randomAction(){
        int random = rand.nextInt(0,9);
        switch (random){
            case 0 -> {return Action.MOVEDOWNLEFT;}
            case 1 -> {return Action.MOVEDOWNRIGHT;}
            case 2 -> {return Action.MOVERIGHT;}
            case 3 -> {return Action.MOVEDOWN;}
            case 4 -> {return Action.MOVEUP;}
            case 5 -> {return Action.MOVELEFT;}
            case 6 -> {return Action.MOVEUPRIGHT;}
            case 7 -> {return Action.MOVEUPLEFT;}
            case 8 -> {return Action.NONE;}
        }
        return null;
    }
    public Population selectFitestByRank(){
        this.sort((a, b) -> {if (a.getStats() > b.getStats()) {
            return -1;
        } else if (a.getStats() < b.getStats()) {
            return 1;
        } else {
            return 0;
        }});
        ArrayList<Agent> selcted = new ArrayList<>();
        for(int i=0; i<this.size()/2; i++){
            selcted.add(this.get(i));
        }
        return new Population(selcted);
    }
    public Population selectFitestByTournament(){
        ArrayList<Agent> selcted = new ArrayList<>();
        for(int i=0; i<this.size()/2; i++){
            int k = rand.nextInt(this.size());
            int l = rand.nextInt(this.size());
            selcted.add((this.get(k).getStats()>this.get(l).getStats()) ? this.get(k) : this.get(l));
        }
        return new Population(selcted);
    }
    public Population crossover(boolean Elitism, boolean fixed){
        ArrayList<Agent> crossover = new ArrayList<>();
        Agent a = randomAgent();
        if(!Elitism){
            for(int i=0; i<this.size(); i++){
                crossover.add(a.agentfixedPointCrossover(this.get(i), this.get(this.size()-1-i)));
                crossover.add(a.agentfixedPointCrossover(this.get(this.size()-1-i), this.get(i)));
            }
        }
        else{
            for(int i=0; i<this.size()*0.2; i++){
                crossover.add(this.get(i));
            }
            for(int i=0; i<this.size()*0.9; i++){
                crossover.add((fixed) ? a.agentfixedPointCrossover(this.get(i), this.get(this.size()-1-i)) : a.agentRandomPointCrossover(this.get(i), this.get(this.size()-1-i)));
                crossover.add((fixed) ? a.agentfixedPointCrossover(this.get(this.size()-1-i), this.get(i)) : a.agentRandomPointCrossover(this.get(this.size()-1-i), this.get(i)));
            }
        }
        return new Population(crossover);
    }
    public void mutate(int mutationStepSize, float mutationRate){
        this.getFirst().setMutateStep(mutationStepSize);
        for(Agent a : this){
            if(rand.nextFloat() < mutationRate){
                a.mutateAgent();
            }
        }
    }

}
