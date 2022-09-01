package wolf3d.scene;

import java.awt.Color;
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
 * ProfoundCarnage class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class ProfoundCarnage13 extends Scene {
    
    private final Color backgroundColor;
    private BufferedImage pc13Image;
    
    public ProfoundCarnage13() {
        super("profound_carnage_13");
        backgroundColor = Util.getColor("0x20a8fcff");
    }
    
    @Override
    public void onEnter() {
        pc13Image = Resource.getPic("PC_13");
        Audio.playMusic("NAZI_NOR");
    }

    @Override
    public void onExit() {
    }
    
    @Override
    public void fixedUpdate() {
        if (Input.isKeyJustPressed(KEY_START_1)
                || Input.isKeyJustPressed(KEY_START_2)) {
            
            SceneManager.switchTo("title", false);
        }
    }
    
    @Override
    public void draw(Graphics2D g) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        g.drawImage(pc13Image, 216, 110, null);
    }

}
