package math;

import math.exception.EvaluationException;
import math.exception.ParseException;


public abstract class Expression {

    public static Expression parse (String text) throws ParseException {
        return ExpressionParser.parse(text);
    }


    @Override
    public boolean equals (Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof Expression))
            return false;


        Expression expression0 = this;
        Expression expression1 = (Expression) obj;

        if (expression0 instanceof Addition) {
            if (!(expression1 instanceof Addition))
                return false;

            return ((Addition) expression0).getAddend0().equals(((Addition) expression1).getAddend0()) &&
                    ((Addition) expression0).getAddend1().equals(((Addition) expression1).getAddend1());
        } else if (expression0 instanceof Subtraction) {
            if (!(expression1 instanceof Subtraction))
                return false;

            return ((Subtraction) expression0).getMinuend().equals(((Subtraction) expression1).getMinuend()) &&
                    ((Subtraction) expression0).getSubtrahend().equals(((Subtraction) expression1).getSubtrahend());
        } else if (expression0 instanceof Multiplication) {
            if (!(expression1 instanceof Multiplication))
                return false;

            return ((Multiplication) expression0).getMultiplicand0().equals(((Multiplication) expression1).getMultiplicand0()) &&
                    ((Multiplication) expression0).getMultiplicand1().equals(((Multiplication) expression1).getMultiplicand1());
        } else if (expression0 instanceof Division) {
            if (!(expression1 instanceof Division))
                return false;

            return ((Division) expression0).getDividend().equals(((Division) expression1).getDividend()) &&
                    ((Division) expression0).getDivisor().equals(((Division) expression1).getDivisor());
        } else if (expression0 instanceof Exponentiation) {
            if (!(expression1 instanceof Exponentiation))
                return false;

            return ((Exponentiation) expression0).getBase().equals(((Exponentiation) expression1).getBase()) &&
                    ((Exponentiation) expression0).getExponent().equals(((Exponentiation) expression1).getExponent());
        } else if (expression0 instanceof Function) {
            if (!(expression1 instanceof Function))
                return false;

            return ((Function) expression0).getFunction().equals(((Function) expression1).getFunction()) &&
                    ((Function) expression0).getParameter().equals(((Function) expression1).getParameter());
        } else if (expression0 instanceof Scalar) {
            if (!(expression1 instanceof Scalar))
                return false;

            double value0 = ((Scalar) expression0).getValue();
            double value1 = ((Scalar) expression1).getValue();

            if (Double.isNaN(value0) && Double.isNaN(value1))
                return true;

            if (!Double.isNaN(value0) && !Double.isNaN(value1))
                return value0 == value1;

            return false;
        } else if (expression0 instanceof Constant) {
            if (!(expression1 instanceof Constant))
                return false;

            return ((Constant) expression0).getConstant().equals(((Constant) expression1).getConstant());
        } else if (expression0 instanceof Variable) {
            if (!(expression1 instanceof Variable))
                return false;

            return ((Variable) expression0).getName().equals(((Variable) expression1).getName());
        } else if (expression0 instanceof AdditionList) {
            if (!(expression1 instanceof AdditionList))
                return false;

            return expression0.equals(expression1);
        } else if (expression0 instanceof MultiplicationList) {
            if (!(expression1 instanceof MultiplicationList))
                return false;

            return expression0.equals(expression1);
        } else {
            throw new UnknownError();
        }
    }

    @Override
    public int hashCode () {
        return 0;  // not implemented
    }


    public abstract double evaluate(VariableDefinition ...variables) throws EvaluationException;
    public abstract String toString();

}
