package wolf3d.asset.loader;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import java.awt.image.DataBufferByte;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import static wolf3d.infra.Palette.INDEX_COLOR_MODEL;
import wolf3d.infra.Util;

/**
 * VGAGRAPHLoader class.
 * 
 * This is responsible for loading the PIC's (HUD images, bitmap fonts, etc)
 * present in the VGAHEAD, VGADICT and VGAGRAPH files.
 * 
 * The pic data is compressed using the Huffman compression, 
 * so the VGADICT file must be used as a dictionary for decompression.
 * 
 * The VGAHEAD file is the header and informs the location (offset)
 * of each chunk within VGAGRAPH.
 * 
 * Reference: 
 * https://devinsmith.net/backups/bruce/wolf3d.html
 * https://moddingwiki.shikadi.net/wiki/Huffman_Compression
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class VGAGRAPHLoader {

    // References:
    // https://github.com/sirjuddington/SLADE/issues/121
    //
    // https://github.com/id-Software/Wolf3D-iOS/blob/ ->
    //                      -> d7fff51d7d3bc7f3c0d44d58f91ba2ba3b4a7951/ ->
    //                                  -> wolf3d/wolfextractor/wolf/wolf_gfx.c
    // typedef struct
    // {
    // 	short height;
    //	short location[ 256 ];
    //	char width[ 256 ];
    // } fontstruct;
    public static class VGAGRAPHFont {
        
        private final byte[] fontData;
        private int fontHeight;
        private int[] fontLocation;
        private int[] fontWidth;
        private final BufferedImage[] charImages = new BufferedImage[256];
        
        public VGAGRAPHFont(byte[] fontData, Color color) {
            this.fontData = fontData;
            extractFontInformation();
            generateCharImages(color);
        }
        
        private void extractFontInformation() {
            ByteBuffer bb = ByteBuffer.wrap(fontData);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            fontHeight = bb.getShort() & 0xffff;
            fontLocation = new int[256];
            fontWidth = new int[256];
            for (int i = 0; i < fontLocation.length; i++) {
                fontLocation[i] = bb.getShort() & 0xffff;
            }
            for (int i = 0; i < fontWidth.length; i++) {
                fontWidth[i] = bb.get() & 0xff;
            }            
        }
        
        private void generateCharImages(Color color) {
            for (int c = 0; c < 256; c++) {
                
                int ch = fontHeight;
                int cw = fontWidth[c];
                int cl = fontLocation[c];

                BufferedImage charImage = null;
                if (ch > 0 && cw > 0) {
                    charImage = new BufferedImage(cw, ch, TYPE_INT_ARGB);
                }
                if (charImage == null) continue;
                charImages[c] = charImage;
                int index = 0;
                for (int y = 0; y < ch; y++) {
                    for (int x = 0; x < cw; x++) {
                        int rgb = fontData[cl + index++] & 0xff;
                        if (rgb > 0) {
                            charImage.setRGB(x, y, color.getRGB());
                        }
                    }
                }            
            }
        }
    
        public void drawString(Graphics2D g, String text, int x, int y) {
            int dx = 0;
            for (int i = 0; i < text.length(); i++) {
                int c = text.charAt(i);
                BufferedImage charImage = charImages[c];
                g.drawImage(charImage, x + dx, y, null);
                dx += fontWidth[c];
            }
        }
        
    }
    
    private static final byte[] huffmanNodes = new byte[256 * 4];
    private static Dimension[] pictable;

    private static final Map<Integer, BufferedImage> PICS = new HashMap<>();
    private static final Map<String, VGAGRAPHFont> FONTS = new HashMap<>();
    
    public static void load(String path, String vgaHeadRes
                , String vgaDictRes, String vgaGraphRes) throws Exception {
        
        try (
            InputStream vgaHeadIs = new FileInputStream(path + vgaHeadRes); 
            InputStream vgaDictIs = new FileInputStream(path + vgaDictRes); 
            InputStream vgaGraphIs = new FileInputStream(path + vgaGraphRes); 
        ) {
            ByteBuffer vgaHeadData = ByteBuffer.wrap(vgaHeadIs.readAllBytes());
            vgaHeadData.order(ByteOrder.LITTLE_ENDIAN);
            
            // size of VGAHEAD file must be multiple of 3
            if (vgaHeadData.limit() % 3 != 0) {
                throw new Exception("VGAHEAD file is corrupted!");
            }

            // extract the offsets information from the header
            int picsCount = vgaHeadData.limit() / 3;
            int[] offsets = new int[picsCount];
            for (int i = 0; i < offsets.length; i++) {
                int o0 = vgaHeadData.get() & 0xff;
                int o1 = vgaHeadData.get() & 0xff;
                int o2 = vgaHeadData.get() & 0xff;
                offsets[i] = o0 + (o1 << 8) + (o2 << 16);
            }
            
            ByteBuffer vgaDictData = ByteBuffer.wrap(vgaDictIs.readAllBytes());
            vgaDictData.order(ByteOrder.LITTLE_ENDIAN);

            // extract huffman dictionary
            vgaDictData.get(huffmanNodes);
            
            ByteBuffer vgaGraphData 
                    = ByteBuffer.wrap(vgaGraphIs.readAllBytes());
            
            vgaGraphData.order(ByteOrder.LITTLE_ENDIAN);
            
            // extract the chunks
            for (int i = 0; i < offsets.length - 1; i++) {
                int length = offsets[i + 1] - offsets[i]; 
                ByteBuffer compressed = vgaGraphData.slice(offsets[i], length);
                compressed.order(ByteOrder.LITTLE_ENDIAN);
                byte[] decompressedData = decompressHuffman(compressed);
                if (decompressedData == null) continue;
                // extract pictable
                if (i == 0) {
                    extractPictable(decompressedData, picsCount);
                }
                // extract bitmap fonts
                else if (i == 1) { // 8x8 small font
                    VGAGRAPHFont fontWhite 
                            = new VGAGRAPHFont(decompressedData, Color.WHITE);

                    VGAGRAPHFont fontBlack
                            = new VGAGRAPHFont(decompressedData, Color.BLACK);

                    VGAGRAPHFont fontYellow 
                            = new VGAGRAPHFont(decompressedData, Color.YELLOW);
                    
                    FONTS.put("SMALL_WHITE", fontWhite);
                    FONTS.put("SMALL_BLACK", fontBlack);
                    FONTS.put("SMALL_YELLOW", fontYellow);
                }
                else if (i == 2) { // game options font
                    VGAGRAPHFont fontGray 
                        = new VGAGRAPHFont(decompressedData, Color.GRAY);
                    
                    VGAGRAPHFont fontLightGray
                        = new VGAGRAPHFont(decompressedData, Color.LIGHT_GRAY);
                    
                    VGAGRAPHFont fontDarkRed = new VGAGRAPHFont(
                                decompressedData, Util.getColor("0x710000ff"));

                    VGAGRAPHFont fontYellow = new VGAGRAPHFont(
                                decompressedData, Color.YELLOW);

                    FONTS.put("BIG_GRAY", fontGray);
                    FONTS.put("BIG_LIGHT_GRAY", fontLightGray);
                    FONTS.put("BIG_DARK_RED", fontDarkRed);
                    FONTS.put("BIG_YELLOW", fontYellow);
                }
                // extract PIC's
                else if (i > 2) {
                    Dimension picDimension = pictable[i];
                    if (picDimension != null) {
                        int picWidth = picDimension.width;
                        int picHeight = picDimension.height;
                        if (picWidth <= 0 || picHeight <= 0) continue;
                        
                        BufferedImage fixedImage = fixVgaModeYPic(
                                    i, decompressedData, picWidth, picHeight);

                        PICS.put(i, fixedImage);
                    }
                }
            }
        } catch (Exception ex) {
            throw new Exception(
                    "Could not load VGA resources properly!", ex);
        }
    }       

    // note: pictable contain the information about the dimension 
    //       (width and height) for each PIC and is located in the 
    //       first VGAGRAPH chunk.
    private static void extractPictable(byte[] pictableData, int picsCount) {
        int pictableOffsetIndex = 3;
        pictable = new Dimension[picsCount];
        for (int i = 0; i < pictableData.length / 4; i++) {
            int b0 = pictableData[4 * i + 0] & 0xff; 
            int b1 = pictableData[4 * i + 1] & 0xff; 
            int b2 = pictableData[4 * i + 2] & 0xff; 
            int b3 = pictableData[4 * i + 3] & 0xff; 
            int picWidth = b0 + (b1 << 8);
            int picHeight = b2 + (b3 << 8);
            pictable[i + pictableOffsetIndex] 
                    = new Dimension(picWidth, picHeight);
        }
    }

    // ref.: https://moddingwiki.shikadi.net/wiki/Huffman_Compression
    private static byte[] decompressHuffman(ByteBuffer compressed) {
        int decompressedLength = (int) (compressed.getInt() & 0xffffffffl);
        if (decompressedLength <= 0) return null;
        byte[] data = new byte[decompressedLength];
        int bitIndex = 0;
        int dataIndex = 0;
        int nodeIndex = 254;
        while (dataIndex < decompressedLength) {
            int bit = (compressed.get(compressed.position()) >> bitIndex) & 1;
            bitIndex++;
            if (bitIndex > 7) {
                bitIndex = 0;
                compressed.position(compressed.position() + 1);
            }
            if (compressed.position() >= compressed.limit()) break;
            if (huffmanNodes[nodeIndex * 4 + 1 + bit * 2] == 0) {
                data[dataIndex++] = huffmanNodes[nodeIndex * 4 + bit * 2];
                nodeIndex = 254;
            }
            else if (huffmanNodes[nodeIndex * 4 + 1 + bit * 2] == 1) {
                nodeIndex = huffmanNodes[nodeIndex * 4 + bit * 2] & 0xff;
            }
        }
        return data;
    }
    
    // note: after huffman decompression, the PIC image must be fixed since it 
    //       is arranged in such a way as to facilitate rendering in VGA mode Y.
    public static BufferedImage fixVgaModeYPic(int index,
                        byte[] picData, int picWidth, int picHeight) {
        
        BufferedImage bi = new BufferedImage(picWidth, picHeight
                    , BufferedImage.TYPE_BYTE_INDEXED, INDEX_COLOR_MODEL);

        DataBufferByte dataBuffer 
                    = (DataBufferByte) bi.getRaster().getDataBuffer();

        System.arraycopy(
                    picData, 0, dataBuffer.getData(), 0, picWidth * picHeight);

        BufferedImage fixedImage = new BufferedImage(
                    picWidth, picHeight, BufferedImage.TYPE_INT_RGB);

        int w4 = picWidth / 4; 
        int h4 = picHeight / 4; 
        for (int y = 0; y < picHeight; y++) {
            for (int x = 0; x < picWidth; x++) {
                int xs = 4 * (x % w4) + (y / h4);
                int ys = 4 * (y % h4) + (x / w4);
                int c = bi.getRGB(x, y);
                fixedImage.setRGB(xs, ys, c);
            }
        }
        return fixedImage;
    }

    public static BufferedImage getPic(int picIndex) {
        return PICS.get(picIndex);
    }
    
    public static VGAGRAPHFont getFont(String fontId) {
        return FONTS.get(fontId);
    }
    
}
