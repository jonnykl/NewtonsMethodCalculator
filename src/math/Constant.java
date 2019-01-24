package math;

import math.exception.EvaluationException;


public class Constant extends Expression {

    public enum C {
        pi, e
    }


    private C constant;


    public Constant (C constant) {
        setConstant(constant);
    }


    public C getConstant () {
        return constant;
    }

    public void setConstant (C constant) {
        if (constant == null)
            throw new NullPointerException("constant may not be null");

        this.constant = constant;
    }


    public double getValue () {
        switch (constant) {
            case pi:
                return Math.PI;

            case e:
                return Math.E;


            default:
                throw new UnknownError();
        }
    }


    @Override
    public double evaluate (VariableDefinition ...variables) throws EvaluationException {
        return getValue();
    }

    @Override
    public String toString () {
        return constant.name();
    }

}
