package terrainnavigator;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;

public class TerrainNavigatorMain {
    public static void main(String[] args) 
    {
         // the main jframe that the chatGUI panel connects to
        JFrame frame = new JFrame("Terrain Navigator - Matt Ripia - 1385931");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new TerrainNavigatorGUI());
        frame.pack();
        
        // position the frame in the middle of the screen
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenDimension = tk.getScreenSize();
        Dimension frameDimension = frame.getSize();
        frame.setLocation((screenDimension.width-frameDimension.width)/2, (screenDimension.height-frameDimension.height)/2);
	frame.setVisible(true);
        frame.setResizable(false);
    }
}
