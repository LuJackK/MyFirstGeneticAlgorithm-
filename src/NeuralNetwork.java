import java.util.ArrayList;
import java.util.Random;
public class NeuralNetwork {
    private int inputSize;
    private int outputSize;
    private int sizeLayers;
    private int noLayers;
    private ArrayList<Tensor[]> weights = new ArrayList();
    private ArrayList<Tensor> biases = new ArrayList();
    public NeuralNetwork(int inputSize, int outputSize, int noLayers, int sizeLayers, int weightMin, int weightMax, int biasMin, int biasMax) {
        Random rand = new Random();
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.sizeLayers = sizeLayers;
        this.noLayers = noLayers;
        double[] biasIn = new double[sizeLayers];
        Tensor[] layer1 = new Tensor[sizeLayers];
        //input weight and bias generation
        for(int i = 0; i<sizeLayers; i++){
            double[] vec = new double[inputSize];
            for(int j = 0; j<inputSize; j++){
                vec[j] = rand.nextDouble(weightMin, weightMax);
            }
            layer1[i] = new Tensor(vec);
            biasIn[i] = rand.nextDouble(biasMin, biasMax);
        }
        biases.add(new Tensor(biasIn));
        weights.add(layer1);
        //hidden layer generation
        for(int i = 0; i<noLayers; i++){
            Tensor [] layer = new Tensor[sizeLayers];
            double[] biasAll = new double[sizeLayers];
            for(int j = 0; j<sizeLayers; j++){
                double[] vec = new double[sizeLayers];
                for(int k = 0; k<sizeLayers; k++){
                    vec[k] = rand.nextDouble(weightMin, weightMax);
                }
                biasAll[j] = rand.nextDouble(biasMin, biasMax);
                layer[j] = new Tensor(vec);
            }
            biases.add(new Tensor(biasAll));
            weights.add(layer);
        }
        // output weights
        Tensor[] layerOut = new Tensor[outputSize];
        double[] biasOut = new double[outputSize];
        for(int i = 0; i<outputSize; i++){
            double[] vec = new double[sizeLayers];
            for(int j = 0; j<sizeLayers; j++){
                vec[j] = rand.nextDouble(weightMin, weightMax);
            }
            layerOut[i] = new Tensor(vec);
            biasOut[i] = rand.nextDouble(biasMin, biasMax);
        }
        weights.add(layerOut);
        biases.add(new Tensor(biasOut));
    }
    public void printNN() {
        System.out.println("Neural Network Structure:");
        System.out.println("Input Size: " + inputSize);
        System.out.println("Hidden Layers: " + noLayers + " with size " + sizeLayers);
        System.out.println("Output Size: " + outputSize);
        System.out.println();

        // Input Layer
        System.out.println("=== Input Layer ===");
        System.out.println("Weights:");
        Tensor[] inputWeights = weights.get(0);
        for (int i = 0; i < inputWeights.length; i++) {
            for(int j = 0; j < inputWeights[i].getSize(); j++) {
                System.out.print(inputWeights[i].getData(j) + " ");
            }
            System.out.println();
        }
        System.out.println("Biases:");
        for(int i = 0; i < biases.getFirst().getSize(); i++) {
            System.out.print(biases.getFirst().getData(i)+ " ");
        }

        // Hidden Layers
        for (int layer = 1; layer <= noLayers; layer++) {
            System.out.println("\n=== Hidden Layer " + layer + " ===");
            System.out.println("Weights:");
            Tensor[] hiddenWeights = weights.get(layer);
            for (int i = 0; i < hiddenWeights.length; i++) {
                for(int j = 0; j < hiddenWeights[i].getSize(); j++) {
                    System.out.print(hiddenWeights[i].getData(j) + " ");
                }
                System.out.println();
            }
            System.out.println("Biases:");
            for(int i = 0; i < biases.get(layer).getSize(); i++) {
                System.out.print(biases.get(layer).getData(i)+ " ");
            }
        }

        // Output Layer
        System.out.println("\n=== Output Layer ===");
        Tensor[] outputWeights = weights.get(weights.size() - 1);
        Tensor outputBias = biases.get(biases.size() - 1);
        System.out.println("Weights:");
        for (int i = 0; i < outputWeights.length; i++) {
            for(int j = 0; j < outputWeights[i].getSize(); j++) {
                System.out.print(outputWeights[i].getData(j) + " ");
            }
            System.out.println();
        }
        System.out.println("Biases:");
        for(int i = 0; i < biases.getLast().getSize(); i++) {
            System.out.print(biases.getLast().getData(i)+ " ");
        }
    }

    public Tensor input(double[] inputData){
        Tensor input = new Tensor(inputData);
        Tensor neurons = new Tensor(sizeLayers);
        Tensor[] inputWeights = weights.get(0);
        Tensor inputBias = biases.get(0);
        for(int i = 0; i<sizeLayers ; i++){
            neurons.setNeuronValue((input.dot(inputWeights[i])+inputBias.getData(i)), i);
        }
        for(int i = 1; i<noLayers; i++){
            Tensor [] currentWeights = weights.get(i);
            Tensor currentBias = biases.get(i);
            Tensor nextNeurons = new Tensor(sizeLayers);
            for(int j = 0; j<sizeLayers; j++){
                nextNeurons.setNeuronValue((neurons.dot(currentWeights[j])+currentBias.getData(j)), i);
                //calulates the dot product of the previous layer and its respective weights for a specific neuron and adds bias.
            }
            neurons = nextNeurons;
        }
        Tensor [] outputWeights = weights.get(noLayers);
        Tensor outputBias = biases.get(noLayers);
        Tensor output = new Tensor(outputSize);
        for(int i = 0; i<outputSize; i++){
            output.setNeuronValue((neurons.dot(outputWeights[i])+outputBias.getData(i)), i);
        }
        return output;
    }
}
