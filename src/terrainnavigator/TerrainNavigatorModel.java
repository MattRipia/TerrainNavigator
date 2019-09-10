package terrainnavigator;

import java.awt.Point;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

public class TerrainNavigatorModel 
{
    public int[][] gridMatrix;
    public Point currentPosition;
    public int tally, size;
    public boolean firstMove;
    public ArrayList<Point> history;
    public Database db;
    
    public TerrainNavigatorModel() throws SQLException{
        db = new Database();
        gridMatrix = new int[size][size];
        tally = 0;
        firstMove = true;
        this.history = new ArrayList();
        currentPosition = new Point(-1, -1);
    }
    
    public TerrainNavigatorModel(int size)
    {
        Random rand = new Random();
        gridMatrix = new int[size][size];
        currentPosition = new Point(-1, -1);
        this.size = size;
        tally = 0;
        firstMove = true;
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
