package wolf3d.infra;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import wolf3d.infra.GameMap.RaycastResult;
import wolf3d.infra.Objs.CollectableObj;
import wolf3d.infra.Objs.EnemyObj;
import static wolf3d.infra.Objs.EnemyObj.EnemyState.DEAD;
import wolf3d.infra.Objs.Obj;
import static wolf3d.infra.Objs.ObjType.COLLECTABLE;
import static wolf3d.infra.Objs.ObjType.END_GAME;
import static wolf3d.infra.Player.PlayerState.DYING_FIZZLE_FADE_OUT;
import static wolf3d.infra.Player.PlayerState.DYING_ROTATING;
import static wolf3d.infra.Player.PlayerState.GAME_CLEARED_WALK;
import static wolf3d.infra.Player.PlayerState.PLAYING;
import static wolf3d.infra.Player.PlayerState.TRY_NEXT_LIFE;
import static wolf3d.infra.Settings.*;
import wolf3d.infra.Tiles.DoorTile;
import wolf3d.infra.Tiles.DoorTile.DoorKey;
import static wolf3d.infra.Tiles.DoorTile.DoorKey.*;
import wolf3d.infra.Tiles.FloorTile;
import wolf3d.infra.Tiles.SecretDoorTile;
import static wolf3d.infra.Tiles.TILE_VERTICAL;
import wolf3d.infra.Tiles.Tile;
import static wolf3d.infra.Tiles.TileType.*;
import wolf3d.infra.Tiles.WallTile;
import wolf3d.infra.Weapons.Weapon;
import wolf3d.infra.Weapons.WeaponType;
import static wolf3d.infra.Weapons.WeaponType.*;
import wolf3d.scene.Stage;

/**
 * Player class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Player {
    
    public static final double PLAYER_RADIUS = 0x5800 / (double) 0xffff;
    
    public static enum PlayerState { PLAYING, DYING_ROTATING
        , DYING_FIZZLE_FADE_IN, TRY_NEXT_LIFE, DYING_FIZZLE_FADE_OUT
            , GAME_CLEARED_ROTATE_TO_TARGET_TILE, GAME_CLEARED_WALK
                , GAME_CLEARED_ROTATE_TO_END_PLAYER, GAME_CLEARED_FINISHED }
    
    private static PlayerState playerState;
    private static double playerX;
    private static double playerY;
    private static double playerAngle;
    private static boolean playerHasSilverKey;
    private static boolean playerHasGoldKey;
    private static boolean showUserMsgGoldKeyRequired;
    private static boolean showUserMsgSilverKeyRequired;

    private static final double SPEED = 0.4 * 0x3000 / (double) 0xffff;
    private static final double ROT_SPEED = 2.0;
    
    private static final Color DEATH_COLOR = Util.getColor("0x8a0000ff");
        
    private static final RaycastResult raycastResult 
                                            = new GameMap.RaycastResult();

    private static long levelStartTimeMs;
    private static long levelEndTimeMs;
    private static long tryNextLifeStartTimeMs;
    
    private static EnemyObj killerEnemy;
    private static boolean playerTriggeredEndGame;

    public static boolean isPlayerTriggeredEndGame() {
        return playerTriggeredEndGame;
    }
    
    public static PlayerState getPlayerState() {
        return playerState;
    }
    
    public static double getPlayerX() {
        return playerX;
    }

    public static double getPlayerY() {
        return playerY;
    }

    public static double getPlayerAngle() {
        return playerAngle;
    }

    public static boolean isPlayerHasSilverKey() {
        return playerHasSilverKey;
    }

    public static void setPlayerHasSilverKey(boolean playerHasSilverKey) {
        Player.playerHasSilverKey = playerHasSilverKey;
    }

    public static boolean isPlayerHasGoldKey() {
        return playerHasGoldKey;
    }

    public static void setPlayerHasGoldKey(boolean playerHasGoldKey) {
        Player.playerHasGoldKey = playerHasGoldKey;
    }

    public static boolean isShowUserMsgGoldKeyRequired() {
        return showUserMsgGoldKeyRequired;
    }

    public static boolean isShowUserMsgSilverKeyRequired() {
        return showUserMsgSilverKeyRequired;
    }

    public static long getLevelStartTimeMs() {
        return levelStartTimeMs;
    }

    public static long getLevelEndTimeMs() {
        return levelEndTimeMs;
    }

    public static long getPlayTimeMs() {
        return levelEndTimeMs - levelStartTimeMs;
    }

    public static void reset(
            double playerX, double playerY, double playerAngle) {
        
        Player.playerState = PLAYING;
        Player.playerX = playerX;
        Player.playerY = playerY;
        Player.playerAngle = playerAngle;
        Player.playerHasSilverKey = false;
        Player.playerHasGoldKey = false;
        Player.levelStartTimeMs = Util.getTimeMs();
        Player.levelEndTimeMs = Player.levelStartTimeMs;
        Player.killerEnemy = null;
        Player.playerTriggeredEndGame = false;
    }

    public static void update(double delta) {
        switch (playerState) {
            case PLAYING -> updatePlaying(delta);
            case DYING_ROTATING -> updateDyingRotating(delta);
            case GAME_CLEARED_ROTATE_TO_TARGET_TILE -> {
                gameClearedRotateToTargetTileUpdate(delta);
            }
            case GAME_CLEARED_ROTATE_TO_END_PLAYER -> {
                gameClearedRotateToEndPlayerUpdate(delta);
            }
        }
    }
    
    private static void updatePlaying(double delta) {
        boolean strafeActivated = Input.isKeyPressed(KEY_PLAYER_STRAFE);
        
        if (!strafeActivated && Input.isKeyPressed(KEY_PLAYER_LEFT)) {
            playerAngle -= ROT_SPEED * delta;
        }
        else if (!strafeActivated && Input.isKeyPressed(KEY_PLAYER_RIGHT)) {
            playerAngle += ROT_SPEED * delta;
        }
    }

    // rotate to the direction of the enemy that killed you
    private static void updateDyingRotating(double delta) {
        double ex = killerEnemy.getEnemyX();
        double ey = killerEnemy.getEnemyY();
        if (rotateTowards(ex, ey, delta)) {
            FizzleFade.fadeIn(DEATH_COLOR);
            playerState = PlayerState.DYING_FIZZLE_FADE_IN;
        }
    }
    
    private static boolean rotateTowards(double targetX, double targetY, double delta) {
        double pa = playerAngle % (2 * Math.PI);
        if (pa < 0) pa += 2 * Math.PI;
        double dx = targetX - playerX;
        double dy = targetY - playerY;
        double targetAngle = Math.atan2(dy, dx);
        if (targetAngle < 0) targetAngle += 2 * Math.PI;
        double dif = targetAngle - pa;
        double difAbs = Math.abs(dif);
        double sign = Math.signum(dif);
        if (2 * Math.PI - difAbs < difAbs) sign *= -1;
        double rotSpeed = delta * ROT_SPEED;
        if (Math.abs(dif) <= rotSpeed) {
            playerAngle = targetAngle;
            return true;
        }
        else {
            playerAngle += sign * rotSpeed;
        }    
        return false;
    }

    public static void fixedUpdate() {
        switch (playerState) {
            case PLAYING -> playingFixedUpdate();
            case DYING_FIZZLE_FADE_IN -> dyingFizzleFadeInFixedUpdate();
            case TRY_NEXT_LIFE -> tryNextLifeFixedUpdate();
            case DYING_FIZZLE_FADE_OUT -> dyingFizzleFadeOutFixedUpdate();
            case GAME_CLEARED_WALK -> gameClearedWalkFixedUpdate();
        }
    }
    
    private static void dyingFizzleFadeInFixedUpdate() {
        if (FizzleFade.isFinished()) {
            playerState = TRY_NEXT_LIFE;
            tryNextLifeStartTimeMs = Util.getTimeMs() + 2000;
            if (Wolf3DGame.getLives() == 1) {
                Audio.playSound("GAMEOVER");
            }
        }
    }
    
    private static void tryNextLifeFixedUpdate() {
        if (Util.getTimeMs() < tryNextLifeStartTimeMs) {
            return;
        }
        if (Wolf3DGame.tryNextLife()) {
            FizzleFade.fadeOut();
            Audio.playMusicByFloorNumber(Wolf3DGame.getFloor());
            playerState = DYING_FIZZLE_FADE_OUT;
        }
    }

    private static void dyingFizzleFadeOutFixedUpdate() {
        if (FizzleFade.isFinished()) {
            playerState = PLAYING;
        }
    }
    
    private static void gameClearedRotateToTargetTileUpdate(double delta) {
        double targetTileX = 34.5;
        double targetTileY = 2.5;
        if (rotateTowards(targetTileX, targetTileY, delta)) {
            playerState = PlayerState.GAME_CLEARED_WALK;
        }        
    }
    
    private static void gameClearedWalkFixedUpdate() {
        double exitX = 34.5;
        double exitY = 2.5;
        double dx = exitX - playerX;
        double dy = exitY - playerY;
        double dist = Math.hypot(dx, dy);
        double vx = SPEED * dx / dist;
        double vy = SPEED * dy / dist;
        if (dist <= SPEED) {
            playerX = exitX;
            playerY = exitY;
            playerState = PlayerState.GAME_CLEARED_ROTATE_TO_END_PLAYER;
        }
        else {
            playerX += vx;
            playerY += vy;
        }
        
        double endPlayerX = 34.5;
        double endPlayerY = 7.5;
        rotateTowards(endPlayerX, endPlayerY, TIME_PER_UPDATE * 0.000000001);
    }
    
    private static void gameClearedRotateToEndPlayerUpdate(double delta) {
        double endPlayerX = 34.5;
        double endPlayerY = 7.5;
        if (rotateTowards(endPlayerX, endPlayerY, delta)) {
            playerState = PlayerState.GAME_CLEARED_FINISHED;
        }        
    }
    
    private static void playingFixedUpdate() {
        // return to game options
        if (Input.isKeyPressed(KEY_CANCEL)) {
            Audio.playSound("ESCPRESSED");
            SceneManager.switchTo("game_options");
        }
        
        // change player's weapon
        if (Input.isKeyJustPressed(KEY_PLAYER_WEAPON_KNIFE)) {
            Weapons.setCurrentPlayerWeapon(KNIFE);
        }
        else if (Input.isKeyJustPressed(KEY_PLAYER_WEAPON_PISTOL)) {
            Weapons.setCurrentPlayerWeapon(PISTOL);
        }
        else if (Input.isKeyJustPressed(KEY_PLAYER_WEAPON_MACHINE)) {
            Weapons.setCurrentPlayerWeapon(MACHINE);
        }
        else if (Input.isKeyJustPressed(KEY_PLAYER_WEAPON_GATLING)) {
            Weapons.setCurrentPlayerWeapon(GATLING);
        }
        
        boolean strafeActivated = Input.isKeyPressed(KEY_PLAYER_STRAFE);
        
        if (strafeActivated && Input.isKeyPressed(KEY_PLAYER_LEFT)) {
            double strafe = -Math.PI * 0.5;
            movePlayer(SPEED, strafe);
        }
        else if (strafeActivated && Input.isKeyPressed(KEY_PLAYER_RIGHT)) {
            double strafe = Math.PI * 0.5;
            movePlayer(SPEED, strafe);
        }
        
        if (Input.isKeyPressed(KEY_PLAYER_UP)) {
            movePlayer(SPEED, 0);
        }
        else if (Input.isKeyPressed(KEY_PLAYER_DOWN)) {
            movePlayer(-SPEED, 0);
        }

        WeaponType weaponType = Weapons.getCurrentPlayerWeapon().getType();
        
        boolean knifePistolAtk = Input.isKeyJustPressed(KEY_PLAYER_FIRE)
                && (weaponType == KNIFE || weaponType == PISTOL);
        
        boolean machineGatlingAtk = Input.isKeyPressed(KEY_PLAYER_FIRE)
                && (weaponType == MACHINE || weaponType == GATLING);
        
        if ((knifePistolAtk || machineGatlingAtk) && Weapons.canAttack()) {
            Weapon currentWeapon = Weapons.getCurrentPlayerWeapon();
            if (Wolf3DGame.getClosestEnemyInSight() != null) {
                Enemies.tryToHit(Wolf3DGame.getClosestEnemyInSight()
                                        , currentWeapon.getMinHitDistance());
            }
            Audio.playSound(currentWeapon.getSoundId());
            Weapons.getCurrentPlayerWeapon().attack();
            if (currentWeapon.getType() != KNIFE) {
                propagateGunFiringSoundThroughoutRooms();
                Weapons.decAmmo();
                if (Weapons.getAmmo() <= 0) {
                    Weapons.setCurrentPlayerWeapon(KNIFE);
                }
            }
        }

        // --- check interactions with tiles ---
        
        showUserMsgGoldKeyRequired = false;
        showUserMsgSilverKeyRequired = false;
        
        GameMap.performRaycastDDA(playerX, playerY, playerAngle, raycastResult);
        boolean needToCheckInteraction = raycastResult.isIntersecting() 
                                           && raycastResult.getDistance() < 1.0;
        
        Tile tile = raycastResult.getTile();
        
        // check if the interacting tile is door and if the 
        // player has the appropriate key if necessary
        boolean isTileDoor = tile != null && tile.getType() == DOOR;
        DoorTile doorTile = null;
        boolean doorUnlockable = true;
        
        if (isTileDoor && needToCheckInteraction) {
            doorTile = (DoorTile) tile;
            DoorKey requiredKey = doorTile.getRequiredKey();
            doorUnlockable = !doorTile.isLocked() || (doorTile.isLocked() 
                            && (requiredKey == SILVER && playerHasSilverKey)
                                || (requiredKey == GOLD && playerHasGoldKey));
            
            if (!doorUnlockable) {
                showUserMsgGoldKeyRequired = requiredKey == GOLD;
                showUserMsgSilverKeyRequired = requiredKey == SILVER;
            }
        }
        
        if (Input.isKeyPressed(KEY_PLAYER_DOOR)) {
            
            if (needToCheckInteraction) {
            
                if (isTileDoor) {
                    if (doorUnlockable) {
                        Doors.activateDoor(doorTile);
                    }
                    else {
                        Audio.playSound("NOWAY");
                    }
                }
                else if (tile != null && tile.getType() == SECRET_DOOR) {
                    SecretDoorTile secretDoorTile = (SecretDoorTile) tile;
                    int dx = (int) Math.signum(Math.cos(playerAngle));
                    int dy = (int) Math.signum(Math.sin(playerAngle));
                    CardinalDirection pushDirection 
                            = CardinalDirection.getDirection(0, dy);
                    
                    if (secretDoorTile.getDoorSide() == TILE_VERTICAL) {
                        pushDirection = CardinalDirection.getDirection(dx, 0);
                    }
                    secretDoorTile.setPushDirection(pushDirection);
                    SecretDoors.activateSecretDoor(secretDoorTile);
                }
                else if (tile != null && tile.getType() == WALL) {
                    WallTile wallTile = (WallTile) tile;
                    if (wallTile.isElevator()) {
                        // check current floor
                        Tile floorTile 
                                = GameMap.getTile((int) playerX, (int) playerY);

                        if (floorTile != null && floorTile.getType() == FLOOR 
                                        && ((FloorTile) floorTile).isSecret()) {

                            // game cleared, go to secret level
                            Wolf3DGame.gotoSecretLevel();
                        }
                        else {
                            // game cleared, go to next level
                            Wolf3DGame.gotoNextLevel();
                        }
                        levelEndTimeMs = Util.getTimeMs();
                        
                        // replace elevator's texture to activated
                        int wallIndex = wallTile.getId() * 2;
                        wallTile.setTextureHorizontal(
                                Resource.getWallTexture(wallIndex, 0));

                        wallTile.setTextureVertical(
                                Resource.getWallTexture(wallIndex, 1));
                    }
                    else {
                        Audio.playSound("DONOTHING");
                    }
                }
            }
        }
        
        checkCollectableObj();
    }
    
    private static void movePlayer(double speed, double strafe) {
        boolean hitWall = false;
        double vx = speed * Math.cos(playerAngle + strafe);
        double vy = speed * Math.sin(playerAngle + strafe);
                
        // check collision x
        int signX = (int) Math.signum(vx);
        double playerTmpX = playerX + vx;
        int collX = (int) (playerTmpX + signX * PLAYER_RADIUS);
        Tile t1 = GameMap.getTile(collX, (int) (playerY - PLAYER_RADIUS));
        Tile t2 = GameMap.getTile(collX, (int) (playerY + PLAYER_RADIUS));
        if (isTileBlocked(t1) || isTileBlocked(t2)) {
            hitWall = true;
            playerX = collX + (signX < 0 ? 1 : 0)
                                - signX * (PLAYER_RADIUS + 0.01) ;
        }
        else {
            playerX = playerTmpX;
        }

        // check collision y
        int signY = (int) Math.signum(vy);
        double playerTmpY = playerY + vy;
        int collY = (int) (playerTmpY + signY * PLAYER_RADIUS);
        Tile t3 = GameMap.getTile((int) (playerX - PLAYER_RADIUS), collY);
        Tile t4 = GameMap.getTile((int) (playerX + PLAYER_RADIUS), collY);
        if (isTileBlocked(t3) || isTileBlocked(t4)) {
            hitWall = true;
            playerY = collY + (signY < 0 ? 1 : 0)
                                - signY * (PLAYER_RADIUS + 0.01);
        }
        else {
            playerY = playerTmpY;
        }
        
        if (hitWall) {
            // Audio.playSound("HITWALL");
        }
    }
    
    private static boolean isTileBlocked(Tile tile) {
        boolean blocked = tile.isBlockMovement();
        if (tile.getType() == DOOR && !tile.isTileObstructedByPlayer()) {
            DoorTile doorTile = (DoorTile) tile;
            if (doorTile.getObstructingEnemy() != null) {
                EnemyObj enemy = doorTile.getObstructingEnemy();
                if (enemy.getEnemyState() != DEAD) {
                    int dx = doorTile.getCol() - enemy.getCol();
                    int dy = doorTile.getRow() - enemy.getRow();
                    dx = (int) Math.signum(dx);
                    dy = (int) Math.signum(dy);
                    CardinalDirection direction 
                            = CardinalDirection.getDirection(dx, dy);
                
                    blocked = blocked || direction == enemy.getDirection();
                }
            }
        }
        return blocked;
    }
    
    public static int getCurrentRoomId() {
        Tile tile = GameMap.getTile((int) playerX, (int) playerY); 
        if (tile.getType() == DOOR) {
            return ((DoorTile) tile).getConnectedRoom1();
        }
        else {
            return tile.getId();        
        }
    }

    private static final Set<Integer> visitedRooms = new HashSet<>();
    private static final List<Integer> neighborRooms = new ArrayList<>();
    
    private static void propagateGunFiringSoundThroughoutRooms() {
        int currentRoomId = getCurrentRoomId();
        visitedRooms.clear();
        neighborRooms.clear();
        neighborRooms.add(currentRoomId);
        while (!neighborRooms.isEmpty()) {
            currentRoomId = neighborRooms.remove(0);
            visitedRooms.add(currentRoomId);
            for (int i = 0; i < 256; i++) {
                if (i != currentRoomId && !visitedRooms.contains(i) 
                            && GameMap.isRoomConnected(currentRoomId, i)
                                            && !neighborRooms.contains(i)) {

                    neighborRooms.add(i);
                } 
            }
        }

        // if non attacking enemy hears the propagated gun fire sound 
        // in his rooms, then start chasing player.
        for (EnemyObj enemy : Enemies.getEnemies()) {
            if (visitedRooms.contains(enemy.getRoomId())) {
                enemy.chaseReact();
            }
        }
    }
    
    private static void checkCollectableObj() {
        int playerCol = (int) playerX;
        int playerRow = (int) playerY;
        Obj obj = GameMap.getObj(playerCol, playerRow);
        if (obj != null && obj.getType() == COLLECTABLE) {
            CollectableObj collectableObj = (CollectableObj) obj;
            switch (collectableObj.getCollectableType()) {
                case AMMO -> {
                    if (Weapons.getAmmo() < 99) {
                        GameMap.getObjs()[playerRow][playerCol] = null;
                        Stage.flashScreen(0.0, 1.0, 0, 0.4);
                        Audio.playSound("GETAMMO");
                        Weapons.incAmmo(5);
                        Weapons.selectBestWeapon();
                    }
                }
                case EXTRALIFE -> {
                    GameMap.getObjs()[playerRow][playerCol] = null;
                    Stage.flashScreen(0.0, 0.0, 1.0, 0.4);
                    Audio.playSound("BONUS1UP");
                    Wolf3DGame.incLives();
                }
                case HEALTH -> {
                    if (Wolf3DGame.getLifeEnergy() < 100) {
                        int recover = switch (collectableObj.getObjId()) {
                            case HEALTH1 -> 5;
                            case HEALTH2 -> 10;
                            case HEALTH3 -> 20;
                            default -> 0;
                        };
                        Wolf3DGame.addLifeEnergy(recover);
                        GameMap.getObjs()[playerRow][playerCol] = null;
                        Stage.flashScreen(1.0, 1.0, 0, 0.4);
                        Audio.playSound(collectableObj.getObjId().toString());
                    }
                }
                case KEY -> {
                    switch (collectableObj.getObjId()) {
                        case GOLDKEY -> playerHasGoldKey = true;
                        case SILVERKEY -> playerHasSilverKey = true;
                    }
                    GameMap.getObjs()[playerRow][playerCol] = null;
                    Stage.flashScreen(1.0, 1.0, 0.0, 0.4);
                    Audio.playSound("GETKEY");
                }
                case TREASURE -> {
                    int bonus = switch (collectableObj.getObjId()) {
                        case BONUS1 -> 100;
                        case BONUS2 -> 200;
                        case BONUS3 -> 500;
                        case BONUS4 -> 1000;
                        default -> 0;
                    };
                    Wolf3DGame.addScorePoints(bonus);
                    Wolf3DGame.incCollectedTreasuresCount();
                    GameMap.getObjs()[playerRow][playerCol] = null;
                    Stage.flashScreen(1.0, 1.0, 0, 0.5);
                    Audio.playSound(collectableObj.getObjId().toString());
                }
                case WEAPON -> {
                    switch (collectableObj.getObjId()) {
                        case MACHINEGUN -> {
                            Audio.playSound("GETMACHINE");
                            Weapons.getPlayerWeapon(MACHINE).setOwn(true);
                            if (Weapons.getAmmo() > 0) {
                                Weapons.setCurrentPlayerWeapon(MACHINE);
                            }
                        }
                        case GATLINGGUN -> {
                            Audio.playSound("GETGATLING");
                            Weapons.getPlayerWeapon(GATLING).setOwn(true);
                            if (Weapons.getAmmo() > 0) {
                                Weapons.setCurrentPlayerWeapon(GATLING);
                            }
                        }
                    }
                    GameMap.getObjs()[playerRow][playerCol] = null;
                    Stage.flashScreen(1.0, 1.0, 0.0, 0.4);
                }
            }
        }
        // game cleared :) !
        else if (obj != null && obj.getType() == END_GAME) {
            // System.out.println("GAME CLEARED :) !");
            playerTriggeredEndGame = true;
            playerState = PlayerState.GAME_CLEARED_ROTATE_TO_TARGET_TILE;
            levelEndTimeMs = Util.getTimeMs();
        }
    }
    
    // when enemy hits the player
    public static void tryToHit(EnemyObj enemy) {
        int damage = Util.random(0, 25);
        if (enemy.isBoss()) {
            double dist = enemy.calculateDistanceFromPlayer();
            if (dist < 6) {
                damage = (int) (100 / (dist * 0.5));
            }
            else {
                if (Math.random() < 0.20) {
                    return;
                }
                if (dist < 0) dist = 1;
                damage = (int) (150 * (1 / dist));
            }
        } 
        else {
            // common enemy
            if (Math.random() < 0.20) {
                return;
            }
        }
        
        
        Wolf3DGame.addLifeEnergy(-damage);
        Stage.flashScreen(1.0, 0, 0, 0.5);
        
        if (Wolf3DGame.getLifeEnergy() <= 0) {
            Audio.stopMusic();
            Audio.playSound("PLAYERDEATH");
            playerState = DYING_ROTATING;
            killerEnemy = enemy;
        }
    } 

}
