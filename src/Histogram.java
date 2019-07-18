// Copyright 2019 Peter "Felix" Nguyen

import java.util.ArrayList;

public class Histogram {
    private double binInterval;
    private double frequencyInterval;
    private double binMin;
    private double maxValue;

    private int binCount;
    private HistBin bins[];
    private int freqTicks[];
    private double binTicks[];

    @SuppressWarnings("unused")
    private Histogram() {
    }

    public Histogram(HistSelections selections,
            ArrayList<Double> values) {
        SortAlgorithms.BubbleSort(values);
        this.maxValue = values.get(values.size() - 1);
        processMainOptions(selections, values);
        makeFreqsAndTicks();
    }

    private void processMainOptions(HistSelections selections, ArrayList<Double> values) {
        if (selections.getInitBinOpt() == 0) {
            this.binMin = values.get(0);
        } else if (selections.getInitBinOpt() == 1) {
            this.binMin = selections.getInitBin();
        } else {
            this.binMin = values.get(0);
        }

        if (selections.getBinOpt() == 0) {
            this.binInterval = selections.getBin();
        } else if (selections.getBinOpt() == 1) {
            this.binInterval = selections.getBin();
        } else {
            this.binInterval = values.get(values.size() - 1) / selections.getBin(); // needs fixing, sometimes out of bounds (July 7)
        }

        makeBinsAndTicks();
        fillBins(values);

        if (selections.getFreqOpt() == 0) {
            this.frequencyInterval = selections.getFreq();
        } else if (selections.getFreqOpt() == 1) {
            this.frequencyInterval = selections.getFreq();
        } else {
            this.frequencyInterval = (double) maxFrequency() / selections.getFreq(); // not always working correctly (July 7)
        }
    }

    private void makeFreqsAndTicks() {
        int maxFrequency = 0, counter = 0;

        while (maxFrequency < maxFrequency()) {
            maxFrequency += frequencyInterval;
            counter++;
        }

        freqTicks = new int[counter];
        int currentFrequency = 0;

        for (int i = 0; i < freqTicks.length; i++) {
            currentFrequency += frequencyInterval;
            freqTicks[i] = currentFrequency;
        }

        for (int i = 0; i < freqTicks.length; i++) {
            System.out.println("freqTick[" + i + "]" + " = " + freqTicks[i]);
        }
    }

    public int[] getFreqTicks() {
        return freqTicks;
    }

    private void makeBinsAndTicks() {
        double binMax = 0;
        binMax += binMin;
        binCount = 0;

        // start algorithm 1A
        //        while (binMax <= maxValue) {
        //            binMax += binInterval;
        //            binCount++;
        //        }
        // end algorithm 1A

        // start algorithm 1B
        while (binMax <= maxValue) {
            binMax += binInterval;
        }

        binCount = (int) ((binMax + binMin * -1)/ binInterval);
        // end algorithm 1B

        System.out.println("BIN MAX " + binMax);
        System.out.println("MAX BIN VALUE " + maxValue);
        System.out.println("BIN COUNT " + binCount);

        bins = new HistBin[binCount];

        int counter = 0;
        double binCurrent = binMin;
        double binMinCurrent = binMin;
        binTicks = new double[binCount];
        while (counter < binCount) {
            binTicks[counter] = binCurrent;
            binCurrent += binInterval;
            System.out.println("binTicks printed: " + binTicks[counter]);
            bins[counter++] = new HistBin(binMinCurrent, binCurrent);
            binMinCurrent = binCurrent;
        }
    }

    public double[] getBinTicks() {
        return binTicks;
    }

    /**
     * Calculates the frequency for each bins with the given data.
     */
    private void fillBins(ArrayList<Double> values) {
        int i = 0;
        int j = 0;
        while (i < values.size()) {
            if (values.get(i) < bins[j].getBinUpper()) {
                if (values.get(i) >= bins[j].getBinLower()) {
                    bins[j].count();
                }
                i++;
            } else {
                j++;
            }
        }
    }

    public int maxFrequency() {
        int max = 0;
        for (int i = 0; i < bins.length;i++) {
            if (max < bins[i].getFrequency()) {
                max = bins[i].getFrequency();
            }
        }
        return max;
    }

    public HistBin[] getBins() {
        return bins;
    }

    public double getFrequencyInterval() {
        return frequencyInterval;
    }
}
