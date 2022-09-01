package wolf3d.infra;

import java.awt.image.IndexColorModel;

/**
 * Palette class.
 * 
 * Contains the Wolfeinstein 3D VGA 256 indexed color palette table.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Palette {
    
    public static final byte[] PALETTE_RED = {
        0,0,0,0,-88,-88,-88,-88,84,84,84,84,-4,-4,-4,-4,-20,-36,-48,-64,-76
        ,-88,-104,-116,124,112,100,84,72,56,44,32,-4,-20,-32,-44,-56,-68,-80
        ,-92,-104,-120,124,112,100,88,76,64,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4
        ,-4,-28,-52,-76,-100,-4,-4,-4,-4,-4,-4,-4,-4,-28,-52,-76,-100,-124
        ,112,88,64,-48,-60,-76,-96,-112,-128,116,96,-40,-68,-100,-128,96,64
        ,32,0,0,0,0,0,4,4,4,4,4,4,4,4,4,4,4,4,-40,-72,-100,124,92,64,32,0,0
        ,0,0,0,0,0,0,0,92,64,32,0,0,0,0,0,-40,-72,-100,124,92,64,32,0,0,0,0
        ,0,0,0,0,0,0,0,0,0,0,0,0,0,40,-4,-4,-4,-4,-4,-76,-88,-104,-128,116
        ,96,80,68,52,40,-4,-4,-4,-4,-4,-4,-4,-4,-32,-56,-76,-100,-124,108,88
        ,64,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-16,-24,-36,-48,-56,-68,-76,-88
        ,-96,-100,-112,-120,-128,116,108,92,84,72,64,56,40,96,0,0,0,0,48,72
        ,80,0,28,76,92,64,48,52,-40,-72,-100,116,72,32,32,0,0,0,0,0,0,0,0,0
        ,-104
    };
   
    public static final byte[] PALETTE_GREEN = {
        0,0,-88,-88,0,0,84,-88,84,84,-4,-4,84,84,-4,-4,-20,-36,-48,-64,-76
        ,-88,-104,-116,124,112,100,84,72,56,44,32,0,0,0,0,0,0,0,0,0,0,0,0,0
        ,0,0,0,-40,-72,-100,124,92,64,32,0,-88,-104,-120,120,108,96,84,76,-4
        ,-4,-4,-4,-8,-12,-12,-12,-40,-60,-84,-100,-124,108,84,64,-4,-4,-4,-4
        ,-28,-52,-76,-100,-4,-4,-4,-4,-4,-4,-4,-4,-4,-20,-32,-44,-56,-68,-80
        ,-92,-104,-120,124,112,100,88,76,64,-4,-4,-4,-4,-4,-4,-4,-4,-28,-52
        ,-76,-100,-124,112,88,64,-68,-80,-88,-100,-116,124,108,92,-40,-68,-100
        ,-128,96,64,36,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,40,-32,-44,-52,-64
        ,-76,32,0,0,0,0,0,0,0,0,0,-40,-72,-100,124,92,64,32,0,0,0,0,0,0,0,0
        ,0,-24,-32,-40,-44,-52,-60,-68,-72,-80,-92,-100,-108,-116,-120,-128
        ,124,120,112,104,100,96,92,88,80,76,72,64,60,56,48,44,32,0,100,96,0
        ,0,36,0,0,0,28,76,92,64,48,52,-12,-24,-36,-56,-64,-76,-80,-92,-104
        ,-116,-124,124,120,116,112,108,0
    };
    
    public static final byte[] PALETTE_BLUE = {
        0,-88,0,-88,0,-88,0,-88,84,-4,84,-4,84,-4,84,-4,-20,-36,-48,-64,-76
        ,-88,-104,-116,124,112,100,84,72,56,44,32,0,0,0,0,0,0,0,0,0,0,0,0,0
        ,0,0,0,-40,-72,-100,124,92,64,32,0,92,64,32,0,0,0,0,0,-40,-72,-100
        ,124,92,64,32,0,0,0,0,0,0,0,0,0,92,64,32,0,0,0,0,0,-40,-72,-100,124
        ,92,64,32,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-4,-4,-4,-8,-4,-4,-4,-4
        ,-28,-52,-76,-100,-124,112,88,64,-4,-4,-4,-4,-28,-52,-76,-100,-4,-4
        ,-4,-4,-4,-4,-4,-4,-4,-20,-32,-44,-56,-68,-80,-92,-104,-120,124,112
        ,100,88,76,64,40,52,36,24,8,0,-4,-4,-28,-52,-76,-100,-124,112,88,64
        ,-4,-4,-4,-4,-4,-4,-4,-4,-28,-52,-76,-100,-124,112,88,64,-36,-48,-60
        ,-68,-80,-92,-100,-112,-128,112,96,92,88,84,80,76,72,68,64,60,56,52
        ,48,44,40,36,32,28,24,24,20,12,100,100,96,28,44,16,72,80,52,28,76,92
        ,64,48,52,-12,-24,-36,-56,-64,-76,-80,-92,-104,-116,-124,124,120,116
        ,112,108,-120
    };
    
    public static final IndexColorModel INDEX_COLOR_MODEL 
        = new IndexColorModel(8, 256, PALETTE_RED, PALETTE_GREEN, PALETTE_BLUE);
            
}