package wolf3d.scene;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import wolf3d.infra.Audio;
import wolf3d.infra.HUD;
import wolf3d.infra.Input;
import wolf3d.infra.Player;
import wolf3d.infra.Resource;
import wolf3d.infra.Scene;
import wolf3d.infra.SceneManager;
import static wolf3d.infra.Settings.*;
import wolf3d.infra.Util;
import wolf3d.infra.Wolf3DGame;

/**
 * LevelClearedStatistics class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class LevelClearedStatistics extends Scene {
    
    private long startTime;
    private final Color backgroundColor;
    private final BufferedImage[] playerPics = new BufferedImage[2];
    private final BufferedImage playerWinPic;
    private int selectionAnimationFrame;
    
    private int targetBonus;
    private int targetKill;
    private int targetSecret;
    private int targetTreasure;

    private int currentBonus;
    private int currentKill;
    private int currentSecret;
    private int currentTreasure;

    private boolean hasDif1;
    private boolean hasDif2;
    private boolean hasDif3;
    private boolean hasDif4;
    
    private int playTimeHour;
    private int playTimeMinutes;
    private int playTimeSeconds;
    
    private int state; // 0=wait, 1=bonus, 2=kill, 3=secret, 4=treasure
    private int nextState; // 0=wait, 1=bonus, 2=kill, 3=secret, 4=treasure
    private int stateFrame;
    
    public LevelClearedStatistics() {
        super("level_cleared_statistics");
        backgroundColor = Util.getColor("0x004040ff");
        playerPics[0] = Resource.getPic("STATISTICS_0");
        playerPics[1] = Resource.getPic("STATISTICS_1");
        playerWinPic = Resource.getPic("STATISTICS_WIN");
        createStatisticFont();
    }
    
    private final BufferedImage[] fontCharPics = new BufferedImage[256];
    
    private void createStatisticFont() {
        int startPicIndex 
                = Resource.getIntProperty("STATISTICS_FONT_START_PIC_INDEX");
        
        String fontChars = Resource.getProperty("STATISTICS_FONT_CHARS");
        for (int i = 0; i < fontChars.length(); i++) {
            int c = fontChars.charAt(i);
            fontCharPics[c] = Resource.getPic(startPicIndex + i);
        }
    }

    private void drawString(Graphics2D g, String text, int x, int y) {
        int dx = 0;
        for (int i = 0; i < text.length(); i++) {
            int c = text.charAt(i);
            BufferedImage charImage = fontCharPics[c];
            g.drawImage(charImage, x + dx, y, null);
            dx += 16;
        }
    }
    
    private void drawNumber(Graphics2D g
            , int number, int numberOfDigits, String preChar, int x, int y) {
        
        String numberStr = preChar.repeat(numberOfDigits) + number;
        numberStr = numberStr.substring(
                numberStr.length() - numberOfDigits, numberStr.length());

        drawString(g, numberStr, x, y);
    }
    
    @Override
    public void onEnter() {
        Audio.playMusic("ENDLEVEL");
        targetBonus = 0;
        targetKill = Wolf3DGame.getStatisticsKill();
        targetSecret = Wolf3DGame.getStatisticsSecret();
        targetTreasure = Wolf3DGame.getStatisticsTreasures();
        currentBonus = 0;
        currentKill = 0;
        currentSecret = 0;
        currentTreasure = 0;
        hasDif1 = (targetBonus - currentBonus) > 0;
        hasDif2 = (targetKill - currentKill) > 0;
        hasDif3 = (targetSecret - currentSecret) > 0;
        hasDif4 = (targetTreasure - currentTreasure) > 0;
        
        long playTime = Player.getPlayTimeMs();
        playTimeHour = Util.extractTimeMsHoursPart(playTime);
        playTimeMinutes = Util.extractTimeMsMinutesPart(playTime);
        playTimeSeconds = Util.extractTimeMsSecondsPart(playTime);
    }

    @Override
    public void onTransitionFinished() {
        state = 0;
        nextState = 1;
        stateFrame = 0;
        startTime = Util.getTimeMs() + 1000;
    }
    
    @Override
    public void fixedUpdate() {
        selectionAnimationFrame = (int) (System.nanoTime() * 0.0000000025) % 2;
        
        final int incSpeed = 5;
        stateFrame++;
        switch (state) {
            case 0 -> { // just wait a little
                if (Util.getTimeMs() >= startTime) {
                    state = nextState;
                }
            }
            case 1 -> { // update bonus
                if (!hasDif1) {
                    state = 2;
                    break;
                }
                currentBonus += incSpeed;
                if (currentBonus >= targetBonus) {
                    currentBonus = targetBonus;
                    Audio.playSound("ENDBONUS2");
                    startTime = Util.getTimeMs() + 1000;
                    state = 0;
                    nextState = 2;
                }
                else {
                    Audio.playSound("ENDBONUS1");
                }
            }
            case 2 -> { // update kill
                if (!hasDif2) {
                    state = 3;
                    break;
                }
                currentKill += incSpeed;
                if (currentKill >= targetKill) {
                    currentKill = targetKill;
                    Audio.playSound("ENDBONUS2");
                    startTime = Util.getTimeMs() + 1000;
                    state = 0;
                    nextState = 3;
                }
                else {
                    Audio.playSound("ENDBONUS1");
                }
            }
            case 3 -> { // update secret
                if (!hasDif3) {
                    state = 4;
                    break;
                }
                currentSecret += incSpeed;
                if (currentSecret >= targetSecret) {
                    currentSecret = targetSecret;
                    Audio.playSound("ENDBONUS2");
                    startTime = Util.getTimeMs() + 1000;
                    state = 0;
                    nextState = 4;
                }
                else {
                    Audio.playSound("ENDBONUS1");
                }
            }
            case 4 -> { // update treasure
                if (!hasDif4) {
                    startTime = Util.getTimeMs() + 1000;
                    state = 5;
                    break;
                }
                currentTreasure += incSpeed;
                if (currentTreasure >= targetTreasure) {
                    currentTreasure = targetTreasure;
                    Audio.playSound("ENDBONUS2");
                    startTime = Util.getTimeMs() + 1000;
                    state = 0;
                    nextState = 5;
                }
                else {
                    Audio.playSound("ENDBONUS1");
                }
            }
            case 5 -> {
                if (Input.isKeyJustPressed(KEY_START_1)
                        || Input.isKeyJustPressed(KEY_START_2)) {

                    if (Player.isPlayerTriggeredEndGame()) {
                        // TODO goto hiscore
                        SceneManager.switchTo("title");
                    }
                    else {
                        SceneManager.switchTo("stage");
                    }
                }
            }
        }

        //HUD.fixedUpdate();
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        g.drawString("GAME CLEARED STATISTICS", 10, 50);
        
        if (Player.isPlayerTriggeredEndGame()) {
            drawString(g, "YOU WIN!", 146, 16);
            g.drawImage(playerWinPic, 16, 16, null);
        }
        else {
            drawString(g, "FLOOR " + Wolf3DGame.getFloor(), 114, 16);
            drawString(g, "COMPLETED", 114, 32);
            
            drawString(g, "BONUS", 114, 56);
            drawNumber(g, 0, 6, " ", 210, 56);

            g.drawImage(playerPics[selectionAnimationFrame], 0, 16, null);
        }
        
        drawString(g, "TIME", 114, 80);
        drawNumber(g, playTimeHour, 2, "0", 191 , 80);
        drawString(g, ":", 220 , 80);
        drawNumber(g, playTimeMinutes, 2, "0", 229, 80);
        drawString(g, ":", 261, 80);
        drawNumber(g, playTimeSeconds, 2, "0", 270, 80);

        // TODO par time
        // ref.: https://wolfenstein.fandom.com/wiki/Par
        drawString(g, "PAR", 130, 96);
        drawNumber(g, 0, 2, "0", 191 , 96);
        drawString(g, ":", 220 , 96);
        drawNumber(g, 1, 2, "0", 229, 96);
        drawString(g, ":", 261, 96);
        drawNumber(g, 30, 2, "0", 270, 96);
        
        drawString(g, "    KILL RATIO    %", 8, 112);
        drawString(g, "  SECRET RATIO    %", 8, 128);
        drawString(g, "TREASURE RATIO    %", 8, 144);

        drawNumber(g, currentKill, 3, " ", 8 + 15 * 16, 112);
        drawNumber(g, currentSecret, 3, " ", 8 + 15 * 16, 128);
        drawNumber(g, currentTreasure, 3, " ", 8 + 15 * 16, 144);
        
        HUD.draw(g);
    }
    
}
