package wolf3d.scene;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import wolf3d.infra.Scene;
import wolf3d.infra.SceneManager;
import static wolf3d.infra.Settings.*;
import wolf3d.infra.Util;

/**
 * OLPresents class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class OLPresents extends Scene {
    
    private BufferedImage skin;
    private double[][] points;
    private double[][] sts;
    private final double[][] va = new double[3][5];
    private int[][] faces;
    private final int[][] ps = new int [2][3];
    private int alpha;
    private boolean animationFinished;
    private Font font;
    private final String presentsText = "PRESENTS";
    private double letterIndex;
    
    public OLPresents() {
        super("ol_presents");
        loadFont();
        loadAnimation();
    }
    
    private void loadFont() {
        try {
            InputStream is = OLPresents.class
                        .getResourceAsStream("/res/8-bit Arcade In.ttf");
            
            Font fontTmp = Font.createFont(Font.TRUETYPE_FONT, is);
            font = fontTmp.deriveFont(17.0f);
        } catch (FontFormatException | IOException ex) {
            String className = OLPresents.class.getName();
            Logger.getLogger(className).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
    }

    private void loadAnimation() {
        try {
            InputStream is = getClass()
                                .getResourceAsStream("/res/ol_presents.png");
            
            skin = ImageIO.read(is);
            try ( DataInputStream dis = new DataInputStream(
                    getClass().getResourceAsStream("/res/ol_presents.mdd")) ) {

                int totalFrames = dis.readInt();
                int totalPoints = dis.readInt();
                dis.read(new byte[totalFrames * 4]); // unused datas
                points = new double[totalFrames][totalPoints * 3];
                for (int s = 0; s < totalFrames; s++) {
                    for (int p = 0; p < totalPoints * 3; p++) {
                        points[s][p] = dis.readFloat() * 0.01;
                        // manually adjusting the positions
                        if ((p + 1) % 3 == 0) {
                            points[s][p] += 0.175; 
                        }
                        if ((p + 2) % 3 == 0) {
                            points[s][p] += 0.01; 
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        try ( Scanner sc = new Scanner(
                getClass().getResourceAsStream("/res/ol_presents.obj")) ) {
            
            List<int[]> facesTmp = new ArrayList<>();
            List<double[]> stsTmp = new ArrayList<>();
            sc.useDelimiter("[ /\n]");
            while (sc.hasNext()) {
                String token = sc.next();
                if (token.equals("vt")) {
                    stsTmp.add(new double[] { sc.nextDouble() * skin.getWidth()
                            , (1 - sc.nextDouble()) * skin.getHeight() } );
                }
                else if (token.equals("f")) {
                    facesTmp.add( new int[] { sc.nextInt() - 1, sc.nextInt() - 1
                      , sc.nextInt() - 1, sc.nextInt() - 1
                      , sc.nextInt() - 1, sc.nextInt() - 1 } );
                }
            }
            faces = facesTmp.toArray(new int[0][0]);
            sts = stsTmp.toArray(new double[0][0]);
        }        
    }

    private double currentDepth, frame;
    private double[] depthBuffer = new double[320 * 200];
    private final BufferedImage nbimodel 
            = new BufferedImage(320, 200, BufferedImage.TYPE_INT_ARGB);

    private final WritableRaster wr 
                            = new WritableRaster(nbimodel.getSampleModel()
                                , new DataBufferInt(320 * 200), new Point()) {
            
        @Override
        public void setDataElements(int x, int y, Object inData) {
            if (currentDepth <= depthBuffer[y * 320 + x]) {
                super.setDataElements(x, y, inData); 
                depthBuffer[y * 320 + x] = currentDepth;
            }
        }
    };
    
    private final ColorModel cm = ColorModel.getRGBdefault();
    private final BufferedImage nbi = new BufferedImage(cm, wr, false, null);
    private final Graphics2D nbig = nbi.createGraphics();
    private final Polygon polygon = new Polygon(ps[0], ps[1], 3);
    private final AffineTransform[] ts 
                    = { new AffineTransform(), new AffineTransform() };
    
    @Override
    public void onEnter() {
        animationFinished = false;
        alpha = 255;
        letterIndex = 0;
    }

    @Override
    public void onExit() {
    }
    
    @Override
    public void fixedUpdate() {
        alpha -= 3;
        if (alpha < 0) {
            alpha = 0;
        }
        
        frame += 1.0;
        if (frame > points.length - 1) {
            frame = points.length - 1;
            animationFinished = true;
        }
        
        if (animationFinished) {
            letterIndex += 0.1;
        }

        if (letterIndex >= 20) {
            SceneManager.switchTo("profound_carnage_13");
        }
    }
    
    @Override
    public void draw(Graphics2D g) {
        nbig.clearRect(0, 0, 320, 200);
        Arrays.fill(depthBuffer, Double.MAX_VALUE);
        for (int[] face : faces) {
            for (int fv = 0; fv < 3; fv++) {
                for (int i = 0; i < 3; i++) {
                    va[fv][i] = points[(int) frame][face[fv * 2] * 3 + i];
                }
                va[fv][3] = sts[face[fv * 2 + 1]][0];
                va[fv][4] = sts[face[fv * 2 + 1]][1];
            }
            drawAffineTextured3DTriangle(va);
        }
        g.drawImage(nbi, 0, 0, 320, 200, null);
        if (alpha > 0) {
            g.setColor(Util.getColor(0, 0, 0, alpha));
            g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        }
        
        int endIndex = (int) letterIndex;
        if (endIndex > presentsText.length()) {
            endIndex = presentsText.length();
        }
        String partialText = "PRESENTS".substring(0, endIndex);
        drawText(g, partialText, 131, 130);
    }

    private void drawAffineTextured3DTriangle(double[][] v) {
        currentDepth = v[0][2] + v[1][2] + v[2][2];
        for (int i = 0; i < 3; i++) {
            ps[0][i] = (int) (160 + 160 * v[i][0] / v[i][2]);
            ps[1][i] = (int) (100 - 160 * v[i][1] / v[i][2]); 
        }
        polygon.xpoints = ps[0];
        polygon.ypoints = ps[1];
        ts[0].setTransform(v[0][3] - v[2][3], v[0][4] - v[2][4]
                , v[1][3] - v[2][3], v[1][4] - v[2][4], v[2][3], v[2][4]);
        
        try {
            ts[0].invert();
        } catch (NoninvertibleTransformException ex) { }
        Shape originalClip = nbig.getClip();
        nbig.clip(polygon);
        ts[1].setTransform(ps[0][0] - ps[0][2], ps[1][0] - ps[1][2]
            , ps[0][1] - ps[0][2], ps[1][1] - ps[1][2], ps[0][2], ps[1][2]);
        
        ts[1].concatenate(ts[0]);
        nbig.drawImage(skin, ts[1], null);
        nbig.setClip(originalClip);
    }

    private void drawText(Graphics2D g, String text, int x, int y) {
        g.setFont(font);
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);
    }
        
}
