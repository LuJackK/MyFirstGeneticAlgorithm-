import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PopulationBatch implements Serializable {
    public ArrayList<Agent> agents;
    public ArrayList<Point> foodSnapshot;

    public PopulationBatch(ArrayList<Agent> agents, ArrayList<Point> foodSnapshot) {
        this.agents = agents;
        this.foodSnapshot = foodSnapshot;
    }
}
