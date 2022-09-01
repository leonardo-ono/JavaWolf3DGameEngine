package wolf3d.asset.loader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * MAPLoader class.
 * 
 * This is responsible for loading the game map's present in the 
 * MAPHEAD and GAMEMAPS files.
 * 
 * The maps are compressed using both Carmack and RLEW compression methods.
 * 
 * References: 
 * https://moddingwiki.shikadi.net/wiki/GameMaps_Format
 * https://vpoupet.github.io/wolfenstein/docs/files.html
 * https://moddingwiki.shikadi.net/wiki/Carmack_compression
 * https://moddingwiki.shikadi.net/wiki/Id_Software_RLEW_compression
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class MAPLoader {

    private static int[] mapOffsets;
    
    // MAPS[mapId][layer], layer can be 0 or 1
    private static final Map<Integer, int[][]> MAPS = new HashMap<>();
    
    private static final Map<Integer, String> MAP_NAMES = new HashMap<>();
    
    public static void load(String path
            , String mapHeadRes, String gameMapsRes) throws Exception {
        
        try (
            InputStream mapHeadIs = new FileInputStream(path + mapHeadRes);
            InputStream gameMapIs = new FileInputStream(path + gameMapsRes); 
        ) {
            // extract the header and map offsets info
            byte[] magic = new byte[2];
            mapHeadIs.read(magic);
            ByteBuffer mapHeadData = ByteBuffer.wrap(mapHeadIs.readAllBytes());
            mapHeadData.order(ByteOrder.LITTLE_ENDIAN);
            
            mapOffsets = new int[100];
            int index = 0;
            while (index < mapOffsets.length) {
                mapOffsets[index++] = mapHeadData.getInt();
            }
            
            // extract all the maps
            ByteBuffer gameMapData = ByteBuffer.wrap(gameMapIs.readAllBytes());
            gameMapData.order(ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < index; i++) {
                int[][] map = new int[2][];
                int mapOffset = mapOffsets[i];
                if (mapOffset <= 0) {
                    continue;
                }
                gameMapData.position(mapOffset);
                int offPlane0 = gameMapData.getInt();
                int offPlane1 = gameMapData.getInt();
                int offPlane2 = gameMapData.getInt();
                int lenPlane0 = gameMapData.getShort();
                int lenPlane1 = gameMapData.getShort();
                int lenPlane2 = gameMapData.getShort();
                int width = gameMapData.getShort();
                int height = gameMapData.getShort();
                byte[] name = new byte[16];
                gameMapData.get(name);
                String mapName = new String(name);

                // layer 0
                ByteBuffer carmackDecompressed 
                        = decompressCarmack(gameMapData, offPlane0);

                carmackDecompressed.order(ByteOrder.LITTLE_ENDIAN);
                carmackDecompressed.position(0);
                IntBuffer rlewDecompressed 
                        = decompressRLEW(carmackDecompressed, 0);

                map[0] = rlewDecompressed.array();

                // layer 1 
                carmackDecompressed = decompressCarmack(gameMapData, offPlane1);
                carmackDecompressed.order(ByteOrder.LITTLE_ENDIAN);
                carmackDecompressed.position(0);
                rlewDecompressed = decompressRLEW(carmackDecompressed, 0);
                map[1] = rlewDecompressed.array();

                MAPS.put(i, map);
                MAP_NAMES.put(i, mapName);
            }
        } catch (IOException ex) {
            throw new Exception(
                    "Could not load MAP resources properly!", ex);
        }
    }

    // https://moddingwiki.shikadi.net/wiki/Carmack_compression
    private static ByteBuffer decompressCarmack(ByteBuffer bb, int start) {
        final byte nearTag = (byte) 0xa7;
        final byte farTag = (byte) 0xa8;
        bb.position(start);
        byte[] decompressedData = new byte[bb.getShort() & 0xffff];
        int decompressedDataIndex = 0;
        while (decompressedDataIndex < decompressedData.length) {
            byte b0 = bb.get(); // number of words
            byte b1 = bb.get(); // signal byte
            if ((b0 == 0 && b1 == nearTag) || (b0 == 0 && b1 == farTag)) {
                decompressedData[decompressedDataIndex++] = bb.get();
                decompressedData[decompressedDataIndex++] = b1;
            }
            else if (b1 == nearTag || b1 == farTag) {
                int location = (b1 == nearTag 
                        ? decompressedDataIndex - 2 * (bb.get() & 0xff) 
                        : 2 * (bb.getShort() & 0xffff));
                
                int sizeInBytes = (b0 & 0xff) * 2;
                System.arraycopy(decompressedData, location
                        , decompressedData, decompressedDataIndex, sizeInBytes);
                
                decompressedDataIndex += sizeInBytes;
            }
            else {
                decompressedData[decompressedDataIndex++] = b0;
                decompressedData[decompressedDataIndex++] = b1;
            }
        }
        ByteBuffer decompressedByteBuffer = ByteBuffer.wrap(decompressedData);
        decompressedByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return decompressedByteBuffer;
    }
    
    // https://moddingwiki.shikadi.net/wiki/Id_Software_RLEW_compression
    private static IntBuffer decompressRLEW(ByteBuffer bb, int start) {
        bb.position(start);
        int[] decompressedData = new int[(bb.getShort() & 0xffff) / 2];
        int decompressedDataIndex = 0;
        while (decompressedDataIndex < decompressedData.length) {
            short s = bb.getShort();
            if (s == (short) 0xABCD) {
                int count = bb.getShort() & 0xffff;
                Arrays.fill(decompressedData, decompressedDataIndex
                    , decompressedDataIndex + count, bb.getShort() & 0xffff);
                
                decompressedDataIndex += count;
            }
            else {
                decompressedData[decompressedDataIndex++] = s & 0xffff;
            }
        }
        IntBuffer decompressedBuffer = IntBuffer.wrap(decompressedData);
        return decompressedBuffer;
    }
    
    public static int[][] getMap(int mapId) {
        return MAPS.get(mapId);
    }

    public static String getMapName(int mapId) {
        return MAP_NAMES.get(mapId);
    }

}
