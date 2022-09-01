package wolf3d.infra;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import wolf3d.infra.GameMap.RaycastResult;
import wolf3d.infra.Objs.CollectableObj.CollectableType;
import wolf3d.infra.Objs.CollectableObj.ObjId;
import wolf3d.infra.Objs.EnemyObj.EnemyState;
import static wolf3d.infra.Objs.EnemyObj.EnemyState.*;
import wolf3d.infra.Objs.EnemyObj.EnemyType;
import static wolf3d.infra.Objs.EnemyObj.EnemyType.*;
import static wolf3d.infra.Objs.ObjType.*;
import static wolf3d.infra.Player.PLAYER_RADIUS;
import wolf3d.infra.Resource.EndPlayerAnimation;
import wolf3d.infra.Resource.EnemyAnimation;
import wolf3d.infra.Tiles.Tile;
import static wolf3d.infra.Tiles.TileType.DOOR;

/**
 * Objs class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Objs {
    
    public static final int SPRITE_SIZE = 64;
    
    public static enum ObjType { PLAYER_START, DECORATION, COLLECTABLE
                        , PATH, SECRET_DOOR, END_GAME, END_PLAYER, ENEMY };
        
    public static class Obj {

        protected final int id;
        protected final ObjType type;
        protected int col;
        protected int row;
        protected boolean blockMovement;
        protected final boolean drawable;
        protected boolean drawFirst;
        protected BufferedImage sprite;
        protected int sizeHor;
        protected int sizeVer;
        protected double distanceFromPlayer;
        
        public Obj(int id, int col, int row, ObjType type, boolean drawable) {
            
            this.id = id;
            this.col = col;
            this.row = row;
            this.type = type;
            this.drawable = drawable;
        }

        public int getId() {
            return id;
        }

        public int getCol() {
            return col;
        }

        public int getRow() {
            return row;
        }
        
        public void setLocation(int col, int row) {
            this.col = col;
            this.row = row;
        }
        
        public ObjType getType() {
            return type;
        }

        public boolean isBlockMovement() {
            return blockMovement;
        }

        public void setBlockMovement(boolean blockMovement) {
            this.blockMovement = blockMovement;
        }

        public boolean isDrawable() {
            return drawable;
        }

        public boolean isDrawFirst() {
            return drawFirst;
        }

        public void setDrawFirst(boolean drawFirst) {
            this.drawFirst = drawFirst;
        }

        public BufferedImage getSprite() {
            return sprite;
        }

        public void setSprite(BufferedImage sprite) {
            this.sprite = sprite;
        }

        public int getSizeHor() {
            return sizeHor;
        }

        public void setSizeHor(int sizeHor) {
            this.sizeHor = sizeHor;
        }

        public int getSizeVer() {
            return sizeVer;
        }

        public void setSizeVer(int sizeVer) {
            this.sizeVer = sizeVer;
        }

        public double getDistanceFromPlayer() {
            return distanceFromPlayer;
        }

        public void setDistanceFromPlayer(double distanceFromPlayer) {
            this.distanceFromPlayer = distanceFromPlayer;
        }

    }

    public static class PlayerStartObj extends Obj {
        
        private CardinalDirection direction;
        
        public PlayerStartObj(int id, int col, int row) {
            super(id, col, row, PLAYER_START, false);
        }

        public CardinalDirection getDirection() {
            return direction;
        }

        public void setDirection(CardinalDirection direction) {
            this.direction = direction;
        }
        
    }
    
    public static class DecorationObj extends Obj {
        
        public DecorationObj(int id, int col, int row) {
            super(id, col, row, DECORATION, true);
        }
        
    }
    
    public static class CollectableObj extends Obj {

        public static enum CollectableType {
                KEY, HEALTH, AMMO, WEAPON, TREASURE, EXTRALIFE }

        public static enum ObjId {
                GOLDKEY, SILVERKEY, HEALTH1, HEALTH2, HEALTH3, DEFAULT
                    , MACHINEGUN, GATLINGGUN, BONUS1, BONUS2, BONUS3, BONUS4 }
        
        private CollectableType collectableType;
        private ObjId objId;
        
        public CollectableObj(int id, int col, int row) {
            super(id, col, row, COLLECTABLE, true);
        }

        public CollectableType getCollectableType() {
            return collectableType;
        }

        public void setCollectableType(CollectableType collectableType) {
            this.collectableType = collectableType;
        }

        public ObjId getObjId() {
            return objId;
        }

        public void setObjId(ObjId objId) {
            this.objId = objId;
        }
        
    }

    public static class PathObj extends Obj {
        
        private CardinalDirection direction;
        
        public PathObj(int id, int col, int row) {
            super(id, col, row, PATH, false);
        }

        public CardinalDirection getDirection() {
            return direction;
        }

        public void setDirection(CardinalDirection direction) {
            this.direction = direction;
        }
        
    }
    
    public static class SecretDoorObj extends Obj {

        public SecretDoorObj(int id, int col, int row) {
            super(id, col, row, SECRET_DOOR, false);
        }
        
    }
    
    public static class EnemyObj extends Obj {
    
        public static final double ENEMY_RADIUS = 0x5800 / (double) 0xffff;

        public static enum EnemyType { GUARD, OFFICER, SS, DOG, HANS }
        
        public static enum EnemyState { STAND, PATROL, WALK
            , CHASE_REACT, CHASE, ATTACK_REACT, ATTACK, HIT, DYING, DEAD, BOSS }
        
        private EnemyType enemyType;
        private EnemyState enemyState;
        private EnemyState enemyRestoreState;
        private int difficulty;
        private int enemyLife;
        public double enemyPatrolSpeed;
        public double enemyChaseSpeed;
        private double enemyX;
        private double enemyY;
        private CardinalDirection direction;
        private double enemyTargetX;
        private double enemyTargetY;
        private boolean boss;
        private boolean use360View;
        private EnemyAnimation animationInfo;
        private double animationFrame;
        private long reactTime;
        private boolean ableToOpenDoor;
        private boolean keepDirection;
        private long attackTime;
        private double minAttackDistance;
        private String[] haltSoundIds;
        private String[] attackSoundIds;
        private String[] deathSoundIds;
        private CollectableObj dropItem;
        private int scorePoints;
        
        public EnemyObj(int id, int col, int row) {
            super(id, col, row, ENEMY, true);
            enemyLife = 100;
            enemyX = col + 0.5;
            enemyY = row + 0.5;
            enemyTargetX = enemyX;
            enemyTargetY = enemyY;
        }

        public double getEnemyPatrolSpeed() {
            return enemyPatrolSpeed;
        }

        public void setEnemyPatrolSpeed(double enemyPatrolSpeed) {
            this.enemyPatrolSpeed = enemyPatrolSpeed;
        }

        public double getEnemyChaseSpeed() {
            return enemyChaseSpeed;
        }

        public void setEnemyChaseSpeed(double enemyChaseSpeed) {
            this.enemyChaseSpeed = enemyChaseSpeed;
        }

        public double getSpeed() {
            return switch (enemyRestoreState) {
                case PATROL -> enemyPatrolSpeed;
                case CHASE -> enemyChaseSpeed;
                default -> 0;
            };
        }

        public int getEnemyLife() {
            return enemyLife;
        }

        public void setEnemyLife(int enemyLife) {
            this.enemyLife = enemyLife;
        }
        
        public void hit(int damage) {
            enemyLife -= damage;
            if (enemyLife < 0) enemyLife = 0;
        }

        public EnemyType getEnemyType() {
            return enemyType;
        }

        public void setEnemyType(EnemyType enemyType) {
            this.enemyType = enemyType;
            this.animationInfo = Resource.getEnemyAnimationsFrames(enemyType);
        }

        public EnemyState getEnemyState() {
            return enemyState;
        }

        public void setEnemyState(EnemyState enemyState) {
            this.enemyState = enemyState;
            if (enemyState == BOSS) {
                this.enemyState = STAND;
            }
        }

        public EnemyState getEnemyRestoreState() {
            return enemyRestoreState;
        }

        public void setEnemyRestoreState(EnemyState enemyRestoreState) {
            this.enemyRestoreState = enemyRestoreState;
        }

        public int getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(int difficulty) {
            this.difficulty = difficulty;
        }

        public double getEnemyX() {
            return enemyX;
        }

        public double getEnemyY() {
            return enemyY;
        }

        public void setEnemyPosition(double x, double y) {
            enemyX = x;
            enemyY = y;
        }
        
        public CardinalDirection getDirection() {
            return direction;
        }

        public double getEnemyTargetX() {
            return enemyTargetX;
        }

        public double getEnemyTargetY() {
            return enemyTargetY;
        }

        public void setEnemyTargetPosition(double x, double y) {
            enemyTargetX = x;
            enemyTargetY = y;
        }

        public void setEnemyGridPosition(int col, int row) {
            this.col = col;
            this.row = row;
        }

        public void setDirection(CardinalDirection direction) {
            this.direction = direction;
        }

        public boolean isBoss() {
            return boss;
        }

        public void setBoss(boolean boss) {
            this.boss = boss;
        }

        public boolean isUse360View() {
            if (boss) return false;
            return use360View;
        }

        public void setUse360View(boolean use360View) {
            this.use360View = use360View;
        }

        public EnemyAnimation getAnimationInfo() {
            return animationInfo;
        }

        public int getAnimationFramesCount() {
            List<Integer> frames = animationInfo.getFrames(enemyState);
            if (frames == null) {
                return 0;
            }
            else {
                return frames.size();
            }
        }

        public double getAnimationFrame() {
            return animationFrame;
        }

        public void setAnimationFrame(double animationFrame) {
            this.animationFrame = animationFrame;
        }

        @Override
        public BufferedImage getSprite() {
            int spriteOffset = 0;
            if (use360View && !boss) {
                spriteOffset = calculate360ViewSpriteIdOffset();
            }
            List<Integer> frames = animationInfo.getFrames(enemyState);
            int spriteIndex = frames.get((int) animationFrame + spriteOffset);
            sprite = Resource.getSprite(spriteIndex);
            return sprite;
        }
        
        public int getRoomId() {
            Tile tile = GameMap.getTile((int) enemyX, (int) enemyY);
            if (tile.getType() == DOOR) {
                return ((Tiles.DoorTile) tile).getConnectedRoom1();
            }
            else {
                return tile.getId();        
            }
        }

        public long getReactTime() {
            return reactTime;
        }

        public void setReactTime(long reactTime) {
            this.reactTime = reactTime;
        }

        public void chaseReact() {
            if (enemyState == STAND || enemyState == PATROL 
                    || (enemyState == WALK && enemyRestoreState == PATROL)) {
                
                enemyState = CHASE_REACT;
                reactTime = Util.getTimeMs() + Util.random(250, 750);
            }
        }

        public void attackReact() {
            if (enemyState == CHASE
                    || (enemyState == WALK && enemyRestoreState == CHASE)) {
                
                enemyState = ATTACK_REACT;
                reactTime = Util.getTimeMs() + Util.random(50, 500);
                use360View = false;
                animationFrame = 0;
            }
        }

        public boolean isAbleToOpenDoor() {
            return ableToOpenDoor;
        }

        public void setAbleToOpenDoor(boolean ableToOpenDoor) {
            this.ableToOpenDoor = ableToOpenDoor;
        }

        public boolean isKeepDirection() {
            return keepDirection;
        }

        public void setKeepDirection(boolean keepDirection) {
            this.keepDirection = keepDirection;
        }

        public long getAttackTime() {
            return attackTime;
        }

        public void setAttackTime(long attackTime) {
            this.attackTime = attackTime;
        }

        public double getMinAttackDistance() {
            return minAttackDistance;
        }

        public void setMinAttackDistance(double minAttackDistance) {
            this.minAttackDistance = minAttackDistance;
        }
        
        private static final double PI_DIV_BY_4 = Math.PI / 4;
        
        public int calculate360ViewSpriteIdOffset() {
            double dx = enemyX - Player.getPlayerX();
            double dy = enemyY - Player.getPlayerY();
            double playerEnemyAngle = Math.atan2(dy, dx);
            double enemyAngle = direction.angle + Math.PI;
            double dif = (enemyAngle - playerEnemyAngle 
                                + PI_DIV_BY_4 * 0.5) % (2 * Math.PI);
            
            if (dif < 0) {
                dif = 2 * Math.PI + dif;
            }
            int billboardIndex = (int) (dif / PI_DIV_BY_4);
            return billboardIndex;        
        }
        
        public double calculateDistanceFromPlayer() {
            double dx = Player.getPlayerX() - enemyX;
            double dy = Player.getPlayerY() - enemyY;
            return Math.hypot(dx, dy);
        }

        public double calculateRelativeAngleBetweenPlayer() {
            double dx = Player.getPlayerX() - enemyX;
            double dy = Player.getPlayerY() - enemyY;
            return Math.atan2(dy, dx);
        }

        private final RaycastResult raycastResult 
                                        = new GameMap.RaycastResult();
    
        public boolean canSeePlayer(boolean playerInFront) {
            int viewDir = calculate360ViewSpriteIdOffset();
            if (playerInFront && viewDir != 0 && viewDir != 1 && viewDir != 7) {
                return false;
            }
            double srcX = enemyX;
            double srcY = enemyY;
            double srcAngle = calculateRelativeAngleBetweenPlayer();
            GameMap.performRaycastDDA(srcX, srcY, srcAngle, raycastResult);
            double distFromPlayer = calculateDistanceFromPlayer();
            double compDist = distFromPlayer - PLAYER_RADIUS - 0.000001;
            return raycastResult.getDistance() >= compDist;
        }

        public void kill() {
            enemyState = DYING;
            use360View = false;
            animationFrame = 0;

            int dropCol = (int) enemyTargetX;
            int dropRow = (int) enemyTargetY;
            Tile tile = GameMap.getTile(dropCol, dropRow);
            tile.setBlockMovement(false);
            
            // drop item ?
            if (dropItem != null) { // && tile.getType() == FLOOR) {
                dropItem.setLocation(dropCol, dropRow);
                GameMap.getObjs()[dropRow][dropCol] = dropItem;
            }
            
            // add score points
            Wolf3DGame.addScorePoints(scorePoints);
        }

        public String[] getHaltSoundIds() {
            return haltSoundIds;
        }

        public void setHaltSoundIds(String[] haltSoundIds) {
            this.haltSoundIds = haltSoundIds;
        }

        public String[] getAttackSoundIds() {
            return attackSoundIds;
        }

        public void setAttackSoundIds(String[] attackSoundIds) {
            this.attackSoundIds = attackSoundIds;
        }

        public String[] getDeathSoundIds() {
            return deathSoundIds;
        }

        public void setDeathSoundIds(String[] deathSoundIds) {
            this.deathSoundIds = deathSoundIds;
        }
       
        private String getRandomStringArrayValue(String[] stringArray) {
            if (stringArray == null) {
                return "";
            }
            return stringArray[Util.random(0, stringArray.length - 1)];
        }
        
        public String getRandomHaltSoundId() {
            return getRandomStringArrayValue(haltSoundIds);
        }

        public String getRandomAttackSoundId() {
            return getRandomStringArrayValue(attackSoundIds);
        }

        public String getRandomDeathSoundId() {
            return getRandomStringArrayValue(deathSoundIds);
        }

        public CollectableObj getDropItem() {
            return dropItem;
        }

        public void setDropItem(CollectableObj dropItem) {
            this.dropItem = dropItem;
        }

        public int getScorePoints() {
            return scorePoints;
        }

        public void setScorePoints(int scorePoints) {
            this.scorePoints = scorePoints;
        }
        
    }
    
    private static final Map<Integer, String> OBJ_KEYS = new HashMap<>();
    
    static {
        for (Object keyObj : Resource.PROPERTIES.keySet()) {
            String key = (String) keyObj;
            if (key.toUpperCase().startsWith("OBJ_")) {
                String[] values = Resource.getProperty(key).trim().split(",");
                for (String value : values) {
                    int objIndex = Integer.parseInt(value.trim());
                    OBJ_KEYS.put(objIndex, key);
                }
            }
        }
    }
    
    public static class EndGameObj extends Obj {

        public EndGameObj(int id, int col, int row) {
            super(id, col, row, END_GAME, false);
        }
        
    }

    // END_SPRITE_WALK = 514-517
    // END_SPRITE_CELEBRATE = 518-521
    public static class EndPlayerObj extends Obj {
        
        public static enum EndPlayerState { 
                            NONE, WALK, CELEBRATE, END }
        
        private final double endPlayerSpeed = 0.025;
        private double endPlayerX;
        private double endPlayerY;
        private double endPlayerTargetX;
        private double endPlayerTargetY;
        private EndPlayerState state;
        private final EndPlayerAnimation animationInfo;
        private double animationFrame;
        private long endStartTime;
        private boolean end;
        
        public EndPlayerObj() {
            super(0, 0, 0, END_PLAYER, false);
            state = EndPlayerState.NONE;
            animationInfo = Resource.extractEndPlayerAnimationFrames();
        }

        public double getEndPlayerX() {
            return endPlayerX;
        }

        public double getEndPlayerY() {
            return endPlayerY;
        }
        
        public void setEndPlayerLocation(int col, int row) {
            endPlayerX = col + 0.5;
            endPlayerY = row + 0.5;
            endPlayerTargetX = endPlayerX;
            endPlayerTargetY = endPlayerY;
        }
        
        public EndPlayerState getState() {
            return state;
        }

        public EndPlayerAnimation getAnimationInfo() {
            return animationInfo;
        }

        @Override
        public boolean isDrawable() {
            return state != EndPlayerState.NONE;
        }
        
        @Override
        public BufferedImage getSprite() {
            List<Integer> frames = animationInfo.getFrames(state);
            if (frames != null) {
                int spriteIndex = frames.get((int) animationFrame);
                sprite = Resource.getSprite(spriteIndex);
            }
            return sprite;
        }

        public boolean isEnd() {
            return end;
        }
        
        public void fixedUpdate() {
            if (end) {
                return;
            }
            
            switch (state) {
                case WALK -> updateWalk();
                case CELEBRATE -> updateCelebrate();
                case END -> updateEnd();
            }
        }

        private void updateWalk() {
            double dx = endPlayerTargetX - endPlayerX;
            double dy = endPlayerTargetY - endPlayerY;
            double dist = Math.hypot(dx, dy);
            double vx = endPlayerSpeed * dx / dist;
            double vy = endPlayerSpeed * dy / dist;
            if (dist <= endPlayerSpeed) {
                endPlayerX = endPlayerTargetX;
                endPlayerY = endPlayerTargetY;
                Audio.playSound("YEAH");
                animationFrame = 0;
                state = EndPlayerState.CELEBRATE;
            }
            else {
                endPlayerX += vx;
                endPlayerY += vy;
                animationFrame = (int) (System.nanoTime() * 0.000000005) % 4;
            }
        }

        private void updateCelebrate() {
            animationFrame += 0.1;
            int framesSize = animationInfo.getFrames(state).size();
            if (animationFrame >= framesSize) {
                animationFrame = framesSize - 1;
                endStartTime = Util.getTimeMs() + 3000;
                state = EndPlayerState.END;
            }
        }
        
        private void updateEnd() {
            if (Util.getTimeMs() > endStartTime) {
                end = true;
            }
        }
        
        public void walkTo(int col, int row) {
            endPlayerTargetX = col + 0.5;
            endPlayerTargetY = row + 0.5;
            state = EndPlayerState.WALK;
        }

        public void celebrate() {
            animationFrame = 0;
            state = EndPlayerState.CELEBRATE;
        }
        
    }
    
    public static Obj createObj(int objId, int col, int row) {
        String objKey = OBJ_KEYS.get(objId);
        if (objKey == null) {
            return null;
        }
        int sprOffset = Resource.getIntProperty("OFFSET_OBJID_TO_SPRITEID");
        String[] objInfo = objKey.trim().toUpperCase().split("_");
        Obj obj = null;
        switch (objInfo[1]) {
            case "PLAYER" -> {
                PlayerStartObj playerObj = new PlayerStartObj(objId, col, row);
                playerObj.setDirection(CardinalDirection.valueOf(objInfo[2]));
                obj = playerObj;
            }
            case "DECORATION" -> {
                DecorationObj decoratObj = new DecorationObj(objId, col, row);
                decoratObj.setBlockMovement(objInfo[2].equals("BLOCKING"));
                decoratObj.setDrawFirst(objInfo[2].equals("DRAWFIRST"));
                decoratObj.setSprite(Resource.getSprite(objId + sprOffset));
                obj = decoratObj;
            }
            case "COLLECT" -> {
                CollectableObj collectObj = new CollectableObj(objId, col, row);
                collectObj.setObjId(ObjId.valueOf(objInfo[3]));
                collectObj.setCollectableType(
                                    CollectableType.valueOf(objInfo[2]));
                
                collectObj.setSprite(Resource.getSprite(objId + sprOffset));
                obj = collectObj;
            }
            case "PATH" -> {
                PathObj pathObj = new PathObj(objId, col, row);
                pathObj.setDirection(CardinalDirection.valueOf(objInfo[2]));
                obj = pathObj;
            }
            case "SECRETDOOR" -> {
                obj = new SecretDoorObj(objId, col, row);
            }
            case "ENDGAME" -> {
                obj = new EndGameObj(objId, col, row);
            }
            case "ENEMY" -> {
                EnemyObj enemyObj = new EnemyObj(objId, col, row);
                enemyObj.setEnemyType(EnemyType.valueOf(objInfo[2]));
                enemyObj.setEnemyState(EnemyState.valueOf(objInfo[3]));
                enemyObj.setDifficulty(Integer.parseInt(objInfo[4]));
                enemyObj.setDirection(CardinalDirection.valueOf(objInfo[5]));
                enemyObj.setBoss(objInfo[3].equals("BOSS"));
                enemyObj.setEnemyLife(enemyObj.isBoss() ? 500 : 100);
                // patrol speed
                String patrolSpeedPropName = "INFO_ENEMY_" 
                        + enemyObj.getEnemyType() + "_PATROL_SPEED";

                double patrolSpeed
                        = Resource.getDoubleProperty(patrolSpeedPropName);
        
                enemyObj.setEnemyPatrolSpeed(patrolSpeed);

                // chase speed
                String chaseSpeedPropName = "INFO_ENEMY_" 
                        + enemyObj.getEnemyType() + "_CHASE_SPEED";

                double chaseSpeed
                        = Resource.getDoubleProperty(chaseSpeedPropName);
        
                enemyObj.setEnemyChaseSpeed(chaseSpeed);
                
                // min attack distance
                String minAtkDistPropName = "INFO_ENEMY_" 
                        + enemyObj.getEnemyType() + "_MIN_ATTACK_DIST";
                
                double minAttackDistance 
                        = Resource.getDoubleProperty(minAtkDistPropName);
                
                enemyObj.setMinAttackDistance(minAttackDistance);

                // halt sound ids
                String haltSoundsPropName = "INFO_ENEMY_" 
                        + enemyObj.getEnemyType() + "_HALT_SOUND_ID";
                
                String[] haltSoundIds
                        = Resource.getStringArrayProperty(haltSoundsPropName);
                
                enemyObj.setHaltSoundIds(haltSoundIds);

                // attack sound ids
                String attackSoundsPropName = "INFO_ENEMY_" 
                        + enemyObj.getEnemyType() + "_ATTACK_SOUND_ID";
                
                String[] attackSoundIds
                        = Resource.getStringArrayProperty(attackSoundsPropName);
                
                enemyObj.setAttackSoundIds(attackSoundIds);
                
                // death sound ids
                String deathSoundsPropName = "INFO_ENEMY_" 
                        + enemyObj.getEnemyType() + "_DEATH_SOUND_ID";
                
                String[] deathSoundIds
                        = Resource.getStringArrayProperty(deathSoundsPropName);
                
                enemyObj.setDeathSoundIds(deathSoundIds);

                // drop item
                String dropItemPropName = "INFO_ENEMY_" 
                        + enemyObj.getEnemyType() + "_DROP_ITEM_ID";
                
                int dropItemId = Resource.getIntProperty(dropItemPropName);
                if (dropItemId > 0) {
                    CollectableObj collectableObj 
                            = (CollectableObj) createObj(dropItemId, 0, 0);

                    enemyObj.setDropItem(collectableObj);
                }

                // score points
                String scorePointsPropName = "INFO_ENEMY_" 
                        + enemyObj.getEnemyType() + "_SCORE_POINTS";
                
                int scorePoints = Resource.getIntProperty(scorePointsPropName);
                enemyObj.setScorePoints(scorePoints);
                
                enemyObj.setAbleToOpenDoor(enemyObj.getEnemyType() != DOG);
                enemyObj.setUse360View(!enemyObj.isBoss());
                // if enemy not dead, then add collision
                if (enemyObj.getEnemyState() == DEAD) {
                    enemyObj.setEnemyState(DEAD);
                    enemyObj.setUse360View(false);
                }
                obj = enemyObj;
            }
        }
        return obj;
    }    
    
}
