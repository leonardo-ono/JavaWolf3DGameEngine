package wolf3d.scene;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import wolf3d.infra.Audio;
import wolf3d.infra.Input;
import wolf3d.infra.Resource;
import wolf3d.infra.Scene;
import wolf3d.infra.SceneManager;
import static wolf3d.infra.Settings.*;
import wolf3d.infra.Util;

/**
 * Title class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Title extends Scene {
    
    private BufferedImage titleImage;
    private long nextSceneTime;
    
    public Title() {
        super("title");
    }
    
    private void reset() {
    }
    
    @Override
    public void onEnter() {
        reset();
        titleImage = Resource.getPic("TITLE");
        if (!"NAZI_NOR".equals(Audio.getCurrentMusicId())) {
            Audio.playMusic("NAZI_NOR");
        }
    }

    @Override
    public void onTransitionFinished() {
        nextSceneTime = Util.getTimeMs() + 15000;
    }
    
    @Override
    public void onExit() {
        reset();
        System.gc();
    }
    
    @Override
    public void fixedUpdate() {
        if (Input.isKeyJustPressed(KEY_START_1)
                || Input.isKeyJustPressed(KEY_START_2)) {
            
            SceneManager.switchTo("game_options");
        }
        else if (Util.getTimeMs() >= nextSceneTime) {
            SceneManager.switchTo("credits", false);
        }
    }
    
    @Override
    public void draw(Graphics2D g) {
        g.drawImage(titleImage, 0, 0, null);
    }

}
