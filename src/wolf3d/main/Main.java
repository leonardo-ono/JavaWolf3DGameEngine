package wolf3d.main;

import javax.swing.SwingUtilities;
import wolf3d.infra.Display;
import wolf3d.infra.GameCanvas;
import wolf3d.infra.Wolf3DGame;

/**
 * Main class.
 * 
 * Game entry point.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Main {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameCanvas gameCanvas = new GameCanvas(new Wolf3DGame());
            Display display = new Display(gameCanvas);
            display.setTitle("Java Wolfenstein 3D Engine v0.0.1  "
                            + "[F12 - Full screen][F11 - Keep aspect ratio]");
            
            // display.setIconImage(Resource.getImage("icon"));
            display.start();
        });
    }   
    
}
