package terrainnavigator;

import java.awt.Point;
import java.util.Random;

public class TerrainNavigatorModel 
{
    int[][] gridMatrix;
    Point currentPosition;
    int tally, size;
    boolean firstMove;
    
    public TerrainNavigatorModel(int size)
    {
        gridMatrix = new int[size][size];
        Random rand = new Random();
        this.size = size;
        currentPosition = new Point(-1, -1);
        tally = 0;
        firstMove = true;
        
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
        boolean valid = false;
        
        if(firstMove)
        {
            // if last row is clicked
            if(y == size - 1)
            {
                // and if x is valid
                if(x >= 0 || x < size)
                {
                    valid = true;
                    firstMove = false;
                }
            }
        }
        else
        {
            if(currentPosition.y == y + 1)
            {
                if(currentPosition.x == x || currentPosition.x == x -1 || currentPosition.x == x+1)
                {
                    valid = true;
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
        
        if(valid)
        {
            currentPosition.x = x;
            currentPosition.y = y; 
            tally += gridMatrix[currentPosition.x][currentPosition.y];
            System.out.println("valid move!");
        }
        else
        {
            System.out.println("invlaid move!");
        }
    }
}
