package wolf3d.infra;

import static wolf3d.infra.Settings.*;
import java.awt.Graphics2D;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

/**
 * GameCanvas class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class GameCanvas extends Canvas {
    
    private final Dimension preferredSize 
            = new Dimension(PREFERRED_SCREEN_WIDTH, PREFERRED_SCREEN_HEIGHT);

    private BufferedImage offscreen;
    private Graphics2D offscreenG2D;
    private final Wolf3DGame wolf3DGame;
    private BufferStrategy bs;
    private boolean running;
    private Thread gameLoopThread;
    private final Rectangle sizeWithAspectRatio = new Rectangle();
    
    public GameCanvas(Wolf3DGame wolf3DGame) {
        this.wolf3DGame = wolf3DGame;
    }

    @Override
    public Dimension getPreferredSize() {
        return preferredSize;
    }

    public void start() {
        updateAspectRatioDimension();
        createBufferStrategy(2);
        bs = getBufferStrategy();
        offscreen = new BufferedImage(
                CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        
        offscreenG2D = offscreen.createGraphics();
        wolf3DGame.start();
        running = true;
        gameLoopThread = new Thread(new MainLoop());
        gameLoopThread.start();
        addKeyListener(new Input());
        addComponentListener(new ResizeListener());
    }
    
    private class ResizeListener extends ComponentAdapter {

        @Override
        public void componentResized(ComponentEvent e) {
            updateAspectRatioDimension();
        }
        
    }

    public void updateAspectRatioDimension() {
        int left = 0;
        int width = getWidth();
        int height = (int) (width / ASPECT_RATIO);
        int top = (int) ((getHeight() - height) / 2);
        if (top < 0) {
            top = 0;
            height = getHeight();
            width = (int) (height * ASPECT_RATIO);
            left = (int) ((getWidth() - width) / 2);
        }
        sizeWithAspectRatio.setBounds(left, top, width, height);
    }
    
    private class MainLoop implements Runnable {

        @Override
        public void run() {
            long currentTime = System.nanoTime();
            long lastTime = currentTime;
            long delta;
            long unprocessedTime = 0;
            while (running) {
                currentTime = System.nanoTime();
                delta = currentTime - lastTime;
                unprocessedTime += delta;
                lastTime = currentTime;
                while (unprocessedTime >= TIME_PER_UPDATE) {
                    unprocessedTime -= TIME_PER_UPDATE;
                    wolf3DGame.fixedUpdate();
                }
                wolf3DGame.update(delta * 0.000000001);
                
                Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION
                        , RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        
                wolf3DGame.draw(offscreenG2D);

                if (keepAspectRatio) {
                    g.clearRect(0, 0, getWidth(), getHeight());
                    g.drawImage(offscreen
                        , sizeWithAspectRatio.x, sizeWithAspectRatio.y
                        , sizeWithAspectRatio.width, sizeWithAspectRatio.height
                        , null);
                }
                else {
                    g.drawImage(offscreen, 0, 0, getWidth(), getHeight(), null);
                }

                g.dispose();
                bs.show();
                
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                }
            }
        }
        
    }
    
}
