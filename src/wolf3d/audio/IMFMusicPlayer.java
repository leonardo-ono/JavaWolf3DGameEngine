package wolf3d.audio;

import java.awt.Point;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

/**
 * IMFMusicPlayer class.
 * 
 * Allows to play Wolfenstein-3D IMF musics natively in java 
 * through the built-in midi synthesizer.
 * 
 * This will try to select automatically the best general midi instrument 
 * for each channel according to the opl2ToMidiInstruments table.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class IMFMusicPlayer {
    
    // { reg, channel, instrumentIndex, ... }
    private final int opl2InstrumentRegisters[] = {
        32, 0, 0, 33, 1, 0, 34, 2, 0, 35, 0, 1, 36, 1, 1, 37, 2, 1, 
        40, 3, 0, 41, 4, 0, 42, 5, 0, 43, 3, 1, 44, 4, 1, 45, 5, 1, 
        48, 6, 0, 49, 7, 0, 50, 8, 0, 51, 6, 1, 52, 7, 1, 53, 8, 1, 
        64, 0, 2, 65, 1, 2, 66, 2, 2, 67, 0, 3, 68, 1, 3, 69, 2, 3, 
        72, 3, 2, 73, 4, 2, 74, 5, 2, 75, 3, 3, 76, 4, 3, 77, 5, 3, 
        80, 6, 2, 81, 7, 2, 82, 8, 2, 83, 6, 3, 84, 7, 3, 85, 8, 3, 
        96, 0, 4, 97, 1, 4, 98, 2, 4, 99, 0, 5, 100, 1, 5, 101, 2, 5, 
        104, 3, 4, 105, 4, 4, 106, 5, 4, 107, 3, 5, 108, 4, 5, 109, 5, 5, 
        112, 6, 4, 113, 7, 4, 114, 8, 4, 115, 6, 5, 116, 7, 5, 117, 8, 5, 
        128, 0, 6, 129, 1, 6, 130, 2, 6, 131, 0, 7, 132, 1, 7, 133, 2, 7, 
        136, 3, 6, 137, 4, 6, 138, 5, 6, 139, 3, 7, 140, 4, 7, 141, 5, 7, 
        144, 6, 6, 145, 7, 6, 146, 8, 6, 147, 6, 7, 148, 7, 7, 149, 8, 7, 
        192, 0, 8, 193, 1, 8, 194, 2, 8, 195, 3, 8, 196, 4, 8, 197, 5, 8, 
        198, 6, 8, 199, 7, 8, 200, 8, 8, 224, 0, 9, 225, 1, 9, 226, 2, 9, 
        227, 0, 10, 228, 1, 10, 229, 2, 10, 232, 3, 9, 233, 4, 9, 234, 5, 9, 
        235, 3, 10, 236, 4, 10, 237, 5, 10, 240, 6, 9, 241, 7, 9, 242, 8, 9, 
        243, 6, 10, 244, 7, 10, 245, 8, 10
    };
    
    private final Map<Integer, Point> opl2RegistersMap = new HashMap<>();
    
    // ref.: https://github.com/ericvids/wolfmidi/blob/main/inst.txt
    // opl2ToMidiInstruments[index][] = { 20_0, 20_1, 40_0, 40_1, 60_0, 60_1, 
    // 80_0, 80_1, c0, e0_0, e0_1, isDrum, midiInstrumentNumber, transpose }
    private final int[][] opl2ToMidiInstruments = {
        { 0x0, 0x0, 0x0, 0x3f, 0xf0, 0xf7, 0xf0, 0xf7, 0xe, 0x0, 0x0, 1, 42, 0},
        { 0, 0, 11, 0, 168, 214, 76, 79, 0, 0, 0, 0, 109, -12 },
        { 0, 0, 13, 0, 232, 165, 239, 255, 6, 0, 0, 1, 36, 0 },
        { 0, 0, 64, 0, 9, 247, 83, 148, 0, 0, 0, 0, 48, -12 },
        { 1, 1, 17, 0, 240, 240, 255, 248, 10, 0, 0, 0, 76, 0 },
        { 1, 17, 79, 0, 241, 242, 83, 116, 6, 0, 0, 0, 2, 0 },
        { 1, 33, 24, 128, 212, 196, 242, 138, 10, 0, 0, 0, 37, 0 },
        { 2, 1, 41, 128, 240, 244, 117, 51, 0, 0, 0, 0, 100, 0 },
        { 2, 1, 41, 128, 245, 242, 117, 243, 0, 0, 0, 0, 9, 0 },
        { 3, 33, 143, 128, 245, 243, 85, 51, 0, 0, 0, 0, 105, 0 },
        { 5, 0, 0, 0, 240, 246, 255, 255, 14, 0, 0, 1, 46, 0 },
        { 5, 0, 0, 0, 240, 248, 119, 229, 14, 0, 0, 1, 42, 0 },
        { 5, 0, 0, 0, 240, 248, 255, 185, 14, 1, 0, 1, 40, 0 },
        { 5, 0, 0, 0, 240, 250, 119, 229, 14, 0, 0, 1, 42, 0 },
        { 6, 0, 0, 0, 85, 248, 240, 245, 14, 0, 0, 1, 38, 0 },
        { 6, 0, 0, 0, 240, 247, 240, 247, 14, 0, 0, 1, 38, 0 },
        { 6, 0, 0, 0, 244, 246, 160, 70, 14, 0, 0, 1, 46, 0 },
        { 7, 0, 0, 0, 240, 92, 240, 220, 14, 0, 0, 0, 120, 0 },
        { 7, 18, 10, 0, 242, 242, 96, 82, 0, 3, 3, 1, 51, 0 },
        { 7, 18, 79, 0, 242, 242, 96, 114, 8, 0, 0, 0, 15, 27 },
        { 17, 1, 68, 0, 248, 247, 255, 69, 0, 0, 0, 1, 36, 0 },
        { 17, 17, 10, 0, 254, 242, 4, 189, 8, 0, 0, 0, 40, 0 },
        { 17, 228, 3, 64, 130, 240, 151, 242, 8, 0, 0, 0, 73, 24 },
        { 19, 225, 77, 0, 250, 241, 17, 241, 8, 0, 0, 0, 89, 0 },
        { 22, 225, 77, 0, 250, 241, 17, 241, 8, 0, 0, 0, 89, 0 },
        { 23, 49, 192, 128, 18, 19, 65, 49, 6, 0, 0, 0, 96, 36 },
        { 32, 33, 27, 0, 99, 99, 10, 11, 12, 0, 0, 0, 58, 0 },
        { 33, 33, 21, 0, 180, 148, 76, 172, 10, 0, 0, 0, 40, 0 },
        { 33, 33, 21, 128, 211, 195, 44, 44, 10, 0, 0, 0, 33, 0 },
        { 33, 33, 22, 0, 99, 99, 14, 14, 12, 0, 0, 0, 57, 0 },
        { 33, 33, 154, 128, 83, 160, 86, 22, 14, 0, 0, 0, 59, 0 },
        { 33, 161, 22, 128, 119, 96, 143, 42, 6, 0, 0, 0, 47, 0 },
        { 33, 161, 25, 128, 119, 96, 191, 42, 6, 0, 0, 0, 47, 0 },
        { 43, 33, 202, 0, 248, 192, 229, 255, 0, 0, 0, 0, 6, 0 },
        { 44, 161, 212, 0, 249, 192, 255, 255, 0, 0, 0, 0, 6, 0 },
        { 48, 16, 144, 0, 244, 244, 73, 51, 12, 0, 0, 0, 39, -12 },
        { 48, 33, 22, 0, 115, 32, 126, 158, 14, 0, 0, 0, 96, 0 },
        { 48, 33, 22, 0, 115, 128, 126, 158, 14, 0, 0, 0, 39, -12 },
        { 48, 53, 53, 0, 245, 240, 240, 155, 2, 0, 0, 0, 5, 28 },
        { 49, 22, 129, 128, 161, 194, 48, 116, 8, 0, 0, 0, 63, 0 },
        { 49, 33, 22, 0, 99, 99, 10, 11, 12, 0, 0, 0, 67, 0 },
        { 49, 33, 22, 0, 115, 128, 142, 158, 14, 0, 0, 0, 82, 0 },
        { 49, 34, 195, 0, 135, 139, 23, 14, 2, 0, 0, 0, 69, 0 },
        { 49, 50, 68, 0, 242, 240, 154, 39, 6, 0, 0, 0, 64, 0 },
        { 49, 50, 69, 0, 241, 242, 83, 39, 6, 0, 0, 0, 88, 12 },
        { 49, 53, 53, 0, 245, 240, 0, 155, 2, 0, 0, 0, 81, 28 },
        { 49, 97, 27, 0, 97, 210, 6, 54, 12, 0, 0, 0, 58, 0 },
        { 49, 97, 27, 0, 100, 208, 7, 103, 14, 0, 0, 0, 61, 0 },
        { 49, 97, 31, 0, 49, 80, 6, 54, 12, 0, 0, 0, 58, 0 },
        { 49, 97, 31, 0, 65, 160, 6, 54, 12, 0, 0, 0, 61, 0 },
        { 50, 17, 64, 0, 248, 245, 255, 127, 14, 0, 0, 0, 109, 0 },
        { 50, 17, 75, 0, 248, 245, 255, 127, 14, 0, 0, 0, 109, 0 },
        { 50, 17, 87, 0, 248, 245, 255, 127, 14, 0, 0, 0, 109, 0 },
        { 50, 97, 28, 128, 130, 96, 24, 7, 12, 0, 0, 0, 39, 0 },
        { 50, 97, 154, 128, 81, 96, 25, 57, 12, 0, 0, 0, 39, 0 },
        { 97, 33, 25, 0, 83, 160, 88, 24, 12, 0, 0, 0, 59, 0 },
        { 97, 33, 25, 0, 115, 160, 87, 23, 12, 0, 0, 0, 58, 0 },
        { 97, 225, 167, 128, 114, 80, 142, 26, 2, 0, 0, 0, 39, 0 },
        { 112, 34, 141, 0, 110, 107, 23, 14, 2, 0, 0, 0, 39, -12 },
        { 113, 33, 28, 0, 84, 83, 21, 73, 14, 0, 0, 0, 41, 0 },
        { 113, 34, 195, 0, 142, 139, 23, 14, 2, 0, 0, 0, 39, -12 },
        { 113, 97, 86, 0, 81, 84, 3, 23, 14, 0, 0, 0, 41, 0 },
        { 113, 97, 141, 64, 113, 114, 17, 21, 6, 0, 0, 0, 41, 0 },
        { 147, 161, 76, 0, 250, 241, 17, 241, 8, 0, 3, 0, 15, 12 },
        { 161, 226, 19, 128, 214, 96, 175, 42, 2, 0, 0, 0, 39, 0 },
        { 162, 226, 29, 128, 149, 96, 36, 42, 2, 0, 0, 0, 39, 12 },
        { 176, 215, 196, 128, 164, 64, 2, 66, 0, 0, 0, 0, 10, 0 },
        { 202, 204, 132, 0, 240, 89, 240, 98, 12, 0, 0, 0, 123, 0 },
        { 215, 210, 79, 0, 242, 241, 97, 178, 8, 0, 0, 0, 15, 12 },
        { 226, 225, 202, 0, 248, 192, 229, 14, 8, 0, 0, 0, 18, 0 },
        { 241, 225, 24, 0, 50, 241, 17, 19, 0, 0, 0, 0, 91, 0 },
    };
    
    // [channel][] = { 20_0, 20_1, 40_0, 40_1, 60_0, 60_1,
    //                 80_0, 80_1,   c0, e0_0, e0_1 }
    private final int opl2Instruments[][] = new int[9][11];
    
    private final boolean[] useDrum = new boolean[9];
    private final int[] drumInstrument = new int[9];
    private final int[] transpose = new int[9];
    private final double[] freqMultiplicationFactor = new double[9];

    private Synthesizer synth;
    private MidiChannel[] channels;
    private final int playbackRate;
    
    private int volume = 48;
    private double volumeScale = 1.0;
    
    private Thread musicThread;
    private ByteBuffer currentMusicImfData;
    private ByteBuffer newMusicImfData;
    private boolean running;
    
    private boolean playing;
    
    public IMFMusicPlayer(int playbackRate) {
        this.playbackRate = playbackRate;
        createOpl2RegistersMap();
        startSynthesizer();
        start();
    }
    
    private void createOpl2RegistersMap() {
        for (int i = 0; i < opl2InstrumentRegisters.length; i += 3) {
            int reg = opl2InstrumentRegisters[i + 0];
            int channel = opl2InstrumentRegisters[i + 1];
            int instrumentIndex = opl2InstrumentRegisters[i + 2];
            opl2RegistersMap.put(reg, new Point(channel, instrumentIndex));
        }
    }
    
    private void startSynthesizer() {
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            channels = synth.getChannels();
        } catch (MidiUnavailableException ex) {
            Logger.getLogger(getClass().getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
            
            System.exit(-1);
        }
    }

    private void start() {
        running = true;
        musicThread = new Thread(new MusicPlayback());
        musicThread.start();
    }
    
    public void dispose() {
        running = false;
    }
    
    public int getPlaybackRate() {
        return playbackRate;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public double getVolumeScale() {
        return volumeScale;
    }

    public void setVolumeScale(double volumeScale) {
        this.volumeScale = volumeScale;
    }

    public ByteBuffer loadMusicFromFile(String file) {
        ByteBuffer musicImfData = null;
        try (InputStream is = new FileInputStream(file)) {
            musicImfData = ByteBuffer.wrap(is.readAllBytes());
            musicImfData.order(ByteOrder.LITTLE_ENDIAN);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
            
            System.exit(-1);
        }
        return musicImfData;
    }
    
    public void playFromFile(String res) {
        ByteBuffer musicImfData = loadMusicFromFile(res);
        play(musicImfData);
    }
    
    public synchronized void play(ByteBuffer musicImfData) {
        newMusicImfData = musicImfData;
        playing = true;
    }
    
    public void stop() {
        playing = false;
        stopAllNotes();
    }

    public void resume() {
        playing = true;
    }

    private class MusicPlayback implements Runnable {

        @Override
        public void run() {
            int delay = 0;
            int currentMusicLength = 0;
            int[] curfreq = new int[9];
            while (running) {
                // delay
                try {
                    Thread.sleep((long) (delay * (1000.0 / playbackRate)));
                } 
                catch (InterruptedException ex) { }
                
                if (!playing) {
                    continue;
                }
                
                // new music available
                synchronized (this) {
                    if (newMusicImfData != null) {
                        stopAllNotes();
                        currentMusicImfData = newMusicImfData;
                        newMusicImfData = null;
                        extractOpl2Instruments(currentMusicImfData);
                        selectBestMatchMidiInstruments();
                        currentMusicImfData.position(0);
                        currentMusicLength 
                                = currentMusicImfData.getShort() & 0xffff;
                    }
                }
                
                // play current music
                delay = 1;
                if (currentMusicImfData != null) {
                    int cml = currentMusicLength;
                    // looping
                    if (currentMusicImfData.position() > cml - 1) {
                        currentMusicImfData.position(2);
                    }
                    int reg = currentMusicImfData.get() & 0xff;
                    int param = currentMusicImfData.get() & 0xff;
                    delay = currentMusicImfData.getShort() & 0xffff;
                    if (reg >= 0xa0 && reg <= 0xa8) {
                        int channel = reg - 0xa0;
                        curfreq[channel] 
                                = (curfreq[channel] & 0xf00) + (param & 0xff);
                        
                    } else if (reg >= 0xb0 && reg <= 0xb8) {
                        int channel = reg - 0xb0;
                        curfreq[channel] = (curfreq[channel] & 0x0ff) 
                                                    + ((param & 0x03) << 8);

                        int block = (param >> 2) & 7;
                        int keyon = (param >> 5) & 1;
                        int cf = curfreq[channel];
                        double fmf = freqMultiplicationFactor[channel];
                        int note = freqToMidiNote(cf, block, fmf) 
                                                        + transpose[channel];
                        
                        if (useDrum[channel]) {
                            note = drumInstrument[channel];
                            channel = 9;
                        }
                        int scVolume = (int) (volume * volumeScale);
                        switch (keyon) {
                            case 1 -> channels[channel].noteOn(note, scVolume);
                            case 0 -> channels[channel].noteOff(note, scVolume);
                        }    
                    }
                }
                
            }
        }
        
    }
    
    private static final double LOG2_INV = 1.0 / Math.log(2);
    
    // ref.: https://moddingwiki.shikadi.net/wiki/OPL_chip
    //       http://www.inspiredacoustics.com/en ->
    //                              -> /MIDI_note_numbers_and_center_frequencies
    private static int freqToMidiNote(int fnum, int block, double factor) {
        double freq = 49716 * fnum * Math.pow(2, block - 20) * (factor + 0.01);
        return (int) (69.0 + 12.0 * Math.log(freq / 440.0) * LOG2_INV);
    }
    
    private void extractOpl2Instruments(ByteBuffer musicImfData) {
        int c1, c2;
        musicImfData.position(2);
        Arrays.fill(freqMultiplicationFactor, 1.0);
        while (musicImfData.hasRemaining()) {
            c1 = musicImfData.get() & 0xff; // reg
            c2 = musicImfData.get() & 0xff; // data
            musicImfData.position(musicImfData.position() + 2);
            Point p = opl2RegistersMap.get(c1);
            if (p == null) continue;
            int channel = p.x;
            int instrumentIndex = p.y;
            opl2Instruments[channel][instrumentIndex] = c2;
            if (c1 >= 0x20 && c1 <= 0x35) {
                int freq = c2 & 0xf;
                freqMultiplicationFactor[channel] = freq == 0 ?  0.5 : freq;
            }
        }
    }
    
    private void selectBestMatchMidiInstruments() {
        for (int channel = 0; channel < opl2Instruments.length; channel++) {
            int[] opl2Instrument = opl2Instruments[channel];
            int bestInstrumentScore = Integer.MAX_VALUE;
            for (int[] midiInstrument : opl2ToMidiInstruments) {
                int currentInstrumentScore = 0;
                for (int i2 = 0; i2 < opl2Instrument.length; i2++) {
                    int s = Math.abs(opl2Instrument[i2] - midiInstrument[i2]);
                    currentInstrumentScore += s;
                }
                if (currentInstrumentScore < bestInstrumentScore) {
                    bestInstrumentScore = currentInstrumentScore;
                    useDrum[channel] = midiInstrument[11] == 1;
                    drumInstrument[channel] = midiInstrument[12];
                    transpose[channel] = midiInstrument[13];
                    channels[channel].programChange(0, midiInstrument[12]);
                }
            }
        }
    }

    private void stopAllNotes() {
        for (int channel = 0; channel < channels.length; channel++) {
            for (int note = 0; note < 128; note++) {
                channels[channel].noteOff(note, volume);
            }
        }
    }
    
}
    
