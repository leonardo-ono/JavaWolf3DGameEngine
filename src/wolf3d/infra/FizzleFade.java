package wolf3d.infra;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * FizzleFade class.
 * 
 * Effect when player dies.
 * 
 * Implemented using Linear Feedback Shift Register technique.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class FizzleFade {

    private static final BufferedImage BACKGROUND;
    private static final Graphics2D BACKGROUND_G2D;
    
    private static int pixelCount;
    private static int fadeDirection;
    private static Color fadeColor;
    
    static {
        BACKGROUND = new BufferedImage(320, 200, BufferedImage.TYPE_INT_ARGB);
        BACKGROUND_G2D = BACKGROUND.createGraphics();
        BACKGROUND_G2D.setBackground(new Color(0, 0, 0, 0));
    }

    private static int lfsr = 1;

    private static int nextLFSR() {
        int bn = (lfsr & 1) ^ ((lfsr & 8) >> 3);
        lfsr = (lfsr >> 1) + (bn << 16);
        return lfsr;
    }
    
    public static boolean isFinished() {
        return fadeDirection == 0 
                    || (fadeDirection > 0 && pixelCount >= 72000)
                        || (fadeDirection < 0 && pixelCount <= 0);
    }
    
    public static void fixedUpdate() {
        if (isFinished()) {
            return;
        }
        
        for (int i = 0; i < 1600; i++) {
            int rnd = nextLFSR();
            int x = rnd & 0x1ff;
            int y = (rnd >> 9) & 0xff;
            if (x < 320 && y < 200) {
                int color = fadeDirection > 0 ? fadeColor.getRGB() : 0x00000000;
                BACKGROUND.setRGB(x, y, color);
                pixelCount += fadeDirection;
            }
        }        
    }

    public static void draw(Graphics2D g) {
        if (fadeDirection < 0 && pixelCount <= 0) {
            return;
        }
        g.drawImage(BACKGROUND, 0, 0, null);
    }
    
    public static void fadeIn(Color fadeColor) {
        lfsr = 1;
        fadeDirection = 1;
        pixelCount = 0;
        FizzleFade.fadeColor = fadeColor;
    }

    public static void fadeOut() {
        lfsr = 1;
        fadeDirection = -1;
        pixelCount = 72000;
    }
    
    public static void reset() {
        BACKGROUND_G2D.clearRect(0, 0, 320, 200);
        pixelCount = 0;
        fadeDirection = -1;
    }
    
}
