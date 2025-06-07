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
        Tensor[] layer1 = new Tensor[inputSize];
        //input weight and bias generation
        for(int i = 0; i<inputSize; i++){
            double[] vec = new double[sizeLayers];
            for(int j = 0; j<sizeLayers; j++){
                vec[j] = rand.nextDouble(weightMin, weightMax);
            }
            layer1[i] = new Tensor(vec);
            biasIn[i] = rand.nextDouble(biasMin, biasMax);
        }
        biases.add(new Tensor(biasIn));
        weights.add(layer1);
        //hidden layer generation
        for(int i = 0; i<noLayers-1; i++){
            Tensor [] layer = new Tensor[sizeLayers];
            double[] biasAll = new double[sizeLayers];
            for(int j = 0; j<sizeLayers; j++){
                double[] vec = new double[sizeLayers];
                for(int k = 0; k<sizeLayers; k++){
                    vec[k] = rand.nextDouble(weightMin, weightMax);
                }
                biasAll[j] = rand.nextDouble(biasMin, biasMax);
                layer[i] = new Tensor(vec);
            }
            biases.add(new Tensor(biasAll));
            weights.add(layer);
        }
        // output weights
        Tensor[] layerOut = new Tensor[outputSize];
        double[] biasOut = new double[outputSize];
        for(int i = 0; i<noLayers; i++){
            double[] vec = new double[outputSize];
            for(int j = 0; j<outputSize; j++){
                vec[j] = rand.nextDouble(weightMin, weightMax);
            }
            layerOut[i] = new Tensor(vec);
            biasOut[i] = rand.nextDouble(biasMin, biasMax);
        }
        weights.add(layerOut);
        biases.add(new Tensor(biasOut));
    }
    public Tensor input(double[] inputData){
        Tensor input = new Tensor(inputData);
        Tensor neurons = new Tensor(sizeLayers);
        Tensor[] inputWeights = weights.get(0);
        for(int i = 0; i<sizeLayers; i++){
            neurons.setNeuronValue(input.dot(inputWeights[i]), i);
        }
        for(int i = 1; i<noLayers-1; i++){
            Tensor [] currentWeights = weights.get(i);
            Tensor nextNeurons = new Tensor(sizeLayers);
            for(int j = 0; j<sizeLayers; j++){
                nextNeurons.setNeuronValue(neurons.dot(currentWeights[j]), i);
            }
            neurons = nextNeurons;
        }
        Tensor [] outputWeights = weights.get(noLayers);
        Tensor output = new Tensor(outputSize);
        for(int i = 0; i<outputSize; i++){
            output.setNeuronValue(neurons.dot(outputWeights[i]), i);
        }
        return output;
    }
}
