import math.Expression;
import math.Scalar;
import math.VariableDefinition;
import math.exception.EvaluationException;
import math.exception.UnknownVariableException;


public class NewtonsMethod {

    private Expression function;
    private Expression functionDerivative;

    private double startValue;
    private double minimumPrecision;
    private int maximumIterationCount;

    private int iterationCount;
    private double currentValue;


    public NewtonsMethod (Expression function, double startValue, double minimumPrecision, int maximumIterationCount) {
        setFunction(function);
        setStartValue(startValue);
        setMinimumPrecision(minimumPrecision);
        setMaximumIterationCount(maximumIterationCount);

        reset();
    }


    public Expression getFunction () {
        return function;
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

    public double getCurrentValue () {
        return currentValue;
    }


    public boolean setFunction (Expression function) {
        this.function = function;
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
        // TODO
        functionDerivative = null;

        return functionDerivative != null;
    }


    public void reset () {
        iterationCount = 0;
        currentValue = 0;
    }

    public boolean step () throws UnknownVariableException {
        if (iterationCount >= maximumIterationCount)
            return true;


        VariableDefinition[] variables = new VariableDefinition[]{
                new VariableDefinition("x", new Scalar(currentValue))
        };

        double a, b;

        try {
            a = function.evaluate(variables);
            if (a == 0)
                return true;

            b = functionDerivative.evaluate(variables);
            if (b == 0)
                return true;
        } catch (EvaluationException e) {
            if (e instanceof UnknownVariableException)
                throw (UnknownVariableException) e;

            return true;
        }


        currentValue = currentValue - a/b;

        return currentValue <= minimumPrecision;
    }

    public void run () throws UnknownVariableException {
        while (true) {
            boolean stop = step();
            if (stop)
                break;
        }
    }

}
