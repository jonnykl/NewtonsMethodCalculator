package math;


public class ExpressionSimplifier {

    private static final int MAX_ITERATIONS = 10000;


    public static Expression simplify (Expression expression) {
        Expression prevExpression;
        for (int i=0; i<MAX_ITERATIONS; i++) {
            prevExpression = expression;

            expression = removeNeutralElement(expression);
            expression = removeZeroMultiplication(expression);
            expression = simplifyExponentiation(expression);
            expression = evaluateBasicOperations(expression);
            expression = reduceFraction(expression);



            if (expression instanceof Addition) {
                expression = new Addition(
                        simplify(((Addition) expression).getAddend0()),
                        simplify(((Addition) expression).getAddend1())
                );
            } else if (expression instanceof Subtraction) {
                expression = new Subtraction(
                        simplify(((Subtraction) expression).getMinuend()),
                        simplify(((Subtraction) expression).getSubtrahend())
                );
            } else if (expression instanceof Multiplication) {
                expression = new Multiplication(
                        simplify(((Multiplication) expression).getMultiplicand0()),
                        simplify(((Multiplication) expression).getMultiplicand1())
                );
            } else if (expression instanceof Division) {
                expression = new Division(
                        simplify(((Division) expression).getDividend()),
                        simplify(((Division) expression).getDivisor())
                );
            } else if (expression instanceof Exponentiation) {
                expression = new Exponentiation(
                        simplify(((Exponentiation) expression).getBase()),
                        simplify(((Exponentiation) expression).getExponent())
                );
            } else if (expression instanceof Function) {
                expression = new Function(
                        ((Function) expression).getFunction(),
                        simplify(((Function) expression).getParameter())
                );
            } else if (expression instanceof AdditionList) {
                AdditionList.Addend[] addends = ((AdditionList) expression).getAddends();

                AdditionList additionList = new AdditionList();
                for (AdditionList.Addend addend : addends)
                    additionList.addAddend(new AdditionList.Addend(simplify(addend.expression), addend.subtract));

                expression = additionList;
            } else if (expression instanceof MultiplicationList) {
                Expression[] multiplicands = ((MultiplicationList) expression).getMultiplicands();

                MultiplicationList multiplicationList = new MultiplicationList();
                for (Expression multiplicand : multiplicands)
                    multiplicationList.addMultiplicand(simplify(multiplicand));

                expression = multiplicationList;
            }

            if (prevExpression.equals(expression))
                break;
        }

        return expression;


        /*

        ((((1.0 - 0.0) * (1.0 / (x - 1.0))) * 2.0) + (ln((x - 1.0)) * 0.0)) * (e^(ln((x - 1.0)) * 2.0))
        (((1 * (1.0 / (x - 1.0))) * 2.0) + (ln((x - 1.0)) * 0.0)) * (e^(ln((x - 1.0)) * 2.0))
        (((1 * (1 / (x - 1))) * 2) + (ln((x - 1)) * 0)) * (e^(ln((x - 1)) * 2))
        ((((1 / (x - 1))) * 2) + (ln(x - 1) * 0)) * (e^(ln((x - 1)) * 2))
        (((1 / (x - 1))) * 2) * (e^(ln((x - 1)) * 2))
        (2 / (x - 1)) * (e^(ln((x - 1)) * 2))
        2 * (x - 1)
        2x - 2

         */
    }



    private static Expression reduceFraction (Expression expression) {
        if (expression instanceof Division) {
            Expression dividend = ((Division) expression).getDividend();
            Expression divisor = ((Division) expression).getDivisor();

            // TODO: 2x/x = 2
        }

        return expression;
    }


    private static Expression evaluateBasicOperations (Expression expression) {
        if (expression instanceof Addition) {
            Expression addend0 = ((Addition) expression).getAddend0();
            Expression addend1 = ((Addition) expression).getAddend1();

            if (addend0 instanceof Scalar && addend1 instanceof Scalar)
                return new Scalar(((Scalar) addend0).getValue() + ((Scalar) addend1).getValue());
        } else if (expression instanceof Subtraction) {
            Expression minuend = ((Subtraction) expression).getMinuend();
            Expression subtrahend = ((Subtraction) expression).getSubtrahend();

            if (minuend instanceof Scalar && subtrahend instanceof Scalar)
                return new Scalar(((Scalar) minuend).getValue() - ((Scalar) subtrahend).getValue());
        } else if (expression instanceof Multiplication) {
            Expression multiplicand0 = ((Multiplication) expression).getMultiplicand0();
            Expression multiplicand1 = ((Multiplication) expression).getMultiplicand1();

            if (multiplicand0 instanceof Scalar && multiplicand1 instanceof Scalar)
                return new Scalar(((Scalar) multiplicand0).getValue() * ((Scalar) multiplicand1).getValue());
        } else if (expression instanceof Division) {
            Expression dividend = ((Division) expression).getDividend();
            Expression divisor = ((Division) expression).getDividend();

            if (dividend instanceof Scalar && divisor instanceof Scalar)
                return new Scalar(((Scalar) dividend).getValue() / ((Scalar) divisor).getValue());
        }

        return expression;
    }

    private static Expression simplifyExponentiation (Expression expression) {
        if (expression instanceof Exponentiation) {
            Expression base = ((Exponentiation) expression).getBase();
            Expression exponent = ((Exponentiation) expression).getExponent();

            if (base instanceof Scalar) {
                double baseValue = ((Scalar) base).getValue();
                if (baseValue == 0)
                    return new Scalar(0);

                if (baseValue == 1)
                    return new Scalar(1);
            }

            if (exponent instanceof Scalar) {
                double exponentValue = ((Scalar) exponent).getValue();
                if (exponentValue == 0)
                    return new Scalar(1);

                if (exponentValue == 1)
                    return base;
            }

            // TODO: e^(ln(x)*y) = x^y
        }

        return expression;
    }


    private static Expression removeZeroMultiplication (Expression expression) {
        if (expression instanceof Multiplication) {
            Expression multiplicand0 = ((Multiplication) expression).getMultiplicand0();
            Expression multiplicand1 = ((Multiplication) expression).getMultiplicand1();

            if ((multiplicand0 instanceof Scalar && ((Scalar) multiplicand0).getValue() == 0) ||
                    (multiplicand1 instanceof Scalar && ((Scalar) multiplicand1).getValue() == 0))
                return new Scalar(0);
        }

        return expression;
    }


    private static Expression removeNeutralElement (Expression expression) {
        if (expression instanceof Addition) {
            Expression addend0 = ((Addition) expression).getAddend0();
            Expression addend1 = ((Addition) expression).getAddend1();

            if (addend0 instanceof Scalar && ((Scalar) addend0).getValue() == 0)
                return addend1;

            if (addend1 instanceof Scalar && ((Scalar) addend1).getValue() == 0)
                return addend0;
        } else if (expression instanceof Subtraction) {
            Expression minuend = ((Subtraction) expression).getMinuend();
            Expression subtrahend = ((Subtraction) expression).getSubtrahend();

            if (minuend instanceof Scalar && ((Scalar) minuend).getValue() == 0) {
                return new Multiplication(
                        new Scalar(-1),
                        subtrahend
                );
            }

            if (subtrahend instanceof Scalar && ((Scalar) subtrahend).getValue() == 0)
                return minuend;
        } else if (expression instanceof Multiplication) {
            Expression multiplicand0 = ((Multiplication) expression).getMultiplicand0();
            Expression multiplicand1 = ((Multiplication) expression).getMultiplicand1();

            if (multiplicand0 instanceof Scalar && ((Scalar) multiplicand0).getValue() == 1)
                return multiplicand1;

            if (multiplicand1 instanceof Scalar && ((Scalar) multiplicand1).getValue() == 1)
                return multiplicand0;
        } else if (expression instanceof Division) {
            Expression dividend = ((Division) expression).getDividend();
            Expression divisor = ((Division) expression).getDivisor();

            if (divisor instanceof Scalar && ((Scalar) divisor).getValue() == 1)
                return dividend;
        } else if (expression instanceof AdditionList) {
            AdditionList.Addend[] addends = ((AdditionList) expression).getAddends();

            double allScalars = 0;

            AdditionList additionList = new AdditionList();
            for (AdditionList.Addend addend : addends) {
                Expression tmp = addend.expression;
                if (tmp instanceof Scalar) {
                    allScalars += (addend.subtract ? -1 : 1) * ((Scalar) tmp).getValue();
                    continue;
                }

                additionList.addAddend(addend);
            }

            if (allScalars != 0)
                additionList.addAddend(new AdditionList.Addend(new Scalar(allScalars < 0 ? -allScalars : allScalars), allScalars < 0));

            if (additionList.getAddends().length == 0)
                return new Scalar(0);
            else if (additionList.getAddends().length == 1 && !additionList.getAddends()[0].subtract)
                return additionList.getAddends()[0].expression;

            return additionList;
        } else if (expression instanceof MultiplicationList) {
            Expression[] multiplicands = ((MultiplicationList) expression).getMultiplicands();

            double allScalars = 1;

            MultiplicationList multiplicationList = new MultiplicationList();
            for (Expression multiplicand : multiplicands) {
                if (multiplicand instanceof Scalar) {
                    double value = ((Scalar) multiplicand).getValue();
                    if (value == 0)
                        return new Scalar(0);

                    allScalars *= value;
                    continue;
                }

                multiplicationList.addMultiplicand(multiplicand);
            }

            if (allScalars != 1)
                multiplicationList.addMultiplicand(new Scalar(allScalars));

            if (multiplicationList.getMultiplicands().length == 0)
                return new Scalar(1);
            else if (multiplicationList.getMultiplicands().length == 1)
                return multiplicationList.getMultiplicands()[0];

            return multiplicationList;
        }

        return expression;
    }

}
