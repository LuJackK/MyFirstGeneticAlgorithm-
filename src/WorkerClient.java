import java.awt.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
public class WorkerClient {
    private final String masterHost;
    private final int port;

    public WorkerClient(String masterHost, int port) {
        this.masterHost = masterHost;
        this.port = port;
    }

    public void start() throws IOException, ClassNotFoundException {
        Socket socket = new Socket(masterHost, port);
        System.out.println("Worker: Connected to master");

        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

        while (true) {
            try {
                PopulationBatch batch = (PopulationBatch) in.readObject();
                ArrayList<Agent> updated = simulate(batch.agents, batch.foodSnapshot);
                out.writeObject(new SimulationResult(updated));
                out.flush();
            } catch (EOFException | InterruptedException e) {
                break; // Master closed connection
            }
        }

        socket.close();
    }

    private ArrayList<Agent> simulate(ArrayList<Agent> agents, ArrayList<Point> food) throws InterruptedException {
        for (Agent a : agents) {
            performSimulationStep(agents, food);
        }
        return agents;
    }

    public void performSimulationStep(ArrayList<Agent> agents, List<Point> foods) throws InterruptedException {
        ForkJoinPool executor = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

        List<Agent> agentList = new ArrayList<>(agents);
        List<Point> foodList = Collections.synchronizedList(new ArrayList<>(foods));

        checkVision(agentList, foodList, executor);

        executor.shutdown();
    }
    private void checkVision(List<Agent> agents, List<Point> foods, ForkJoinPool executor) throws InterruptedException {
        int batchSize = Math.max(1, agents.size() / Runtime.getRuntime().availableProcessors());

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < agents.size(); i += batchSize) {
            int from = i;
            int to = Math.min(i + batchSize, agents.size());

            tasks.add(() -> {
                for (int j = from; j < to; j++) {
                    Agent a = agents.get(j);
                    if (a.getStatus() == AgentState.DEAD) continue;

                    Point pos = a.getCordinates();

                    // Food vision
                    List<Point> seenFoods = foods.stream()
                            .sorted(Comparator.comparingDouble(p -> calculateDistance(pos, p)))
                            .limit(5)
                            .toList();

                    int[][] foodInputs = new int[5][2];
                    for (int k = 0; k < seenFoods.size(); k++) {
                        Point f = seenFoods.get(k);
                        foodInputs[k][0] = f.x - pos.x;
                        foodInputs[k][1] = f.y - pos.y;
                    }

                    // Player vision
                    List<Agent> seenOthers = agents.stream()
                            .filter(b -> b != a && b.getStatus() == AgentState.ALIVE)
                            .sorted(Comparator.comparingDouble(b -> calculateDistance(pos, b.getCordinates())))
                            .limit(5)
                            .toList();

                    int[][] playerInputs = new int[5][3];
                    for (int k = 0; k < seenOthers.size(); k++) {
                        Agent b = seenOthers.get(k);
                        Point bp = b.getCordinates();
                        playerInputs[k][0] = bp.x - pos.x;
                        playerInputs[k][1] = bp.y - pos.y;
                        playerInputs[k][2] = b.getSize();
                    }

                    evaluateOutput(a, a.evaluateBehavior(foodInputs, playerInputs));
                }
                return null;
            });
        }

        executor.invokeAll(tasks);
    }
    private void evaluateOutput(Agent a, Action b){
        switch(b){
            case MOVEUP -> a.translateCordinates(0, Simulator.agentSpeed);
            case MOVEDOWN -> a.translateCordinates(0, -Simulator.agentSpeed);
            case MOVELEFT -> a.translateCordinates(-Simulator.agentSpeed, 0);
            case MOVERIGHT -> a.translateCordinates(Simulator.agentSpeed, 0);
            case MOVEUPLEFT -> a.translateCordinates(-Simulator.agentSpeed, Simulator.agentSpeed);
            case MOVEUPRIGHT -> a.translateCordinates(Simulator.agentSpeed, Simulator.agentSpeed);
            case MOVEDOWNLEFT -> a.translateCordinates(-Simulator.agentSpeed, -Simulator.agentSpeed);
            case MOVEDOWNRIGHT -> a.translateCordinates(Simulator.agentSpeed, -Simulator.agentSpeed);
        }
    }
    private void checkCollision(List<Agent> agents, List<Point> foods, ForkJoinPool executor) throws InterruptedException {
        List<Callable<Void>> tasks = new ArrayList<>();

        for (Agent a : agents) {
            tasks.add(() -> {
                if (a.getStatus() == AgentState.DEAD) return null;

                synchronized (foods) {
                    for (Point p : new ArrayList<>(foods)) {
                        if (calculateDistance(a.getCordinates(), p) < Simulator.agentSize + a.getSize()) {
                            a.foodEat();
                            foods.remove(p);
                        }
                    }
                }

                synchronized (agents) {
                    for (Agent other : agents) {
                        if (a == other || other.getStatus() == AgentState.DEAD) continue;

                        if (calculateDistance(a.getCordinates(), other.getCordinates()) <
                                (a.getSize() + Simulator.agentSize * 2 + other.getSize()) / 2.0) {
                            if (a.getSize() > other.getSize()) {
                                other.setStatus(AgentState.DEAD);
                                a.eatEnemy(other);
                            }
                        }
                    }
                }

                return null;
            });
        }

        executor.invokeAll(tasks);
    }

    private double calculateDistance(Point p1, Point p2)  {
        return Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
    }


}
