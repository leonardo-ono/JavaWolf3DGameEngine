package wolf3d.infra;

import java.util.HashSet;
import java.util.Set;
import wolf3d.infra.Objs.EnemyObj;
import static wolf3d.infra.Objs.EnemyObj.EnemyState.DEAD;
import wolf3d.infra.Tiles.DoorTile;
import static wolf3d.infra.Tiles.DoorTile.DoorState.*;

/**
 * Doors class.
 * 
 * Handle all active doors.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Doors {

    private static final double DOOR_SPEED = 0.02;
    
    private static final Set<DoorTile> activatedDoors = new HashSet<>();
    private static final Set<DoorTile> deactivatedDoors = new HashSet<>();

    public static void activateDoor(DoorTile door) {
        if ((door.getDoorState() == CLOSING 
                || door.getDoorState() == CLOSED) 
                            && canPlayerHearDoor(door)) {
            
            Audio.playSound("OPENDOOR");
        }

        door.setDoorState(OPENING);
        activatedDoors.add(door);
    }
    
    public static void fixedUpdateDoors() {
        for (DoorTile door : activatedDoors) {
            switch (door.getDoorState()) {
                case OPENING -> {
                    door.incDoorOpenRate(DOOR_SPEED);
                    if (door.getDoorOpenRate() > 1.0) {
                        door.setDoorOpenRate(1.0);
                        door.setDoorState(OPEN);
                        door.setDoorCloseTime(Util.getTimeMs() + 3000);
                    }
                    GameMap.connectRooms(
                        door.getConnectedRoom1(), door.getConnectedRoom2());
                }
                case OPEN -> {
                    boolean isDoorObstructed = door.isDoorObstructed();
                    if (Util.getTimeMs() >= door.getDoorCloseTime()
                            && !isDoorObstructed && !door.isBlockMovement()) {
                        
                        door.setDoorState(CLOSING);
                        if (canPlayerHearDoor(door)) {
                            Audio.playSound("CLOSEDOOR");
                        }
                    }
                    EnemyObj enemyObj = door.getObstructingEnemy();
                    if (isDoorObstructed && enemyObj != null 
                                && enemyObj.getEnemyState() == DEAD) {
                        
                        deactivatedDoors.add(door);
                    }
                }
                case CLOSING -> {
                    door.incDoorOpenRate(-DOOR_SPEED);
                    if (door.getDoorOpenRate() < 0.0) {
                        door.setDoorOpenRate(0.0);
                        door.setObstructingEnemy(null);
                        door.setDoorState(CLOSED);
                        deactivatedDoors.add(door);
                        GameMap.disconnectRooms(
                            door.getConnectedRoom1(), door.getConnectedRoom2());
                    }
                }
            }
        }
        if (!deactivatedDoors.isEmpty()) {
            activatedDoors.removeAll(deactivatedDoors);
            deactivatedDoors.clear();
        }
    } 

    private static boolean canPlayerHearDoor(DoorTile door) {
        int playerRoomId = Player.getCurrentRoomId();
        int soundRoomId1 = door.getConnectedRoom1();
        int soundRoomId2 = door.getConnectedRoom2();
        boolean c1 = playerRoomId == soundRoomId1;
        boolean c2 = playerRoomId == soundRoomId2;
        boolean c3 = GameMap.isRoomConnected(playerRoomId, soundRoomId1); 
        boolean c4 = GameMap.isRoomConnected(playerRoomId, soundRoomId2);
        return c1 || c2 || c3 || c4;
    }
        
}
