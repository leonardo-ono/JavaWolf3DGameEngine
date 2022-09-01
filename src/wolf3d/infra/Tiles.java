package wolf3d.infra;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import wolf3d.infra.Objs.EnemyObj;
import static wolf3d.infra.Objs.EnemyObj.ENEMY_RADIUS;
import static wolf3d.infra.Objs.EnemyObj.EnemyState.*;
import static wolf3d.infra.Player.PLAYER_RADIUS;
import static wolf3d.infra.Tiles.DoorTile.DoorState.*;
import wolf3d.infra.Tiles.DoorTile.DoorKey;
import static wolf3d.infra.Tiles.TileType.*;

/**
 * Tiles class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Tiles {

    public static final int TILE_HORIZONTAL = 0;
    public static final int TILE_VERTICAL = 1;

    public static enum TileType { WALL, FLOOR, DOOR, SECRET_DOOR, ELEVATOR }

    public static class Tile {

        protected final int id;
        protected int col;
        protected int row;
        protected final TileType type;
        protected final boolean blockRaycast;
        protected boolean blockMovement;

        public Tile(int id, int col, int row, TileType type
                        , boolean blockRaycast, boolean blockMovement) {
            
            this.id = id;
            this.col = col;
            this.row = row;
            this.type = type;
            this.blockRaycast = blockRaycast;
            this.blockMovement = blockMovement;
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
        
        public TileType getType() {
            return type;
        }

        public boolean isBlockRaycast() {
            return blockRaycast;
        }

        public boolean isBlockMovement() {
            return blockMovement;
        }

        public void setBlockMovement(boolean blockMovement) {
            this.blockMovement = blockMovement;
        }

        public boolean isTileObstructedByPlayer() {
            double px = Player.getPlayerX();
            double py = Player.getPlayerY();
            double pr = PLAYER_RADIUS;
            return isTileObstructed(px, py, pr);
        }

        protected boolean isTileObstructed(
                            double ex, double ey, double eRadius) {
            
            for (int py = -1; py <= 1; py++) {
                for (int px = -1; px <= 1; px++) {
                    int eCol = (int) (ex + px * eRadius);
                    int eRow = (int) (ey + py * eRadius);
                    if (eCol == col && eRow == row) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        public BufferedImage getTexture(int side) {
            return null;
        }
        
    }
    
    public static class WallTile extends Tile {
        
        private BufferedImage textureHorizontal;
        private BufferedImage textureVertical;
        private boolean elevator;
        
        public WallTile(int id, int col, int row) {
            super(id, col, row, WALL, true, true);
        }

        protected WallTile(int id, int col, int row, TileType type) {
            super(id, col, row, type, true, true);
        }

        public BufferedImage getTextureHorizontal() {
            return textureHorizontal;
        }

        public void setTextureHorizontal(BufferedImage textureHorizontal) {
            this.textureHorizontal = textureHorizontal;
        }

        public BufferedImage getTextureVertical() {
            return textureVertical;
        }

        public void setTextureVertical(BufferedImage textureVertical) {
            this.textureVertical = textureVertical;
        }

        public boolean isElevator() {
            return elevator;
        }

        public void setElevator(boolean elevator) {
            this.elevator = elevator;
        }

        @Override
        public BufferedImage getTexture(int side) {
            return switch (side) {
                case TILE_HORIZONTAL -> textureHorizontal;
                case TILE_VERTICAL -> textureVertical;
                default -> null;
            };
        }
        
    }
    
    public static class DoorTile extends Tile {

        public static enum DoorState { OPENING, OPEN, CLOSING, CLOSED }
        public static enum DoorKey { SILVER, GOLD }
        
        private double doorOpenRate = 0.0; // 0.0~1.0
        private DoorState doorState = CLOSED;
        private long doorCloseTime;
        
        private BufferedImage texture;
        private final int doorSide; // 0=horizontal, 1=vertical
        private boolean locked;
        private final DoorKey requiredKey;
        
        private int connectedRoom1;
        private int connectedRoom2;
        
        private EnemyObj obstructingEnemy;
        
        public DoorTile(int id, int col, int row
                        , int doorSide, boolean locked, DoorKey requiredKey) {
            
            super(id, col, row, DOOR, false, false);
            this.doorSide = doorSide;
            this.locked = locked;
            this.requiredKey = requiredKey;
        }

        public BufferedImage getTexture() {
            return texture;
        }

        public void setTexture(BufferedImage texture) {
            this.texture = texture;
        }

        public double getDoorOpenRate() {
            return doorOpenRate;
        }

        public void setDoorOpenRate(double doorOpenRate) {
            this.doorOpenRate = doorOpenRate;
        }

        public void incDoorOpenRate(double inc) {
            this.doorOpenRate += inc;
        }

        public DoorState getDoorState() {
            return doorState;
        }

        public void setDoorState(DoorState doorState) {
            this.doorState = doorState;
        }

        public long getDoorCloseTime() {
            return doorCloseTime;
        }

        public void setDoorCloseTime(long doorCloseTime) {
            this.doorCloseTime = doorCloseTime;
        }

        public int getDoorSide() {
            return doorSide;
        }

        public boolean isLocked() {
            return locked;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }

        public DoorKey getRequiredKey() {
            return requiredKey;
        }

        @Override
        public boolean isBlockMovement() {
            boolean doorObstructed = false;
            if (isDoorObstructedByEnemy()) {
                doorObstructed = obstructingEnemy.getEnemyState() != DEAD;
            }
            return doorState != OPEN 
                    || doorObstructed || super.isBlockMovement();
        }
        
        public void setConnectedRooms(int r1, int r2) {
            this.connectedRoom1 = r1;
            this.connectedRoom2 = r2;
        }
        
        public int getConnectedRoom1() {
            return connectedRoom1;
        }

        public int getConnectedRoom2() {
            return connectedRoom2;
        }

        public EnemyObj getObstructingEnemy() {
            return obstructingEnemy;
        }

        public void setObstructingEnemy(EnemyObj obstructingEnemy) {
            this.obstructingEnemy = obstructingEnemy;
        }

        public boolean isDoorObstructed() {
            boolean doorObstructed = isTileObstructedByPlayer();
            doorObstructed |= isDoorObstructedByEnemy();
            return doorObstructed;
        }

        public boolean isDoorObstructedByEnemy() {
            if (obstructingEnemy == null) return false;
            double ex = obstructingEnemy.getEnemyX();
            double ey = obstructingEnemy.getEnemyY();
            double er = ENEMY_RADIUS;
            return isTileObstructed(ex, ey, er);
        }

        @Override
        public BufferedImage getTexture(int side) {
            return texture;
        }
        
    }
    
    public static class SecretDoorTile extends Tile {

        public static enum SecretDoorState { CLOSED, OPENING, OPEN }
        public static enum DoorKey { SILVER, GOLD }
        private double secretDoorOpenRate = 0.0; // 0.0~1.0
        private SecretDoorState secretDoorState = SecretDoorState.CLOSED;
        private int doorSide; // 0=horizontal, 1=vertical
        private final Tile tile;
        private CardinalDirection pushDirection;
        private int movementCount = 2;
        private final FloorTile[] floorTiles;
        
        public SecretDoorTile(Tile tile) {
            super(tile.id, tile.col, tile.row
                    , TileType.SECRET_DOOR, false, true);
            
            this.tile = tile;
            this.floorTiles = new FloorTile[] { 
                                new FloorTile(tile.id, tile.col, tile.row)
                                , new FloorTile(tile.id, tile.col, tile.row) };
        }

        public Tile getTile() {
            return tile;
        }

        public double getSecretDoorOpenRate() {
            return secretDoorOpenRate;
        }

        public void setSecretDoorOpenRate(double doorOpenRate) {
            this.secretDoorOpenRate = doorOpenRate;
        }

        public void incSecretDoorOpenRate(double inc) {
            this.secretDoorOpenRate += inc;
        }

        public SecretDoorState getSecretDoorState() {
            return secretDoorState;
        }

        public void setSecretDoorState(SecretDoorState secretDoorState) {
            this.secretDoorState = secretDoorState;
        }

        public int getDoorSide() {
            return doorSide;
        }

        public void setDoorSide(int doorSide) {
            this.doorSide = doorSide;
        }

        @Override
        public BufferedImage getTexture(int side) {
            return tile.getTexture(side);
        }

        public CardinalDirection getPushDirection() {
            return pushDirection;
        }

        public void setPushDirection(CardinalDirection pushDirection) {
            this.pushDirection = pushDirection;
        }

        public int getMovementCount() {
            return movementCount;
        }

        public void setMovementCount(int movementCount) {
            this.movementCount = movementCount;
        }
        
        public void decMovementCount() {
            movementCount--;
        }

        public FloorTile getFloorTile(int index) {
            return floorTiles[index];
        }
        
    }

    public static class FloorTile extends Tile {
        
        private boolean ambush;
        private boolean secret;

        public FloorTile(int id, int col, int row) {
            super(id, col, row, FLOOR, false, false);
        }

        public boolean isAmbush() {
            return ambush;
        }

        public void setAmbush(boolean ambush) {
            this.ambush = ambush;
        }

        public boolean isSecret() {
            return secret;
        }

        public void setSecret(boolean secret) {
            this.secret = secret;
        }
        
    }
    
    private static final Map<Integer, String> TILE_KEYS = new HashMap<>();
    
    static {
        for (Object keyObj : Resource.PROPERTIES.keySet()) {
            String key = (String) keyObj;
            if (key.toUpperCase().startsWith("TILE_")) {
                String[] values = Resource.getProperty(key).trim().split("-");
                int startIndex = Integer.parseInt(values[0]);
                int endIndex = Integer.parseInt(values[1]);
                for (int i = startIndex; i <= endIndex; i++) {
                    TILE_KEYS.put(i, key);
                }
            }
        }
    }
    
    public static Tile createTile(int tileId, int col, int row) {
        int elevatorTileId = Resource.getIntProperty("ELEVATOR_TILE_ID");
        String tileKey = TILE_KEYS.get(tileId);
        String[] tileInfo = tileKey.trim().toUpperCase().split("_");
        Tile tile = null;
        switch (tileInfo[1]) {
            case "WALL" -> {
                WallTile wallTile = new WallTile(tileId, col, row);
                int wallIndex = (tileId - 1) * 2;
                wallTile.setTextureHorizontal(
                        Resource.getWallTexture(wallIndex, 0));
                
                wallTile.setTextureVertical(
                        Resource.getWallTexture(wallIndex, 1));
                
                // check if elevator
                wallTile.setElevator(tileId == elevatorTileId);

                tile = wallTile;
            }
            case "DOOR" -> {
                boolean locked = tileInfo[2].equals("LOCKED");
                DoorKey requiredKey = null;
                if (locked) requiredKey = DoorKey.valueOf(tileInfo[3]);
                int doorSide = 1 - tileId % 2;
                DoorTile doorTile = new DoorTile(
                            tileId, col, row, doorSide, locked, requiredKey);
                
                int doorId = Resource.getIntProperty("TEXTURE_DOOR_LOCKED");
                if (!locked && tileInfo[3].equals("ELEVATOR")) {
                    doorId = Resource.getIntProperty("TEXTURE_DOOR_ELEVATOR");
                }
                else if (!locked) {
                    doorId = Resource.getIntProperty("TEXTURE_DOOR_DEFAULT");
                }
                doorTile.setTexture(Resource.getWallTexture(doorId, doorSide));
                tile = doorTile;
            }
            case "FLOOR" -> {
                FloorTile floorTile = new FloorTile(tileId, col, row);
                floorTile.setAmbush(tileInfo[2].equals("AMBUSH"));
                floorTile.setSecret(tileInfo[2].equals("SECRET"));
                tile = floorTile;
            }
        }
        return tile;
    }

}
