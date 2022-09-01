package wolf3d.infra;

import java.awt.Graphics2D;

/**
 * State class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public abstract class Scene {

    protected final String name;

    public Scene(String name) {
        this.name = name;
    }
    
    public void start() {
        // implement your code here
    }
    
    public String getName() {
        return name;
    }

    public void onEnter() {
        // implement your code here
    }

    public void onTransitionFinished() {
        // implement your code here
    }

    public void onExit() {
        // implement your code here
    }

    public void update(double delta) {
        // implement your code here
    }

    public void fixedUpdate() {
        // implement your code here
    }
    
    public void draw(Graphics2D g) {
        // implement your code here
    }

}
