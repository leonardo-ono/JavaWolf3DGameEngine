package wolf3d.infra;

import java.nio.ByteBuffer;
import wolf3d.audio.IMFMusicPlayer;
import wolf3d.audio.PCMSoundPlayer;
import static wolf3d.infra.Settings.*;

/**
 * Audio class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Audio {

    private static PCMSoundPlayer pcSpeakerSoundPlayer;
    private static PCMSoundPlayer digitizedSoundPlayer;
    
    // TODO: adlib sound player

    private static String currentMusicId;
    private static IMFMusicPlayer imfMusicPlayer;
    
    public static void initialize() {
        pcSpeakerSoundPlayer = new PCMSoundPlayer(PC_SPEAKER_SOUND_PCM_FREQ);
        digitizedSoundPlayer = new PCMSoundPlayer(DIGITIZED_SOUND_PCM_FREQ);
        imfMusicPlayer = new IMFMusicPlayer(IMF_MUSIC_PLAYBACK_RATE);
    }
    
    public static void playMusic(String musicId) {
        ByteBuffer musicImfData = Resource.getMusic(musicId);
        imfMusicPlayer.play(musicImfData);
        currentMusicId = musicId;
    }

    public static String getCurrentMusicId() {
        return currentMusicId;
    }
    
    public static void playMusicByFloorNumber(int floor) {
        String musicId 
                = Resource.getProperty("INFO_FLOOR_" + floor + "_MUSIC_ID");
        
        playMusic(musicId);
    }

    public static void stopMusic() {
        imfMusicPlayer.stop();
    }
    
    // volume = 0~255
    public static void setMusicVolume(int volume) {
        imfMusicPlayer.setVolume(volume);
    }

    // volumeScale = 0.0~1.0
    public static void setMusicScaleVolume(double volumeScale) {
        imfMusicPlayer.setVolumeScale(volumeScale);
    }
    
    public static void playSound(String soundId) {
        boolean digitizedSoundOk = false;
        
        if (Resource.hasProperty("DIGITIZED_SOUND_" + soundId)) {
            int si = Resource.getIntProperty("DIGITIZED_SOUND_" + soundId);
            byte[] soundData = Resource.getDigitizedSound(si);
            if (soundData != null) {
                digitizedSoundPlayer.play(soundData);
                digitizedSoundOk = true;
            }
        }
        
        if (!digitizedSoundOk 
                && Resource.hasProperty("EFFECT_SOUND_" + soundId)) {
            
            int si = Resource.getIntProperty("EFFECT_SOUND_" + soundId);
            byte[] soundData = Resource.getPCSpeakerSound(si);
            pcSpeakerSoundPlayer.play(soundData);
        }
    }

}
