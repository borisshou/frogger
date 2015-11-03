// A simplified version of Grid.java with arbitrary height and width for the display of instructions

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import javax.imageio.*;
import javax.swing.*;

public class Splash extends JComponent implements KeyListener {

    private JFrame frame;
    private int lastKeyPressed;
    private Image background = null;
    private boolean keyPressed = false;

    private int imageWidth = 0;
    private int imageHeight = 0;


    public Splash(String imageFileName, int w, int h) {
        background = new ImageIcon(getClass().getResource(imageFileName)).getImage();
        imageWidth = w;
        imageHeight = h;
        init();
    }

    private void init()
    {
        lastKeyPressed = -1;

        frame = new JFrame("Instruction");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addKeyListener(this);

        setPreferredSize(new Dimension(imageWidth, imageHeight));
        frame.getContentPane().add(this);

        frame.pack();
        frame.setVisible(true);

        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        paintComponent(bi.getGraphics());


    }

    public void paintComponent(Graphics g) {
        if (background != null)
            g.drawImage(background, 0, 0, frame.getWidth(), frame.getHeight(), null);
    }

    public int checkLastKeyPressed()
    {
        int key = lastKeyPressed;
        lastKeyPressed = -1;
        return key;
    }

    public boolean checkKeyPressed() {
        boolean result = keyPressed;
        keyPressed = false;
        return result;
    }

    public void keyPressed(KeyEvent e)
    {
        keyPressed = true;
    }

    public void keyReleased(KeyEvent e)
    {
        //ignored
    }

    public void keyTyped(KeyEvent e)
    {
        //ignored
    }

    public static void pause(int milliseconds)
    {
        try
        {
            Thread.sleep(milliseconds);
        }
        catch(Exception e)
        {
            //ignore
        }
    }

    public void close() {
        frame.setVisible(false);
    }
}