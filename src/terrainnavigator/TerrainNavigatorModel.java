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
    public Point currentPosition, computersPosition;
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
        this.computersPosition = new Point(-1, -1);
        this.db = new Database();
        
        // gets the size of the matrix
        ResultSet rs = db.queryDB("select * from " + terrain);
        while(rs.next())
        {
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
        this.computersPosition = new Point(-1, -1);
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
    }
    
    public void moveAutomatically(int x, int y)
    {
        computersPosition.x = x;
        computersPosition.y = y; 
        computersTally += gridMatrix[y][x];
        computersHistory.add(new Point(x, y));
        notifyGUI();
    }
    
    public void solveOptimalPath(float intelligence)
    {
        // greedy choice
        if(intelligence == 0.0f)
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
                    validPositions.add(new Point(computersPosition.x, computersPosition.y - 1));
                    System.out.println("adding point y:" + (computersPosition.y - 1) + " x:" + computersPosition.x);
                    
                    if(computersPosition.x - 1 >= 0)
                    {
                        Point p = new Point( computersPosition.x - 1, computersPosition.y - 1);
                        System.out.println("adding point y:" + p.y + " x:" + p.x);
                        validPositions.add(p);
                    }
                    if(computersPosition.x + 1 < xSize)
                    {
                        validPositions.add(new Point(computersPosition.x + 1, computersPosition.y - 1));
                        System.out.println("adding point y:" + (computersPosition.y - 1) + " x:" + (computersPosition.x + 1));
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
                if(computersPosition.y <= 0)
                {
                    running = false;
                    System.out.println("done!");
                }
            }
        }
        else
        {
            // dynamic solution
            boolean running = true;
            int forwardRows = (int)(ySize * intelligence);
            int rowsLeft = 0;
            
            System.out.println("forward rows of " + ySize + " - " + forwardRows);
            while(running)
            {
                // shows what the computer can currently see
                if(computersFirstMove)
                {
                    rowsLeft = ySize;
                    computersPosition.y = ySize;
                    computersPosition.x = xSize;
                    computersFirstMove = false;
                }
                else
                {
                    rowsLeft = computersPosition.y;
                }
                
                int y = Math.min(forwardRows,rowsLeft);
                Node p = findNextMove(getSubMatrix(y), y);
                System.out.println("before currPos: " + computersPosition.y + " rows left: " + rowsLeft);
                moveAutomatically(p.x, p.y);
                System.out.println("after currPos: " + computersPosition.y + " rows left: " + rowsLeft);
                
                // end reached
                if(computersPosition.y <= 0)
                {
                    running = false;
                    System.out.println("done!");
                }
            }
        }
    }

    private Node findNextMove(int[][] subMatrix, int height) 
    {
        Node[][] nodes = new Node[height][xSize];
        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < xSize; j++)
            {
                nodes[i][j] = new Node(i,j,subMatrix[i][j]);
            }
        }
        
        // loop through the nodes backwards
        for(int i = height - 1; i >= 0; i--)
        {
            for(int j = xSize - 1; j >= 0; j--)
            {   
                // bottom row
                if(i == height - 1)
                {
                    nodes[i][j].previousBestChoice = null;
                }
                else
                {
                    ArrayList<Node> selectableNodes = new ArrayList();
                    
                    // add the node directly below
                    selectableNodes.add(nodes[i+1][j]);
                    
                    // add the node below and to the right
                    if(j + 1 != xSize){
                        selectableNodes.add(nodes[i+1][j+1]);
                    }
                    
                    // add the node below and to the right
                    if(j != 0){
                        selectableNodes.add(nodes[i+1][j-1]);
                    }
                    Node node = null;
                    for(Node n : selectableNodes){
                        if(node == null){
                            node = n;
                        }
                        if(n.totalDifficulty < node.totalDifficulty){
                            node = n;
                        }
                    }
                    
                    nodes[i][j].previousBestChoice = node;
                    nodes[i][j].totalDifficulty += node.totalDifficulty;
                }
                
                System.out.println("current j: " + j);
            }
        }
        
        Node returnNode = null;
        for(int i = 0 ;i < xSize; i++)
        {
            if(returnNode == null)
            {
                returnNode = nodes[0][i];
            }
            
            if(nodes[0][i].totalDifficulty < returnNode.totalDifficulty)
            {
                returnNode = nodes[0][i];
            }
        }
        
        return returnNode;
    }
    
    private int[][] getSubMatrix(int height)
    {
        int[][] subMatrix = new int[height][xSize];
        int yPos = Math.abs(computersPosition.y - height);
        for(int i = 0 ; i < height; i++)
        {
            for(int j = 0 ; j < xSize; j++){
                subMatrix[i][j] = gridMatrix[yPos][j];
            }
            yPos++;
        }
        return subMatrix;
    }
    
    private class Node
    {
        int x, y, originalDifficulty, totalDifficulty;
        Node previousBestChoice;
        
        public Node(int x, int y, int od)
        {
            this.x = x;
            this.y = y;
            this.originalDifficulty = od;
            this.totalDifficulty = od;
        }
    }
}
