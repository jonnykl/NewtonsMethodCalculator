package math;

import math.exception.EvaluationException;


public class Addition extends Expression {

    private Expression addend0;
    private Expression addend1;


    public Addition (Expression addend0, Expression addend1) {
        setAddend0(addend0);
        setAddend1(addend1);
    }


    public Expression getAddend0 () {
        return addend0;
    }

    public Expression getAddend1 () {
        return addend1;
    }

    public void setAddend0 (Expression addend0) {
        if (addend0 == null)
            throw new NullPointerException("addend may not be null");

        this.addend0 = addend0;
    }

    public void setAddend1 (Expression addend1) {
        if (addend1 == null)
            throw new NullPointerException("addend may not be null");

        this.addend1 = addend1;
    }


    @Override
    public double evaluate (VariableDefinition ...variables) throws EvaluationException {
        return addend0.evaluate(variables) + addend1.evaluate(variables);
    }

    @Override
    public String toString () {
        return "(" + addend0.toString() + " + " + addend1.toString() + ")";
    }

}
