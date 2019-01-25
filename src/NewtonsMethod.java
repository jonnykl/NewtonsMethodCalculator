import math.*;
import math.exception.EvaluationException;
import math.exception.UnknownVariableException;


public class NewtonsMethod {

    private Expression function;
    private Expression functionDerivative;
    private String variableName;

    private double startValue;
    private double minimumPrecision;
    private int maximumIterationCount;

    private int iterationCount;
    private double currentValueX, currentValueY;

    private boolean error;


    public NewtonsMethod (Expression function, String variableName, double startValue, double minimumPrecision, int maximumIterationCount) {
        setFunction(function, variableName);
        setStartValue(startValue);
        setMinimumPrecision(minimumPrecision);
        setMaximumIterationCount(maximumIterationCount);

        reset();
    }


    public Expression getFunction () {
        return function;
    }

    public String getVariableName () {
        return variableName;
    }

    public Expression getFunctionDerivative () {
        return functionDerivative;
    }

    public double getStartValue () {
        return startValue;
    }

    public double getMinimumPrecision () {
        return minimumPrecision;
    }

    public int getMaximumIterationCount () {
        return maximumIterationCount;
    }


    public double getCurrentValueX () {
        return currentValueX;
    }

    public double getCurrentValueY () {
        return currentValueY;
    }


    public boolean setFunction (Expression function, String variableName) {
        this.function = ExpressionSimplifier.simplify(function);
        this.variableName = variableName;

        return computeFunctionDerivative();
    }

    public void setFunctionDerivative (Expression functionDerivative) {
        this.functionDerivative = functionDerivative;
    }

    public void setStartValue (double startValue) {
        this.startValue = startValue;
    }

    public void setMinimumPrecision (double minimumPrecision) {
        this.minimumPrecision = minimumPrecision;
    }

    public void setMaximumIterationCount (int maximumIterationCount) {
        this.maximumIterationCount = maximumIterationCount;
    }


    public boolean computeFunctionDerivative () {
        try {
            functionDerivative = ExpressionSimplifier.simplify(FunctionDerivative.compute(function, variableName));
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
            functionDerivative = null;
        }

        return functionDerivative != null;
    }


    public void reset () {
        iterationCount = 0;

        currentValueX = startValue;
        currentValueY = 0;

        error = false;
    }

    public boolean step () throws UnknownVariableException {
        if (maximumIterationCount > 0 && iterationCount >= maximumIterationCount)
            return true;

        iterationCount++;


        VariableDefinition[] variables = new VariableDefinition[]{
                new VariableDefinition("x", new Scalar(currentValueX))
        };

        double a, b;

        try {
            a = function.evaluate(variables);
            //System.out.println("a: " + a);
            if (!Double.isFinite(a)) {
                error = true;
                return true;
            }

            if (a == 0)
                return true;

            b = functionDerivative.evaluate(variables);
            //System.out.println("b: " + b + " -> " + functionDerivative);
            if (b == 0 || !Double.isFinite(b)) {
                error = true;
                return true;
            }


            currentValueX = currentValueX - a/b;

            variables[0].setValue(new Scalar(currentValueX));
            currentValueY = function.evaluate(variables);
        } catch (EvaluationException e) {
            if (e instanceof UnknownVariableException)
                throw (UnknownVariableException) e;

            return true;
        }

        return Math.abs(currentValueY) <= minimumPrecision || (maximumIterationCount > 0 && iterationCount >= maximumIterationCount);
    }

    public void run () throws UnknownVariableException {
        while (true) {
            boolean stop = step();
            if (stop)
                break;
        }
    }


    public boolean success () {
        return iterationCount > 0 && !error && Math.abs(currentValueY) <= minimumPrecision;
    }

}
