package wolf3d.scene;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import wolf3d.asset.loader.VGAGRAPHLoader.VGAGRAPHFont;
import wolf3d.infra.Audio;
import wolf3d.infra.Doors;
import wolf3d.infra.Enemies;
import wolf3d.infra.FizzleFade;
import wolf3d.infra.GameMap;
import wolf3d.infra.GameMap.RaycastResult;
import static wolf3d.infra.GameMap.performRaycastDDA;
import wolf3d.infra.HUD;
import wolf3d.infra.Objs;
import wolf3d.infra.Objs.EndPlayerObj;
import wolf3d.infra.Objs.EndPlayerObj.EndPlayerState;
import wolf3d.infra.Objs.EnemyObj;
import static wolf3d.infra.Objs.EnemyObj.EnemyState.DEAD;
import wolf3d.infra.Objs.Obj;
import static wolf3d.infra.Objs.ObjType.END_PLAYER;
import static wolf3d.infra.Objs.ObjType.ENEMY;
import static wolf3d.infra.Objs.SPRITE_SIZE;
import wolf3d.infra.Player;
import static wolf3d.infra.Player.PLAYER_RADIUS;
import wolf3d.infra.Player.PlayerState;
import static wolf3d.infra.Player.PlayerState.PLAYING;
import wolf3d.infra.Resource;
import wolf3d.infra.Scene;
import wolf3d.infra.SecretDoors;
import static wolf3d.infra.Settings.*;
import wolf3d.infra.Util;
import wolf3d.infra.Weapons;
import wolf3d.infra.Wolf3DGame;

/**
 * Stage class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Stage extends Scene {

    // https://www.doomworld.com/forum/topic ->
    //                      -> /118059-what-was-the-fov-in-the-original-doom/
    private final double fov = Math.toRadians(72);

    private final double projPlaneDistance;
    private final int projectionWidth = CANVAS_WIDTH;
    private final double[] projectionAngles = new double[projectionWidth];
    
    private final double[] wallDepth = new double[CANVAS_WIDTH];    

    private int offsetX = 0;
    private int offsety = CANVAS_HEIGHT / 2 - 20;
    
    private static final int MAX_RAY_SIZE = 1000;
    
    private final RaycastResult raycastResult = new RaycastResult();
    
    private Color floorColor;
    private Color ceilingColor;
    private final VGAGRAPHFont fontYellow;
    private final VGAGRAPHFont fontBlack;
    
    private EndPlayerObj endPlayerObj;
    
    public Stage() {
        super("stage");
        
        projPlaneDistance = (projectionWidth * 0.5) / Math.tan(fov * 0.5);
        for (int sx = 0; sx < projectionWidth; sx++) {
            double op = sx - projectionWidth / 2;
            projectionAngles[sx] = Math.atan(op / projPlaneDistance);
        }
        
        // workaround: fix missing vertical wall column at center of screen 
        //             when this forms exactly 45 degrees angle.
        projectionAngles[160] += 0.000001;
        
        fontYellow = Resource.getFont("SMALL_YELLOW");
        fontBlack = Resource.getFont("SMALL_BLACK");
    }
    
    private void reset() {
        Wolf3DGame.startNextLevel();
        floorColor = Resource.getFloorColor();
        ceilingColor = Resource.getCeilingColorByFloorNumber(
                                                    Wolf3DGame.getFloor());
        
        // debug: start player at specific location
        if (Wolf3DGame.playerOverrideLocation != null) {
            Player.reset(Wolf3DGame.playerOverrideLocation.x + 0.5
                        , Wolf3DGame.playerOverrideLocation.y + 0.5
                            , Player.getPlayerAngle());
        }

        endPlayerObj = new EndPlayerObj();
    }
    
    @Override
    public void onEnter() {
        if (Wolf3DGame.isBackToGame()) {
            Wolf3DGame.setBackToGame(false);
        }
        // new game or next level
        else {
            reset();
        }
        Audio.playMusicByFloorNumber(Wolf3DGame.getFloor());
        
        // endPlayerObj.walkTo(34, 4);
    }

    @Override
    public void onExit() {
    }

    @Override
    public void update(double delta) {
        Player.update(delta);
        updateFlashScreenEffect(delta);
    }
    
    @Override
    public void fixedUpdate() {
        Player.fixedUpdate();
        if (Player.getPlayerState() == PLAYING) {
            Enemies.fixedUpdateEnemies();
            Doors.fixedUpdateDoors();
            SecretDoors.fixedUpdateSecretDoors();
            Weapons.fixedUpdate();
            HUD.fixedUpdate();
        }
        FizzleFade.fixedUpdate();
        endPlayerObj.fixedUpdate();
        activateEndPlayer();
        
        // game was cleared and all cutscene animations was finished
        if (endPlayerObj.isEnd()) {
            Wolf3DGame.gameCleared();
        }
    }
    
    private void activateEndPlayer() {
        if (Player.isPlayerTriggeredEndGame()
                && endPlayerObj.getState() == EndPlayerState.NONE
                    && Player.getPlayerState() 
                            == PlayerState.GAME_CLEARED_ROTATE_TO_END_PLAYER) {
            
            endPlayerObj.setEndPlayerLocation(34, 8);
            endPlayerObj.walkTo(34, 4);
        }
    }
    
    @Override
    public void draw(Graphics2D g) {
        drawFloorAndCeiling(g);
        drawWalls(g);
        drawObjs(g);
        if (Player.getPlayerState() == PLAYING) {
            Weapons.draw(g);
        }
        FizzleFade.draw(g);
        HUD.draw(g);
        drawRequiredKeyUserMsg(g);
        drawFlashScreenEffect(g);
    }

    private void drawRequiredKeyUserMsg(Graphics2D g) {
        if (Player.isShowUserMsgGoldKeyRequired()) {
            String userMsg = "GOLD KEY IS REQUIRED TO OPEN THIS DOOR";
            fontBlack.drawString(g, userMsg, 37, 75);
            fontYellow.drawString(g, userMsg, 36, 74);
        }
        else if (Player.isShowUserMsgSilverKeyRequired()) {
            String userMsg = "SILVER KEY IS REQUIRED TO OPEN THIS DOOR";
            fontBlack.drawString(g, userMsg, 43, 75);
            fontYellow.drawString(g, userMsg, 42, 74);
        }
    }
    
    private void drawFloorAndCeiling(Graphics2D g) {
        g.setColor(ceilingColor);
        g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        g.setColor(floorColor);
        g.fillRect(0, CANVAS_HEIGHT / 2 - 20, CANVAS_WIDTH, CANVAS_HEIGHT / 2);
    }        
    
    private void drawWalls(Graphics2D g) {
        GameMap.getObjsDuringRaycast().clear();
        
        // perform raycast and draw the columns of the walls
        for (int r = 0; r < projectionAngles.length; r++) {
            double px = Player.getPlayerX();
            double py = Player.getPlayerY();
            double pa = Player.getPlayerAngle() + projectionAngles[r];
            performRaycastDDA(px, py, pa, raycastResult, MAX_RAY_SIZE);
            
            if (raycastResult.isIntersecting()) {
                wallDepth[r] = raycastResult.getDistance();
                
                int wallHeight = (int) (projPlaneDistance * 0.5 
                                        / (raycastResult.getDistance() 
                                            * Math.cos(projectionAngles[r])));
                
                double textureUnit = 0.0;
                double ripx = raycastResult.getIntersectionPoint().x;
                double ripy = raycastResult.getIntersectionPoint().y;
                double to = raycastResult.getTextureOffset();
                switch (raycastResult.getWallSide()) {
                    case 0 -> textureUnit = ripx - (int) ripx + to;
                    case 1 -> textureUnit = ripy - (int) ripy + to;
                }
                
                int textureRow = (int) (SPRITE_SIZE * textureUnit);
                
                int dx1 = offsetX + r;
                int dy1 = offsety - wallHeight;
                int dx2 = offsetX + r + 1;
                int dy2 = offsety + wallHeight;
                int sx1 = textureRow;
                int sy1 = 0;
                int sx2 = textureRow + 1;
                int sy2 = 64;
                
                BufferedImage texture = raycastResult.getTile()
                                    .getTexture(raycastResult.getWallSide());
                
                g.drawImage(
                        texture, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
            }
        }
    }
    
    private final Rectangle sight = new Rectangle(
                    offsetX + CANVAS_WIDTH / 2 - 16, 0, 32, CANVAS_HEIGHT);
    
    private final Rectangle targetEnemyRegionTmp = new Rectangle();
    private final Rectangle targetEnemy = new Rectangle();
    private List<Objs.Obj> orderedObjs = new ArrayList<>();
    
    private void drawObjs(Graphics2D g) {
        Set<Objs.Obj> objs = GameMap.getObjsDuringRaycast();
        
        objs.add(endPlayerObj);
        
        orderedObjs.clear();
        
        double dirHorX = Math.cos(Player.getPlayerAngle());
        double dirHorY = Math.sin(Player.getPlayerAngle());
        double dirVerX = -dirHorY; 
        double dirVerY = dirHorX;
        
        Wolf3DGame.setClosestEnemyInSight(null);
        targetEnemy.setBounds(0, 0, 0, 0);
        
        objs.addAll(Enemies.getEnemies());
        
        for (Objs.Obj obj : objs) {
            if (obj.isDrawable()) {
                
                double objX = obj.getCol() + 0.5;
                double objY = obj.getRow() + 0.5;
                
                if (obj.getType() == ENEMY) {
                    EnemyObj enemy = (EnemyObj) obj;
                    objX = enemy.getEnemyX();
                    objY = enemy.getEnemyY();
                }
                else if (obj.getType() == END_PLAYER) {
                    EndPlayerObj epObj = (EndPlayerObj) obj;
                    objX = epObj.getEndPlayerX();
                    objY = epObj.getEndPlayerY();
                }
                
                double distX = objX - Player.getPlayerX();
                double distY = objY - Player.getPlayerY();
                
                double distHor = dirHorX * distX + dirHorY * distY;
                double distVer = dirVerX * distX + dirVerY * distY;
                
                if (distHor > PLAYER_RADIUS) {
                    int sizeHor = (int) (projPlaneDistance * 1 / distHor);
                    int sizeVer = (int) (projPlaneDistance * distVer / distHor);

                    obj.setSizeHor(sizeHor);
                    obj.setSizeVer(sizeVer);
                    obj.setDistanceFromPlayer(distHor);
                    orderedObjs.add(obj);
                }
            }
            Collections.sort(orderedObjs, objComparator);
            for (Objs.Obj obj2 : orderedObjs) {
                int sizeHor = obj2.getSizeHor();
                int sizeVer = obj2.getSizeVer();
                if (sizeHor <= 0) return;

                // calculate the clipping region of sprite
                boolean clip = true;
                int startClip = -1;
                int endClip = 0;
                //double tol = 0.1; // tolerance to avoid visibility problems
                for (int x = 0; x < sizeHor; x++) {
                    int scrX = 160 - sizeHor / 2 + sizeVer + x;
                    
                    boolean draw = scrX >= 0 && scrX < CANVAS_WIDTH 
                            && wallDepth[scrX] > obj2.getDistanceFromPlayer() 
                                            / Math.cos(projectionAngles[scrX]); // + tol;

                    if (clip && draw) {
                        startClip = scrX;
                        clip = false;
                    }
                    else if (!clip && (!draw || x == sizeHor - 1)) {
                        endClip = scrX;
                        break;
                    }
                }
                
                // sprite completely occluded
                if (startClip < 0) {
                    continue;
                }
                
                // draw sprite
                int dx1 = offsetX + 160 - sizeHor / 2 + sizeVer;
                int dy1 = offsety - sizeHor / 2;
                int dx2 = dx1 + sizeHor;
                int dy2 = dy1 + sizeHor;
                int sx1 = 0;
                int sy1 = 0;
                int sx2 = 64;
                int sy2 = 64;
                Shape oc = g.getClip();
                g.setClip(
                        startClip, 0, endClip - startClip, CANVAS_HEIGHT - 40);
                
                g.drawImage(obj2.getSprite()
                        , dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
                
                g.setClip(oc);
                
                // reuse this drawing routine 
                // to choose the closest enemy in sight
                int terX = offsetX + 160 - sizeHor / 4 + sizeVer;
                int terY = dy1;
                int terW = sizeHor / 2;
                int terH = dy2 - dy1;
                targetEnemyRegionTmp.setBounds(terX, terY, terW, terH);
                EnemyObj closestEnemy = Wolf3DGame.getClosestEnemyInSight();
                if (obj2.getType() == ENEMY) { 
                    Objs.EnemyObj enemyObj = (Objs.EnemyObj) obj2;
                    if (enemyObj.getEnemyState() != DEAD && 
                        ((closestEnemy == null 
                            || enemyObj.getDistanceFromPlayer() 
                            < closestEnemy.getDistanceFromPlayer()) 
                                && sight.intersects(dx1, 0, dx2 - dx1, 200) 
                                    && targetEnemyRegionTmp.intersects(
                                                    dx1, 0, dx2 - dx1, 200))) {
                    
                        Wolf3DGame.setClosestEnemyInSight(enemyObj);
                        targetEnemy.setBounds(targetEnemyRegionTmp);
                    }
                }
            }
        }
    } 
    
    private final Comparator<Obj> objComparator = (Obj o1, Obj o2) -> { 
        // for sprites that have shadow, give priority to draw first
        // so that enemies are always drawn on top of it.
        double obj1Dist = o1.getDistanceFromPlayer();
        if (o1.isDrawFirst()) obj1Dist += 1000;
        double obj2Dist = o2.getDistanceFromPlayer();
        if (o2.isDrawFirst()) obj2Dist += 1000;
        return (int) Math.signum(obj2Dist - obj1Dist);
    };

    // --- flash screen effect ---

    private static double flashScreenRed;
    private static double flashScreenGreen;
    private static double flashScreenBlue;
    private static double flashScreenAlpha = 0.0;
    
    // r, g, b = 0.0~1.0
    public static void flashScreen(double r, double g, double b, double alpha) {
        flashScreenRed = r;
        flashScreenGreen = g;
        flashScreenBlue = b;
        flashScreenAlpha = alpha;
    }

    private static void updateFlashScreenEffect(double delta) {
        flashScreenAlpha -= 1.25 * delta;
        if (flashScreenAlpha < 0.0) flashScreenAlpha = 0.0;
    }
    
    // draw the flash effect (hit and collect indicators)
    public static void drawFlashScreenEffect(Graphics2D g) {
        if (flashScreenAlpha > 0.0) {
            int red = (int) (255 * flashScreenRed);
            int green = (int) (255 * flashScreenGreen);
            int blue = (int) (255 * flashScreenBlue);
            int alpha = (int) (255 * flashScreenAlpha);
            Color color = Util.getColor(red, green, blue, alpha);
            g.setColor(color);
            g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        }
    }
    
}
