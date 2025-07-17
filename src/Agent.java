import java.awt.*;
import java.util.*;

public class Agent {
    private NeuralNetwork genome;
    private int foodEaten;
    private int mapExited;
    private double foodProximty=0;
    private Point cordinates;
    private static final int noOfHiddenLayers = 2;
    private static final double biasMin = 0;
    private static final double biasMax = 0.001;
    private static final double weightMin = -1;
    private static final int weightMax = 1;
    public Agent(int inputSize, int outputSize) {
        int sizeLayers =(int) (((inputSize + outputSize) / 2.0) + (2*inputSize)) / 2;
        this.genome = new NeuralNetwork(inputSize, outputSize, noOfHiddenLayers, sizeLayers, weightMin, weightMax, biasMin, biasMax);
        foodEaten = 0;
        //this.genome.printNN();
    }
    public Agent(NeuralNetwork genome) {
        this.genome = genome;
    }
    public Action evaluateBehavior(int[][] foodInputs, int[][] playerInputs){
        double[] input = new double[foodInputs.length+playerInputs.length+2];
        int counter = 0;
        for(int i =0; i<foodInputs.length; i++){
            input[counter] = foodInputs[i][0];
            input[counter+1] = foodInputs[i][1];
            counter += 2;
        }
        counter = 0;
        for(int i =0; i<playerInputs.length; i++){
            input[counter] = playerInputs[i][0];
            input[counter+1] = playerInputs[i][1];
            counter += 2;
        }
        input[(foodInputs.length + playerInputs.length)-2] = cordinates.x;
        input[(foodInputs.length + playerInputs.length)-1] = cordinates.y;
        double[] output = this.genome.input(input);
        return evaluateNNOutput(output);
    }

    private Action evaluateNNOutput(double[] output){
        int mostActivatedNeuron=-1;
        double maxValue = Double.NEGATIVE_INFINITY;
        //System.out.println(Arrays.toString(output));
        for(int i =0; i<output.length; i++){
            if(output[i] > maxValue){
                maxValue = output[i];
                mostActivatedNeuron = i;
            }
        }

        return switch (mostActivatedNeuron) {
            case (0) -> Action.MOVEUP;
            case (1) -> Action.MOVEDOWN;
            case (2) -> Action.MOVERIGHT;
            case (3) -> Action.MOVELEFT;
            case (4) -> Action.MOVEUPRIGHT;
            case (5) -> Action.MOVEDOWNRIGHT;
            case (6) -> Action.MOVEDOWNLEFT;
            case (7) -> Action.MOVEUPLEFT;
            default -> Action.NONE;
        };
    }

    public void foodEat(){
        foodEaten++;
    }
    public double getStats(){
        return (foodEaten*100) + (1000-foodProximty) - (mapExited*100);
    }

    public void setFoodProximty(double foodProximty) {
        this.foodProximty = foodProximty;
    }

    public Point getCordinates() {
        return cordinates;
    }
    public void setCordinates(Point cordinates) {
        this.cordinates = cordinates;
    }
    public void translateCordinates(int dx, int dy) {
        this.cordinates.x += dx;
        this.cordinates.y += dy;
    }

    public NeuralNetwork getGenome() {
        return genome;
    }

    public void mutateAgent(double mutationStepSize, double mutationRate){
        ArrayList<Tensor[]> weights = this.genome.getWeights();
        ArrayList<Tensor> biases = this.genome.getBiases();
        ArrayList<Tensor[]> newWeights = new ArrayList<>();
        ArrayList<Tensor> newBiases = new ArrayList<>();
        NeuralNetwork newGenome;
        for(int i=0; i<weights.size(); i++){
            Tensor[] newLayer = new Tensor[weights.get(i).length];
            for(int j=0; j<weights.get(i).length; j++){
                double[] currentVector = weights.get(i)[j].getData();
                double[] newVector = new double[currentVector.length];
                for(int k=0; k<currentVector.length; k++){
                    if(Math.random()>mutationRate) {
                        double randomNum = ((Math.random()*mutationStepSize) * (Math.random()<0.5 ? -1 : 1));
                        newVector[k] = currentVector[k] + randomNum;
                    }
                    else{
                        newVector[k] = currentVector[k];
                    }
                }
                newLayer[j] = (new Tensor(newVector));
            }
            newWeights.add(newLayer);
            double[] currentBias = biases.get(i).getData();
            double[] newBiasVector = new double[currentBias.length];
            for(int j=0; j<currentBias.length; j++){
                if(Math.random()<mutationRate) {
                    double randomNum = ((Math.random()*mutationStepSize) * (Math.random()<0.5 ? -1 : 1));
                    newBiasVector[j] = currentBias[j] + randomNum;
                }
                else{
                    newBiasVector[j] = currentBias[j];
                }
            }
            Tensor newBiasTensor = new Tensor(newBiasVector);
            newBiases.add(newBiasTensor);
        }
        this.genome = new NeuralNetwork(newWeights, newBiases);
    }


    public void punish(){
        mapExited++;
    }
}
