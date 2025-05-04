import java.util.ArrayList;
public class Behavior  {
    private Condition condition;
    private int weight;
    private ArrayList<Action> actions;

    public Behavior(Condition condition, ArrayList<Action> actions, int weight) {
        this.condition = condition;
        this.actions = actions;
        this.weight = weight;
    }
    public Condition getCondition() {
        return condition;
    }
    public ArrayList<Action> getActions () {
        return actions;
    }

    public int getWeight() {
        return weight;
    }
    public void setWeight(int weight) {
        this.weight += weight;
    }
}
