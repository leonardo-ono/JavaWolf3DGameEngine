package wolf3d.scene;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import wolf3d.asset.loader.VGAGRAPHLoader.VGAGRAPHFont;
import wolf3d.infra.Audio;
import wolf3d.infra.Input;
import wolf3d.infra.Resource;
import wolf3d.infra.Scene;
import wolf3d.infra.SceneManager;
import static wolf3d.infra.Settings.*;
import wolf3d.infra.Util;
import wolf3d.infra.Wolf3DGame;
import wolf3d.infra.Wolf3DGame.Difficulty;

/**
 * GameDifficulty class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class GameDifficulty extends Scene {
    
    private final VGAGRAPHFont font;
    private final VGAGRAPHFont fontSelection;
    private final VGAGRAPHFont fontYellow;
    
    private final BufferedImage[] selectionPics = new BufferedImage[2];
    private final BufferedImage footerPic;
    private final BufferedImage[] facePics = new BufferedImage[4];
    private final Color backgroundColor;
    private final Color backgroundColor2;

    private final String[] options = { 
        "Can I play, Daddy?", 
        "Don't hurt me.", 
        "Bring'em on!",
        "I am Death incarnate!"};
    
    private int selectedOptionIndex;
    private int selectionAnimationFrame;
    
    public GameDifficulty() {
        super("game_difficulty");
        
        font = Resource.getFont("BIG_GRAY");
        fontSelection = Resource.getFont("BIG_LIGHT_GRAY");
        fontYellow = Resource.getFont("BIG_YELLOW");
        
        selectionPics[0] = Resource.getPic("OPTIONS_SELECTION_0");
        selectionPics[1] = Resource.getPic("OPTIONS_SELECTION_1");
        footerPic = Resource.getPic("OPTIONS_FOOTER");
        
        int facesStartPicIndex = Resource.getIntProperty(
                                        "DIFFICULTY_FACES_START_PIC_INDEX");
        
        for (int i = 0; i < 4; i++) {
            facePics[i] = Resource.getPic(facesStartPicIndex + i);
        }
        
        backgroundColor = Util.getColor("0x8a0000ff");
        backgroundColor2 = Util.getColor("0x590000ff");
    }
    
    @Override
    public void onEnter() {
        if (!"WONDERIN".equals(Audio.getCurrentMusicId())) {
            Audio.playMusic("WONDERIN");
        }
        selectedOptionIndex = 2;
    }

    @Override
    public void onTransitionFinished() {
    }
    
    @Override
    public void onExit() {
    }
    
    @Override
    public void fixedUpdate() {
        selectionAnimationFrame = (int) (System.nanoTime() * 0.0000000025) % 2;
        
        if (Input.isKeyJustPressed(KEY_PLAYER_UP)) {
            selectedOptionIndex--;
            if (selectedOptionIndex < 0) {
                selectedOptionIndex = options.length - 1;
            }
            Audio.playSound("WALK1");
            //Audio.playSound("MOVEGUN2");
        }
        else if (Input.isKeyJustPressed(KEY_PLAYER_DOWN)) {
            selectedOptionIndex++;
            if (selectedOptionIndex > options.length - 1) {
                selectedOptionIndex = 0;
            }
            Audio.playSound("WALK2");
            //Audio.playSound("MOVEGUN2");
        }
        
        if (Input.isKeyJustPressed(KEY_CANCEL)) {
            SceneManager.switchTo("game_options", false);
            Audio.playSound("ESCPRESSED");
        }
        else if (Input.isKeyJustPressed(KEY_START_1)
                || Input.isKeyJustPressed(KEY_START_2)) {
            
            Wolf3DGame.setDifficulty(Difficulty.values()[selectedOptionIndex]);
            Wolf3DGame.newGame();
            Audio.playSound("MISSILEFIRE");
        }
    }
    
    public void quit() {
        SceneManager.switchTo("quit", false);
    }
    
    @Override
    public void draw(Graphics2D g) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        g.drawImage(footerPic, 112, 184, null);
        
        fontYellow.drawString(g, "How tough are you?", 69, 68);
        
        g.setColor(backgroundColor2);
        g.fillRect(46, 91, 224, 66);
        g.draw3DRect(46, 91, 224, 66, false);
        
        int optionDy = 0;
        for (int i = 0; i < options.length; i++) {
            String option = options[i];
            if (i == selectedOptionIndex) {
                fontSelection.drawString(g, option, 73, 100 + optionDy);
            }
            else {
                font.drawString(g, option, 73, 100 + optionDy);
            }
            if (i == selectedOptionIndex) {
                fontSelection.drawString(g, option, 73, 100 + optionDy);
                g.drawImage(selectionPics[selectionAnimationFrame]
                                                    , 47, 100 + optionDy, null);
            }
            optionDy += 13;
        }
        g.drawImage(facePics[selectedOptionIndex], 232, 107, null);
    }

}
