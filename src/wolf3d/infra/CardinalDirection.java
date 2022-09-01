package wolf3d.infra;

/**
 * CardinalDirection enum.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public enum CardinalDirection {
    
       S( 0,  1, Math.toRadians( 90)), 
      SW(-1,  1, Math.toRadians(135)), 
       W(-1,  0, Math.toRadians(180)), 
      NW(-1, -1, Math.toRadians(225)), 
       N( 0, -1, Math.toRadians(270)), 
      NE( 1, -1, Math.toRadians(315)), 
       E( 1,  0, Math.toRadians(  0)), 
      SE( 1,  1, Math.toRadians( 45));

    public final int dx, dy;
    public final double angle;

    CardinalDirection(int dx, int dy, double angle) {
        this.dx = dx;
        this.dy = dy;
        this.angle = angle;
    }

    public static CardinalDirection get(int i) {
        switch (i) {
            case 0: return CardinalDirection.E;
            case 1: return CardinalDirection.N;
            case 2: return CardinalDirection.W;
            case 3: return CardinalDirection.S;
        }
        return null;
    }

    public static CardinalDirection getPathDirection(int i) {
        switch (i) {
            case 0: return CardinalDirection.E;
            case 1: return CardinalDirection.NE;
            case 2: return CardinalDirection.N;
            case 3: return CardinalDirection.NW;
            case 4: return CardinalDirection.W;
            case 5: return CardinalDirection.SW;
            case 6: return CardinalDirection.S;
            case 7: return CardinalDirection.SE;
        }
        return null;
    }

    public static CardinalDirection getOpposite(CardinalDirection d) {
        switch (d) {
            case E: return W;
            case NE: return SW;
            case N: return S;
            case NW: return SE;
            case W: return E;
            case SW: return NE;
            case S: return N;
            case SE: return NW;
        }
        return null;
    }

    // dir 0=CCW 1=CW
    public static CardinalDirection rotate(CardinalDirection d, int dir) {
        switch (d) {
            case E: return dir == 0 ? N : S;
            case N: return dir == 0 ? W : E;
            case W: return dir == 0 ? S : N;
            case S: return dir == 0 ? E : W;
        }
        return null;
    }

    public static CardinalDirection getDirection(int dx, int dy) {
        if (dx == 1 && dy == 0) return E;
        if (dx == 1 && dy == 1) return SE;
        if (dx == 0 && dy == 1) return S;
        if (dx == -1 && dy == 1) return SW;
        if (dx == -1 && dy == 0) return W;
        if (dx == -1 && dy == -1) return NW;
        if (dx == 0 && dy == -1) return N;
        if (dx == 1 && dy == -1) return NE;
        return null;
    }

}
