import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DistributedManager {
    private final int port;
    private final List<WorkerHandler> workers = new ArrayList<>();
    private ServerSocket serverSocket;
    private ExecutorService pool;


    public DistributedManager(int port) {
        this.port = port;
    }

    public void startServer(int expectedWorkers) throws IOException {
        pool = Executors.newFixedThreadPool(expectedWorkers);
        serverSocket = new ServerSocket(port);
        System.out.println("Master: Listening on port " + port);
        for (int i = 0; i < expectedWorkers; i++) {
            Socket socket = serverSocket.accept();
            WorkerHandler handler = new WorkerHandler(socket);
            workers.add(handler);
            System.out.println("Master: Connected to worker " + (i + 1));
        }
    }

    public List<Agent> distributeAndCollect(List<Agent> agents, List<Point> food) throws Exception {

        int batchSize = agents.size() / workers.size();
        List<Future<List<Agent>>> futures = new ArrayList<>();
        for (int i = 0; i < workers.size(); i++) {
            int from = i * batchSize;
            int to = (i == workers.size() - 1) ? agents.size() : from + batchSize;
            List<Agent> subList = agents.subList(from, to);
            PopulationBatch batch = new PopulationBatch(subList, food);

            WorkerHandler handler = workers.get(i);
            futures.add(pool.submit(() -> handler.sendAndReceive(batch)));
        }

        // Collect and merge agent results
        List<Agent> updated = new ArrayList<>();
        for (Future<List<Agent>> future : futures) {
            updated.addAll(future.get());
        }

        // Replace old agent list or merge results here
        agents.clear();
        agents.addAll(updated);
        return updated;
    }

    public void stop() throws IOException {
        for (WorkerHandler wh : workers) {
            wh.close();
        }
        serverSocket.close();
    }
}
