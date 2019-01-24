package math;

import math.exception.EvaluationException;


public class Multiplication extends Expression {

    private Expression multiplicand0;
    private Expression multiplicand1;


    public Multiplication (Expression multiplicand0, Expression multiplicand1) {
        setMultiplicand0(multiplicand0);
        setMultiplicand1(multiplicand1);
    }


    public Expression getMultiplicand0 () {
        return multiplicand0;
    }

    public Expression getMultiplicand1 () {
        return multiplicand1;
    }

    public void setMultiplicand0 (Expression multiplicand0) {
        if (multiplicand0 == null)
            throw new NullPointerException("multiplicand may not be null");

        this.multiplicand0 = multiplicand0;
    }

    public void setMultiplicand1 (Expression multiplicand1) {
        if (multiplicand0 == null)
            throw new NullPointerException("multiplicand may not be null");

        this.multiplicand1 = multiplicand1;
    }


    @Override
    public double evaluate (VariableDefinition ...variables) throws EvaluationException {
        return multiplicand0.evaluate(variables) * multiplicand1.evaluate(variables);
    }

    @Override
    public String toString () {
        return "(" + multiplicand0.toString() + " * " + multiplicand1.toString() + ")";
    }

}
