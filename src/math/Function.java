package math;

import math.exception.EvaluationException;


public class Function extends Expression {

    public enum F {
        sin, cos, tan, ln, abs, sqrt, round, ceil, floor
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

            case tan:
                return Math.tan(parameter.evaluate(variables));

            case ln:
                return Math.log(parameter.evaluate(variables));

            case abs:
                return Math.abs(parameter.evaluate(variables));

            case sqrt:
                return Math.sqrt(parameter.evaluate(variables));

            case round:
                return Math.round(parameter.evaluate(variables));

            case ceil:
                return Math.ceil(parameter.evaluate(variables));

            case floor:
                return Math.floor(parameter.evaluate(variables));


            default:
                throw new UnknownError();
        }
    }

    @Override
    public String toString () {
        return function.name() + "(" + parameter.toString() + ")";
    }

}
