package wolf3d.infra;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import wolf3d.infra.Objs.CollectableObj;
import static wolf3d.infra.Objs.CollectableObj.ObjId.*;
import wolf3d.infra.Objs.EnemyObj;
import wolf3d.infra.Objs.Obj;
import wolf3d.infra.Objs.ObjType;
import static wolf3d.infra.Objs.ObjType.*;
import wolf3d.infra.Objs.PlayerStartObj;
import wolf3d.infra.Tiles.DoorTile;
import static wolf3d.infra.Tiles.*;
import wolf3d.infra.Tiles.Tile;
import static wolf3d.infra.Tiles.TileType.*;

/**
 * GameCanvas class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class GameMap {

    // used to propagate player gunshot sounds to alert enemies
    private static boolean[][] connectedRooms;

    public static final int MAP_ROWS = 64;
    public static final int MAP_COLS = 64;
    
    private static Tile[][] tiles;
    private static Obj[][] objs;
    
    private static int totalEnemies;
    private static int totalSecrets;
    private static int totalTreasures;

    public static int getTotalEnemies() {
        return totalEnemies;
    }

    public static int getTotalSecrets() {
        return totalSecrets;
    }

    public static int getTotalTreasures() {
        return totalTreasures;
    }
    
    public static void loadByFloorNumber(int floor) {
        loadByMapIndex(floor - 1);
    }
    
    private static void loadByMapIndex(int mapIndex) {
        Enemies.clear();
        totalEnemies = 0;
        totalSecrets = 0;
        totalTreasures = 0;
        connectedRooms = new boolean[256][256];
        tiles = new Tile[MAP_ROWS][MAP_COLS];
        objs = new Obj[MAP_ROWS][MAP_COLS];
        int[][] map = Resource.getMap(mapIndex);
        int[] mapTiles = map[0];
        int[] mapObjs = map[1];
        List<DoorTile> doorTiles = new ArrayList<>();
        List<SecretDoorTile> secretDoorTiles = new ArrayList<>();
        for (int r = 0; r < MAP_ROWS; r++) {
            for (int c = 0; c < MAP_COLS; c++) {
                int mi = r * MAP_COLS + c;
                        
                int tileId = mapTiles[mi];
                Tile tile = Tiles.createTile(tileId, c, r);
                tiles[r][c] = tile;
                if (tile.getType() == DOOR) doorTiles.add((DoorTile) tile);
                
                int objId = mapObjs[mi];
                Obj obj = Objs.createObj(objId, c, r);
                if (obj != null && obj.getType() == ObjType.SECRET_DOOR) {
                    SecretDoorTile secretDoorTile = new SecretDoorTile(tile);
                    tiles[r][c] = secretDoorTile;
                    secretDoorTiles.add(secretDoorTile);
                    totalSecrets++;
                }
                else if (obj != null && obj.getType() == PLAYER_START) {
                    PlayerStartObj playerObj = (PlayerStartObj) obj;
                    double px = playerObj.getCol() + 0.5;
                    double py = playerObj.getRow() + 0.5;
                    double pa = playerObj.getDirection().angle;
                    Player.reset(px, py, pa);
                }
                else if (obj != null && obj.getType() == ENEMY) {
                    EnemyObj enemyObj = (EnemyObj) obj;
                    int difficulty = Wolf3DGame.getDifficulty().ordinal() + 1;
                    if (enemyObj.getDifficulty() <= difficulty) {
                        Enemies.addEnemy(enemyObj);
                    }
                    totalEnemies++;
                }
                else {
                    objs[r][c] = obj;
                    // treasure
                    if (obj != null && obj.getType() == COLLECTABLE) {
                        CollectableObj collObj = (CollectableObj) obj;
                        if (collObj.getObjId() == BONUS1 
                                || collObj.getObjId() == BONUS2
                                || collObj.getObjId() == BONUS3
                                || collObj.getObjId() == BONUS4) {
                            
                            totalTreasures++;
                        }
                    }
                }
                
                // make the tile blocked for movement according to obj
                if (obj != null && obj.isBlockMovement()) {
                    tile.setBlockMovement(true);
                }
            }
        }
        
        // fix secret doors orientation
        secretDoorTiles.forEach(secretDoor -> {
            int dc = secretDoor.getCol();
            int dr = secretDoor.getRow();
            Tile tu = getTile(dc, dr - 1);
            Tile td = getTile(dc, dr + 1);
            Tile tl = getTile(dc - 1, dr);
            Tile tr = getTile(dc + 1, dr);
            boolean wu = tu != null && tu.isBlockMovement();
            boolean wd = td != null && td.isBlockMovement();
            boolean wl = tl != null && tl.isBlockMovement();
            boolean wr = tr != null && tr.isBlockMovement();
            if (wu && wd) {
                secretDoor.setDoorSide(TILE_VERTICAL);
            }
            else if (wl && wr) {
                secretDoor.setDoorSide(TILE_HORIZONTAL);
            }
            // ensure it can detect the correct orientation for 2 secret doors 
            // very close to each other, for example: floor 10, location (33,16)
            else if (wu || wd) {
                secretDoor.setDoorSide(TILE_VERTICAL);
            }
            else if (wl || wr) {
                secretDoor.setDoorSide(TILE_HORIZONTAL);
            }
            else {
                throw new RuntimeException("secret door invalid orientation !");
            }
        });
        
        // find all connected rooms for each door
        doorTiles.forEach(door -> {
            Tile ft1 = null;
            Tile ft2 = null;
            if (door.getDoorSide() == TILE_HORIZONTAL) {
                ft1 = getTile(door.getCol(), door.getRow() - 1);
                ft2 = getTile(door.getCol(), door.getRow() + 1);
            }
            else if (door.getDoorSide() == TILE_VERTICAL) {
                ft1 = getTile(door.getCol() - 1, door.getRow());
                ft2 = getTile(door.getCol() + 1, door.getRow());
            }
            
            if (ft1 != null && ft1.getType() == FLOOR 
                    && ft2 != null && ft2.getType() == FLOOR) {
                
                door.setConnectedRooms(ft1.getId(), ft2.getId());
            }
        });
        
        // fix door side walls
        int doorSideIndex = Resource.getIntProperty("TEXTURE_DOOR_SIDE");
        BufferedImage doorSideTextureH 
                = Resource.getWallTexture(doorSideIndex, TILE_HORIZONTAL);
        
        BufferedImage doorSideTextureV 
                = Resource.getWallTexture(doorSideIndex, TILE_VERTICAL);
        
        for (int r = 0; r < MAP_ROWS; r++) {
            for (int c = 0; c < MAP_COLS; c++) {
                Tile tile = tiles[r][c];
                if (tile.getType() == DOOR) {
                    DoorTile doorTile = (Tiles.DoorTile) tile;
                    if (doorTile.getDoorSide() == TILE_HORIZONTAL) {
                        Tile tileW = getTile(c - 1, r);
                        Tile tileE = getTile(c + 1, r);
                        if (tileW != null && tileW.getType() == WALL) {
                            WallTile wallTile = (WallTile) tileW;
                            wallTile.setTextureVertical(doorSideTextureV);
                        }
                        if (tileE != null && tileE.getType() == WALL) {
                            WallTile wallTile = (WallTile) tileE;
                            wallTile.setTextureVertical(doorSideTextureV);
                        }
                    }
                    else if (doorTile.getDoorSide() == TILE_VERTICAL) {
                        Tile tileN = getTile(c, r - 1);
                        Tile tileS = getTile(c, r + 1);
                        if (tileN != null && tileN.getType() == WALL) {
                            WallTile wallTile = (WallTile) tileN;
                            wallTile.setTextureHorizontal(doorSideTextureH);
                        }
                        if (tileS != null && tileS.getType() == WALL) {
                            WallTile wallTile = (WallTile) tileS;
                            wallTile.setTextureHorizontal(doorSideTextureH);
                        }
                    }
                }
            }
        }
    }

    public static Tile getTile(int col, int row) {
        if (col < 0 || col > MAP_COLS - 1 || row < 0 || row > MAP_ROWS - 1) {
            return null;
        }
        return tiles[row][col];
    }
    
    public static Tile[][] getTiles() {
        return tiles;
    }

    public static Obj getObj(int col, int row) {
        if (col < 0 || col > MAP_COLS - 1 || row < 0 || row > MAP_ROWS - 1) {
            return null;
        }
        return objs[row][col];
    }

    public static Obj[][] getObjs() {
        return objs;
    }

    public static void connectRooms(int r1, int r2) {
        connectedRooms[r1][r2] = true;
        connectedRooms[r2][r1] = true;
    }

    public static void disconnectRooms(int r1, int r2) {
        connectedRooms[r1][r2] = false;
        connectedRooms[r2][r1] = false;
    }
    
    public static boolean isRoomConnected(int r1, int r2) {
        return connectedRooms[r1][r2];
    }
    
    // --- raycasting ---
    
    private static final Set<Obj> objsDuringRaycast = new HashSet<>();

    public static Set<Obj> getObjsDuringRaycast() {
        return objsDuringRaycast;
    }

    public static final int MAX_RAYCAST_DISTANCE = 1000;

    public static class RaycastResult {
        
        private boolean intersecting;
        private Tile tile;
        private final Point rayCell = new Point();
        private final Point.Double intersectionPoint = new Point.Double();
        private double distance;
        private int wallSide;
        private double textureOffset;

        public boolean isIntersecting() {
            return intersecting;
        }

        public Tile getTile() {
            return tile;
        }

        public Point getRayCell() {
            return rayCell;
        }

        public Point2D.Double getIntersectionPoint() {
            return intersectionPoint;
        }

        public double getDistance() {
            return distance;
        }

        public int getWallSide() {
            return wallSide;
        }

        public double getTextureOffset() {
            return textureOffset;
        }
        
    }
    
    private static final double DIV_BY_ZERO_REPLACE = 0.000000001;
    
    // refs.: javidx9 - https://www.youtube.com/watch?v=NbSee-XM7WA&t=815s
    //        https://lodev.org/cgtutor/raycasting.html
    //
    // Raycastresult.wallSide: 0 - intersection horizontal wall
    //                         1 - intersection vertical wall
    public static void performRaycastDDA(double srcX, double srcY, double angle
                                                    , RaycastResult result) {
        
        performRaycastDDA(srcX, srcY, angle, result, MAX_RAYCAST_DISTANCE);
    }
    
    public static void performRaycastDDA(double srcX, double srcY, double angle
                            , RaycastResult result, double maxRayDistance) {

        double dy = Math.sin(angle);
        double dx = Math.cos(angle);
        dx = dx == 0 ? DIV_BY_ZERO_REPLACE : dx;
        dy = dy == 0 ? DIV_BY_ZERO_REPLACE : dy;
        int dxSign = (int) Math.signum(dx);
        int dySign = (int) Math.signum(dy);
        result.rayCell.setLocation((int) srcX, (int) srcY);
        double startDy = result.rayCell.y + dySign * 0.5 + 0.5 - srcY;
        double startDx = result.rayCell.x + dxSign * 0.5 + 0.5 - srcX;
        double distDx = Math.abs(1 / dx);
        double distDy = Math.abs(1 / dy);
        double totalDistDx = distDx * dxSign * startDx;
        double totalDistDy = distDy * dySign * startDy;
        result.intersecting = false;
        result.distance = 0;
        while (result.distance < maxRayDistance) {
            if (totalDistDx < totalDistDy) {
                result.rayCell.x += dxSign;
                result.distance = totalDistDx;
                totalDistDx += distDx;
                result.wallSide = 1;
            }
            else {
                result.rayCell.y += dySign;
                result.distance = totalDistDy;
                totalDistDy += distDy;
                result.wallSide = 0;
            }

            result.textureOffset = 0;
            result.tile = GameMap.getTile(result.rayCell.x, result.rayCell.y);
            if (result.tile == null) return;

            // collect visible objs during this raycasting
            Obj obj = GameMap.getObj(result.rayCell.x, result.rayCell.y);
            if (obj != null && obj.isDrawable() 
                                && !objsDuringRaycast.contains(obj)) {
                
                objsDuringRaycast.add(obj);
            }
            
            // check door
            if (result.tile.getType() == DOOR) {
                DoorTile doorTile = ((DoorTile) result.tile);
                boolean isDoorHoriz = doorTile.getDoorSide() == TILE_HORIZONTAL;
                result.distance += (isDoorHoriz ? distDy : distDx) * 0.5;
                double ipx = srcX + result.distance * dx;
                double ipy = srcY + result.distance * dy;

                double doorOpenRate = doorTile.getDoorOpenRate();
                boolean doorVisible = ipy - (int) ipy >= doorOpenRate; 
                double textureOffset = -doorOpenRate;
                if (isDoorHoriz) {
                    doorVisible = ipx - (int) ipx >= doorOpenRate;
                    doorOpenRate = -doorOpenRate;
                    textureOffset = doorOpenRate;
                }

                if ((int) ipx == result.rayCell.x 
                        && (int) ipy == result.rayCell.y  && doorVisible) {

                    result.textureOffset = textureOffset;
                    result.intersectionPoint.setLocation(ipx, ipy);
                    result.intersecting = true;
                    break;
                }
            }
            
            // check secret door
            if (result.tile.getType() == TileType.SECRET_DOOR) {
                SecretDoorTile secretDoorTile = ((SecretDoorTile) result.tile);
                boolean isDoorHoriz 
                        = secretDoorTile.getDoorSide() == TILE_HORIZONTAL;
                
                double sdor = secretDoorTile.getSecretDoorOpenRate(); 
                double openRate = sdor + 0.000001;
                result.distance += (isDoorHoriz ? distDy : distDx) * openRate;
                double ipx = srcX + result.distance * dx;
                double ipy = srcY + result.distance * dy;

                if ((int) ipx == result.rayCell.x 
                                && (int) ipy == result.rayCell.y) {

                    result.textureOffset = 0;
                    result.intersectionPoint.setLocation(ipx, ipy);
                    result.intersecting = true;
                    break;
                }
            }
            
            if (result.tile.isBlockRaycast()) {
                double ipx = srcX + result.distance * dx;
                double ipy = srcY + result.distance * dy;
                result.intersectionPoint.setLocation(ipx, ipy);
                result.intersecting = true;
                break;
            }
        }        
    }
    
}
