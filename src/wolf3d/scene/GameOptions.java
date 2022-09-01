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

/**
 * ProfoundCarnage class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class GameOptions extends Scene {
    
    private final VGAGRAPHFont font;
    private final VGAGRAPHFont fontSelection;
    private final VGAGRAPHFont fontDisabled;
    
    private final BufferedImage optionsPic;
    private final BufferedImage[] selectionPics = new BufferedImage[2];
    private final BufferedImage footerPic;
    private final Color backgroundColor;
    private final Color backgroundColor2;

    private final String[] options = { "New Game", "Back to Game", "Quit" };
    private int selectedOptionIndex;
    private int selectionAnimationFrame;
    
    public GameOptions() {
        super("game_options");
        
        font = Resource.getFont("BIG_GRAY");
        fontSelection = Resource.getFont("BIG_LIGHT_GRAY");
        fontDisabled = Resource.getFont("BIG_DARK_RED");
        
        optionsPic = Resource.getPic("OPTIONS");
        selectionPics[0] = Resource.getPic("OPTIONS_SELECTION_0");
        selectionPics[1] = Resource.getPic("OPTIONS_SELECTION_1");
        footerPic = Resource.getPic("OPTIONS_FOOTER");
        backgroundColor = Util.getColor("0x8a0000ff");
        backgroundColor2 = Util.getColor("0x590000ff");
    }
    
    @Override
    public void onEnter() {
        if (!"WONDERIN".equals(Audio.getCurrentMusicId())) {
            Audio.playMusic("WONDERIN");
        }
        selectedOptionIndex = Wolf3DGame.isPlaying() ? 1 : 0;
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
            if (selectedOptionIndex == 1 && !Wolf3DGame.isPlaying()) {
                selectedOptionIndex--;
            }
            if (selectedOptionIndex < 0) {
                selectedOptionIndex = options.length - 1;
            }
            Audio.playSound("WALK1");
            //Audio.playSound("MOVEGUN2");
        }
        else if (Input.isKeyJustPressed(KEY_PLAYER_DOWN)) {
            selectedOptionIndex++;
            if (selectedOptionIndex == 1 && !Wolf3DGame.isPlaying()) {
                selectedOptionIndex++;
            }
            if (selectedOptionIndex > options.length - 1) {
                selectedOptionIndex = 0;
            }
            Audio.playSound("WALK2");
            //Audio.playSound("MOVEGUN2");
        }
        
        if (Input.isKeyJustPressed(KEY_CANCEL)) {
            SceneManager.switchTo("title");
            Audio.playSound("ESCPRESSED");
        }
        else if (Input.isKeyJustPressed(KEY_START_1)
                || Input.isKeyJustPressed(KEY_START_2)) {
            
            switch (selectedOptionIndex) {
                case 0 -> newGame();
                case 1 -> Wolf3DGame.backToGame();
                case 2 -> quit();
            }
            Audio.playSound("MISSILEFIRE");
        }
    }

    private void newGame() {
        SceneManager.switchTo("game_difficulty", false);
    }
    
    private void quit() {
        SceneManager.switchTo("quit", false);
    }
    
    @Override
    public void draw(Graphics2D g) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 10, CANVAS_WIDTH, 24);
        g.setColor(backgroundColor2);
        g.drawLine(0, 32, CANVAS_WIDTH, 32);
        
        g.drawImage(optionsPic, 80, 0, null);
        g.drawImage(footerPic, 112, 184, null);
        
        g.fillRect(69, 53, 177, 135);
        g.draw3DRect(69, 53, 177, 135, false);
        
        int optionDy = 0;
        for (int i = 0; i < options.length; i++) {
            String option = options[i];
            if (i == 1 && !Wolf3DGame.isPlaying()) {
                fontDisabled.drawString(g, option, 100, 57 + optionDy);
            }
            else if (i == selectedOptionIndex) {
                fontSelection.drawString(g, option, 100, 57 + optionDy);
            }
            else {
                font.drawString(g, option, 100, 57 + optionDy);
            }
            if (i == selectedOptionIndex) {
                fontSelection.drawString(g, option, 100, 57 + optionDy);
                g.drawImage(selectionPics[selectionAnimationFrame]
                                                    , 72, 55 + optionDy, null);
            }
            optionDy += 13;
        }
        
    }

}
