package math;


import math.exception.EvaluationException;

import java.util.List;


public class Division extends Expression {

    private Expression dividend;
    private Expression divisor;


    public Division (Expression dividend, Expression divisor) {
        setDividend(dividend);
        setDivisor(divisor);
    }


    public Expression getDividend () {
        return dividend;
    }

    public Expression getDivisor () {
        return divisor;
    }

    public void setDividend (Expression dividend) {
        if (dividend == null)
            throw new NullPointerException("dividend may not be null");

        this.dividend = dividend;
    }

    public void setDivisor (Expression divisor) {
        if (divisor == null)
            throw new NullPointerException("divisor may not be null");

        this.divisor = divisor;
    }


    @Override
    public double evaluate (VariableDefinition ...variables) throws EvaluationException {
        return dividend.evaluate(variables) / divisor.evaluate(variables);
    }

    @Override
    public String toString () {
        return "(" + dividend.toString() + " / " + divisor.toString() + ")";
    }

}
