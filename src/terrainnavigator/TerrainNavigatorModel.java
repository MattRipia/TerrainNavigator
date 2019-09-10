package terrainnavigator;

import java.awt.Point;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

public class TerrainNavigatorModel 
{
    public int[][] gridMatrix;
    public Point currentPosition;
    public int tally, xSize, ySize;
    public boolean firstMove;
    public ArrayList<Point> history;
    public Database db;
    
    public TerrainNavigatorModel(String terrain) throws SQLException
    {
        int i = 0;
        int maxX = 0;
        int maxY = 0;
        this.tally = 0;
        this.firstMove = true;
        this.history = new ArrayList();
        this.currentPosition = new Point(-1, -1);
        this.db = new Database();
        
        // gets the size of the matrix
        ResultSet rs = db.queryDB("select * from " + terrain);
        while(rs.next())
        {
            // 1-x 2-y 3-diff
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
        gridMatrix = new int[xSize][ySize];

        // reset the cursor
        rs.first();
        while(rs.next())
        {
            // fill out the grid
            gridMatrix[rs.getInt(1)][rs.getInt(2)] = rs.getInt(3);
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
        this.history = new ArrayList();
        
        for(int i = 0; i < size; i++)
        {
            for(int j = 0; j < size; j++)
            {
                int num = rand.nextInt(21) - 5;
                gridMatrix[i][j] = num;
                System.out.println("num: " + num);
            }
        }
    }

    public void move(int x, int y) 
    {
        currentPosition.x = x;
        currentPosition.y = y; 
        tally += gridMatrix[y][x];
        history.add(new Point(x, y));
        System.out.println("valid move - x" +x+ " y" +y+ " Tally: " + tally);
    }
}
