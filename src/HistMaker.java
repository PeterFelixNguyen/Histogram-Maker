// Copyright 2019 Peter "Felix" Nguyen

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

@SuppressWarnings("serial")
public class HistMaker extends JPanel {
    private JButton jbtImport;
    private JButton jbtDefault;
    private File inFile;
    private Scanner input;
    private String inputValues;
    private UpperPanel upperPanel;
    private LowerPanel lowerPanel;
    private JButton jbtPrevious, jbtNext;
    private int panelIndex = 0;
    private ValuesPanel valuesPanel;
    private BinAndFreqOptions binAndFreqOptions;
    private HistCustomizePanel1 customizeHist1;
    private HistCustomizePanel2 customizeHist2;
    private HistSelections selections;

    public HistMaker() {
        setLayout(new GridLayout(2, 1));

        UIManager.put("RadioButton.font", "Arial");

        upperPanel = new UpperPanel();
        lowerPanel = new LowerPanel();

        valuesPanel = new ValuesPanel();
        binAndFreqOptions = new BinAndFreqOptions();
        customizeHist1 = new HistCustomizePanel1();
        customizeHist2 = new HistCustomizePanel2();

        upperPanel.add(valuesPanel);

        jbtImport = new JButton("Import");
        jbtImport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    inFile = fileChooser.getSelectedFile();
                    try {
                        input = new Scanner(inFile);
                        inputValues = "";

                        while (input.hasNext()) {
                            inputValues += input.nextDouble() + " ";
                        }

                        valuesPanel.setValues(inputValues);

                        try {
                        }
                        catch (InputMismatchException ex) {
                            JOptionPane.showMessageDialog(null, "InputMismatchException, "
                                    + "make sure the data set contains numbers only",
                                    "Failure", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                    catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        jbtDefault = new JButton("Default");

        jbtPrevious = new JButton("Previous");
        jbtPrevious.setEnabled(false);
        jbtNext = new JButton("Next");
        jbtNext.setPreferredSize(new Dimension(86, 26));

        jbtPrevious.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (panelIndex > 0) {
                    panelIndex--;
                }

                upperPanel.removeAll();

                switch (panelIndex) {
                    case 0:
                        jbtPrevious.setEnabled(false);
                        jbtImport.setEnabled(true);
                        upperPanel.add(valuesPanel);
                        break;
                    case 1:
                        upperPanel.add(binAndFreqOptions);
                        break;
                    case 2:
                        jbtNext.setText("Next");
                        upperPanel.add(customizeHist1);
                        break;
                }
                upperPanel.revalidate();
                upperPanel.repaint();
            }
        });

        jbtNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (panelIndex < 4) {
                    panelIndex++;
                }

                if (panelIndex < 4) {
                    upperPanel.removeAll();
                }

                switch (panelIndex) {
                    case 1:
                        try {
                            binAndFreqOptions.makeValuesArray(valuesPanel.getValues());
                            binAndFreqOptions.setXPreview();
                            binAndFreqOptions.setYPreview();
                        } catch (NumberFormatException ex) {
                            panelIndex = 0;
                            upperPanel.add(valuesPanel);
                            break;
                        }
                        jbtPrevious.setEnabled(true);
                        jbtImport.setEnabled(false);
                        upperPanel.add(binAndFreqOptions);
                        break;
                    case 2:
                        upperPanel.add(customizeHist1);
                        break;
                    case 3:
                        jbtNext.setText("Generate");
                        upperPanel.add(customizeHist2);
                        break;
                    case 4:
                        HistLoader histogramLoader = new HistLoader();
                        customizeHist2.setGraphLabels();
                        if (histogramLoader.loadFileIntoArray(selections, valuesPanel.getValues())) {
                            JFrame frame = new JFrame("Histogram");
                            frame.add(histogramLoader.getHistogramPanel());
                            frame.setSize(500, 300);
                            frame.setLocationRelativeTo(null);
                            frame.setVisible(true);
                        }
                        panelIndex--;
                        break;
                }
                upperPanel.revalidate();
                upperPanel.repaint();
            }
        });

        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        panel1.add(jbtImport);
        panel1.add(jbtDefault);

        panel2.add(jbtPrevious);
        panel2.add(jbtNext);

        lowerPanel.add(panel1);
        lowerPanel.add(panel2);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(upperPanel);
        add(lowerPanel);
    }

    class UpperPanel extends JPanel {

        public UpperPanel() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setMaximumSize(new Dimension(400, 300));
            setMinimumSize(new Dimension(400, 300));
            setBorder(new TitledBorder(""));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(400, 300);
        }
    }

    class LowerPanel extends JPanel {

        public LowerPanel() {
            setLayout(new GridLayout(1, 2));
            setBorder(new TitledBorder(""));
            setMaximumSize(new Dimension(400, 40));
            setMinimumSize(new Dimension(400, 40));
        }
    }

    class ValuesPanel extends JPanel {
        private JTextArea jtaValues;
        private JScrollPane scrollerInput;

        public ValuesPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panel.add(new JLabel("Enter values"));
            panel.setPreferredSize(new Dimension(400, 25));
            panel.setMaximumSize(new Dimension(400, 25));
            jtaValues = new JTextArea("");
            jtaValues.setLineWrap(true);
            jtaValues.setWrapStyleWord(true);
            jtaValues.setEditable(true);
            scrollerInput = new JScrollPane(jtaValues);
            add(panel);
            add(scrollerInput);
        }

        public void setValues(String string) {
            jtaValues.setText(string);
        }

        public String getValues() {
            return jtaValues.getText();
        }
    }

    class BinAndFreqOptions extends JPanel {
        private JPanel jpFreq;
        private JRadioButton jrbInitFreqValue, jrbInitFreqMin;
        private JRadioButton jrbFreqInterval, jrbFreqNumTicks;
        private JLabel[] jlblsInitFreq, jlblsFreq;
        private JPanel jpBin;
        private JRadioButton jrbInitBinValue, jrbInitBinMin;
        private JRadioButton jrbBinInterval, jrbBinNumTicks;
        private JLabel[] jlblsInitBin, jlblsBin;
        private ArrayList<Double> values;
        private int[] freqTicks;
        private double[] binTicks;
        private JLabel jlblFreqPreview, jlblBinPreview;
        private Histogram histogram;

        public BinAndFreqOptions() {
            setLayout(new GridLayout());

            selections = new HistSelections(5, 5);

            firstMakeLeftSide();
            secondMakeRightSide();
        }

        private void firstMakeLeftSide() {
            jpFreq = new JPanel(new GridLayout(9, 1));
            jpFreq.setBorder(new TitledBorder("Frequency  (y-axis)"));

            jrbInitFreqValue = new JRadioButton("specified value", true);
            jrbInitFreqMin = new JRadioButton("smallest freq.");
            jrbFreqInterval = new JRadioButton("size of intervals", true);
            jrbFreqNumTicks = new JRadioButton("number of ticks");
            ButtonGroup freqGroup1 = new ButtonGroup();
            freqGroup1.add(jrbInitFreqValue);
            freqGroup1.add(jrbInitFreqMin);
            ButtonGroup freqGroup2 = new ButtonGroup();
            freqGroup2.add(jrbFreqInterval);
            freqGroup2.add(jrbFreqNumTicks);
            jlblFreqPreview = new JLabel("   y1, y2, y3, ...");
            jlblFreqPreview.setFont(new Font("Arial", Font.PLAIN, 12));

            JPanel[] freqRow = new JPanel[8];
            for (int i = 0; i < 8 ; i++) {
                freqRow[i] = new JPanel(new FlowLayout(FlowLayout.LEFT));
                jpFreq.add(freqRow[i]);
            }

            jlblsInitFreq = new JLabel[2];
            for (int i = 0; i < jlblsInitFreq.length ;i++) {
                jlblsInitFreq[i] = new JLabel();
                jlblsInitFreq[i].setFont(new Font("Arial", Font.PLAIN, 12));
            }

            jlblsInitFreq[0].setText("(0)");

            jlblsFreq = new JLabel[2];
            for (int i = 0; i < jlblsFreq.length; i++) {
                jlblsFreq[i] = new JLabel();
                jlblsFreq[i].setPreferredSize(new Dimension(45, 15));
                jlblsFreq[i].setFont(new Font("Arial", Font.PLAIN, 12));
            }
            jlblsFreq[0].setText("(5)");

            freqRow[0].add(new JLabel("Preview:"));
            freqRow[1].add(jlblFreqPreview);
            freqRow[2].add(new JLabel("Set minimum frequency"));
            freqRow[3].add(jrbInitFreqValue); freqRow[3].add(jlblsInitFreq[0]);
            freqRow[4].add(jrbInitFreqMin); freqRow[4].add(jlblsInitFreq[1]);
            freqRow[5].add(new JLabel("Set y-axis intervals"));
            freqRow[6].add(jrbFreqInterval); freqRow[6].add(jlblsFreq[0]);
            freqRow[7].add(jrbFreqNumTicks); freqRow[7].add(jlblsFreq[1]);

            // should rewrite listener so need need for int option
            jrbInitFreqValue.addActionListener(new BinAndFreqRadioListener(jrbInitFreqValue, jlblsInitFreq, selections, "setInitFreq", 0));
            jrbInitFreqMin.addActionListener(new BinAndFreqRadioListener(jrbInitFreqMin, jlblsInitFreq, selections, "setInitFreq", 1));

            jrbFreqInterval.addActionListener(new BinAndFreqRadioListener(jrbFreqInterval, jlblsFreq, selections, "setFreq", 0));
            jrbFreqNumTicks.addActionListener(new BinAndFreqRadioListener(jrbFreqNumTicks, jlblsFreq, selections, "setFreq", 1)); // warning: number bins not set yet

            add(jpFreq);
        }

        private void secondMakeRightSide() {
            jpBin = new JPanel(new GridLayout(9, 1));
            jpBin.setBorder(new TitledBorder("Bin  (x-axis)"));

            jrbInitBinValue = new JRadioButton("specified value", true); // user provided value, not rounded
            jrbInitBinMin = new JRadioButton("smallest bin");
            jrbBinInterval = new JRadioButton("size of intervals", true);
            jrbBinNumTicks = new JRadioButton("number of bins");
            ButtonGroup binGroup1 = new ButtonGroup();
            binGroup1.add(jrbInitBinValue);
            binGroup1.add(jrbInitBinMin);
            ButtonGroup binGroup2 = new ButtonGroup();
            binGroup2.add(jrbBinInterval);
            binGroup2.add(jrbBinNumTicks);
            jlblBinPreview = new JLabel("   x1, x2, x3, ...");
            jlblBinPreview.setFont(new Font("Arial", Font.PLAIN, 12));

            JPanel[] binRow = new JPanel[8];
            for (int i = 0; i < 8 ; i++) {
                binRow[i] = new JPanel(new FlowLayout(FlowLayout.LEFT));
                jpBin.add(binRow[i]);
            }

            jlblsInitBin = new JLabel[2];
            for (int i = 0; i < jlblsInitBin.length; i++) {
                jlblsInitBin[i] = new JLabel();
                jlblsInitBin[i].setFont(new Font("Arial", Font.PLAIN, 12));
            }
            jlblsInitBin[0].setText("(0)");

            jlblsBin = new JLabel[2];
            for (int i = 0; i < jlblsBin.length; i++) {
                jlblsBin[i] = new JLabel();
                jlblsBin[i].setPreferredSize(new Dimension(45, 15));
                jlblsBin[i].setFont(new Font("Arial", Font.PLAIN, 12));
            }
            jlblsBin[0].setText("(5)");

            binRow[0].add(new JLabel("Preview:"));
            binRow[1].add(jlblBinPreview);
            binRow[2].add(new JLabel("Set minimum bin value"));
            binRow[3].add(jrbInitBinValue); binRow[3].add(jlblsInitBin[0]);
            binRow[4].add(jrbInitBinMin); binRow[4].add(jlblsInitBin[1]);
            binRow[5].add(new JLabel("Set x-axis intervals"));
            binRow[6].add(jrbBinInterval); binRow[6].add(jlblsBin[0]);
            binRow[7].add(jrbBinNumTicks); binRow[7].add(jlblsBin[1]);

            jrbInitBinValue.addActionListener(new BinAndFreqRadioListener(jrbInitBinValue, jlblsInitBin, selections, "setInitBin", 0));
            jrbInitBinMin.addActionListener(new BinAndFreqRadioListener(jrbInitBinMin, jlblsInitBin, selections, "setInitBin", 1)); // warning: number bins not set yet

            jrbBinInterval.addActionListener(new BinAndFreqRadioListener(jrbBinInterval, jlblsBin, selections, "setBin", 0));
            jrbBinNumTicks.addActionListener(new BinAndFreqRadioListener(jrbBinNumTicks, jlblsBin, selections, "setBin", 1));

            add(jpBin);
        }

        public void makeValuesArray(String strValues) {
            values = new ArrayList<Double>();

            String[] stringArray = strValues.split("\\s+");

            for (int i = 0; i < stringArray.length; i++) {
                values.add(new Double(stringArray[i]));
            }
        }

        private void setYPreview() {
            histogram = new Histogram(selections, values);
            freqTicks = histogram.getFreqTicks();

            String previewString = "   ";

            for (int i = 0; i < freqTicks.length; i++) {
                previewString += freqTicks[i];
                if (i != freqTicks.length - 1) {
                    previewString += ", ";
                }
                if (i == 2 && freqTicks.length > 4) {
                    previewString += "... " + freqTicks[freqTicks.length - 1];
                    break;
                }
            }
            jlblFreqPreview.setText(previewString);
        }

        private void setXPreview() {
            histogram = new Histogram(selections, values);
            binTicks = histogram.getBinTicks();

            String previewString = "   ";

            for (int x = 0; x < binTicks.length; x++) {
                System.out.println("setXPreview: " + binTicks[x]);
            }

            for (int i = 0; i < binTicks.length; i++) {
                previewString += binTicks[i];
                if (i != binTicks.length - 1) {
                    previewString += ", ";
                }
                if (i == 2 && binTicks.length > 4) {
                    previewString += "... " + binTicks[binTicks.length - 1];
                    break;
                }
            }
            jlblBinPreview.setText(previewString);
        }

        class BinAndFreqRadioListener implements ActionListener {
            private JRadioButton button;
            private JLabel[] labels;
            private HistSelections histOptions;
            private String optionStr;
            private int option;

            public BinAndFreqRadioListener(JRadioButton button, JLabel[] labels, HistSelections histOptions, String optionStr, int option) {
                this.button = button;
                this.labels = labels;
                this.histOptions = histOptions;
                this.optionStr = optionStr;
                this.option = option;
            }

            @Override
            public void actionPerformed(ActionEvent arg0) {
                String value = JOptionPane.showInputDialog(null, "Enter value",
                        "Tick value", JOptionPane.PLAIN_MESSAGE);
                for (int i = 0; i < labels.length; i++) {
                    labels[i].setText("");
                }
                if (value == null) {
                    button.setSelected(true);
                } else {
                    try {
                        switch (optionStr) {
                            case "setInitBin":
                                histOptions.setInitBin(Double.valueOf(value), option);
                                break;
                            case "setBin":
                                histOptions.setBin(Double.valueOf(value), option);
                                break;
                            case "setInitFreq":
                                histOptions.setInitFreq(Integer.valueOf(value), option);
                                break;
                            case "setFreq":
                                histOptions.setFreq(Integer.valueOf(value), option);
                                break;
                        }
                        labels[option].setText("(" + value + ")");
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "NumberFormatException, "
                                + "make sure the data set contains numbers only",
                                "Failure", JOptionPane.WARNING_MESSAGE);
                        button.setSelected(true);
                    }
                }
                setXPreview();
                setYPreview();
            }
        }
    }

    class HistCustomizePanel1 extends JPanel {
        private JPanel colorPanel = new JPanel();
        private JPanel binAndGapWidthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        private JPanel otherSettingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        private JTextField jtfRedValues = new JTextField("0");
        private JTextField jtfGreenValues = new JTextField("0");
        private JTextField jtfBlueValues = new JTextField("255");
        private int rValue = 0;
        private int gValue = 0;
        private int bValue = 255;
        private ColorButton jbtColorPreview;
        private boolean isSolidColor = false;
        private ColorPalette palette;
        private JSlider gapSlider;

        public HistCustomizePanel1() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new TitledBorder("Customize"));

            firstMakeColorsPanel();
            secondMakeColorGradientPanel();
            thirdMakeBinAndGapPanel();

            colorPanel.setOpaque(false);

            add(colorPanel);
            add(otherSettingsPanel);
            add(binAndGapWidthPanel);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D)g;

            g2d.setPaint(new GradientPaint(40, 0, Color.BLACK, 185, 0, Color.RED));
            g2d.fill(new Rectangle2D.Double(40, 52, 185, 13));

            g2d.setPaint(new GradientPaint(40, 0, Color.BLACK, 185, 0, Color.GREEN));
            g2d.fill(new Rectangle2D.Double(40, 92, 185, 13));

            g2d.setPaint(new GradientPaint(40, 0, Color.BLACK, 185, 0, Color.BLUE));
            g2d.fill(new Rectangle2D.Double(40, 132, 185, 13));
        }

        public void firstMakeColorsPanel() {
            JPanel leftChildPanel = new JPanel();
            leftChildPanel.setLayout(new GridLayout(3, 1));
            leftChildPanel.setOpaque(false);

            JPanel rightChildPanel = new JPanel();
            jbtColorPreview = new ColorButton(null, selections.getSelectedColor(), selections.getGradientColor());
            jbtColorPreview.setPreferredSize(new Dimension(90, 90));
            rightChildPanel.add(jbtColorPreview);

            JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT));

            row1.setOpaque(false);
            row2.setOpaque(false);
            row3.setOpaque(false);

            final JSlider redSlider = new JSlider();
            final JSlider greenSlider = new JSlider();
            final JSlider blueSlider = new JSlider();

            redSlider.setOpaque(false);
            greenSlider.setOpaque(false);
            blueSlider.setOpaque(false);

            redSlider.setMinimum(0);
            redSlider.setMaximum(255);
            redSlider.setPaintLabels(true);
            redSlider.setPaintTicks(true);
            redSlider.setValue(rValue);
            redSlider.addChangeListener(new RgbSliderListener(jtfRedValues, "red"));
            jtfRedValues.addFocusListener(new RgbFocusListener(jtfRedValues));

            greenSlider.setMinimum(0);
            greenSlider.setMaximum(255);
            greenSlider.setPaintLabels(true);
            greenSlider.setPaintTicks(true);
            greenSlider.setValue(gValue);
            greenSlider.addChangeListener(new RgbSliderListener(jtfGreenValues, "green"));
            jtfGreenValues.addFocusListener(new RgbFocusListener(jtfGreenValues));

            blueSlider.setMinimum(0);
            blueSlider.setMaximum(255);
            blueSlider.setPaintLabels(true);
            blueSlider.setPaintTicks(true);
            blueSlider.setValue(bValue);
            blueSlider.addChangeListener(new RgbSliderListener(jtfBlueValues, "blue"));
            jtfBlueValues.addFocusListener(new RgbFocusListener(jtfBlueValues));

            jtfRedValues.setPreferredSize(new Dimension(30, 30));
            jtfGreenValues.setPreferredSize(new Dimension(30, 30));
            jtfBlueValues.setPreferredSize(new Dimension(30, 30));
            jtfRedValues.setBackground(new Color(238, 238, 238));
            jtfGreenValues.setBackground(new Color(238, 238, 238));
            jtfBlueValues.setBackground(new Color(238, 238, 238));
            jtfRedValues.setBorder(javax.swing.BorderFactory.createEmptyBorder());
            jtfGreenValues.setBorder(javax.swing.BorderFactory.createEmptyBorder());
            jtfBlueValues.setBorder(javax.swing.BorderFactory.createEmptyBorder());

            AbstractDocument doc1 = (AbstractDocument) jtfRedValues.getDocument();
            AbstractDocument doc2 = (AbstractDocument) jtfGreenValues.getDocument();
            AbstractDocument doc3 = (AbstractDocument) jtfBlueValues.getDocument();
            doc1.setDocumentFilter(new SliderFilter(redSlider, jtfRedValues));
            doc2.setDocumentFilter(new SliderFilter(greenSlider, jtfGreenValues));
            doc3.setDocumentFilter(new SliderFilter(blueSlider, jtfBlueValues));

            palette = new ColorPalette(selections.getSelectedColor());

            jbtColorPreview.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int selected = JOptionPane.showConfirmDialog(null, palette,
                            "Choose color", JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE);
                    if (selected == JOptionPane.OK_OPTION) {
                        redSlider.setValue(palette.getColor().getRed());
                        greenSlider.setValue(palette.getColor().getGreen());
                        blueSlider.setValue(palette.getColor().getBlue());
                        if (isSolidColor) {
                            selections.setGradientColor(selections.getSelectedColor());
                        }
                        jbtColorPreview.setColor(selections.getSelectedColor(), selections.getGradientColor());
                        selections.setBinColors(selections.getSelectedColor(), selections.getGradientColor());
                    }
                }
            });

            row1.add(new JLabel("R"));
            row1.add(redSlider);
            row1.add(jtfRedValues);

            row2.add(new JLabel("G"));
            row2.add(greenSlider);
            row2.add(jtfGreenValues);

            row3.add(new JLabel("B"));
            row3.add(blueSlider);
            row3.add(jtfBlueValues);

            leftChildPanel.add(row1);
            leftChildPanel.add(row2);
            leftChildPanel.add(row3);

            colorPanel.setBorder(new TitledBorder("Choose bin color"));
            colorPanel.add(leftChildPanel);
            colorPanel.add(rightChildPanel);
        }

        class RgbSliderListener implements ChangeListener {
            private JTextField textField;
            private String color;

            public RgbSliderListener(JTextField textField, String color) {
                this.textField = textField;
                this.color = color;
            }

            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                switch (color) {
                    case "red":
                        rValue = source.getValue();
                        textField.setText(String.valueOf(rValue));
                        break;
                    case "green":
                        gValue = source.getValue();
                        textField.setText(String.valueOf(gValue));
                        break;
                    case "blue":
                        bValue = source.getValue();
                        textField.setText(String.valueOf(bValue));
                        break;
                }

                selections.setSelectedColor(new Color(rValue, gValue, bValue));

                if (isSolidColor) {
                    selections.setGradientColor(selections.getSelectedColor());
                }
                selections.setBinColors(selections.getSelectedColor(), selections.getGradientColor());
                jbtColorPreview.setColor(selections.getSelectedColor(), selections.getGradientColor());
            }
        }

        class RgbFocusListener implements FocusListener {
            private JTextField textField;

            public RgbFocusListener(JTextField textField) {
                this.textField = textField;
            }

            @Override
            public void focusGained(FocusEvent e) {
                // No action
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().length() == 0) {
                    textField.setText("0");
                }
            }
        }

        /*
         * Regular Expression tool:
         * 
         * http://utilitymill.com/utility/Regex_For_Range
         */
        class SliderFilter extends DocumentFilter {
            private JSlider slider;
            private Pattern pattern;
            private JTextField txtField;

            public SliderFilter(JSlider slider, JTextField txtField) {
                this.slider = slider;
                pattern = Pattern.compile("([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])");
                this.txtField = txtField;
            }

            // manual invocation
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                String newStr = fb.getDocument().getText(0, fb.getDocument().getLength()) + string;
                Matcher m = pattern.matcher(newStr);

                if (m.matches()) {
                    super.insertString(fb, offset, string, attr);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }

                try {
                    slider.setValue(Integer.valueOf(txtField.getText()));
                } catch (NumberFormatException ex) {
                    slider.setValue(Integer.valueOf(0));
                }
            }

            // automatic invocation
            @Override
            public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
                slider.setValue(Integer.valueOf(txtField.getText()));
                super.remove(fb, offset, length);
            }

            // automatic invocation
            @Override
            public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attr) throws BadLocationException {
                if (length > 0) {
                    fb.remove(offset, length);
                }

                insertString(fb, offset, string, attr);
            }
        }

        public void thirdMakeBinAndGapPanel() {
            binAndGapWidthPanel.setBorder(new TitledBorder("Adjust gap between bins"));
            gapSlider = new JSlider();
            gapSlider.setMinimum(0);
            gapSlider.setMaximum(10);
            gapSlider.setPaintTicks(true);
            gapSlider.setPaintLabels(true);
            gapSlider.setMajorTickSpacing(1);
            gapSlider.setValue(0);
            gapSlider.setPreferredSize(new Dimension(265, 45));

            gapSlider.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    JSlider source = (JSlider)e.getSource();
                    if (!source.getValueIsAdjusting()) {
                        double gap = source.getValue() * 5;
                        selections.setGapSize(gap);
                    }
                }
            });

            binAndGapWidthPanel.add(new JLabel("no gap"));
            binAndGapWidthPanel.add(gapSlider);
            binAndGapWidthPanel.add(new JLabel("big gap"));
        }

        public void secondMakeColorGradientPanel() {
            otherSettingsPanel.setBorder(new TitledBorder("Choose color gradient"));

            JRadioButton jrbSolid = new JRadioButton("Solid color", null, false);
            jrbSolid.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    jbtColorPreview.setColor(selections.getSelectedColor(), selections.getSelectedColor());
                    isSolidColor = true;
                    selections.setBinColors(selections.getSelectedColor(), selections.getSelectedColor());
                }
            });
            JRadioButton jrbBlack = new JRadioButton("Black gradient", null, false);
            jrbBlack.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    selections.setGradientColor(Color.BLACK);
                    jbtColorPreview.setColor(selections.getSelectedColor(), selections.getGradientColor());
                    isSolidColor = false;
                    selections.setBinColors(selections.getSelectedColor(), selections.getGradientColor());
                }
            });
            JRadioButton jrbWhite = new JRadioButton("White gradient", null, true);
            jrbWhite.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    selections.setGradientColor(Color.WHITE);
                    jbtColorPreview.setColor(selections.getSelectedColor(), selections.getGradientColor());
                    isSolidColor = false;
                    selections.setBinColors(selections.getSelectedColor(), selections.getGradientColor());
                }
            });
            ButtonGroup gradientGroup = new ButtonGroup();
            gradientGroup.add(jrbSolid);
            gradientGroup.add(jrbBlack);
            gradientGroup.add(jrbWhite);
            otherSettingsPanel.add(jrbSolid);
            otherSettingsPanel.add(jrbBlack);
            otherSettingsPanel.add(jrbWhite);
        }
    }

    class HistCustomizePanel2 extends JPanel {
        private JPanel jpLabelBins, jpOtherOptions;
        private JRadioButton jrbMiddleOfBin, jrbBetweenBin;
        private JCheckBox jchkHorizontalLines, jchkIndividFreq;
        private String[] graphLabels;
        private JTextField yLabel, xLabel, graphTitle;

        public HistCustomizePanel2() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new TitledBorder("Customize"));

            yLabel = new JTextField(25);
            xLabel = new JTextField(25);
            graphTitle = new JTextField(25);
            yLabel.setText("Frequency");
            xLabel.setText("Ranges");
            graphTitle.setText("Histogram");

            graphLabels = new String[3];
            Arrays.fill(graphLabels, "");

            jrbMiddleOfBin = new JRadioButton("in the middle of bins", null, false);
            jrbBetweenBin = new JRadioButton("between bins", null, true);

            ButtonGroup group0 = new ButtonGroup();
            group0.add(jrbMiddleOfBin);
            group0.add(jrbBetweenBin);

            jrbMiddleOfBin.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    selections.setCenteredBinLabels(true);
                }
            });

            jrbBetweenBin.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    selections.setCenteredBinLabels(false);
                }
            });

            JPanel jpLabelRow0 = new JPanel(new FlowLayout(FlowLayout.CENTER));
            jpLabelRow0.add(new JLabel("Label bins"));
            jpLabelRow0.add(jrbMiddleOfBin);
            jpLabelRow0.add(jrbBetweenBin);

            JPanel jpLabelRow1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            jpLabelRow1.add(new JLabel("y-axis"));
            jpLabelRow1.add(yLabel);

            JPanel jpLabelRow2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            jpLabelRow2.add(new JLabel("x-axis"));
            jpLabelRow2.add(xLabel);

            JPanel jpLabelRow3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            jpLabelRow3.add(new JLabel("Graph title"));
            jpLabelRow3.add(graphTitle);

            jpLabelBins = new JPanel();
            jpLabelBins.setLayout(new BoxLayout(jpLabelBins, BoxLayout.Y_AXIS));
            jpLabelBins.setBorder(new TitledBorder("Labels"));
            jpLabelBins.setMaximumSize(new Dimension(400, 200));
            jpLabelBins.add(jpLabelRow0);
            jpLabelBins.add(jpLabelRow1);
            jpLabelBins.add(jpLabelRow2);
            jpLabelBins.add(jpLabelRow3);

            jchkHorizontalLines = new JCheckBox("Horizontal guide lines", null, true);
            jchkIndividFreq = new JCheckBox("Label individual frequencies", null, true);

            jchkHorizontalLines.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    selections.toggleHorizontalLines();
                }
            });

            jchkIndividFreq.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    selections.toggleFrequencyLabels();
                }
            });

            jpOtherOptions = new JPanel(new FlowLayout(FlowLayout.LEFT));
            jpOtherOptions.setLayout(new GridLayout(2, 2));
            jpOtherOptions.setMaximumSize(new Dimension(400, 100));
            jpOtherOptions.setBorder(new TitledBorder("Other options"));
            jpOtherOptions.add(jchkHorizontalLines);
            jpOtherOptions.add(jchkIndividFreq);

            add(jpLabelBins);
            add(jpOtherOptions);
        }

        public void setGraphLabels() {
            graphLabels[0] = yLabel.getText();
            graphLabels[1] = xLabel.getText();
            graphLabels[2] = graphTitle.getText();
            selections.setGraphLabels(graphLabels);
        }
    }

    class ColorButton extends JButton{
        private Color firstColor;
        private Color secondColor;

        public ColorButton(String buttonName, Color firstColor, Color secondColor){
            super(buttonName);
            setContentAreaFilled(false);
            setFocusPainted(false);
            this.firstColor = firstColor;
            this.secondColor = secondColor;
        }

        @Override
        protected void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setPaint(new GradientPaint((float) (getWidth() * 0.3), 0, firstColor, getWidth(), 0, secondColor));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();

            super.paintComponent(g);
        }

        public void setColor(Color firstColor, Color secondColor) {
            this.firstColor = firstColor;
            this.secondColor = secondColor;
            repaint();
        }
    }

    class ColorPalette extends JPanel {
        private JButton[] colorButtons = new JButton[13];
        private int j;
        private Color selectedColor;
        private JPanel previewLine, upperPanel;

        public Color getColor() {
            return selectedColor;
        }

        public ColorPalette(final Color currentColor) {
            setPreferredSize(new Dimension(400, 160));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            previewLine = new JPanel();
            previewLine.setPreferredSize(new Dimension(400, 5));
            previewLine.setBackground(Color.BLUE);

            upperPanel = new JPanel(new GridLayout(1, 13));
            upperPanel.setPreferredSize(new Dimension(400, 100));
            upperPanel.setBackground(Color.BLUE);

            for (int i = 0; i < colorButtons.length; i++) {
                colorButtons[i] = new JButton();
                colorButtons[i].setBorderPainted(false);
                upperPanel.add(colorButtons[i]);
            }
            colorButtons[0].setBackground(Color.magenta);
            colorButtons[1].setBackground(Color.blue);
            colorButtons[2].setBackground(Color.cyan);
            colorButtons[3].setBackground(Color.green);
            colorButtons[4].setBackground(Color.yellow);
            colorButtons[5].setBackground(Color.orange);
            colorButtons[6].setBackground(Color.pink);
            colorButtons[7].setBackground(Color.red);
            colorButtons[8].setBackground(Color.black);
            colorButtons[9].setBackground(Color.darkGray);
            colorButtons[10].setBackground(Color.gray);
            colorButtons[11].setBackground(Color.lightGray);
            colorButtons[12].setBackground(Color.white);

            selectedColor = currentColor;

            for (j = 0; j < colorButtons.length; j++) {
                final int temp = j; // required, otherwise j will always be 13 for all indices
                colorButtons[j].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        selectedColor = colorButtons[temp].getBackground();
                        previewLine.setBackground(selectedColor);
                        upperPanel.setBackground(selectedColor);
                    }
                });
            }

            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(400, 50));
            panel.setOpaque(false);

            add(previewLine);
            add(upperPanel);
            add(panel);
        }

        public BufferedImage createStringImage(Graphics g, String s) {
            int w = g.getFontMetrics().stringWidth(s) + 5;
            int h = g.getFontMetrics().getHeight();

            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D imageGraphics = image.createGraphics();
            imageGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            imageGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            imageGraphics.setColor(Color.BLACK);
            imageGraphics.setFont(g.getFont());
            imageGraphics.drawString(s, 0, h - g.getFontMetrics().getDescent());
            imageGraphics.dispose();

            return image;
        }

        private void drawString(Graphics g, String s, int tx, int ty, double theta) {
            // Translate then rotate
            AffineTransform aff = new AffineTransform();
            aff.translate(tx, ty);
            aff.rotate(theta);

            Graphics2D g2D = ((Graphics2D) g);
            g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2D.drawImage(createStringImage(g, s), aff, this);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setFont(new Font("Arial", Font.PLAIN, 15));

            drawString(g, "magenta", 10, 105, Math.toRadians(30));
            drawString(g, "blue", 40, 105, Math.toRadians(30));
            drawString(g, "cyan", 70, 105, Math.toRadians(30));
            drawString(g, "green", 100, 105, Math.toRadians(30));
            drawString(g, "yellow", 130, 105, Math.toRadians(30));
            drawString(g, "orange", 160, 105, Math.toRadians(30));
            drawString(g, "pink", 190, 105, Math.toRadians(30));
            drawString(g, "red", 220, 105, Math.toRadians(30));
            drawString(g, "black", 250, 105, Math.toRadians(30));
            drawString(g, "dark gray", 280, 105, Math.toRadians(30));
            drawString(g, "gray", 310, 105, Math.toRadians(30));
            drawString(g, "light gray", 340, 105, Math.toRadians(30));
            drawString(g, "white", 370, 105, Math.toRadians(30));
        }
    }
}
