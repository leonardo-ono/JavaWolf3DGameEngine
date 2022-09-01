package wolf3d.infra;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Input class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Input implements KeyListener {
    
    private static final Set<Integer> KEYS_PRESSED = new HashSet<>();
    private static final Set<Integer> KEYS_PRESSED_CONSUMED = new HashSet<>();
    private static final List<KeyListener> LISTENERS = new ArrayList<>();
    
    public static void addListener(KeyListener listener) {
        Input.LISTENERS.add(listener);
    }

    public static synchronized boolean isKeyPressed(int keyCode) {
        return KEYS_PRESSED.contains(keyCode);
    }

    public static synchronized boolean isKeyJustPressed(int keyCode) {
        if (!KEYS_PRESSED_CONSUMED.contains(keyCode) 
                && KEYS_PRESSED.contains(keyCode)) {
            
            KEYS_PRESSED_CONSUMED.add(keyCode);
            return true;
        }
        return false;
    }
    
    @Override
    public synchronized void keyTyped(KeyEvent e) {
        if (LISTENERS != null) {
            LISTENERS.forEach(action -> action.keyTyped(e));
        }
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        if (!KEYS_PRESSED.contains(e.getKeyCode())) {
            KEYS_PRESSED.add(e.getKeyCode());
            if (LISTENERS != null) {
                LISTENERS.forEach(action -> action.keyPressed(e));
            }
        }
    }
    
    @Override
    public synchronized void keyReleased(KeyEvent e) {
        KEYS_PRESSED.remove(e.getKeyCode());
        KEYS_PRESSED_CONSUMED.remove(e.getKeyCode());
        if (LISTENERS != null) {
            LISTENERS.forEach(action -> action.keyReleased(e));
        }
    }
    
}
