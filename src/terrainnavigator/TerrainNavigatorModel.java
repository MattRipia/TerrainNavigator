package terrainnavigator;

import java.awt.Point;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import terrainnavigator.TerrainNavigatorGUI.DrawPanel;

public class TerrainNavigatorModel 
{
    public int[][] gridMatrix;
    public Point currentPosition;
    public int tally, xSize, ySize, computersTally;
    public boolean firstMove, computersFirstMove;
    public ArrayList<Point> history, computersHistory;
    public Database db;
    public DrawPanel drawPanel;
    
    
    public TerrainNavigatorModel(String terrain) throws SQLException
    {
        int i = 0;
        int maxX = 0;
        int maxY = 0;
        this.tally = 0;
        this.firstMove = true;
        this.computersFirstMove = true;
        this.history = new ArrayList();
        this.computersHistory = new ArrayList();
        this.currentPosition = new Point(-1, -1);
        this.db = new Database();
        
        // gets the size of the matrix
        ResultSet rs = db.queryDB("select * from " + terrain);
        while(rs.next())
        {
            //ResultSetMetaData md = rs.getMetaData();
            //System.out.println("Col 1: " + md.getColumnName(1));
            //System.out.println("Col 2: " + md.getColumnName(2));
            //System.out.println("Col 3: " + md.getColumnName(3));
            // 1-x 2-y 3-diff

            //System.out.println("x: " + rs.getString(1));
            //System.out.println("y: " + rs.getString(2));
            //System.out.println("d: " + rs.getString(3));
            maxX = Math.max(maxX, Integer.valueOf(rs.getString(1)));
            maxY = Math.max(maxY, Integer.valueOf(rs.getString(2)));
            i++;
        }
        
        // matrix size
        maxX += 1;
        maxY += 1;
        xSize = maxX;
        ySize = maxY;
        
        System.out.println(terrain + " has " + i + " rows");
        System.out.println("Max x: " + xSize);
        System.out.println("Max y: " + ySize);
        
        // create the matrix
        gridMatrix = new int[ySize][xSize];

        // reset the cursor
        rs.first();
        rs.previous();
        while(rs.next())
        {
            // fill out the grid
            gridMatrix[rs.getInt(2)][rs.getInt(1)] = rs.getInt(3);
        }
    }
    
    public TerrainNavigatorModel(int size)
    {
        Random rand = new Random();
        this.gridMatrix = new int[size][size];
        this.currentPosition = new Point(-1, -1);
        this.xSize = size;
        this.ySize = size;
        this.tally = 0;
        this.firstMove = true;
        this.computersFirstMove = true;
        this.history = new ArrayList();
        this.computersHistory = new ArrayList();
        
        for(int i = 0; i < ySize; i++)
        {
            for(int j = 0; j < xSize; j++)
            {
                int num = rand.nextInt(21) - 5;
                gridMatrix[i][j] = num;
                System.out.println("num: " + num);
            }
        }
    }
    
    public void addDrawPanel(DrawPanel dp){
        this.drawPanel = dp;
    }
    
    public void notifyGUI(){
        drawPanel.wakeUp();
    }

    public void move(int x, int y) 
    {
        currentPosition.x = x;
        currentPosition.y = y; 
        tally += gridMatrix[y][x];
        history.add(new Point(x, y));
        notifyGUI();
        //System.out.println("valid move - x" +x+ " y" +y+ " Tally: " + tally);
    }
    
    public void moveAutomatically(int x, int y)
    {
        currentPosition.x = x;
        currentPosition.y = y; 
        computersTally += gridMatrix[y][x];
        computersHistory.add(new Point(x, y));
        notifyGUI();
        //System.out.println("valid move - y:" +y+ " x:" +x+ " Tally: " + tally);
    }
    
    public void solveOptimalPath(int intelligence)
    {
        // greedy choice
        if(intelligence == 0)
        {
            // find the first move
            boolean running = true;
            while(running)
            {
                ArrayList<Point> validPositions = new ArrayList();
                if(computersFirstMove)
                {
                    for(int i = 0; i < xSize; i++)
                    {
                        validPositions.add(new Point(i, ySize - 1));
                        System.out.println("adding point y:" + (ySize - 1) + " x:" + i);
                    }
                    System.out.println();
                    computersFirstMove = false;
                }
                else
                {
                    validPositions.add(new Point(currentPosition.x, currentPosition.y - 1));
                    System.out.println("adding point y:" + (currentPosition.y - 1) + " x:" + currentPosition.x);
                    
                    if(currentPosition.x - 1 >= 0)
                    {
                        Point p = new Point( currentPosition.x - 1, currentPosition.y - 1);
                        System.out.println("adding point y:" + p.y + " x:" + p.x);
                        validPositions.add(p);
                    }
                    if(currentPosition.x + 1 < xSize)
                    {
                        validPositions.add(new Point(currentPosition.x + 1, currentPosition.y - 1));
                        System.out.println("adding point y:" + (currentPosition.y - 1) + " x:" + (currentPosition.x + 1));
                    }
                }
                System.out.println();
                Point nextPoint = null;
                for(Point p : validPositions)
                {
                    if(nextPoint == null)
                    {
                        nextPoint = p;
                    }
                    //System.out.println("current y:" + p.y + " x: " + p.x);
                    if(gridMatrix[nextPoint.y][nextPoint.x] > gridMatrix[p.y][p.x])
                    {
                        nextPoint = p;
                    }
                    
                }

                System.out.println("point: " + nextPoint.x + " " + nextPoint.y + " value: " + gridMatrix[nextPoint.y][nextPoint.x]);
                moveAutomatically(nextPoint.x, nextPoint.y);
                validPositions.clear();
                if(currentPosition.y <= 0)
                {
                    running = false;
                    System.out.println("done!");
                }
            }
        }
    }
}
