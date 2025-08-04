import java.util.ArrayList;
import java.util.Random;

public class Population extends ArrayList<Agent> {
    private static final int outputSize = 8;
    private Random rand = new Random();
    public Population(int n, int inputSize) {
        for(int i = 0; i < n; i++) {
            this.add(new Agent(inputSize, outputSize));
        }
    }
    public Population(ArrayList<Agent> a) {
        this.addAll(a);
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
            //System.out.println(this.get(i).getStats());
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
    
    public Population crossover(boolean Elitism){
        ArrayList<Agent> crossover = new ArrayList<>();
        if(!Elitism){
            for(int i=0; i<this.size(); i++){

                crossover.add(agentUniformCrossover(this.get(i), this.get(this.size()-1-i)));
                crossover.add(agentUniformCrossover(this.get(this.size()-1-i), this.get(i)));
            }
        }
        else{
            for(int i=0; i<Math.floor(this.size()*0.2); i++){
                this.get(i).setSize(0);
                crossover.add(this.get(i));
            }
            for(int i=0; i<this.size()-Math.floor(this.size()*0.2); i++){
                crossover.add(agentUniformCrossover(this.get(i), this.get(this.size()-1-i)));
                crossover.add(agentUniformCrossover(this.get(this.size()-1-i), this.get(i)));
            }
        }
        return new Population(crossover);
    }
    private Agent agentUniformCrossover(Agent a, Agent b){
        NeuralNetwork genomeA = a.getGenome();
        NeuralNetwork genomeB = b.getGenome();
        NeuralNetwork newGenome;
        ArrayList<Tensor[]> weightsA = genomeA.getWeights();
        ArrayList<Tensor[]> weightsB = genomeB.getWeights();
        ArrayList<Tensor> biasesA = genomeA.getBiases();
        ArrayList<Tensor> biasesB = genomeB.getBiases();
        ArrayList<Tensor[]> newWeights = new ArrayList<>();
        ArrayList<Tensor> newBiases = new ArrayList<>();
        for(int i=0; i<weightsA.size(); i++){
            Tensor[] newLayer = new Tensor[weightsA.get(i).length];
            for(int j=0; j<weightsA.get(i).length; j++){
                double[] currentVectorA = weightsA.get(i)[j].getData();
                double[] currentVectorB = weightsB.get(i)[j].getData();
                double[] newVector = new double[currentVectorA.length];
                for(int k=0; k<currentVectorA.length; k++){
                    newVector[k] = (Math.random()<0.5) ? currentVectorA[k] : currentVectorB[k];
                }
                Tensor newTensor = new Tensor(newVector);
                newLayer[j] = newTensor;
            }
            newWeights.add(newLayer);
            double[] newBiasVector = new double[biasesA.get(i).getData().length];
            double[] currentBiasA = biasesA.get(i).getData();
            double[] currentBiasB = biasesB.get(i).getData();
            for(int j=0; j<currentBiasA.length; j++){
                newBiasVector[j] = (Math.random()<0.5) ? currentBiasA[j] : currentBiasB[j];
            }
            Tensor newBiasTensor = new Tensor(newBiasVector);
            newBiases.add(newBiasTensor);
        }
        newGenome = new NeuralNetwork(newWeights, newBiases);
        //newGenome.printNN();
        return new Agent(newGenome);
    }
    public void mutate(double mutationStepSize, double mutationRate){
        for(Agent a : this){
            a.mutateAgent(mutationStepSize, mutationRate);

        }
    }

}
