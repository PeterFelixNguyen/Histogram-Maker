// Copyright 2019 Peter "Felix" Nguyen

import java.awt.Color;

public class HistSelections {
    // Main
    private int initFreq;
    private int freq;
    private double initBin;
    private double bin;
    private int initFreqOption;
    private int freqOption;
    private int initBinOption;
    private int binOption;
    // Color
    private Color selectedColor;
    private Color gradientColor;
    // Gap
    private double gapSize;
    // Graph Labels
    private boolean centeredBins;
    private String[] graphLabels;
    // Other
    private boolean horizontalLines;
    private boolean frequencyLabels;

    public HistSelections(int bin, int freq) {
        this.bin = bin;
        this.freq = freq;

        this.selectedColor = new Color(0, 0, 255);
        this.gradientColor = Color.WHITE;
        centeredBins = false;
        horizontalLines = true;
        frequencyLabels = true;
    }

    public void setInitFreq(int initFreq, int option) {
        this.initFreq = initFreq;
        initFreqOption = option;
    }

    public void setFreq(int freq, int option) {
        this.freq = freq;
        freqOption = option;
    }

    public void setInitBin(double initBin, int option) {
        this.initBin = initBin;
        initBinOption = option;
    }

    public void setBin(double bin, int option) {
        this.bin = bin;
        binOption = option;
    }

    public int getInitFreq() {
        return initFreq;
    }

    public int getInitFreqOpt() {
        return initFreqOption;
    }

    public int getFreq() {
        return freq;
    }

    public int getFreqOpt() {
        return freqOption;
    }

    public double getInitBin() {
        return initBin;
    }

    public int getInitBinOpt() {
        return initBinOption;
    }
    public double getBin() {
        return bin;
    }

    public int getBinOpt() {
        return binOption;
    }

    public void setBinColors(Color color1, Color color2) {
        this.selectedColor = color1;
        this.gradientColor = color2;
    }

    public void setSelectedColor(Color color) {
        selectedColor = color;
    }

    public void setGradientColor(Color color) {
        gradientColor = color;
    }

    public Color getSelectedColor() {
        return selectedColor;
    }

    public Color getGradientColor() {
        return gradientColor;
    }

    public void setGapSize(double gapSize) {
        this.gapSize= gapSize;
    }

    public double getGapSize() {
        return gapSize;
    }

    public void setGraphLabels(String[] graphLabels) {
        this.graphLabels = graphLabels;
    }

    public String[] getGraphLabels() {
        return graphLabels;
    }

    public void toggleHorizontalLines() {
        horizontalLines = !horizontalLines;
    }

    public boolean hasHorizontalLines() {
        return horizontalLines;
    }

    public void toggleFrequencyLabels() {
        frequencyLabels = !frequencyLabels;
    }

    public boolean hasFrequencyLabels() {
        return frequencyLabels;
    }

    public void setCenteredBinLabels(boolean centeredBins) {
        this.centeredBins = centeredBins;
    }

    public boolean isCenteredBinLabels() {
        return centeredBins;
    }
}
