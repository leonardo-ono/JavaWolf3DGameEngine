package wolf3d.scene;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import wolf3d.infra.Input;
import wolf3d.infra.Resource;
import wolf3d.infra.Scene;
import wolf3d.infra.SceneManager;
import static wolf3d.infra.Settings.*;
import wolf3d.infra.Util;

/**
 * ProfoundCarnage class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Credits extends Scene {
    
    private BufferedImage creditsPic;
    private long nextSceneTime;
    
    public Credits() {
        super("credits");
    }
    
    @Override
    public void onEnter() {
        creditsPic = Resource.getPic("CREDITS");
    }

    @Override
    public void onTransitionFinished() {
        nextSceneTime = Util.getTimeMs() + 10000;
    }
    
    @Override
    public void onExit() {
    }
    
    @Override
    public void fixedUpdate() {
        if (Input.isKeyJustPressed(KEY_START_1)
                || Input.isKeyJustPressed(KEY_START_2)
                    || Util.getTimeMs() >= nextSceneTime) {
            
            SceneManager.switchTo("game_options", false);
        }
    }
    
    @Override
    public void draw(Graphics2D g) {
        g.drawImage(creditsPic, 0, 0, null);
    }

}
