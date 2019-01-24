package math.exception;


public class ParseException extends Exception {

    private int pos;


    public ParseException (int pos) {
        this("error parsing expression at position " + pos, pos);
    }

    public ParseException (String message, int pos) {
        super(message);
        this.pos = pos;
    }


    public int getPos () {
        return pos;
    }

}
