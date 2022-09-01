package wolf3d.infra;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Util class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Util {
    
    private static final Random RANDOM = new Random(System.nanoTime());
    
    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public static int random(int n) {
        return RANDOM.nextInt(n);
    }

    public static int random(int a, int b) {
        return a + RANDOM.nextInt(b - a + 1);
    }
    
    public static long getTimeMs() {
        return System.currentTimeMillis();
    }

    public static long getTimeNano() {
        return System.nanoTime();
    }
    
    private static final Map<Integer, Color> colorsCache = new HashMap<>();
    
    public static Color getColor(int r, int g, int b, int a) {
        int colorKey = b + (g << 8) + (r << 16) + (a << 24);
        Color color = colorsCache.get(colorKey);
        if (color == null) {
            color = new Color(r, g, b, a);
            colorsCache.put(colorKey, color);
        }
        return color;
    }

    // example: red color = 0xff0000
    public static Color getColor(String encodedColor) {
        Color color = null;
        if (encodedColor.startsWith("0x") && encodedColor.length() == 10) {
            int r = Integer.parseUnsignedInt(
                        encodedColor.substring(2, 4), 16);

            int g = Integer.parseUnsignedInt(
                        encodedColor.substring(4, 6), 16);

            int b = Integer.parseUnsignedInt(
                        encodedColor.substring(6, 8), 16);

            int a = Integer.parseUnsignedInt(
                        encodedColor.substring(8, 10), 16);

            color = new Color(r, g, b, a);
        }
        else {
            color = Color.decode(encodedColor);
        }
        return color;
    }

    public static int extractTimeMsSecondsPart(long timeMs) {
        int totalSeconds = (int) (timeMs * 0.001);
        int secondsPart = totalSeconds % 60;
        return secondsPart;
    }

    public static int extractTimeMsMinutesPart(long timeMs) {
        int totalSeconds = (int) (timeMs * 0.001);
        int totalMinutes = totalSeconds / 60;
        int minutesPart = totalMinutes % 60;
        return minutesPart;
    }
    
    public static int extractTimeMsHoursPart(long timeMs) {
        int totalSeconds = (int) (timeMs * 0.001);
        int totalMinutes = totalSeconds / 60;
        int totalHours = totalMinutes / 60;
        return totalHours % 100; // maximum of 2 digits
    }
    
}

