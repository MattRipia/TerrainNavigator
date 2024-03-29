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
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class TerrainNavigatorGUI extends JPanel implements ActionListener, MouseListener
{
    JPanel eastPanel;
    JLabel scoreLabel, computersScoreLabel;
    DrawPanel drawPanel;
    int size, offSet, boxSize;
    TerrainNavigatorModel model;
    JButton infoButton, playAgainButton, newTerrainButtonDB, newTerrainButtonRandom, retry, solveOptimalPath, solveOptimalPathDP;
    String[] dbOptions = {"tinyA", "tinyB", "small", "medium", "large", "illustrated"};
    boolean validMove = false;
    
    public TerrainNavigatorGUI()
    {
        super(new BorderLayout());
        super.setBackground(Color.WHITE);
        super.setPreferredSize(new Dimension(1200,900));

        eastPanel = new JPanel(new GridLayout(20, 0));
        scoreLabel = new JLabel("Total Difficulty: 0");
        computersScoreLabel = new JLabel("Computers Difficulty: 0");
        infoButton = new JButton("Info");
        infoButton.addActionListener(this);
        newTerrainButtonDB = new JButton("New Terrain (From Database)");
        newTerrainButtonDB.addActionListener(this);
        solveOptimalPath = new JButton("Solve Optimal Path (Greedy)");
        solveOptimalPath.addActionListener(this);
        solveOptimalPathDP = new JButton("Solve Optimal Path (Using Intelligence)");
        solveOptimalPathDP.addActionListener(this);
        newTerrainButtonRandom = new JButton("New Terrain (Randomly Generated)");
        newTerrainButtonRandom.addActionListener(this);
        retry = new JButton("Reset Current Terrain");
        retry.addActionListener(this);
        eastPanel.add(scoreLabel);
        eastPanel.add(computersScoreLabel);
        eastPanel.add(infoButton);
        eastPanel.add(newTerrainButtonDB);
        eastPanel.add(newTerrainButtonRandom);
        eastPanel.add(retry);
        eastPanel.add(solveOptimalPath);
        eastPanel.add(solveOptimalPathDP);
        
        size = 20;
        offSet = 0;
        startTerrain(this.size);
        
        addMouseListener(this);
        add(eastPanel, BorderLayout.EAST);
        add(drawPanel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {
        Object source = e.getSource();
        if(source == infoButton)
        {
            String messageOne = "Welcome to the terrain navigator created by Matt Ripia - 1385931.";
            String messageTwo = "You can either generate a random terrain, or load a terrain \nheld in a SQL Server by clicking one of the 'New Terrain' buttons.";
            String messageThree = "You can select any squares with a blue outline \nmanually and navigate the terrain using your mouse.";
            String messageFour = "You can also get the computer to solve the current terrain by clicking on one of the 'Solve Optimal Path' buttons."
                    + "\n\nThis will show the best route from the bottom to the top of the terrain depending on what intelligence level is set."
                    + "\n\n                                                                ( D Y N A M I C  P R O G R A M M I N G ) ";
            String messageFive = "At any time you can reset the current terrain using the 'Reset Current Terrain' button."
                    + "\n\nYou can also see your current difficulty as well as the computers difficulty in completing a terrain.\nThis is a running total of all the moves you have made.";
            String messageSix = "Your path is shown is Green and the computers path is shown in Orange.";
            
            JOptionPane.showMessageDialog(this, messageOne);
            JOptionPane.showMessageDialog(this, messageTwo);
            JOptionPane.showMessageDialog(this, messageThree);
            JOptionPane.showMessageDialog(this, messageFour);
            JOptionPane.showMessageDialog(this, messageFive);
            JOptionPane.showMessageDialog(this, messageSix);
        }
        else if(source == newTerrainButtonRandom)
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
                        JOptionPane.showMessageDialog(this, "That number is out of range!");
                    }
                    else
                    {
                        // kill the old thread
                        this.drawPanel.drawing = false;
                        
                        // re-instantiate a new model / draw thread
                        this.size = intSize;
                        startTerrain(this.size);
                        
                        // add the new panel to the view, then revalidate
                        add(drawPanel, BorderLayout.CENTER);
                        drawPanel.revalidate();
                    }
                } 
                catch(NumberFormatException numFormat)
                {
                   JOptionPane.showMessageDialog(this, "You didnt enter a number!");
                }
            }
        }
        else if(source == newTerrainButtonDB)
        {
            JComboBox selectionList = new JComboBox(dbOptions);
            Object[] fields = { "Load matrix From which table: ", selectionList};
            int choice = JOptionPane.showConfirmDialog(this, fields,"The Choice of a Lifetime", JOptionPane.OK_CANCEL_OPTION);
            
            if(choice == 0)
            {
                try {
                    String selectedDB = (String)selectionList.getSelectedItem();
                    
                    // re-instantiate a new model / draw thread
                    startTerrainDB(selectedDB);
                    
                    // add the new panel to the view, then revalidate
                    add(drawPanel, BorderLayout.CENTER);
                    drawPanel.revalidate();
                    drawPanel.repaint();
                } 
                catch (SQLException ex) 
                {
                    JOptionPane.showMessageDialog(this, "Unable to connect to database! Try generate a new grid randomly instead...");
                }
            }
        }
        else if(source == retry)
        {
            scoreLabel.setText("Total Difficulty: 0");
            computersScoreLabel.setText("Computers Difficulty: 0");
            
            model.resetComputer();
            model.resetPlayer();
            
            this.drawPanel.revalidate();
            this.drawPanel.repaint();
        }
        else if(source == solveOptimalPath)
        {
            if(!model.computersFirstMove){
                model.resetComputer();
                computersScoreLabel.setText("Computers Difficulty: 0");
            }
            model.solveOptimalPath(0);
            computersScoreLabel.setText("Computers Difficulty: " + model.computersTally);
            this.drawPanel.revalidate();
            this.drawPanel.repaint();
        }
        else if(source == solveOptimalPathDP)
        {
            JSlider selection = new JSlider(0, 0, 100, 100);
            selection.setPaintLabels(true);
            selection.setMajorTickSpacing(20);
            Object[] fields = { "Select intelligence level: ", selection};
            int choice = JOptionPane.showConfirmDialog(this, fields,"The Choice of a Lifetime", JOptionPane.OK_CANCEL_OPTION);
            
            if(choice == 0)
            {
                if(!model.computersFirstMove){
                    model.resetComputer();
                    computersScoreLabel.setText("Computers Difficulty: 0");
                }
                float selectedValue = selection.getValue() / 100.0f;
                System.out.println("Value: " + selection.getValue() + " Percentage: " + selectedValue);
                model.solveOptimalPath(selectedValue);
                computersScoreLabel.setText("Computers Difficulty: " + model.computersTally);
                this.drawPanel.revalidate();
                this.drawPanel.repaint();
            }
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
            if(y == model.ySize - 1)
            {
                // and if x is valid
                if(x >= 0 && x < model.xSize)
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
                 else{
                    System.out.println("invlaid x!");
                }
            }
            else{
                System.out.println("invlaid y!");
            }
        }

        if(validMove)
        {
            System.out.println("x: " + x + " y: " + y);
            model.move(x, y);
            validMove = false;
            System.out.println("score changed: " + model.tally);
            scoreLabel.setText("Total Difficulty: " + String.valueOf(model.tally));
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

    private void startTerrainDB(String selectedDB) throws SQLException
    {
        try 
        {
            // tinyA, tinyB, small, medium, large, illustrated
            this.model = new TerrainNavigatorModel(selectedDB);
            
            if(drawPanel != null)
            {
                this.drawPanel.killThread();
            }
            
            if(model.ySize > model.xSize)
            {
                this.boxSize = (900 - this.offSet * 2) / model.ySize;
            }
            else
            {
                this.boxSize = (900 - this.offSet * 2) / model.xSize;
            }
            
            this.drawPanel = new DrawPanel(boxSize, offSet, model);
            this.model.addDrawPanel(drawPanel);
            this.scoreLabel.setText("Total Difficulty: " + model.tally );
            drawPanel.drawing = true;
            
            Thread t1 = new Thread(drawPanel);
            t1.start();
        } 
        catch (SQLException ex) 
        {
            throw new SQLException();
        }
    }
    
    private void startTerrain(int size) 
    {
        if(drawPanel != null)
        {
            this.drawPanel.killThread();
        }
        
        this.model = new TerrainNavigatorModel(size);
        this.boxSize = (900 - this.offSet * 2) / model.xSize;
        this.drawPanel = new DrawPanel(boxSize, offSet, model);
        this.model.addDrawPanel(drawPanel);
        this.scoreLabel.setText("Total Difficulty: " + model.tally );
        drawPanel.drawing = true;
        
        Thread t1 = new Thread(drawPanel);
        t1.start();
    }


    
    protected class DrawPanel extends JPanel implements Runnable, Observer
    {
        int boxSize, offSet;
        TerrainNavigatorModel model;
        boolean drawing;
        
        public DrawPanel(int boxSize, int offSet, TerrainNavigatorModel model)
        {
            this.drawing = false;
            this.boxSize = boxSize;
            this.offSet = offSet;
            this.model = model;
            model.addObserver(this);
        }
        
        @Override
        protected synchronized void paintComponent(Graphics g) 
        {
            if(model != null)
            {
                super.paintComponent(g);
                printGrid(g);
                printAvailibleMove(g);
                printHistory(g);
                printComputersHistory(g);
                System.out.println("gui painted");
            }
        }
        
        private void printGrid(Graphics g)
        {
            int xStart = offSet;
            int xEnd = xStart + boxSize;
            int yStart = offSet;
            int yEnd = yStart + boxSize;
            
            // height
            for(int i = 0; i < model.ySize; i++)
            {
                // width
                for(int j = 0; j < model.xSize; j ++)
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
                    g.drawString(String.valueOf(currNum), xStart + (boxSize / 2) - (boxSize / 4), yStart + (boxSize / 2) + (boxSize / 6));
                    
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
            g2.setColor(Color.BLUE);

             // height
            for(int i = 0; i < model.ySize; i ++)
            {
                // width
                for(int j = 0; j < model.xSize; j ++)
                {
                    // first move
                    if(model.firstMove)
                    {
                        if(model.ySize - 1 == i)
                        {
                            g2.drawRect(xStart, yStart, boxSize, boxSize);
                        }
                    }
                    else
                    {
                        // find the next available move
                        if(currentX == j || currentX - 1 == j || currentX + 1 == j)
                        {
                            if(currentY - 1 == i)
                            {
                                g2.drawRect(xStart, yStart, boxSize, boxSize);
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
            Graphics2D g2 = (Graphics2D)g;
            g2.setColor(Color.GREEN);
            g2.setStroke(new BasicStroke(3));
            Point prevPoint = null;
            
            for(Point p : model.history)
            {
                if(prevPoint == null)
                {
                    prevPoint = p;
                }
                
                g2.drawLine((offSet + (boxSize * prevPoint.x)) + boxSize / 2,  (offSet + (boxSize * prevPoint.y)) + boxSize / 2,  (offSet + (boxSize * p.x)) + boxSize / 2,  (offSet + (boxSize * p.y)) + boxSize / 2);
                prevPoint = p;
            }
        }
        
        private void printComputersHistory(Graphics g)
        {
            Graphics2D g2 = (Graphics2D)g;
            g2.setColor(Color.ORANGE);
            g2.setStroke(new BasicStroke(3));
            Point prevPoint = null;
            for(Point p : model.computersHistory)
            {
                if(prevPoint == null)
                {
                    prevPoint = p;
                }
                
                g2.drawLine((offSet + (boxSize * prevPoint.x)) + boxSize / 2,  (offSet + (boxSize * prevPoint.y)) + boxSize / 2,  (offSet + (boxSize * p.x)) + boxSize / 2,  (offSet + (boxSize * p.y)) + boxSize / 2);
                prevPoint = p;
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
                        wait();
                        System.out.println("Thread is awake!");
                        repaint();
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(TerrainNavigatorGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            System.out.println("thread died! =[");
        }

        private synchronized void killThread()
        {
            boxSize = 0;
            offSet = 0;
            model = null;
            drawing = false;
            wakeUp();
        }
        
        public synchronized void wakeUp() 
        {
            notifyAll();
        }

        @Override
        public void update(Observable o, Object arg) 
        {
            System.out.println("update hit - observer");
            wakeUp();
        }
    }
}

