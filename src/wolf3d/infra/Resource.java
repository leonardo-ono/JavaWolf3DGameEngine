package wolf3d.infra;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import wolf3d.asset.loader.AUDIOTLoader;
import wolf3d.asset.loader.MAPLoader;
import wolf3d.asset.loader.VGAGRAPHLoader;
import wolf3d.asset.loader.VGAGRAPHLoader.VGAGRAPHFont;
import wolf3d.asset.loader.VSWAPLoader;
import wolf3d.infra.Objs.EndPlayerObj.EndPlayerState;
import wolf3d.infra.Objs.EnemyObj.EnemyState;
import static wolf3d.infra.Objs.EnemyObj.EnemyState.ATTACK_REACT;
import static wolf3d.infra.Objs.EnemyObj.EnemyState.CHASE;
import static wolf3d.infra.Objs.EnemyObj.EnemyState.CHASE_REACT;
import static wolf3d.infra.Objs.EnemyObj.EnemyState.PATROL;
import static wolf3d.infra.Objs.EnemyObj.EnemyState.WALK;
import wolf3d.infra.Objs.EnemyObj.EnemyType;

/**
 * Resource class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Resource {
    
    public static final String USER_DIR;
    public static final Properties PROPERTIES;

    private static final Map<EnemyType, EnemyAnimation> enemyAnimationFrames 
                                                            = new HashMap<>();
    
    static {
        // user directory
        String userDirTmp = System.getProperty("user.dir");
        if (!userDirTmp.endsWith(File.separator)) {
            userDirTmp += File.separator;
        }
        USER_DIR = userDirTmp;
        
        // constant values
        PROPERTIES = new Properties();
        try {
            PROPERTIES.load(Resource.class.getResourceAsStream("/res/wl1.def"));
        } catch (IOException ex) {
            String logClassName = Resource.class.getName();
            Logger.getLogger(logClassName).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        
        extractEnemiesSpriteAnimationFrames();
    }
    
    public static void initialize() {
        try {
            String path = USER_DIR;
            
            // load AUDIOT (PC Speaker sounds, Adlib sounds and IMF musics)
            String audioHeadRes = getProperty("AUDIO_HEAD_RES");
            String audioTRes = getProperty("AUDIO_T_RES");
            int pcSpStartIndex = getIntProperty("PC_SPEAKER_SOUND_START_INDEX");
            int pcSpEndIndex = getIntProperty("PC_SPEAKER_SOUND_END_INDEX");
            int adlSoundStartIndex = getIntProperty("ADLIB_SOUND_START_INDEX");
            int adlSoundEndIndex = getIntProperty("ADLIB_SOUND_END_INDEX");
            int musicStartIndex = getIntProperty("MUSIC_START_INDEX");
            int musicEndIndex = getIntProperty("MUSIC_END_INDEX");
            AUDIOTLoader.load(path, audioHeadRes, audioTRes
                                , pcSpStartIndex, pcSpEndIndex
                                , adlSoundStartIndex, adlSoundEndIndex
                                , musicStartIndex, musicEndIndex);
            
            // load VSWAP (wall textures, sprites and digitized sounds)
            String vswapRes = Resource.getProperty("VSWAP_RES");
            VSWAPLoader.load(path, vswapRes);
            
            fixDigitizedSoundsPriority();
            
            // load VGAGRAPH (PIC's)
            String vgaHeadRes = Resource.getProperty("VGA_HEAD_RES");
            String vgaDictRes = Resource.getProperty("VGA_DICT_RES");
            String vgaGraphRes = Resource.getProperty("VGA_GRAPH_RES");
            VGAGRAPHLoader.load(path, vgaHeadRes, vgaDictRes, vgaGraphRes);
            
            // load GAMEMAPS
            String mapHeadRes = Resource.getProperty("MAP_HEAD_RES");
            String gameMapsRes = Resource.getProperty("GAME_MAPS_RES");
            MAPLoader.load(path, mapHeadRes, gameMapsRes);
        } catch (Exception ex) {
            String logClassName = Resource.class.getName();
            Logger.getLogger(logClassName).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }        
    }
    
    // fix digitized sounds priority using the same priority of adlib sounds.
    private static void fixDigitizedSoundsPriority() {
        for (Object digitizedSoundId : PROPERTIES.keySet()) {
            if (digitizedSoundId.toString().startsWith("DIGITIZED_SOUND_")) {
                int digitizedSoundIndex 
                                = getIntProperty(digitizedSoundId.toString());
                
                byte[] digitizedSound = getDigitizedSound(digitizedSoundIndex);
                if (digitizedSound == null) continue;
                String soundId = digitizedSoundId.toString().substring(16);
                String pcSpeakerSoundId = "EFFECT_SOUND_" + soundId;
                if (getProperty(pcSpeakerSoundId) == null) continue;
                int effectSoundIndex = getIntProperty(pcSpeakerSoundId);
                ByteBuffer adlibSound 
                            = AUDIOTLoader.getAdlibSound(effectSoundIndex);
                
                if (adlibSound == null) continue;
                digitizedSound[digitizedSound.length - 2] = adlibSound.get(4);
                digitizedSound[digitizedSound.length - 1] = adlibSound.get(5);
            }
        }
    }
    
    public static boolean hasProperty(String propertyName) {
        return PROPERTIES.containsKey(propertyName);
    }
    
    public static String getProperty(String propertyName) {
        return PROPERTIES.getProperty(propertyName);
    }

    public static int getIntProperty(String propertyName) {
        return Integer.parseInt(PROPERTIES.getProperty(propertyName).trim());
    }

    public static double getDoubleProperty(String propertyName) {
        return Double.parseDouble(PROPERTIES.getProperty(propertyName).trim());
    }

    public static String[] getStringArrayProperty(String propertyName) {
        String values = PROPERTIES.getProperty(propertyName).trim();
        if (values != null) {
            return values.split(",");
        }
        return null;
    }

    public static ByteBuffer getMusic(String musicId) {
        int musicIndex = Resource.getIntProperty("MUSIC_" + musicId);
        return AUDIOTLoader.getMusic(musicIndex);
    }

    public static byte[] getPCSpeakerSound(int soundIndex) {
        return AUDIOTLoader.getPcSpeakerSound(soundIndex);
    }

    public static byte[] getDigitizedSound(int soundIndex) {
        return VSWAPLoader.getDigitizedSound(soundIndex);
    }

    public static BufferedImage getPic(String picId) {
        int picIndex = getIntProperty("PIC_" + picId);
        return VGAGRAPHLoader.getPic(picIndex);
    }

    public static BufferedImage getPic(int picIndex) {
        return VGAGRAPHLoader.getPic(picIndex);
    }

    // side: 0=horizontal / 1=vertical (1=darker)
    public static BufferedImage getWallTexture(int textureId, int side) {
        return VSWAPLoader.getWallTexture(textureId + side);
    }

    public static BufferedImage getSprite(int sprId) {
        return VSWAPLoader.getSprite(sprId);
    }

    public static int[][] getMap(int mapIndex) {
        return MAPLoader.getMap(mapIndex);
    }
    
    public static VGAGRAPHFont getFont(String fontId) {
        return VGAGRAPHLoader.getFont(fontId);
    }

    // --- enemy sprite animation frames for each state ---
    
    public static final class EnemyAnimation {
        
        private List<Integer> stand;
        private List<Integer> walk;
        private List<Integer> dying;
        private List<Integer> hit;
        private List<Integer> dead;
        private List<Integer> attack;

        public List<Integer> getFrames(EnemyState state) {
            return switch(state) {
                case STAND -> stand;
                case ATTACK, ATTACK_REACT -> attack;
                case WALK, PATROL, CHASE, CHASE_REACT -> walk;
                case HIT -> hit;
                case DYING -> dying;
                case DEAD -> dead;
                default -> null;
            };
        }
        
    }   
    
    //    SPRITE_ENEMY_GUARD_STAND = 50-57
    //    SPRITE_ENEMY_GUARD_WALK = 58-89
    //    SPRITE_ENEMY_GUARD_DYING = 90-93
    //    SPRITE_ENEMY_GUARD_HIT = 94
    //    SPRITE_ENEMY_GUARD_DEAD = 95
    //    SPRITE_ENEMY_GUARD_ATTACK = 96
    private static void extractEnemiesSpriteAnimationFrames() {
        for (EnemyType enemyType : EnemyType.values()) {
            EnemyAnimation enemyAnimation = new EnemyAnimation();
            for (Object property : Resource.PROPERTIES.keySet()) {
                String spriteId = "SPRITE_ENEMY_" + enemyType + "_";
                if (property.toString().startsWith(spriteId)) {
                    String value = getProperty(property.toString());
                    String state = property.toString().replace(spriteId, "");
                    switch (state) {
                        case "STAND" -> {
                            enemyAnimation.stand = getSpriteIndices(value);
                        }
                        case "WALK" -> {
                            enemyAnimation.walk = getSpriteIndices(value);
                        }
                        case "DYING" -> {
                            enemyAnimation.dying = getSpriteIndices(value);
                        }
                        case "HIT" -> {
                            enemyAnimation.hit = getSpriteIndices(value);
                        }
                        case "DEAD" -> {
                            enemyAnimation.dead = getSpriteIndices(value);
                        }
                        case "ATTACK" -> {
                            enemyAnimation.attack = getSpriteIndices(value);
                        }
                    }
                }
            }
            enemyAnimationFrames.put(enemyType, enemyAnimation);
        }
    }
    
    private static List<Integer> getSpriteIndices(String value) {
        List<Integer> indicesList = new ArrayList<>();
        String[] indices = value.split(",");
        for (String indice : indices) {
            if (indice.contains("-")) {
                String[] beginEnd = indice.split("-");
                int beginIndex = Integer.parseInt(beginEnd[0]);
                int endIndex = Integer.parseInt(beginEnd[1]);
                for (int i = beginIndex; i <= endIndex; i++) {
                    indicesList.add(i);
                }
            }
            else {
                int index = Integer.parseInt(indice);
                indicesList.add(index);
            }
        }
        return indicesList;
    }
    
    public static EnemyAnimation getEnemyAnimationsFrames(EnemyType enemyType) {
        return enemyAnimationFrames.get(enemyType);
    }
    
    // --- end player animations ---
    
    public static final class EndPlayerAnimation {
        
        private List<Integer> walk;
        private List<Integer> celebrate;

        public List<Integer> getFrames(EndPlayerState state) {
            return switch(state) {
                case WALK -> walk;
                case CELEBRATE -> celebrate;
                default -> null;
            };
        }
        
    }
    
    //    END_SPRITE_WALK = 514-517
    //    END_SPRITE_CELEBRATE = 518-521
    public static EndPlayerAnimation extractEndPlayerAnimationFrames() {
        EndPlayerAnimation endPlayerAnimation = new EndPlayerAnimation();
        for (Object property : Resource.PROPERTIES.keySet()) {
            String spriteId = "END_SPRITE_";
            if (property.toString().startsWith(spriteId)) {
                String value = getProperty(property.toString());
                String state = property.toString().replace(spriteId, "");
                switch (state) {
                    case "WALK" -> {
                        endPlayerAnimation.walk = getSpriteIndices(value);
                    }
                    case "CELEBRATE" -> {
                        endPlayerAnimation.celebrate = getSpriteIndices(value);
                    }
                }
            }
        }
        return endPlayerAnimation;
    }
    
    // --- floor / ceiling colors ---
    
    public static Color getFloorColor() {
        String encodedColor = Resource.getProperty("INFO_FLOOR_COLOR");
        return Util.getColor(encodedColor);
    }

    public static Color getCeilingColorByFloorNumber(int floor) {
        String encodedColor = Resource.getProperty(
                                "INFO_FLOOR_" + floor + "_CEILING_COLOR");
        
        return Util.getColor(encodedColor);
    }

}
