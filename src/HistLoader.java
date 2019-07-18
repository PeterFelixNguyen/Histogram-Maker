// Copyright 2019 Peter "Felix" Nguyen

import java.util.ArrayList;
import java.util.InputMismatchException;

import javax.swing.JOptionPane;

public class HistLoader {
    private ArrayList<Double> values;
    private Histogram histogram;
    private HistOutput histOutput;
    private String stringValues;

    // initialize empty histogram panel
    public HistLoader() {
    }

    public boolean loadFileIntoArray(HistSelections selections, String stringValues) {
        values = new ArrayList<Double>();
        this.stringValues = stringValues;

        try {
            stringToDoubleArray();

            histogram = new Histogram(selections, values);
            histOutput = new HistOutput(histogram, selections);
            return true;
        }
        catch (InputMismatchException ex) {
            JOptionPane.showMessageDialog(null, "InputMismatchException, "
                    + "make sure the data set contains numbers only",
                    "Failure", JOptionPane.WARNING_MESSAGE);
        }
        return false;
    }

    private void stringToDoubleArray() {
        String[] stringArray = stringValues.split("\\s+");

        for (int i = 0; i < stringArray.length; i++) {
            values.add(new Double(stringArray[i]));
        }
    }

    // do i need this?
    public Histogram getHistogram() {
        return histogram;
    }

    public HistOutput getHistogramPanel() {
        return histOutput;
    }
}
