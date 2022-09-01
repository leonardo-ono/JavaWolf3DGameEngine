package wolf3d.asset.loader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static wolf3d.infra.Settings.PC_SPEAKER_SOUND_PCM_FREQ;

/**
 * AUDIOTLoader class.
 * 
 * This is responsible for loading the PC Speaker sounds, Adlib sounds and
 * IMF musics present in the AUDIOHED and AUDIOT files.
 * 
 * No compression methods are used in these files, so the audio data can 
 * be extracted directly.
 * 
 * In this implementation, the PC Speaker sounds are converted to 
 * PCM digitized sounds and played through the PCMSoundPlayer class.
 * 
 * Note: the digitized sounds are located in the VSWAP file.
 * 
 * Reference: 
 * https://moddingwiki.shikadi.net/wiki/AudioT_Format
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class AUDIOTLoader {
    
    private static final Map<Integer, ByteBuffer> 
                                    ADLIB_SOUNDS = new HashMap<>();

    private static final Map<Integer, byte[]> 
                                    PC_SPEAKER_SOUNDS = new HashMap<>();

    private static final Map<Integer, ByteBuffer> MUSICS = new HashMap<>();
    
    public static void load(
            String path, String audioHeadRes, String audioTRes
                , int pcSpeakerStartIndex, int pcSpeakerEndIndex
                , int adlibSoundStartIndex, int adlibSoundEndIndex
                , int musicStartIndex, int musicEndIndex) throws Exception {
        
        try (
            InputStream audioHeadIs = new FileInputStream(path + audioHeadRes);
            InputStream audioTIs = new FileInputStream(path + audioTRes); 
        ) {
            // read audio header data
            ByteBuffer headData = ByteBuffer.wrap(audioHeadIs.readAllBytes());
            headData.order(ByteOrder.LITTLE_ENDIAN);

            // read audioT data
            ByteBuffer audioTData = ByteBuffer.wrap(audioTIs.readAllBytes());
            audioTData.order(ByteOrder.LITTLE_ENDIAN);
            
            // retrieve the offsets for each resource
            int[] offsets = new int[headData.limit() / 4];
            int offsetsIndex = 0;
            while (headData.hasRemaining()) {
                offsets[offsetsIndex++] = headData.getInt();
            }
            
            extractPCSpeakerSounds(
                pcSpeakerStartIndex, pcSpeakerEndIndex, offsets, audioTData);
            
            extractAdlibSounds(
                adlibSoundStartIndex, adlibSoundEndIndex, offsets, audioTData);            
            
            extractIMFMusics(
                musicStartIndex, musicEndIndex, offsets, audioTData);
        } 
        catch (Exception ex) {
            throw new Exception(
                    "Could not load AUDIOT resources properly!", ex);
        }
    }

    private static void extractPCSpeakerSounds(
                int pcSpeakerStartIndex, int pcSpeakerEndIndex
                    , int[] offsets, ByteBuffer audioTData) throws Exception {
        
        for (int i = pcSpeakerStartIndex; i <= pcSpeakerEndIndex; i++) {
            int chunkDataOffset = offsets[i];
            audioTData.position(chunkDataOffset);
            int length = audioTData.getInt();
            int priority = audioTData.getShort() & 0xffff;
            byte[] soundData = new byte[length];
            audioTData.get(soundData);
            byte terminator = audioTData.get(); // must be zero
            if (terminator != 0) {
                throw new Exception("PCSpeaker sound data is corrupted!");
            }
            // convert to 44100Hz unsigned 8 bits mono PCM
            byte[] pcm = convertPCSpeakerSoundToPCM(
                            soundData, PC_SPEAKER_SOUND_PCM_FREQ);
            
            // 2 last bytes form a UINTLE16 indicating the sound priority
            pcm[pcm.length - 2] = (byte) (priority & 0xff);
            pcm[pcm.length - 1] = (byte) ((priority >> 8) & 0xff);
            PC_SPEAKER_SOUNDS.put(i - pcSpeakerStartIndex, pcm);
        }
    }

    private static void extractAdlibSounds(
                    int adlibSoundStartIndex, int adlibSoundEndIndex
                                , int[] offsets, ByteBuffer audioTData) {

        for (int i = adlibSoundStartIndex; i <= adlibSoundEndIndex; i++) {
            int chunkDataOffset = offsets[i];
            int chunkDataLength = offsets[i + 1] - offsets[i];
            ByteBuffer soundData
                    = audioTData.slice(chunkDataOffset, chunkDataLength);
            
            soundData.order(ByteOrder.LITTLE_ENDIAN);
            ADLIB_SOUNDS.put(i - adlibSoundStartIndex, soundData);
        }
    }

    private static void extractIMFMusics(
                        int musicStartIndex, int musicEndIndex, 
                                int[] offsets, ByteBuffer audioTData) {
        
        for (int i = musicStartIndex; i <= musicEndIndex; i++) {
            int chunkDataOffset = offsets[i];
            int chunkDataLength = offsets[i + 1] - offsets[i];
            ByteBuffer musicData
                    = audioTData.slice(chunkDataOffset, chunkDataLength);
            
            musicData.order(ByteOrder.LITTLE_ENDIAN);
            MUSICS.put(i - musicStartIndex, musicData);
        }
    }
    
    private static final int PC_BASE_TIMER = 1193181;
    private static final int PC_VOLUME = 15;
    private static final int PC_RATE = 140;

    private static byte[] convertPCSpeakerSoundToPCM(byte[] src, long hertz) {
        List<Byte> dstBytes = new ArrayList<>();
        int sign = -1;
        long tone = 0;
        long i = 0;
        long phaseLength = 0;
        long phaseTic = 0;
        long samplesPerByte = hertz / PC_RATE;
        long srcLength = src.length;
        int srcIndex = 0;
        while (srcLength-- > 0) {
            tone = (src[srcIndex++] & 0xff) * 60;
            phaseLength = (hertz * tone) / (2 * PC_BASE_TIMER);
            for (i = 0; i < samplesPerByte; i++) {
                if (tone > 0) {
                    dstBytes.add((byte) (128 + sign * PC_VOLUME));
                    if (phaseTic++ >= phaseLength) {
                        sign = -sign;
                        phaseTic = 0;
                    }
                } else {
                    phaseTic = 0;
                    dstBytes.add((byte) 128);
                }
            }
        }
        byte[] bytesArr = new byte[dstBytes.size() + 2];
        int index = 0;
        for (byte b : dstBytes) {
            bytesArr[index++] = b;
        }
        return bytesArr;
    }
    
    public static byte[] getPcSpeakerSound(int soundIndex) {
        return PC_SPEAKER_SOUNDS.get(soundIndex);
    }

    public static ByteBuffer getAdlibSound(int soundIndex) {
        return ADLIB_SOUNDS.get(soundIndex);
    }
    
    public static ByteBuffer getMusic(int musicIndex) {
        return MUSICS.get(musicIndex);
    }

}
