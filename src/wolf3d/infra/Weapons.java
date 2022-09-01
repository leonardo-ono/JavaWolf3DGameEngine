package wolf3d.infra;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static wolf3d.infra.Settings.*;
import static wolf3d.infra.Weapons.WeaponType.*;

/**
 * Weapons class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Weapons {

    public static enum WeaponType { KNIFE, PISTOL, MACHINE, GATLING };
    
    public static class Weapon {
        
        private final WeaponType type;
        private final double minHitDistance;
        private final long waitUntilNextAttackTimeMs; // in milli seconds
        private final double animationSpeed;
        private final BufferedImage hudPic;
        private final List<BufferedImage> attackAnimationFrames;
        private boolean own;
        private double frameIndex;
        private boolean attacking;
        private long lastAttackTime;
        private final String soundId;
        
        public Weapon(WeaponType type, double minHitDistance
            , long waitTime, double animationSpeed, BufferedImage hudPic
                                , List<BufferedImage> attackAnimationFrames
                                                            , String soundId) {
            
            this.minHitDistance = minHitDistance;
            this.type = type;
            this.waitUntilNextAttackTimeMs = waitTime;
            this.animationSpeed = animationSpeed;
            this.hudPic = hudPic;
            this.attackAnimationFrames = attackAnimationFrames;
            this.soundId = soundId;
        }

        public WeaponType getType() {
            return type;
        }

        public double getMinHitDistance() {
            return minHitDistance;
        }

        public long getWaitTime() {
            return waitUntilNextAttackTimeMs;
        }

        public double getAnimationSpeed() {
            return animationSpeed;
        }

        public BufferedImage getHudPic() {
            return hudPic;
        }

        public List<BufferedImage> getAttackAnimationFrames() {
            return attackAnimationFrames;
        }

        public boolean isOwn() {
            return own;
        }

        public void setOwn(boolean own) {
            this.own = own;
        }

        public double getFrameIndex() {
            return frameIndex;
        }

        public void setFrameIndex(double frameIndex) {
            this.frameIndex = frameIndex;
        }

        public boolean isAttacking() {
            return attacking;
        }

        public void setAttacking(boolean attacking) {
            this.attacking = attacking;
        }

        public long getLastAttackTime() {
            return lastAttackTime;
        }

        public void setLastAttackTime(long lastAttackTime) {
            this.lastAttackTime = lastAttackTime;
        }

        private BufferedImage getSprite() {
            return attackAnimationFrames.get((int) frameIndex);
        }

        public String getSoundId() {
            return soundId;
        }
        
        public void attack() {
            attacking = true;
            lastAttackTime = Util.getTimeMs();
        }
        
        public void reset() {
            attacking = false;
            frameIndex = 0;
        }
        
    }
    
    private static final Map<WeaponType, Weapon> playerWeapons 
                                                        = new HashMap<>();
    
    //    PIC_HUD_WEAPON_KNIFE = 103
    //    PIC_HUD_WEAPON_PISTOL = 104
    //    PIC_HUD_WEAPON_MACHINE = 105
    //    PIC_HUD_WEAPON_GATLING = 106
    //    SPRITE_WEAPON_KNIFE_ANIMATIONS = 522-526
    //    SPRITE_WEAPON_PISTOL_ANIMATIONS = 527-531 
    //    SPRITE_WEAPON_MACHINE_ANIMATIONS = 532-536
    //    SPRITE_WEAPON_GATLING_ANIMATIONS = 537-541   
    //    INFO_WEAPON_KNIFE_MINHITDISTANCE = 2
    //    INFO_WEAPON_KNIFE_WAITTIME = 1000
    //    INFO_WEAPON_PISTOL_MINHITDISTANCE = 15
    //    INFO_WEAPON_PISTOL_WAITTIME = 750
    //    INFO_WEAPON_MACHINE_MINHITDISTANCE = 15
    //    INFO_WEAPON_MACHINE_WAITTIME = 500
    //    INFO_WEAPON_GATLING_MINHITDISTANCE = 15
    //    INFO_WEAPON_GATLING_WAITTIME = 250
    public static void createPlayerWeapons() {
        playerWeapons.clear();
        for (WeaponType weaponType : WeaponType.values()) {
            String picHudId = "HUD_WEAPON_" + weaponType;
            BufferedImage hudPic = Resource.getPic(picHudId);
            String framesId = "SPRITE_WEAPON_" + weaponType + "_ANIMATIONS";
            String frames = Resource.getProperty(framesId);
            String[] beginEnd = frames.split("-");
            List<BufferedImage> attackAnimationFrames = new ArrayList<>();
            int begin = Integer.parseInt(beginEnd[0]);
            int end = Integer.parseInt(beginEnd[1]);
            for (int i = begin; i <= end; i++) {
                BufferedImage sprite = Resource.getSprite(i);
                attackAnimationFrames.add(sprite);
            }
            String minHitDistId 
                    = "INFO_WEAPON_" + weaponType + "_MINHITDISTANCE";

            String animSpeedId 
                    = "INFO_WEAPON_" + weaponType + "_ANIMATIONSPEED";
            
            String waitTimeId 
                    = "INFO_WEAPON_" + weaponType + "_WAITTIME";

            String soundKey
                    = "INFO_WEAPON_" + weaponType + "_SOUNDID";

            double minHitDistance = Resource.getDoubleProperty(minHitDistId);
            double animationSpeed = Resource.getDoubleProperty(animSpeedId);
            long waitTime = Resource.getIntProperty(waitTimeId);
            String soundId = Resource.getProperty(soundKey);
            
            Weapon weapon = new Weapon(weaponType, minHitDistance, waitTime
                    , animationSpeed, hudPic, attackAnimationFrames, soundId);
            
            playerWeapons.put(weaponType, weapon);
        }
    }
    
    private static int ammo;
    private static Weapon currentPlayerWeapon;

    public static void reset() {
        ammo = 8;
        playerWeapons.values().forEach(weapon -> {
            WeaponType weaponType = weapon.getType();
            weapon.reset();
            weapon.setOwn(weaponType == KNIFE || weaponType == PISTOL);
            
            // debug: start player with all weapons
            if (Wolf3DGame.playerOverrideLocation != null) { 
                weapon.setOwn(true);
                ammo = 99;
            }
        });
        currentPlayerWeapon = playerWeapons.get(PISTOL);
        // player starts with knife and pistol weapons
    }

    public static int getAmmo() {
        return ammo;
    }

    public static void setAmmo(int ammo) {
        Weapons.ammo = ammo;
    }

    public static void incAmmo(int inc) {
        Weapons.ammo += inc;
        if (Weapons.ammo > 99) {
            Weapons.ammo = 99;
        }
    }

    public static void decAmmo() {
        Weapons.ammo--;
        if (Weapons.ammo < 0) {
            Weapons.ammo = 0;
        }
    }

    public static Weapon getPlayerWeapon(WeaponType weaponType) {
        return playerWeapons.get(weaponType);
    }
    
    public static Weapon getCurrentPlayerWeapon() {
        return currentPlayerWeapon;
    }

    public static void setCurrentPlayerWeapon(WeaponType weaponType) {
        Weapon weapon = playerWeapons.get(weaponType);
        if (!weapon.isOwn()) return;
        if (weapon.getType() != KNIFE && ammo <= 0) return;
        currentPlayerWeapon = weapon;
        currentPlayerWeapon.attacking = false;
        currentPlayerWeapon.frameIndex = 0;
    }
    
    public static void selectBestWeapon() {
        if (currentPlayerWeapon.getType() != KNIFE && ammo > 0) {
            return;
        }
        currentPlayerWeapon = playerWeapons.get(KNIFE);
        if (ammo > 0) {
            Weapon gatling = playerWeapons.get(GATLING);
            Weapon machine = playerWeapons.get(MACHINE);
            Weapon pistol = playerWeapons.get(PISTOL);
            if (pistol.isOwn()) currentPlayerWeapon =  pistol;
            if (machine.isOwn()) currentPlayerWeapon =  machine;
            if (gatling.isOwn()) currentPlayerWeapon = gatling;
            currentPlayerWeapon.attacking = false;
            currentPlayerWeapon.frameIndex = 0;
        }
    }
    
    public static boolean canAttack() {
        if (currentPlayerWeapon.isAttacking()) {
            return false;
        }
        else if (Util.getTimeMs() < currentPlayerWeapon.getLastAttackTime() 
                                        + currentPlayerWeapon.getWaitTime()) {
            
            return false;
        }
        else if (currentPlayerWeapon.getType() == KNIFE) {
            return true;
        }
        else {
            return ammo > 0;
        }
    }
    
    public static void fixedUpdate() {
        updateCurrentWeaponAnimation();
    }

    private static void updateCurrentWeaponAnimation() {
        WeaponType weaponType = currentPlayerWeapon.getType();
        if (!currentPlayerWeapon.isAttacking()
                && (weaponType == MACHINE || weaponType == GATLING)
                    && currentPlayerWeapon.frameIndex != 0
                        && !Input.isKeyPressed(KEY_PLAYER_FIRE)) {
            
            currentPlayerWeapon.frameIndex 
                                += currentPlayerWeapon.animationSpeed;
            
            if (currentPlayerWeapon.frameIndex >= 5) {
                currentPlayerWeapon.frameIndex = 0;
                currentPlayerWeapon.attacking = false;
            }   
        }
        
        if (currentPlayerWeapon.isAttacking()) {
            currentPlayerWeapon.frameIndex 
                                += currentPlayerWeapon.animationSpeed;
            
            if (weaponType == MACHINE || weaponType == GATLING) {
                if (currentPlayerWeapon.frameIndex >= 4) {
                    currentPlayerWeapon.frameIndex = 2;
                    currentPlayerWeapon.attacking = false;
                }   
            }
            else if (currentPlayerWeapon.frameIndex >= 5) {
                currentPlayerWeapon.frameIndex = 0;
                currentPlayerWeapon.attacking = false;
            }
        }
    }
    
    // draw weapon sprite on top
    public static void draw(Graphics2D g) {
        int offsety = CANVAS_HEIGHT / 2 - 40;
        g.drawImage(currentPlayerWeapon.getSprite()
            , CANVAS_WIDTH / 2 - 64, offsety + 100 - 128, 128, 128, null);
    }

}
