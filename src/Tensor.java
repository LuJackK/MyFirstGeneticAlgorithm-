public class Tensor {
    private double[] data;
    public Tensor(double[] data) {
        this.data = data;
    }
    public double[] getData() {
        return this.data;
    }
    public Tensor dot(Tensor t) {
        double[] newData = new double[this.data.length];
        for (int i = 0; i < this.data.length; i++) {
            newData[i] = this.data[i] * t.data[i];
        }
        return new Tensor(newData);
    }
    public Tensor sum(Tensor t) {
        double[] newData = new double[this.data.length];
        for (int i = 0; i < this.data.length; i++) {
            newData[i] = this.data[i] + t.data[i];
        }
        return new Tensor(newData);
    }
}
