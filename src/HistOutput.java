// Copyright 2019 Peter "Felix" Nguyen

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class HistOutput extends JPanel {
    private HistBin bins[];
    private int graphWidth;
    private int graphHeight;
    private int binWidth;
    private int gapWidth;
    private Histogram histogram;
    private double binShare;
    private double gapShare;
    private Color binColor;
    private Color binGradient;
    private JPopupMenu contextMenu;
    private String[] graphLabels;
    private HistSelections selections;

    public HistOutput(Histogram histogram, HistSelections selections) {
        setBackground(Color.WHITE);
        this.histogram = histogram;
        bins = this.histogram.getBins();

        this.selections = selections;

        binColor = selections.getSelectedColor();
        binGradient = selections.getGradientColor();
        gapShare = selections.getGapSize() * .005;
        graphLabels = selections.getGraphLabels();

        contextMenu = new ContextMenu();

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent me) {
                showPopup(me);
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                showPopup(me);
            }

            private void showPopup(MouseEvent me) {
                if (me.isPopupTrigger()) {
                    contextMenu.show(me.getComponent(), me.getX(), me.getY());
                }
            }
        });
    }

    private void setBinDimensions() {
        double unused = 0.050;

        double gapPortion;
        double binPortion;

        if (bins.length == 1) {
            gapPortion = 0.9;
            binPortion = 0.1;
        } else if (bins.length == 2) {
            gapPortion = 0.7;
            binPortion = 0.3;
        } else if (bins.length == 3) {
            gapPortion = 0.6;
            binPortion = 0.4;
        } else {
            gapPortion = 1;
            binPortion = 1;
        }

        binShare = 1 - unused - gapShare; // binShare default would be .725
        binWidth = (int) ((graphWidth * binShare) / bins.length * binPortion);
        gapWidth = (int) ((graphWidth * gapShare) / bins.length * gapPortion);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D)g; // enhances graphics

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        graphWidth = panelWidth - 80;
        graphHeight = panelHeight - 45 - 30;

        // draw graph labels
        Font normalFont = g2d.getFont();
        AffineTransform normalAT = g2d.getTransform();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(new Font(Font.SERIF, Font.BOLD, 20));

        int strLength1 = (int) g2d.getFontMetrics().getStringBounds(graphLabels[1], g2d).getWidth();
        int strLength2 = (int) g2d.getFontMetrics().getStringBounds(graphLabels[2], g2d).getWidth();

        g2d.drawString(graphLabels[1], (panelWidth / 2) - (strLength1 / 2), panelHeight - 10); // x-label
        g2d.drawString(graphLabels[2], (panelWidth / 2) - (strLength2 / 2), 20); // graph title

        AffineTransform at = new AffineTransform();
        at.setToRotation(Math.toRadians(-90), 10, 10);
        g2d.setTransform(at);

        g2d.drawString(graphLabels[0], (panelHeight / 2) * -1, 20); // y-label

        g2d.setFont(normalFont);
        g2d.setTransform(normalAT);

        // draw graph lines
        g.drawLine(50, panelHeight - 45, panelWidth - 50, panelHeight - 45); // x-axis
        g.drawLine(50, 30, 50, panelHeight - 45); // y-axis

        setBinDimensions();

        int inverseHeight = panelHeight - 45; // total - buffer

        // find max for frequency ticks
        int frequencyTickMax = 0;
        while (frequencyTickMax < histogram.maxFrequency()) {
            frequencyTickMax += histogram.getFrequencyInterval();
        }

        int frequencyCurrent = 0;

        // draw frequency ticks
        int tickStart;
        int tickStop;

        if (selections.hasHorizontalLines()) {
            tickStart = 40;
            tickStop = panelWidth - 50;
        } else {
            tickStart = 40;
            tickStop = 60;
        }

        g.drawString(String.valueOf(frequencyCurrent), 30, inverseHeight);
        while (frequencyCurrent < histogram.maxFrequency()) {
            frequencyCurrent += histogram.getFrequencyInterval();
            double proportionalStart = (double) frequencyCurrent / frequencyTickMax * graphHeight;
            int strHeight = (int) g2d.getFontMetrics().getStringBounds(String.valueOf(frequencyCurrent), g2d).getHeight();
            g.drawString(String.valueOf(frequencyCurrent), 30, inverseHeight - (int) proportionalStart + strHeight / 2 - 2);
            g.drawLine(tickStart, inverseHeight - (int) proportionalStart, tickStop, inverseHeight - (int) proportionalStart);
        }

        if (selections.hasFrequencyLabels()) {
            // draw individual frequencies
            double frequencyHeight = 0;
            int currentBinLocationX = 75;

            for (int i = 0; i < bins.length; i++) {
                frequencyHeight = ((double) bins[i].getFrequency() / frequencyTickMax * graphHeight) + 10;
                int strWidth = (int) g2d.getFontMetrics().getStringBounds(String.valueOf(bins[i].getFrequency()), g2d).getWidth();
                g.drawString(String.valueOf(bins[i].getFrequency()), currentBinLocationX + binWidth / 2 - strWidth/2,
                        inverseHeight - (int) frequencyHeight);
                currentBinLocationX = currentBinLocationX + gapWidth + binWidth;
            }
        }

        // draw bins and rectangles
        int currentBinLocation = 75;
        for (int i = 0; i < bins.length; i++) {
            // bins
            g.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(3));
            // draw at bin edges
            int lowerStrLength = (int) g2d.getFontMetrics().getStringBounds(String.valueOf(bins[i].getBinLower()), g2d).getWidth();
            int upperStrLength = (int) g2d.getFontMetrics().getStringBounds(String.valueOf(bins[i].getBinUpper()), g2d).getWidth();
            if (selections.isCenteredBinLabels()) {
                // Create a method to process String
                String string = String.valueOf(bins[i].getBinLower()) + " - " + String.valueOf(bins[i].getBinUpper());
                int strLength = (int) g2d.getFontMetrics().getStringBounds(String.valueOf(string), g2d).getWidth();
                g.drawString(string, currentBinLocation + binWidth / 2 - strLength / 2, panelHeight - 30);
            } else {
                g.drawString(String.valueOf(bins[i].getBinLower()), currentBinLocation - lowerStrLength/2, panelHeight - 30);
                g.drawString(String.valueOf(bins[i].getBinUpper()), currentBinLocation + binWidth - upperStrLength/2, panelHeight - 30);
            }
            // rectangles
            double proportionalFreq = (double) bins[i].getFrequency() / frequencyTickMax * graphHeight;
            g2d.setPaint(new GradientPaint((float) (currentBinLocation + binWidth * 0.3), 0, binColor, currentBinLocation + binWidth, 0, binGradient));
            g2d.fill(new Rectangle2D.Double(currentBinLocation, inverseHeight - (int) proportionalFreq, binWidth, (int) proportionalFreq));
            // rectangle borders
            g.setColor(Color.BLACK);
            g.drawRect(currentBinLocation, inverseHeight - (int) proportionalFreq, binWidth, (int) proportionalFreq);
            currentBinLocation = currentBinLocation + gapWidth + binWidth;
        }
    }

    class ContextMenu extends JPopupMenu {
        private JMenuItem save;
        private JMenuItem modify;

        public ContextMenu() {
            save = new JMenuItem("Save as...");
            modify = new JMenuItem("Modify");
            add(save);
            add(modify);

            save.addActionListener(new ActionListener() {
                File outFile;

                @Override
                public void actionPerformed(ActionEvent ae) {
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(
                            "screenshot", "png");
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.addChoosableFileFilter(filter);

                    // check if file is replace / not replaced
                    if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                        outFile = fileChooser.getSelectedFile();
                        if (outFile.exists()) {
                            JOptionPane.showConfirmDialog(null,
                                    "File already exists, " + "do you want to replace file?",
                                    "Histogram", JOptionPane.YES_NO_OPTION,
                                    JOptionPane.WARNING_MESSAGE);
                        }
                        try {
                            BufferedImage img = getHistogramImage();
                            ImageIO.write(img, "png", outFile);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private BufferedImage getHistogramImage() {
        BufferedImage image = new BufferedImage(getWidth(),
                getHeight(), BufferedImage.TYPE_INT_RGB);

        paint(image.getGraphics());

        return image;
    }
}
