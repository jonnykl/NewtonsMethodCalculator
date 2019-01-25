package math;


public class Scalar extends Expression {

    private double value;


    public Scalar () {
        this.value = Double.NaN;
    }

    public Scalar (double value) {
        setValue(value);
    }


    public double getValue () {
        return value;
    }

    public void setValue (double value) {
        this.value = value;
    }


    @Override
    public double evaluate (VariableDefinition ...variables) {
        return value;
    }

    @Override
    public String toString () {
        String str = Double.toString(value);
        return value < 0 ? "(" + str + ")" : str;
    }

}
