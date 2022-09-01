package wolf3d.scene;

import wolf3d.infra.Scene;

/**
 * Quit class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Quit extends Scene {
    
    public Quit() {
        super("quit");
    }
    
    @Override
    public void onEnter() {
        System.exit(0);
    }
    
}
