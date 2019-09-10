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
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class TerrainNavigatorGUI extends JPanel implements ActionListener, MouseListener
{
    JPanel eastPanel;
    JLabel scoreLabel;
    DrawPanel drawPanel;
    int size, offSet, boxSize;
    TerrainNavigatorModel model;
    JButton playAgainButton, newTerrainButtonDB, newTerrainButtonRandom;
    
    boolean validMove = false;
    
    public TerrainNavigatorGUI()
    {
        super(new BorderLayout());
        super.setBackground(Color.WHITE);
        super.setPreferredSize(new Dimension(1300,900));

        eastPanel = new JPanel(new GridLayout(20, 0));
        scoreLabel = new JLabel("Score: 0");
        newTerrainButtonDB = new JButton("New Terrain (From Database)");
        newTerrainButtonDB.addActionListener(this);
        newTerrainButtonRandom = new JButton("New Terrain (Randomly Generated)");
        newTerrainButtonRandom.addActionListener(this);
        eastPanel.add(scoreLabel);
        eastPanel.add(newTerrainButtonDB);
        eastPanel.add(newTerrainButtonRandom);
        
        size = 20;
        startTerrain(false);
        
        drawPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        eastPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        addMouseListener(this);
        add(eastPanel, BorderLayout.EAST);
        add(drawPanel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {
        Object source = e.getSource();
        if(source == newTerrainButtonRandom)
        {
            String size = JOptionPane.showInputDialog(this, "Enter how large you would like the grid (5 - 30)", "Grid Size", 1);
            System.out.println(size);
            
            if(size != null)
            {
                try
                {
                    int intSize = Integer.valueOf(size);
                    if(intSize < 5 || intSize > 30)
                    {
                        JOptionPane.showConfirmDialog(this, "That number is out of range!", "Opps", 2);
                    }
                    else
                    {
                        // kill the old thread
                        this.drawPanel.drawing = false;
                        
                        // re-instantiate a new model / draw thread
                        this.size = intSize;
                        startTerrain(false);
                        
                        // add the new panel to the view, then revalidate
                        add(drawPanel, BorderLayout.CENTER);
                        drawPanel.revalidate();
                    }
                } 
                catch(NumberFormatException numFormat)
                {
                   JOptionPane.showConfirmDialog(this, "You didnt enter a number!", "Opps", 2);
                }
            }
        }
        else if(source == newTerrainButtonDB)
        {
            // kill the old thread
            this.drawPanel.drawing = false;
            
            // re-instantiate a new model / draw thread
            this.size = 20;
            startTerrain(true);
            
            // add the new panel to the view, then revalidate
            add(drawPanel, BorderLayout.CENTER);
            drawPanel.revalidate();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) 
    {
        System.out.println("location: " + e.getLocationOnScreen());
        
        Point p = e.getLocationOnScreen();
        Point currentWindow = super.getLocationOnScreen();
        
        // find out what box was clicked on based on mouse position
        p.x -= currentWindow.x;
        p.y -= currentWindow.y;
        p.x -= offSet;
        p.y -= offSet;
        int x = p.x / boxSize;
        int y = p.y / boxSize;
        
        if(model.firstMove)
        {
            // if last row is clicked
            if(y == size - 1)
            {
                // and if x is valid
                if(x >= 0 || x < size)
                {
                    validMove = true;
                    model.firstMove = false;
                }
            }
        }
        else
        {
            if(model.currentPosition.y == y + 1)
            {
                if(model.currentPosition.x == x || model.currentPosition.x == x -1 || model.currentPosition.x == x+1)
                {
                    validMove = true;
                }
                 else
                {
                    System.out.println("invlaid x!");
                }
            }
            else
            {
                System.out.println("invlaid y!");
            }
        }

        if(validMove)
        {
            System.out.println("x: " + x + " y: " + y);
            model.move(x, y);
            drawPanel.wakeUp();
            validMove = false;
            System.out.println("score changed: " + model.tally);
            scoreLabel.setText("Score: " + String.valueOf(model.tally));
        }
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

    private void startTerrain(boolean db) 
    {
        this.offSet = 20;
        
        if(db)
        {
            try 
            {
                // tinyA, tinyB, small, medium, large, illustrated
                this.model = new TerrainNavigatorModel("large");
            } 
            catch (SQLException ex) 
            {
                Logger.getLogger(TerrainNavigatorGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            this.model = new TerrainNavigatorModel(size);

        }
        
        this.boxSize = (900 - this.offSet * 2) / model.xSize;
        this.drawPanel = new DrawPanel(boxSize, offSet, model);
        this.scoreLabel.setText("Score: " + model.tally );
        Thread t1 = new Thread(drawPanel);
        t1.start();
    }
    
    private class DrawPanel extends JPanel implements Runnable
    {
        int boxSize, offSet;
        TerrainNavigatorModel model;
        boolean drawing = true;
        
        public DrawPanel(int boxSize, int offSet, TerrainNavigatorModel model)
        {
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
            printHistory(g);
        }
        
        private void printGrid(Graphics g)
        {
            int xStart = offSet;
            int xEnd = xStart + boxSize;
            int yStart = offSet;
            int yEnd = yStart + boxSize;
            
            // height
            for(int i = 0; i < model.xSize; i ++)
            {
                // width
                for(int j = 0; j < model.ySize; j ++)
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
            for(int i = 0; i < model.xSize; i ++)
            {
                // width
                for(int j = 0; j < model.ySize; j ++)
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

        private void printHistory(Graphics g) 
        {
            g.setColor(Color.GREEN);
            Point prevPoint = null;
            
            for(Point p : model.history)
            {
                if(prevPoint == null)
                {
                    prevPoint = p;
                }
                
                g.drawLine((offSet + (boxSize * prevPoint.x)) + boxSize / 2,  (offSet + (boxSize * prevPoint.y)) + boxSize / 2,  (offSet + (boxSize * p.x)) + boxSize / 2,  (offSet + (boxSize * p.y)) + boxSize / 2);
                prevPoint = p;
                //g.fillOval((offSet + (boxSize * p.x)) + boxSize / 2, (offSet + (boxSize * p.y)) + boxSize / 2, 10, 10);
            }
        }

        @Override
        public void run() 
        {
            while(drawing)
            {
                try {
                    synchronized(this)
                    {
                        this.wait();
                        System.out.println("in while loop");
                        this.repaint();
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(TerrainNavigatorGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        private synchronized void wakeUp() 
        {
            notifyAll();
        }
    }
}

