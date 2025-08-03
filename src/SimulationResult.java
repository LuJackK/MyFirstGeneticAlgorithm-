import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Serializable data class for result
public class SimulationResult implements Serializable {
    public ArrayList<Agent> updatedAgents;
    public SimulationResult(ArrayList<Agent> updatedAgents) {
        this.updatedAgents = updatedAgents;
    }
}
