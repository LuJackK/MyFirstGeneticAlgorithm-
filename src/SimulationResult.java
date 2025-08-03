import java.awt.*;
import java.io.Serializable;
import java.util.List;

// Serializable data class for result
public class SimulationResult implements Serializable {
    public List<Agent> updatedAgents;
    public List<Point> updatedFood;
    public SimulationResult(List<Agent> updatedAgents) {
        this.updatedAgents = updatedAgents;
    }
}
