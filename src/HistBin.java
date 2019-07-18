// Copyright 2019 Peter "Felix" Nguyen

public class HistBin {
    private int frequency;
    private double binLower;
    private double binUpper;

    @SuppressWarnings("unused")
    private HistBin() {
    }

    public HistBin(double binLower, double binUpper) {
        this.frequency = 0;
        this.binLower = binLower;
        this.binUpper = binUpper;
    }

    public void count() {
        frequency++;
    }

    public int getFrequency() {
        return frequency;
    }

    public double getBinLower() {
        return binLower;
    }

    public double getBinUpper() {
        return binUpper;
    }
}
