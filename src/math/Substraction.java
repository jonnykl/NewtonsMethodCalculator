package math;

import math.exception.EvaluationException;


public class Substraction extends Expression {

    private Expression minuend;
    private Expression subtrahend;


    public Substraction (Expression minuend, Expression subtrahend) {
        setMinuend(minuend);
        setSubtrahend(subtrahend);
    }


    public Expression getMinuend () {
        return minuend;
    }

    public Expression getSubtrahend () {
        return subtrahend;
    }

    public void setMinuend (Expression minuend) {
        if (minuend == null)
            throw new NullPointerException("minuend may not be null");

        this.minuend = minuend;
    }

    public void setSubtrahend (Expression subtrahend) {
        if (subtrahend == null)
            throw new NullPointerException("subtrahend may not be null");

        this.subtrahend = subtrahend;
    }


    @Override
    public double evaluate (VariableDefinition ...variables) throws EvaluationException {
        return minuend.evaluate(variables) - subtrahend.evaluate(variables);
    }

    @Override
    public String toString () {
        return "(" + minuend.toString() + " - " + subtrahend.toString() + ")";
    }

}
