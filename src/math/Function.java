package math;

import math.exception.EvaluationException;


public class Function extends Expression {

    public enum F {
        sin, cos, tan, sinh, cosh, tanh, sech, csch, cot, coth, csc, sec, ln, abs, sqrt, round, ceil, floor
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
        double param = parameter.evaluate(variables);
        switch (function) {
            case sin:
                return Math.sin(param);

            case cos:
                return Math.cos(param);

            case tan:
                return Math.tan(param);

            case sinh:
                return Math.sinh(param);

            case cosh:
                return Math.cosh(param);

            case tanh:
                return Math.tanh(param);

            case sech:
                return 1/Math.cosh(param);

            case csch:
                return 1/Math.sinh(param);

            case cot:
                return 1/Math.tan(param);

            case coth:
                return 1/Math.tanh(param);

            case csc:
                return 1/Math.sin(param);

            case sec:
                return 1/Math.cos(param);

            case ln:
                return Math.log(param);

            case abs:
                return Math.abs(param);

            case sqrt:
                return Math.sqrt(param);

            case round:
                return Math.round(param);

            case ceil:
                return Math.ceil(param);

            case floor:
                return Math.floor(param);


            default:
                throw new UnknownError();
        }
    }

    @Override
    public String toString () {
        return function.name() + "(" + parameter.toString() + ")";
    }

}
