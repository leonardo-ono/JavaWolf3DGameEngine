package wolf3d.scene;

import java.awt.Point;
import wolf3d.infra.Scene;
import wolf3d.infra.SceneManager;
import wolf3d.infra.Util;
import wolf3d.infra.Wolf3DGame;

/**
 * Initializing class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Initializing extends Scene {
    
    private long startTime;
    
    public Initializing() {
        super("initializing");
    }
    
    @Override
    public void onEnter() {
        startTime = Util.getTimeMs() + 500;
    }
    
    @Override
    public void fixedUpdate() {
        if (Util.getTimeMs() >= startTime) {
            SceneManager.switchTo("ol_presents");
            
            // debug: start game with specific level floor and player location
            //Wolf3DGame.newGame(9, new Point(34, 17));
            //Wolf3DGame.newGame(1, new Point(29, 57));
        }
    }

}
