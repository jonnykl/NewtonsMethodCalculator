package math;

import math.exception.EvaluationException;


public class Function extends Expression {

    public enum F {
        sin, cos, ln, abs
    }


    private F function;
    private Expression parameter;


    public Function (F function, Expression parameter) {
        setFunction(function);
        setParameter(parameter);
    }


    public F getFunction () {
        return function;
    }

    public Expression getParameter () {
        return parameter;
    }

    public void setFunction (F function) {
        if (function == null)
            throw new NullPointerException("function may not be null");

        this.function = function;
    }

    public void setParameter (Expression parameter) {
        if (parameter == null)
            throw new NullPointerException("parameter may not be null");

        this.parameter = parameter;
    }


    @Override
    public double evaluate (VariableDefinition ...variables) throws EvaluationException {
        switch (function) {
            case sin:
                return Math.sin(parameter.evaluate(variables));

            case cos:
                return Math.cos(parameter.evaluate(variables));

            case ln:
                return Math.log(parameter.evaluate(variables));

            case abs:
                return Math.abs(parameter.evaluate(variables));


            default:
                throw new UnknownError();
        }
    }

    @Override
    public String toString () {
        switch (function) {
            case sin:
                return "sin(" + parameter.toString() + ")";

            case cos:
                return "cos(" + parameter.toString() + ")";

            case ln:
                return "ln(" + parameter.toString() + ")";

            case abs:
                return "|" + parameter.toString() + "|";


            default:
                throw new UnknownError();
        }
    }

}
