import java.awt.*;
import java.io.Serializable;
import java.util.List;

public class PopulationBatch implements Serializable {
    public List<Agent> agents;
    public List<Point> foodSnapshot;

    public PopulationBatch(List<Agent> agents, List<Point> foodSnapshot) {
        this.agents = agents;
        this.foodSnapshot = foodSnapshot;
    }
}
