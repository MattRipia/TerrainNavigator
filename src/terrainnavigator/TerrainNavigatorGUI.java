package terrainnavigator;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TerrainNavigatorGUI extends JPanel implements ActionListener, MouseListener
{
    TerrainNavigatorModel model;
    DrawPanel drawPanel;
    int size;
    int offSet;
    int boxSize;
    JLabel score, scoreLabel;
    JButton playAgainButton;
    JPanel eastPanel;
    
    public TerrainNavigatorGUI()
    {
        super(new BorderLayout());
        super.setBackground(Color.WHITE);
        super.setPreferredSize(new Dimension(1000,900));
        
        size = 20;
        offSet = 20;
        boxSize = (900 - offSet * 2) / size;
        
        model = new TerrainNavigatorModel(size);
        drawPanel = new DrawPanel(size, boxSize, offSet, model);
        
        eastPanel = new JPanel(new GridLayout(20, 0));
        score = new JLabel("0");
        scoreLabel = new JLabel("Score:     ");
        playAgainButton = new JButton("Play Again");
        eastPanel.add(scoreLabel);
        eastPanel.add(score);
        eastPanel.add(playAgainButton);
        
        
        drawPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        eastPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        addMouseListener(this);
        add(eastPanel, BorderLayout.EAST);
        add(drawPanel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {
        
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        //System.out.println("location: " + e.getLocationOnScreen());
        
        Point p = e.getLocationOnScreen();
        Point currentWindow = super.getLocationOnScreen();
        p.x -= currentWindow.x;
        p.y -= currentWindow.y;
        p.x -= offSet;
        p.y -= offSet;
        
        int x = p.x / boxSize;
        int y = p.y / boxSize;
        
        System.out.println("x: " + x + " y: " + y);
        model.move(x, y);
        score.setText(String.valueOf(model.tally));
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    
    private class DrawPanel extends JPanel
    {
        int size, boxSize, offSet;
        TerrainNavigatorModel model;
        
        public DrawPanel(int size, int boxSize, int offSet, TerrainNavigatorModel model)
        {
            this.size = size;
            this.boxSize = boxSize;
            this.offSet = offSet;
            this.model = model;
        }
        
        @Override
        protected void paintComponent(Graphics g) 
        {
            super.paintComponent(g);
            printGrid(g);
            printAvailibleMove(g);
        }
        
        private void printGrid(Graphics g)
        {
            int xStart = offSet;
            int xEnd = xStart + boxSize;
            int yStart = offSet;
            int yEnd = yStart + boxSize;
            
            // height
            for(int i = 0; i < size; i ++)
            {
                // width
                for(int j = 0; j < size; j ++)
                {
                    int currNum = model.gridMatrix[i][j];
                    // -5 -> -1 = 5
                    if(currNum <  0){
                        g.setColor(Color.WHITE);
                    }
                    // 0 -> 4 = 5
                    else if(currNum < 5){
                        g.setColor(new Color(255, 219, 219));
                    }
                    // 5 - 9 -> 5
                    else if(currNum < 10){
                        g.setColor(new Color(255, 140, 140));
                    }
                    // 10 -> 15 = 6
                    else{
                        g.setColor(new Color(255, 66, 66));
                    }

                    g.fillRect(xStart, yStart, boxSize, boxSize);
                    
                    // draw string
                    g.setColor(Color.BLACK);
                    g.drawString(String.valueOf(currNum), xStart + (boxSize / 2), yStart + (boxSize / 2));
                    
                    // reset x start and endpoints
                    xStart = xEnd;
                    xEnd = xStart + boxSize;
                }
                
                // resets the y start and endpoints
                yStart = yEnd;
                yEnd = yStart + boxSize;

                // resets the x start and endpoints
                xStart = offSet;
                xEnd = xStart + boxSize;
            }
        }

        private void printAvailibleMove(Graphics g) 
        {
            int xStart = offSet;
            int xEnd = xStart + boxSize;
            int yStart = offSet;
            int yEnd = yStart + boxSize;
            int currentX = model.currentPosition.x;
            int currentY  = model.currentPosition.y;
            
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(2));
            g.setColor(Color.BLUE);

             // height
            for(int i = 0; i < size; i ++)
            {
                // width
                for(int j = 0; j < size; j ++)
                {
                    // first move
                    if(currentX == -1 || currentY == -1)
                    {
                        if(size - 1 == i)
                        {
                            g.drawRect(xStart, yStart, boxSize, boxSize);
                        }
                    }
                    else
                    {
                        // find the next available move
                        if(currentX == j || currentX - 1 == j || currentX + 1 == j)
                        {
                            if(currentY - 1 == i)
                            {
                                g.drawRect(xStart, yStart, boxSize, boxSize);
                            }
                        }
                    }

                    // reset x start and endpoints
                    xStart = xEnd;
                    xEnd = xStart + boxSize;
                }
                
                // resets the y start and endpoints
                yStart = yEnd;
                yEnd = yStart + boxSize;

                // resets the x start and endpoints
                xStart = offSet;
                xEnd = xStart + boxSize;
            }
        }
    }
}

