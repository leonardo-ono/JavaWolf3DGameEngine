package wolf3d.infra;

import java.awt.event.KeyEvent;

/**
 * (Project) Settings class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Settings {
    
    // --- display ---
    
    public static final int CANVAS_WIDTH = 320;
    public static final int CANVAS_HEIGHT = 200;

    public static final int PREFERRED_SCREEN_WIDTH = (int) (320 * 2.5);
    public static final int PREFERRED_SCREEN_HEIGHT = (int) (240 * 2.5);
    
    public static final double ASPECT_RATIO 
                = PREFERRED_SCREEN_WIDTH / (double) PREFERRED_SCREEN_HEIGHT;
    
    public static boolean keepAspectRatio = true;

    public static final int KEY_KEEP_ASPECT_RATIO = KeyEvent.VK_F11;
    public static final int KEY_FULLSCREEN = KeyEvent.VK_F12;


    // --- game loop ---
    
    public static final long TIME_PER_UPDATE = 1000000000 / 60;

    
    // --- input (changeable) ---
    
    public static int KEY_START_1 = KeyEvent.VK_SPACE;
    public static int KEY_START_2 = KeyEvent.VK_ENTER;
    public static int KEY_CANCEL = KeyEvent.VK_ESCAPE;

    public static int KEY_PLAYER_UP = KeyEvent.VK_UP;
    public static int KEY_PLAYER_DOWN = KeyEvent.VK_DOWN;
    public static int KEY_PLAYER_LEFT = KeyEvent.VK_LEFT;
    public static int KEY_PLAYER_RIGHT = KeyEvent.VK_RIGHT;
    public static int KEY_PLAYER_FIRE = KeyEvent.VK_Z;
    public static int KEY_PLAYER_STRAFE = KeyEvent.VK_X;
    public static int KEY_PLAYER_DOOR = KeyEvent.VK_SPACE;
    
    public static int KEY_PLAYER_WEAPON_KNIFE = KeyEvent.VK_1;
    public static int KEY_PLAYER_WEAPON_PISTOL = KeyEvent.VK_2;
    public static int KEY_PLAYER_WEAPON_MACHINE = KeyEvent.VK_3;
    public static int KEY_PLAYER_WEAPON_GATLING = KeyEvent.VK_4;
    
    
    // --- audio ---
    
    public static final int PC_SPEAKER_SOUND_PCM_FREQ = 44100;
    public static final int DIGITIZED_SOUND_PCM_FREQ = 7000;
    public static final int IMF_MUSIC_PLAYBACK_RATE = 700;
    
}
