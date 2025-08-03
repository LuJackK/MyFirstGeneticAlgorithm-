import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class WorkerHandler {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public WorkerHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public ArrayList<Agent> sendAndReceive(PopulationBatch batch) throws Exception {
        out.writeObject(batch);
        out.flush();
        SimulationResult result = (SimulationResult) in.readObject();
        return result.updatedAgents;
    }

    public void close() throws IOException {
        socket.close();
    }
}
