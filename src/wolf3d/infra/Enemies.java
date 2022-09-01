package wolf3d.infra;

import java.util.ArrayList;
import java.util.List;
import wolf3d.infra.Objs.EnemyObj;
import wolf3d.infra.Objs.EnemyObj.EnemyState;
import static wolf3d.infra.Objs.EnemyObj.EnemyState.*;
import wolf3d.infra.Objs.Obj;
import static wolf3d.infra.Objs.ObjType.*;
import wolf3d.infra.Objs.PathObj;
import wolf3d.infra.Tiles.DoorTile;
import static wolf3d.infra.Tiles.DoorTile.DoorState.*;
import wolf3d.infra.Tiles.Tile;
import static wolf3d.infra.Tiles.TileType.*;

/**
 * Enemies class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Enemies {

    private static final List<EnemyObj> ENEMIES = new ArrayList<>();
    
    public static void clear() {
        ENEMIES.clear();
    }

    public static List<EnemyObj> getEnemies() {
        return ENEMIES;
    }
    
    public static void addEnemy(EnemyObj enemy) {
        // if enemy not dead, then add collision
        if (enemy.getEnemyState() != DEAD) {
            int enemyCol = enemy.getCol();
            int enemyRow = enemy.getRow();
            GameMap.getTile(enemyCol, enemyRow).setBlockMovement(true);
        }        
        ENEMIES.add(enemy);
    }
    
    public static void fixedUpdateEnemies() {
        for (EnemyObj enemy : ENEMIES) {
            switch (enemy.getEnemyState()) {
                case STAND -> updateStand(enemy);
                case PATROL -> updatePatrol(enemy);
                case WALK -> updateWalk(enemy);
                case CHASE_REACT -> updateChaseReact(enemy);
                case CHASE -> updateChase(enemy);
                case ATTACK_REACT -> updateAttackReact(enemy);
                case ATTACK -> updateAttack(enemy);
                case HIT -> updateHit(enemy);
                case DYING -> updateDying(enemy);
                // case DEAD -> updateDead(enemy);
            }
        }
    } 

    private static void updateStand(EnemyObj enemy) {
        checkMustStartChasingPlayer(enemy);
    }
    
    private static final double PROXIMITY_DISTANCE = 2.5;
    
    private static boolean checkMustStartChasingPlayer(EnemyObj enemy) {
        // proximity
        double dist = enemy.calculateDistanceFromPlayer();
        if (dist <= PROXIMITY_DISTANCE && Math.random() < 0.1 
                                        && enemy.canSeePlayer(false) ) {
            
            enemy.chaseReact();
            return true;
        }

        // enemy can see the player
        if (enemy.canSeePlayer(true)) {
            enemy.chaseReact();
            return true;
        }
        
        // propagated sound ok
        // implemented in Player.propagateGunFiringSoundThroughoutRooms()
        
        return false;
    }
    
    // check if there is a new the path direction and updates the 
    // target position, the enemy can open door if necessary.
    private static void updatePatrol(EnemyObj enemy) {
        // update new path direction?
        Obj obj = GameMap.getObj(enemy.getCol(), enemy.getRow());
        if (obj != null && obj.getType() == PATH) {
            PathObj pathObj = (PathObj) obj;
            enemy.setDirection(pathObj.getDirection());
        }
        
        if (!tryToWalk(enemy, PATROL)) {
            enemy.setAnimationFrame(0);
        }
    }
    
    private static boolean tryToWalk(EnemyObj enemy, EnemyState restoreState) {
        // check door
        int mc = enemy.getCol() + enemy.getDirection().dx;
        int mr = enemy.getRow() + enemy.getDirection().dy;
        Tile destTile = GameMap.getTile(mc, mr);
        if (destTile.getType() == DOOR) {
            DoorTile doorTile = (DoorTile) destTile;
            if (((doorTile.getObstructingEnemy() != null 
                && doorTile.getObstructingEnemy().getEnemyState() != DEAD) 
                    && doorTile.getObstructingEnemy() != enemy)
                        || doorTile.isTileObstructedByPlayer()) {

                return false;
            }
            if (enemy.isAbleToOpenDoor()) {
                doorTile.setObstructingEnemy(enemy);
                if (doorTile.getDoorState() != OPEN) {
                    if (doorTile.getDoorState() == CLOSED 
                                || doorTile.getDoorState() == CLOSING) {

                        Doors.activateDoor(doorTile);
                    }
                    return false;
                }
            }
            else if (doorTile.getDoorState() == OPEN) {
                doorTile.setObstructingEnemy(enemy);
            }
            else if (doorTile.getDoorState() != OPEN) {
                return true;
            }
        }
        
        // destination tile is blocked
        if (destTile.isBlockMovement()) {
            return false;
        }

        // update collision
        mc = enemy.getCol();
        mr = enemy.getRow();
        GameMap.getTile(mc, mr).setBlockMovement(false);
        destTile.setBlockMovement(true);

        double etx = enemy.getCol() + 0.5 + enemy.getDirection().dx;
        double ety = enemy.getRow() + 0.5 + enemy.getDirection().dy;
        enemy.setEnemyTargetPosition(etx, ety);
        enemy.setEnemyRestoreState(restoreState);
        enemy.setEnemyState(WALK);
        return true;
    }
    
    // walk until target position
    private static void updateWalk(EnemyObj enemy) {
        // check if need to start chasing
        if (enemy.getEnemyRestoreState() == PATROL 
                        && checkMustStartChasingPlayer(enemy)) return;
        
        double distanceFromPlayer = enemy.calculateDistanceFromPlayer() ;
        
        // check if need to start attacking
        if (enemy.getEnemyRestoreState() == CHASE 
                    && Util.getTimeMs() >= enemy.getAttackTime()) {
            
            if ((enemy.canSeePlayer(true) || distanceFromPlayer <= 1.5)
                    && distanceFromPlayer <= enemy.getMinAttackDistance()) {
                
                enemy.attackReact();
                return;
            }
            else {
                planNextAttack(enemy);
            }
        }

        // if enemy is too close, speed up the fire rate
        if (enemy.calculateDistanceFromPlayer() <= 1.5) {
            enemy.setAttackTime(enemy.getAttackTime() - 50);
            return;
        }
                
        double dx = enemy.getEnemyTargetX() - enemy.getEnemyX();
        double dy = enemy.getEnemyTargetY() - enemy.getEnemyY();
        double dist = Math.hypot(dx, dy);
        double walkSpeed = enemy.getSpeed();
        if (dist >= walkSpeed) {
            dx /= dist;
            dy /= dist;
            double nex = enemy.getEnemyX() + dx * walkSpeed;
            double ney = enemy.getEnemyY() + dy * walkSpeed;
            enemy.setEnemyPosition(nex, ney);
        }
        else {
            enemy.setEnemyPosition(
                    enemy.getEnemyTargetX(), enemy.getEnemyTargetY());
            
            enemy.setEnemyGridPosition(
                (int) enemy.getEnemyTargetX(), (int) enemy.getEnemyTargetY());
            
            enemy.setEnemyState(enemy.getEnemyRestoreState());
        }
        
        // boss has only 4 frames for walking, no 360 view.
        if (enemy.isBoss()) {
            enemy.setAnimationFrame(
                    (int) (System.nanoTime() * 0.000000005) % 4);
        }
        else {
            enemy.setAnimationFrame(
                        8 * ((int) (System.nanoTime() * 0.000000005) % 4));
        }
    }
        
    private static void updateChaseReact(EnemyObj enemy) {
        if (Util.getTimeMs() >= enemy.getReactTime()) {
            Audio.playSound(enemy.getRandomHaltSoundId());
            enemy.setEnemyRestoreState(CHASE);
            enemy.setEnemyState(WALK);
        }
        enemy.setAnimationFrame(0);
    }

    private static void updateChase(EnemyObj enemy) {
        CardinalDirection nextDir = enemy.isKeepDirection()
                ? enemy.getDirection() : getNextChaseDirection(enemy);
        
        if (nextDir != null) {
            enemy.setDirection(nextDir);
            boolean keepChaseDirection = !tryToWalk(enemy, CHASE);
            enemy.setKeepDirection(keepChaseDirection);
        }
    }

    private static CardinalDirection getNextChaseDirection(EnemyObj enemy) {
        int dx = (int) Player.getPlayerX() - (int) enemy.getEnemyX();
        int dy = (int) Player.getPlayerY() - (int) enemy.getEnemyY();
        
        dx = (int) Math.signum(dx);
        dy = (int) Math.signum(dy);
        
        if (dx == 0) dx = Math.random() < 0.5 ? 1 : -1;
        if (dy == 0) dy = Math.random() < 0.5 ? 1 : -1;
        
        CardinalDirection nextDir = CardinalDirection.getDirection(dx, dy);
        if (isNextDirectionFree(enemy, nextDir)) {
            return nextDir;
        }
        
        CardinalDirection nextDirDx = CardinalDirection.getDirection(dx, 0);
        if (Math.random() < 0.5 && isNextDirectionFree(enemy, nextDirDx)) {
            return nextDirDx;
        }
        
        CardinalDirection nextDirDy = CardinalDirection.getDirection(0, dy);
        if (isNextDirectionFree(enemy, nextDirDy)) {
            return nextDirDy;
        }
        
        CardinalDirection nextDirOppos = CardinalDirection.getOpposite(nextDir);

        CardinalDirection nextDirOpposDx 
                = CardinalDirection.getDirection(nextDirOppos.dx, 0);
        
        if (Math.random() < 0.05 
                && isNextDirectionFree(enemy, nextDirOpposDx)) {
            
            return nextDirOpposDx;
        }
        
        CardinalDirection nextDirOpposDy 
                = CardinalDirection.getDirection(0, nextDirOppos.dy);
        
        if (Math.random() < 0.05 
                && isNextDirectionFree(enemy, nextDirOpposDy)) {
            
            return nextDirOpposDy;
        }

        return null;
    }

    private static boolean isNextDirectionFree(
                    EnemyObj enemy, CardinalDirection nextDir) {
        
        if (nextDir == null) return false;
        
        int ec = enemy.getCol();
        int er = enemy.getRow();
        
        Tile t1 = GameMap.getTile(ec + nextDir.dx, er);
        Tile t2 = GameMap.getTile(ec, er + nextDir.dy);
        Tile t3 = GameMap.getTile(ec + nextDir.dx, er + nextDir.dy);
        
        boolean c1 = !t1.isTileObstructedByPlayer() 
                    && (!t1.isBlockMovement() 
                        || (enemy.isAbleToOpenDoor() && t1.getType() == DOOR 
                            && (((DoorTile) t1).getObstructingEnemy() == null 
                                || ((DoorTile) t1).getObstructingEnemy()
                                                .getEnemyState() == DEAD)));
        
        boolean c2 = !t2.isTileObstructedByPlayer() 
                    && (!t2.isBlockMovement() 
                        || (enemy.isAbleToOpenDoor() && t2.getType() == DOOR 
                            && (((DoorTile) t2).getObstructingEnemy() == null 
                                || ((DoorTile) t2).getObstructingEnemy()
                                                .getEnemyState() == DEAD)));
        
        boolean c3 = !t3.isTileObstructedByPlayer() && !t3.isBlockMovement();
        
        if (nextDir.dx != 0 && nextDir.dy == 0) return c1;
        else if (nextDir.dx == 0 && nextDir.dy != 0) return c2;
        else return c1 && c2 && c3;
    }
    
    private static void updateAttackReact(EnemyObj enemy) {
        double nextFrame = enemy.getAnimationFrame() + 0.1;
        if (nextFrame >= 2) nextFrame = 2;
        enemy.setAnimationFrame(nextFrame);
        if (Util.getTimeMs() >= enemy.getReactTime()) {
            Audio.playSound(enemy.getRandomAttackSoundId());
            enemy.setEnemyState(ATTACK);
            enemy.setAnimationFrame(2.99);
            enemy.setUse360View(false);
            Player.tryToHit(enemy);
        }
    }

    private static void updateAttack(EnemyObj enemy) {
        double nextFrame = enemy.getAnimationFrame() - 0.1;
        enemy.setAnimationFrame(nextFrame);
        if (nextFrame <= 1) {
            planNextAttack(enemy);
        }
    }
    
    private static void planNextAttack(EnemyObj enemy) {
        double halfDist = enemy.calculateDistanceFromPlayer() / 2;
        enemy.setAttackTime(Util.getTimeMs() + (int) (300 * halfDist) 
                                    + Util.random(1 +(int) (300 * halfDist)));

        if (enemy.isBoss()) {
            enemy.setAttackTime(
                    (long) (Util.getTimeMs() + 250 + 100 * halfDist));
        }
        
        enemy.setEnemyRestoreState(CHASE);
        enemy.setEnemyState(WALK);
        enemy.setUse360View(true);
        enemy.setAnimationFrame(0);
    }

    public static void updateHit(EnemyObj enemy) {
        if (Util.getTimeMs() >= enemy.getReactTime()) {
            enemy.setEnemyState(WALK);
            enemy.setEnemyRestoreState(CHASE);
            enemy.setUse360View(true);
            enemy.setAnimationFrame(0);
        }
    }

    public static void updateDying(EnemyObj enemy) {
        double currentFrame = enemy.getAnimationFrame();
        double nextFrame = currentFrame + 0.10;
        
        // give the opportunity to hear the death sound
        if (nextFrame > 0.85 && nextFrame < 0.95) {
            Audio.playSound(enemy.getRandomDeathSoundId());
        }
        
        enemy.setAnimationFrame(nextFrame);
        if (nextFrame >= enemy.getAnimationFramesCount()) {
            enemy.setEnemyState(DEAD);
            enemy.setAnimationFrame(0);
        }
    }

//    public static void updateDead(EnemyObj enemy) {
//    }

    public static void tryToHit(EnemyObj enemy, double minHitDistance) {
        if (enemy.getEnemyState() == DYING 
                || enemy.getEnemyState() == DEAD 
                    || enemy.getEnemyState() == HIT
                        || enemy.getEnemyState() == ATTACK) {
            
            return;
        }
        
        double dist = enemy.calculateDistanceFromPlayer();
        //System.out.println("dist " + dist);
        // make boss very hard to hit
        double bossMissChange = 0;
        if (dist < 3) {
            bossMissChange = 1.0;
        }
        else if (dist < 6) {
            bossMissChange = 0.98;
        }
        else if (dist < 10) {
            bossMissChange = 0.90;
        }
        else {
            bossMissChange = 0.80;
        }
        
        if (enemy.isBoss() && Math.random() < bossMissChange) {
            return;
        }
        
        
        if (dist > minHitDistance) return;
        
        //if (Math.random() < (1.0 / (dist * 0.25))) {
            //int damage = (int) (200 * (0.5 / dist));
            int damage = Util.random(0, 125);
            if (dist < 3) {
                damage = Util.random(25, 150);
            }
            enemy.hit(damage);
            if (enemy.getEnemyLife() <= 0) {
                enemy.kill();
            }
            else {
                enemy.setReactTime(Util.getTimeMs() + 200);
                enemy.setEnemyState(HIT);
                enemy.setUse360View(false);
                enemy.setAnimationFrame(0);
            }
        //}
    }

}
