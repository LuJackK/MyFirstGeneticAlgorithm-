import java.util.Arrays;

public class Tensor {
    private double[] data;
    public Tensor(double[] data) {
        this.data = data;
    }
    public Tensor(int size) {
        this.data = new double[size];
    }
    public void setNeuronValue(double value, int index){
        this.data[index] = value;
    }
    public double[] getData() {
        return this.data;
    }
    public int getSize() {
        return this.data.length;
    }
    public double getData(int i){
        return this.data[i];
    }
    public double dot(Tensor t) {
        double value = 0.0;
        for (int i = 0; i < this.data.length; i++) {
            value+= this.data[i] * t.data[i];
        }
        return value;
    }
    public Tensor sum(Tensor t) {
        double[] newData = new double[this.data.length];
        for (int i = 0; i < this.data.length; i++) {
            newData[i] = this.data[i] + t.data[i];
        }
        return new Tensor(newData);
    }
}
