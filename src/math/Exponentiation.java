package math;

import math.exception.EvaluationException;


public class Exponentiation extends Expression {

    private Expression base;
    private Expression exponent;


    public Exponentiation (Expression base, Expression exponent) {
        setBase(base);
        setExponent(exponent);
    }


    public Expression getBase () {
        return base;
    }

    public Expression getExponent () {
        return exponent;
    }

    public void setBase (Expression base) {
        if (base == null)
            throw new NullPointerException("base may not be null");

        this.base = base;
    }

    public void setExponent (Expression exponent) {
        if (exponent == null)
            throw new NullPointerException("exponent may not be null");

        this.exponent = exponent;
    }


    @Override
    public double evaluate (VariableDefinition ...variables) throws EvaluationException {
        return Math.pow(base.evaluate(variables), exponent.evaluate(variables));
    }

    @Override
    public String toString () {
        return "(" + base.toString() + "^" + exponent.toString() + ")";
    }

}
