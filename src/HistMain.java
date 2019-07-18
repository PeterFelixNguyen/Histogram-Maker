// Copyright 2019 Peter "Felix" Nguyen

import java.io.IOException;

import javax.swing.JFrame;

public class HistMain {
    public static void main(String[] args) throws IOException {

        JFrame optionsFrame = new JFrame("Histogram Options");
        optionsFrame.add(new HistMaker());
        optionsFrame.setSize(400, 370);
        optionsFrame.setResizable(false);
        optionsFrame.setLocationRelativeTo(null);
        optionsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        optionsFrame.setVisible(true);
    }
}
