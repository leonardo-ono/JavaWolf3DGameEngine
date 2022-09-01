package wolf3d.infra;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * HUD class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class HUD {
    
    private static final int FACE_START_PIC_INDEX;
    private static final BufferedImage FOOTER_PIC;
    private static final BufferedImage[] DIGITS = new BufferedImage[10];
    
    private static final BufferedImage KEY_EMPTY_PIC;
    private static final BufferedImage KEY_GOLD_PIC;
    private static final BufferedImage KEY_SILVER_PIC;
    static {
        FACE_START_PIC_INDEX = Resource.getIntProperty("PIC_HUD_FACE");
        FOOTER_PIC = Resource.getPic("HUD_FOOTER");
        KEY_EMPTY_PIC = Resource.getPic("HUD_KEY_EMPTY");
        KEY_GOLD_PIC = Resource.getPic("HUD_KEY_GOLD");
        KEY_SILVER_PIC = Resource.getPic("HUD_KEY_SILVER");
        
        // digits
        int digitStartIndex = Resource.getIntProperty("PIC_HUD_DIGITS");
        for (int i = 0; i < 10; i++) {
            DIGITS[i] = Resource.getPic(digitStartIndex + i);
        }
    }

    private static int faceAnimationIndex = 0;
    private static long faceNextFrameTime = 0;
    
    public static void fixedUpdate() {
        if (Util.getTimeMs() >= faceNextFrameTime) {
            faceNextFrameTime = Util.getTimeMs() + Util.random(300, 500);
            faceAnimationIndex = Util.random(0, 2);
        }
    }

    public static void draw(Graphics2D g) {
        g.drawImage(FOOTER_PIC, 0, 160, null);
        drawNumber(g, Wolf3DGame.getFloor(), 2, 16, 176); // floor
        drawNumber(g, Wolf3DGame.getScore(), 6, 48, 176); // score
        drawNumber(g, Wolf3DGame.getLives(), 1, 112, 176); // lives
        drawNumber(g, Wolf3DGame.getLifeEnergy(), 3, 168, 176); // life energy
        drawNumber(g, Weapons.getAmmo(), 2, 216, 176); // ammo
        
        drawKeys(g);
        drawFunnyFace(g);
                
        // draw current selected weapon
        if (Weapons.getCurrentPlayerWeapon() != null) {
            BufferedImage weaponpic 
                    = Weapons.getCurrentPlayerWeapon().getHudPic();
            
            g.drawImage(weaponpic, 256, 168, null);
        }
    }
    
    private static void drawKeys(Graphics2D g) {
        // draw gold key
        if (Player.isPlayerHasGoldKey()) {
            g.drawImage(KEY_GOLD_PIC, 240, 164, null);
        }
        else {
            g.drawImage(KEY_EMPTY_PIC, 240, 164, null);
        }
        // draw silver key
        if (Player.isPlayerHasSilverKey()) {
            g.drawImage(KEY_SILVER_PIC, 240, 180, null);
        }
        else {
            g.drawImage(KEY_EMPTY_PIC, 240, 180, null);
        }
    }
    
    private static void drawFunnyFace(Graphics2D g) {
        int faceIndex = 0;
        if (Wolf3DGame.getLifeEnergy() <= 0) faceIndex = 7; 
        else if (Wolf3DGame.getLifeEnergy() <= 10) faceIndex = 6; 
        else if (Wolf3DGame.getLifeEnergy() <= 20) faceIndex = 5; 
        else if (Wolf3DGame.getLifeEnergy() <= 30) faceIndex = 4; 
        else if (Wolf3DGame.getLifeEnergy() <= 40) faceIndex = 3; 
        else if (Wolf3DGame.getLifeEnergy() <= 50) faceIndex = 2; 
        else if (Wolf3DGame.getLifeEnergy() <= 60) faceIndex = 1; 
        else if (Wolf3DGame.getLifeEnergy() <= 70) faceIndex = 0; 
        
        BufferedImage facePic = Resource.getPic(
                FACE_START_PIC_INDEX + faceIndex * 3 + faceAnimationIndex);
        
        if (Wolf3DGame.getLifeEnergy() == 0) {
            facePic = Resource.getPic(FACE_START_PIC_INDEX + faceIndex * 3);
        }
        
        g.drawImage(facePic, 136, 164, null);
    }
    
    public static void drawNumber(
            Graphics2D g, int number, int numberOfDigits, int x, int y) {
        
        String numberStr = " ".repeat(numberOfDigits) + number;
        numberStr = numberStr.substring(
                numberStr.length() - numberOfDigits, numberStr.length());
        
        for (int i = 0; i < numberStr.length(); i++) {
            char c = numberStr.charAt(i);
            if (c != ' ') {
                int d = c - '0';
                g.drawImage(DIGITS[d], x, y, null);
            }
            x += DIGITS[0].getWidth();
        }
    }
    
}
