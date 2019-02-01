package math;


public class FunctionDerivative {

    public static Expression compute (Expression function, String variableName) {
        if (function instanceof Addition)
            return addition((Addition) function, variableName);
        else if (function instanceof Subtraction)
            return subtraction((Subtraction) function, variableName);
        else if (function instanceof Variable)
            return variable((Variable) function, variableName);
        else if (function instanceof Exponentiation)
            return exponentiation((Exponentiation) function, variableName);
        else if (function instanceof Multiplication)
            return multiplication((Multiplication) function, variableName);
        else if (function instanceof Division)
            return division((Division) function, variableName);
        else if (function instanceof Scalar)
            return scalar((Scalar) function, variableName);
        else if (function instanceof Constant)
            return constant((Constant) function, variableName);
        else if (function instanceof Function)
            return function((Function) function, variableName);
        else if (function instanceof AdditionList)
            return additionList((AdditionList) function, variableName);
        else if (function instanceof MultiplicationList)
            return multiplicationList((MultiplicationList) function, variableName);

        throw new UnknownError("unknown expression: " + function.getClass().getName());
    }


    private static Expression addition (Addition function, String variableName) {
        return new AdditionList(
                new AdditionList.Addend(compute(function.getAddend0(), variableName)),
                new AdditionList.Addend(compute(function.getAddend1(), variableName))
        );
    }

    private static Expression subtraction (Subtraction function, String variableName) {
        return new AdditionList(
                new AdditionList.Addend(compute(function.getMinuend(), variableName), false),
                new AdditionList.Addend(compute(function.getSubtrahend(), variableName), true)
        );
    }

    private static Expression variable (Variable function, String variableName) {
        if (variableName.equals(function.getName()))
            return new Scalar(1);

        return new Scalar(0);
    }

    private static Expression exponentiation (Exponentiation function, String variableName) {
        Expression base = function.getBase();
        Expression exponent = function.getExponent();

        if (base instanceof Constant && Constant.C.e.equals(((Constant) base).getConstant())) {
            return new MultiplicationList(
                    compute(exponent, variableName),
                    function
            );
        } else {
            return exponentiation(new Exponentiation(
                    new Constant(Constant.C.e),
                    new MultiplicationList(
                            new Function(Function.F.ln, base),
                            exponent
                    )
            ), variableName);
        }
    }

    private static Expression multiplication (Multiplication function, String variableName) {
        Expression multiplicand0 = function.getMultiplicand0();
        Expression multiplicand1 = function.getMultiplicand1();

        // (a*b)' = (a' * b) + (a * b')
        return new AdditionList(
                new AdditionList.Addend(new MultiplicationList(compute(multiplicand0, variableName), multiplicand1)),
                new AdditionList.Addend(new MultiplicationList(multiplicand0, compute(multiplicand1, variableName)))
        );
    }

    private static Expression division (Division function, String variableName) {
        Expression dividend = function.getDividend();
        Expression divisor = function.getDivisor();

        // (a/b)' = ((a' * b) - (a * b')) / b^2
        return new Division(
                new AdditionList(
                        new AdditionList.Addend(new MultiplicationList(compute(dividend, variableName), divisor), false),
                        new AdditionList.Addend(new MultiplicationList(dividend, compute(divisor, variableName)), true)
                ),
                new MultiplicationList(divisor, divisor)
        );
    }

    private static Expression scalar (Scalar function, String variableName) {
        return new Scalar(0);
    }

    private static Expression constant (Constant function, String variableName) {
        return new Scalar(0);
    }

    private static Expression function (Function function, String variableName) {
        Expression parameter = function.getParameter();
        Expression functionDerivative;

        Function.F f = function.getFunction();
        if (Function.F.sin.equals(f)) {
            functionDerivative = new Function(Function.F.cos, parameter);
        } else if (Function.F.cos.equals(f)) {
            functionDerivative = new MultiplicationList(
                    new Scalar(-1),
                    new Function(Function.F.sin, parameter)
            );
        } else if (Function.F.tan.equals(f)) {
            functionDerivative = new AdditionList(
                    new AdditionList.Addend(new MultiplicationList(
                            new Function(Function.F.tan, parameter),
                            new Function(Function.F.tan, parameter)
                    )),
                    new AdditionList.Addend(new Scalar(1))
            );
        } else if (Function.F.sinh.equals(f)) {
            functionDerivative = new Function(Function.F.cosh, parameter);
        } else if (Function.F.cosh.equals(f)) {
            functionDerivative = new Function(Function.F.sinh, parameter);
        } else if (Function.F.tanh.equals(f)) {
            functionDerivative = new Exponentiation(
                    new Function(Function.F.sech, parameter),
                    new Scalar(2)
            );
        } else if (Function.F.sech.equals(f)) {
            functionDerivative = new MultiplicationList(
                    new Scalar(-1),
                    new Function(Function.F.sech, parameter),
                    new Function(Function.F.tanh, parameter)
            );
        } else if (Function.F.csch.equals(f)) {
            functionDerivative = new MultiplicationList(
                    new Scalar(-1),
                    new Function(Function.F.coth, parameter),
                    new Function(Function.F.csch, parameter)
            );
        } else if (Function.F.cot.equals(f)) {
            functionDerivative = new Exponentiation(
                    new Function(Function.F.csc, parameter),
                    new Scalar(2)
            );
        } else if (Function.F.coth.equals(f)) {
            functionDerivative = new MultiplicationList(
                    new Scalar(-1),
                    new Exponentiation(
                            new Function(Function.F.csch, parameter),
                            new Scalar(2)
                    )
            );
        } else if (Function.F.ln.equals(f)) {
            functionDerivative = new Division(
                    new Scalar(1),
                    parameter
            );
        } else if (Function.F.sqrt.equals(f)) {
            functionDerivative = new Division(
                    new Scalar(1),
                    new MultiplicationList(
                            new Scalar(2),
                            new Function(Function.F.sqrt, parameter)
                    )
            );
        } else if (Function.F.abs.equals(f)) {
            functionDerivative = new Division(
                    parameter,
                    new Function(Function.F.abs, parameter)
            );
        } else {
            // round, ceil, floor
            throw new UnsupportedOperationException("not yet implemented");
        }

        return new MultiplicationList(
                compute(parameter, variableName),
                functionDerivative
        );
    }


    private static Expression additionList (AdditionList function, String variableName) {
        AdditionList additionList = new AdditionList();
        for (AdditionList.Addend addend : function.getAddends())
            additionList.addAddend(new AdditionList.Addend(compute(addend.expression, variableName), addend.subtract));

        return additionList;
    }

    private static Expression multiplicationList (MultiplicationList function, String variableName) {
        Expression[] multiplicands = function.getMultiplicands();
        if (multiplicands.length == 1)
            return compute(multiplicands[0], variableName);

        Expression[] newMultiplicands = new Expression[multiplicands.length-1];
        System.arraycopy(multiplicands, 1, newMultiplicands, 0, multiplicands.length-1);

        return multiplication(new Multiplication(
                multiplicands[0],
                new MultiplicationList(newMultiplicands)
        ), variableName);
    }

}
