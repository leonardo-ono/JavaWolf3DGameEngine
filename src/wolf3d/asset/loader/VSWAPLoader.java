package wolf3d.asset.loader;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static wolf3d.infra.Palette.INDEX_COLOR_MODEL;
import static wolf3d.infra.Palette.PALETTE_BLUE;
import static wolf3d.infra.Palette.PALETTE_GREEN;
import static wolf3d.infra.Palette.PALETTE_RED;

/**
 * VSWAPLoader class.
 * 
 * This is responsible for loading the assets present in the VSWAP file.
 * 
 * VSWAP doesn't use any compression method, so the assets can 
 * be extracted directly.
 * 
 * The VSWAP file contains:
 * 
 * - wall textures (64x64 pixels, indexed, 1 byte per color)
 * - sprites (items, enemies, etc): use a special format, check the references.
 * - digitized sounds (7000Hz unsigned 8 bits mono PCM)
 * 
 * Note: other PIC's (HUD images, bitmap fonts, etc) are located
 *       in the VGAGRAPH file.
 * 
 * References: 
 * https://vpoupet.github.io/wolfenstein/docs/files.html
 * https://devinsmith.net/backups/bruce/wolf3d.html
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class VSWAPLoader {

    private static final Map<Integer, BufferedImage> 
                                    WALL_TEXTURES = new HashMap<>();

    private static final Map<Integer, BufferedImage> 
                                    SPRITES = new HashMap<>();

    private static final Map<Integer, byte[]> 
                                    DIGITIZED_SOUNDS = new HashMap<>();

    public static void load(String path, String vswapRes) throws Exception {
        try (
            InputStream vswapIs = new FileInputStream(path + vswapRes); 
        ) {
            ByteBuffer vswapData = ByteBuffer.wrap(vswapIs.readAllBytes());
            vswapData.order(ByteOrder.LITTLE_ENDIAN);

            int numberOfChunks = vswapData.getShort() & 0xffff;
            int indexFirstSprite = vswapData.getShort() & 0xffff;
            int indexFirstSound = vswapData.getShort() & 0xffff;
            int[] addresses = new int[numberOfChunks];
            int[] lengths = new int[numberOfChunks];
            
            for (int i = 0; i < numberOfChunks; i++) {
                addresses[i] = (int) (vswapData.getInt() & 0xffffffffl);
            }
            
            for (int i = 0; i < numberOfChunks; i++) {
                lengths[i] = vswapData.getShort() & 0xffff;
            }
            
            extractWallTextures(indexFirstSprite, vswapData, addresses);
            
            extractDigitizedSounds(
                indexFirstSound, numberOfChunks, addresses, lengths, vswapData);
            
            extractSprites(indexFirstSprite
                    , indexFirstSound - 1, addresses, lengths, vswapData);
            
        } catch (Exception ex) {
            throw new Exception(
                    "Could not load VSWAP resources properly!", ex);
        }        
    }
    
    private static void extractWallTextures(
            int indexFirstSprite, ByteBuffer vswapData, int[] addresses) {
        
        for (int i = 0; i < indexFirstSprite; i++) {
            vswapData.position(addresses[i]);
            BufferedImage wallTextureTmp = new BufferedImage(64, 64
                    , BufferedImage.TYPE_BYTE_INDEXED, INDEX_COLOR_MODEL);
            
            DataBufferByte dataBuffer = (DataBufferByte)
                    wallTextureTmp.getRaster().getDataBuffer();
            
            vswapData.get(dataBuffer.getData());
            
            // convert to INT_ARGB, flip and rotate the image correctly
            BufferedImage wallTexture = new BufferedImage(64, 64
                    , BufferedImage.TYPE_INT_ARGB);
            
            Graphics2D g = wallTexture.createGraphics();
            g.translate(0, 64);
            g.rotate(Math.toRadians(-90));
            g.translate(64, 0);
            g.scale(-1, 1);
            g.drawImage(wallTextureTmp, 0, 0, null);
            
            WALL_TEXTURES.put(i, wallTexture);
        }
    }

    // sprites are saved using a different format described here:
    // https://vpoupet.github.io/wolfenstein/docs/files.html
    private static void extractSprites(
                int indexFirstSprite, int indexLastSprite
                    , int[] addresses, int[] lengths, ByteBuffer vswapData) {
        
        for (int si = indexFirstSprite; si <= indexLastSprite; si++) {
            int address = addresses[si];
            int length = lengths[si];
            if (length <= 0) continue;
            vswapData.position(address);
            int firstCol = vswapData.getShort() & 0xffff;
            int lastCol = vswapData.getShort() & 0xffff;
            int[] postOffsets = new int[lastCol - firstCol + 1];
            for (int i = 0; i < postOffsets.length; i++) {
                postOffsets[i] = vswapData.getShort() & 0xffff;
            }
            int[] pixelPool 
                    = new int[postOffsets[0] - postOffsets.length * 2 - 4];
            
            for (int i = 0; i < pixelPool.length; i++) {
                pixelPool[i] = vswapData.get() & 0xff;
            }
            int[] posts = new int[(length - postOffsets[0]) / 2];
            for (int i = 0; i < posts.length; i++) {
                posts[i] = vswapData.getShort()& 0xffff;
            }
            BufferedImage sprite = new BufferedImage(64, 64
                    , BufferedImage.TYPE_INT_ARGB);
            
            int colorIndex = 0;
            int postIndex = 0;
            int col = firstCol;
            while (col <= lastCol) {
                int startRow = posts[postIndex + 2] / 2;
                int endRow = posts[postIndex + 0] / 2;
                for (int row = startRow; row < endRow; row++) {
                    int r = PALETTE_RED[pixelPool[colorIndex]] & 0xff;
                    int g = PALETTE_GREEN[pixelPool[colorIndex]] & 0xff;
                    int b = PALETTE_BLUE[pixelPool[colorIndex]] & 0xff;
                    int color = 0xff000000 + (r << 16) + (g << 8) + b;
                    sprite.setRGB(col, row, color);
                    colorIndex++;
                }
                postIndex += 3;
                while (postIndex < posts.length && posts[postIndex] == 0) {
                    col++;
                    postIndex++;
                }
            }
            SPRITES.put(si - indexFirstSprite, sprite);
        }
    }

    // note: 1 digitized sound can consist of 1 or more chunks.
    //       chunk length less than 4096 indicates the last chunk.
    private static void extractDigitizedSounds(
                int indexFirstSound, int numberOfChunks, int[] addresses
                                    , int[] lengths, ByteBuffer vswapData) {
        
        List<byte[]> digitizedSoundMultipleChunks = new ArrayList<>();
        int digitizedSoundMultipleChunksLength = 0;
        int digitizedSoundIndex = 0;
        for (int i = indexFirstSound; i < numberOfChunks; i++) {
            int chunkAddress = addresses[i];
            int chunkLength = lengths[i];
            vswapData.position(chunkAddress);
            byte[] soundData = new byte[chunkLength];
            vswapData.get(soundData);
            digitizedSoundMultipleChunks.add(soundData);
            digitizedSoundMultipleChunksLength += chunkLength;
            if (chunkLength < 4096) {
                final int soundPrioritySize = 2;
                ByteBuffer multipleChunks = ByteBuffer.allocate(
                        digitizedSoundMultipleChunksLength + soundPrioritySize);
                
                digitizedSoundMultipleChunks.forEach(
                        chunk -> multipleChunks.put(chunk));
                
                DIGITIZED_SOUNDS.put(
                        digitizedSoundIndex++, multipleChunks.array());
                
                digitizedSoundMultipleChunks.clear();
                digitizedSoundMultipleChunksLength = 0;
            }
        }
    }
    
    public static BufferedImage getWallTexture(int wallTextureIndex) {
        return WALL_TEXTURES.get(wallTextureIndex);
    }

    public static BufferedImage getSprite(int spriteIndex) {
        return SPRITES.get(spriteIndex);
    }

    public static byte[] getDigitizedSound(int soundIndex) {
        return DIGITIZED_SOUNDS.get(soundIndex);
    }

}
