package terrainnavigator;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

public class TerrainNavigatorModel 
{
    int[][] gridMatrix;
    Point currentPosition;
    int tally, size;
    boolean firstMove;
    ArrayList<Point> history;
    
    public TerrainNavigatorModel(int size)
    {
        gridMatrix = new int[size][size];
        Random rand = new Random();
        this.size = size;
        currentPosition = new Point(-1, -1);
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
