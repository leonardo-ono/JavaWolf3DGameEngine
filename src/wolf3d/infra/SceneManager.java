package wolf3d.infra;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import static wolf3d.infra.Settings.*;

/**
 * SceneManager class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class SceneManager {
    
    private static final Map<String, Scene> scenes = new HashMap<>();
    private static Scene currentScene;
    private static Scene nextScene;

    private static final int FADE_SIZE = 40;
    private static final Color[] ALPHAS = new Color[FADE_SIZE + 1];
    
    private static boolean fadeMusic;
    private static int fadeValue;
    private static int fadeStatus;
    private static int waitBetweenFade = 60;
    
    static {
        cacheAllAlphas();
    }

    private SceneManager() {
    }
    
    private static void cacheAllAlphas() {
        for (int i = 0; i < FADE_SIZE + 1; i++) {
            int alpha = (int) (255 * (1.0 - (i / (double) FADE_SIZE)));
            ALPHAS[i] = Util.getColor(0, 0, 0, alpha);
        }        
    }
    
    public static Scene getCurrentState() {
        return currentScene;
    }
    
    public static void addState(Scene state) {
        scenes.put(state.getName(), state);
    }
    
    public static void removeState(String stateName) {
        scenes.remove(stateName);
    }

    public static Scene getState(String stateId) {
        return scenes.get(stateId);
    }

    public static void startAll() {
        scenes.keySet().forEach(key -> scenes.get(key).start());
    }
    
    public static boolean isStateAvailable(String stateName) {
        return scenes.containsKey(stateName);
    }
    
    public static void switchTo(String nextSceneId
                            , boolean fadeMusic, int waitBetweenFade) {
        nextScene = scenes.get(nextSceneId);
        fadeStatus = 1;
        fadeValue = FADE_SIZE;
        SceneManager.waitBetweenFade = waitBetweenFade;
        SceneManager.fadeMusic = fadeMusic;
    }
    
    public static void switchTo(String stateName) { 
        switchTo(stateName, true, waitBetweenFade);
    }

    public static void switchTo(String stateName, boolean fadeMusic) { 
        switchTo(stateName, fadeMusic, waitBetweenFade);
    }
    
    public static void update(double delta) {
        if (currentScene != null) {
            currentScene.update(delta);
        }
    }
    
    public static void fixedUpdate() {
        if (fadeStatus == 1) {
            fadeValue--;
            
            if (fadeMusic) {
                double volumeScale = fadeValue / (double) FADE_SIZE;
                Audio.setMusicScaleVolume(volumeScale);
            }
            
            if (fadeValue < 0) {
                fadeValue = 0;
                fadeStatus = 3;
                if (currentScene != null) {
                    currentScene.onExit();
                }
                currentScene = nextScene;
                currentScene.onEnter();
                nextScene = null;
                
                if (fadeMusic) Audio.setMusicScaleVolume(1.0);
            }
        }
        else if (fadeStatus >= 3) {
            fadeStatus++;
            if (fadeStatus > waitBetweenFade) {
                fadeStatus = 2;
            }
        }
        else if (fadeStatus == 2) {
            fadeValue++;
            if (fadeValue > FADE_SIZE) {
                fadeValue = FADE_SIZE;
                fadeStatus = 0;
                currentScene.onTransitionFinished();
            }
        }
        else {
            if (currentScene != null) {
                currentScene.fixedUpdate();
            }
        }
    }

    public static void draw(Graphics2D g) {
        if (currentScene != null) {
            currentScene.draw(g);
            if (fadeStatus != 0) {
                g.setColor(ALPHAS[fadeValue]);
                g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
            }
        }
    }

}
