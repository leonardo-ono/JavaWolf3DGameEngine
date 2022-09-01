package wolf3d.audio;

import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * PCMSoundPlayer class.
 * 
 * This allows to play:
 * 
 * - PC Speaker sounds present in the AUDIOT file and it's converted 
 *   to PCM sounds in the AudioResource class.
 * 
 * - Digitized sounds present in the VSWAP file.
 * 
 * 2 last bytes of sound data (byte[]) form a UINTLE16 indicating the sound 
 * priority. Any sound will interrupt a sound of equal or lower priority.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class PCMSoundPlayer {
    
    private static final int SEND_DATA_SIZE = 500;
    
    private final AudioFormat audioFormat; 
    private SourceDataLine sourceDataLine;
    private Thread soundThread;
    private boolean initialized;
    private byte[] newSoundData;
    private byte[] currentSoundData;
    private int currentSoundIndex;
    
    public PCMSoundPlayer(int sampleRate) {
        audioFormat = new AudioFormat(sampleRate, 8, 1, false, false);
        start();
    }
    
    private void start() {
        try {
            sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
            sourceDataLine.open();
            sourceDataLine.start();
            initialized = true;
            soundThread = new Thread(new SoundPlayback());
            soundThread.start();
        } catch (LineUnavailableException ex) {
            Logger.getLogger(PCMSoundPlayer.class.getName())
                        .log(java.util.logging.Level.SEVERE, null, ex);
            
            initialized = false;
        }
    }
    
    public void dispose() {
        sourceDataLine.close();
        initialized = false;
    }
    
    private static final int PRIORITY_SIZE = 2;
    
    private class SoundPlayback implements Runnable {

        @Override
        public void run() {
            while (initialized) {
                synchronized (this) {
                    if (newSoundData != null) {
                        if (checkPriority(newSoundData, currentSoundData)) {
                            currentSoundIndex = 0;
                            currentSoundData = newSoundData;
                            sourceDataLine.flush();
                        }
                        newSoundData = null;
                    }
                }
                if (currentSoundData != null) {
                    
                    int len1 = SEND_DATA_SIZE;
                    int len2 = currentSoundData.length 
                                    - PRIORITY_SIZE - currentSoundIndex;
                    
                    int smallestLen = Math.min(len1, len2);
                    sourceDataLine.write(
                            currentSoundData, currentSoundIndex, smallestLen);
                    
                    currentSoundIndex += SEND_DATA_SIZE;
                    if (currentSoundIndex 
                            >= currentSoundData.length - PRIORITY_SIZE) {
                        
                        currentSoundData = null;
                    }
                }
                else if (sourceDataLine.available() 
                        == sourceDataLine.getBufferSize()) {
                    
                    sourceDataLine.flush();
                }
                
                try {
                    Thread.sleep(1000 / 90);
                } catch (InterruptedException ex) { }
            }
        }

    }
    
    // does sound s1 have more priority than s2 ?
    private static boolean checkPriority(byte[] s1, byte[] s2) {
        if (s2 == null) return true;
        int s1Priority = s1[s1.length - 2] & 0xff;
        s1Priority += (s1[s1.length - 1] & 0xff) << 8;
        int s2Priority = s2[s2.length - 2] & 0xff;
        s2Priority += (s2[s2.length - 1] & 0xff) << 8;
        return s1Priority >= s2Priority;
    }
    
    public synchronized void play(byte[] soundData) {
        newSoundData = soundData;
    }

}
