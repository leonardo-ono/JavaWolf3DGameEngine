package wolf3d.infra;

import java.util.HashSet;
import java.util.Set;
import wolf3d.infra.Tiles.FloorTile;
import wolf3d.infra.Tiles.SecretDoorTile;
import wolf3d.infra.Tiles.SecretDoorTile.SecretDoorState;

/**
 * SecretDoors class.
 * 
 * Handle all active secret doors.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class SecretDoors {

    private static final double SECRET_DOOR_SPEED = 0.005;
    
    private static final Set<SecretDoorTile> activatedSecretDoors 
                                                            = new HashSet<>();
    
    private static final Set<SecretDoorTile> deactivatedSecretDoors 
                                                            = new HashSet<>();

    public static void activateSecretDoor(SecretDoorTile secretDoor) {
        if (secretDoor.getSecretDoorState() == SecretDoorState.CLOSED) {
            Audio.playSound("PUSHWALL");
            secretDoor.setSecretDoorState(SecretDoorState.OPENING);
            activatedSecretDoors.add(secretDoor);
            Wolf3DGame.incSecretDoorsFoundCount();
        }
    }
    
    public static void fixedUpdateSecretDoors() {
        for (SecretDoorTile secretDoor : activatedSecretDoors) {
            switch (secretDoor.getSecretDoorState()) {
                case OPENING -> {
                    secretDoor.incSecretDoorOpenRate(SECRET_DOOR_SPEED);
                    if (secretDoor.getSecretDoorOpenRate() > 1.0) {
                        secretDoor.setSecretDoorOpenRate(0);
                        secretDoor.decMovementCount();
                        
                        int sdc = secretDoor.getCol();
                        int sdr = secretDoor.getRow();
                        FloorTile floorTile = secretDoor
                                .getFloorTile(secretDoor.getMovementCount());
                        
                        floorTile.setLocation(sdc, sdr);
                        GameMap.getTiles()[sdr][sdc] = floorTile;
                        sdc += secretDoor.getPushDirection().dx;
                        sdr += secretDoor.getPushDirection().dy;
                        GameMap.getTiles()[sdr][sdc] = secretDoor;
                        secretDoor.setLocation(sdc, sdr);
                        if (secretDoor.getMovementCount() == 0) {
                            GameMap.getTiles()[sdr][sdc] = secretDoor.getTile();
                            secretDoor.setSecretDoorState(SecretDoorState.OPEN);
                            deactivatedSecretDoors.add(secretDoor);
                        }
                        // push again
                        else {
                            Audio.playSound("PUSHWALL");
                        }
                    }
                }
            }
        }
        if (!deactivatedSecretDoors.isEmpty()) {
            activatedSecretDoors.removeAll(deactivatedSecretDoors);
            deactivatedSecretDoors.clear();
        }
    } 
        
}
