package math;


import java.util.ArrayList;
import java.util.List;


public class ExpressionSimplifier {

    private static final int MAX_ITERATIONS = 10000;


    public static Expression simplify (Expression expression) {
        Expression prevExpression;
        for (int i=0; i<MAX_ITERATIONS; i++) {
            prevExpression = expression;

            expression = simplifyLists(expression);
            expression = removeNeutralElement(expression);
            expression = removeZeroMultiplication(expression);
            expression = simplifyExponentiation(expression);
            expression = reduceFraction(expression);
            expression = mergeExponentiation(expression);
            expression = evaluateBasicOperations(expression);



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
                expression = new MultiplicationList(
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


    private static String indent (int n) {
        StringBuilder x = new StringBuilder();
        for (int i=0; i<n; i++)
            x.append("    ");

        return x.toString();
    }

    private static void indentPrint (String str, int n) {
        System.out.println(indent(n) + str);
    }

    private static void printExpression (Expression expression, int depth) {
        indentPrint(expression.getClass().getSimpleName() + " {", depth);

        if (expression instanceof Addition) {
            printExpression(((Addition) expression).getAddend0(), depth+1);
            printExpression(((Addition) expression).getAddend1(), depth+1);
        } else if (expression instanceof Subtraction) {
            printExpression(((Subtraction) expression).getMinuend(), depth+1);
            printExpression(((Subtraction) expression).getSubtrahend(), depth+1);
        } else if (expression instanceof Multiplication) {
            printExpression(((Multiplication) expression).getMultiplicand0(), depth+1);
            printExpression(((Multiplication) expression).getMultiplicand1(), depth+1);
        } else if (expression instanceof Division) {
            printExpression(((Division) expression).getDividend(), depth+1);
            printExpression(((Division) expression).getDivisor(), depth+1);
        } else if (expression instanceof Exponentiation) {
            printExpression(((Exponentiation) expression).getBase(), depth+1);
            printExpression(((Exponentiation) expression).getExponent(), depth+1);
        } else if (expression instanceof Function) {
            indentPrint(((Function) expression).getFunction().name(), depth+1);
            printExpression(((Function) expression).getParameter(), depth+1);
        } else if (expression instanceof AdditionList) {
            for (AdditionList.Addend addend : ((AdditionList) expression).getAddends()) {
                indentPrint(!addend.subtract ? "ADD" : "SUBTRACT", depth+1);
                printExpression(addend.expression, depth+1);
            }
        } else if (expression instanceof MultiplicationList) {
            for (Expression multiplicand : ((MultiplicationList) expression).getMultiplicands())
                printExpression(multiplicand, depth+1);
        } else if (expression instanceof Constant) {
            indentPrint(((Constant) expression).getConstant().name() + " -> " + ((Constant) expression).getValue(), depth+1);
        } else if (expression instanceof Variable) {
            indentPrint(((Variable) expression).getName(), depth+1);
        } else if (expression instanceof Scalar) {
            indentPrint(Double.toString(((Scalar) expression).getValue()), depth+1);
        } else {
            indentPrint("not yet implemented", depth+1);
        }

        indentPrint("}", depth);
    }


    private static Expression mergeExponentiation (Expression expression) {
        if (expression instanceof MultiplicationList) {
            List<Expression> dividendList = new ArrayList<>();
            List<Expression> divisorList = new ArrayList<>();

            splitFraction(dividendList, divisorList, expression);


            mergeMultiplicationExponentiation(dividendList);
            mergeMultiplicationExponentiation(divisorList);

            mergeDivisionExponentiation(dividendList, divisorList);


            if (divisorList.size() == 0) {
                if (dividendList.size() == 0)
                    return new Scalar(1);

                return new MultiplicationList(dividendList.toArray(new Expression[0]));
            }

            return new Division(
                    dividendList.size() > 0 ? new MultiplicationList(dividendList.toArray(new Expression[0])) : new Scalar(1),
                    new MultiplicationList(divisorList.toArray(new Expression[0]))
            );
        }

        return expression;
    }


    private static Expression reduceFraction (Expression expression) {
        if (expression instanceof Division) {
            List<Expression> dividendList = new ArrayList<>();
            List<Expression> divisorList = new ArrayList<>();

            splitFraction(dividendList, divisorList, expression);


            for (int i=0; i<dividendList.size(); i++) {
                Expression dividend = dividendList.get(i);
                for (int j=0; j<divisorList.size(); j++) {
                    Expression divisor = divisorList.get(j);
                    if (dividend.equals(divisor)) {
                        dividendList.remove(i);
                        divisorList.remove(i);

                        i--;
                        break;
                    }
                }
            }


            double allScalars = 1;
            for (int i=0; i<dividendList.size(); i++) {
                Expression dividend = dividendList.get(i);
                if (!(dividend instanceof  Scalar))
                    continue;

                double value = ((Scalar) dividend).getValue();
                allScalars *= value;

                dividendList.remove(i);
                i--;
            }

            for (int i=0; i<divisorList.size(); i++) {
                Expression divisor = divisorList.get(i);
                if (!(divisor instanceof  Scalar))
                    continue;

                double value = ((Scalar) divisor).getValue();
                if (value == 0)
                    continue;

                allScalars /= value;

                divisorList.remove(i);
                i--;
            }

            if (allScalars != 1)
                dividendList.add(new Scalar(allScalars));


            mergeMultiplicationExponentiation(dividendList);
            mergeMultiplicationExponentiation(divisorList);

            mergeDivisionExponentiation(dividendList, divisorList);


            if (divisorList.size() == 0) {
                if (dividendList.size() == 0)
                    return new Scalar(1);

                return new MultiplicationList(dividendList.toArray(new Expression[0]));
            }

            return new Division(
                    dividendList.size() > 0 ? new MultiplicationList(dividendList.toArray(new Expression[0])) : new Scalar(1),
                    new MultiplicationList(divisorList.toArray(new Expression[0]))
            );
        }

        return expression;
    }

    private static void mergeMultiplicationExponentiation (List<Expression> list) {
        for (int i=0; i<list.size(); i++) {
            Expression expression0 = list.get(i);
            Expression base0, exponent0;

            if (expression0 instanceof Exponentiation) {
                base0 = ((Exponentiation) expression0).getBase();
                exponent0 = ((Exponentiation) expression0).getExponent();
            } else {
                base0 = expression0;
                exponent0 = new Scalar(1);
            }

            for (int j=i+1; j<list.size(); j++) {
                Expression expression1 = list.get(j);
                Expression base1, exponent1;

                if (expression1 instanceof Exponentiation) {
                    base1 = ((Exponentiation) expression1).getBase();
                    exponent1 = ((Exponentiation) expression1).getExponent();
                } else {
                    base1 = expression1;
                    exponent1 = new Scalar(1);
                }

                if (base0.equals(base1)) {
                    exponent0 = evaluateBasicOperations(removeNeutralElement(new AdditionList(
                            new AdditionList.Addend(exponent0),
                            new AdditionList.Addend(exponent1)
                    )));

                    list.remove(j);
                    j--;
                }
            }

            if (exponent0 instanceof Scalar) {
                double value = ((Scalar) exponent0).getValue();
                if (value == 0)
                    list.set(i, new Scalar(1));
                else if (value == 1)
                    list.set(i, base0);
                else
                    list.set(i, new Exponentiation(base0, exponent0));
            } else {
                list.set(i, new Exponentiation(base0, exponent0));
            }
        }
    }

    private static void mergeDivisionExponentiation (List<Expression> dividend, List<Expression> divisor) {
        for (int i=0; i<dividend.size(); i++) {
            Expression expression0 = dividend.get(i);
            Expression base0, exponent0;

            if (expression0 instanceof Exponentiation) {
                base0 = ((Exponentiation) expression0).getBase();
                exponent0 = ((Exponentiation) expression0).getExponent();
            } else {
                base0 = expression0;
                exponent0 = new Scalar(1);
            }

            for (int j=0; j<divisor.size(); j++) {
                Expression expression1 = divisor.get(j);
                Expression base1, exponent1;

                if (expression1 instanceof Exponentiation) {
                    base1 = ((Exponentiation) expression1).getBase();
                    exponent1 = ((Exponentiation) expression1).getExponent();
                } else {
                    base1 = expression1;
                    exponent1 = new Scalar(1);
                }

                if (base0.equals(base1)) {
                    exponent0 = evaluateBasicOperations(removeNeutralElement(new AdditionList(
                            new AdditionList.Addend(exponent0, false),
                            new AdditionList.Addend(exponent1, true)
                    )));

                    divisor.remove(j);
                    j--;
                }
            }

            if (exponent0 instanceof Scalar) {
                double value = ((Scalar) exponent0).getValue();
                if (value == 0)
                    dividend.set(i, new Scalar(1));
                else if (value == 1)
                    dividend.set(i, base0);
                else
                    dividend.set(i, new Exponentiation(base0, exponent0));
            } else {
                dividend.set(i, new Exponentiation(base0, exponent0));
            }
        }
    }

    private static void splitFraction (List<Expression> list0, List<Expression> list1, Expression expression) {
        if (expression instanceof MultiplicationList) {
            for (Expression multiplicand : ((MultiplicationList) expression).getMultiplicands())
                splitFraction(list0, list1, multiplicand);
        } else if (expression instanceof Division) {
            splitFraction(list0, list1, ((Division) expression).getDividend());
            splitFraction(list1, list0, ((Division) expression).getDivisor());
        } else {
            list0.add(expression);
        }
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
            Expression divisor = ((Division) expression).getDivisor();

            if (dividend instanceof Scalar && divisor instanceof Scalar) {
                double dividendValue = ((Scalar) dividend).getValue();
                double divisorValue = ((Scalar) divisor).getValue();

                if (divisorValue != 0)
                    return new Scalar(dividendValue / divisorValue);
            }
        } else if (expression instanceof Exponentiation) {
            Expression base = ((Exponentiation) expression).getBase();
            Expression exponent = ((Exponentiation) expression).getExponent();

            if (base instanceof Scalar && exponent instanceof Scalar) {
                double value = Math.pow(((Scalar) base).getValue(), ((Scalar) exponent).getValue());
                if (Double.isFinite(value))
                    return new Scalar(value);
            }
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

            if (base instanceof Constant && Constant.C.e.equals(((Constant) base).getConstant())) {
                if (exponent instanceof MultiplicationList) {
                    Expression[] multiplicands = ((MultiplicationList) exponent).getMultiplicands();

                    Expression newBase = null;
                    Expression[] newMultiplicands = new Expression[multiplicands.length-1];
                    int idx = 0;

                    for (Expression multiplicand : multiplicands) {
                        if (multiplicand instanceof Function && Function.F.ln.equals(((Function) multiplicand).getFunction())) {
                            newBase = ((Function) multiplicand).getParameter();
                            continue;
                        }

                        if (idx == newMultiplicands.length)
                            break;

                        newMultiplicands[idx] = multiplicand;
                        idx++;
                    }

                    if (newBase != null) {
                        if (newMultiplicands.length == 0)
                            return newBase;

                        Expression newExponent;
                        if (newMultiplicands.length == 1)
                            newExponent = newMultiplicands[0];
                        else
                            newExponent = new MultiplicationList(newMultiplicands);

                        return new Exponentiation(newBase, newExponent);
                    }
                } else if (exponent instanceof Function && Function.F.ln.equals(((Function) exponent).getFunction())) {
                    return ((Function) exponent).getParameter();
                }
            }
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
        } else if (expression instanceof Division) {
            Expression dividend = ((Division) expression).getDividend();
            if (dividend instanceof Scalar && ((Scalar) dividend).getValue() == 0)
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
                return new AdditionList(
                        new AdditionList.Addend(subtrahend, true)
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
                    if (Double.isNaN(value))
                        return new Scalar(Double.NaN);

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

    
    
    public static Expression simplifyLists (Expression expression) {
        if (expression instanceof Addition) {
            Expression addend0 = ((Addition) expression).getAddend0();
            Expression addend1 = ((Addition) expression).getAddend1();

            addend0 = simplifyLists(addend0);
            addend1 = simplifyLists(addend1);

            AdditionList additionList = new AdditionList();

            if (addend0 instanceof AdditionList) {
                for (AdditionList.Addend addend : ((AdditionList) addend0).getAddends())
                    additionList.addAddend(addend);
            } else {
                additionList.addAddend(new AdditionList.Addend(addend0, false));
            }

            if (addend1 instanceof AdditionList) {
                for (AdditionList.Addend addend : ((AdditionList) addend1).getAddends())
                    additionList.addAddend(addend);
            } else {
                additionList.addAddend(new AdditionList.Addend(addend1, false));
            }

            return additionList;
        } else if (expression instanceof Subtraction) {
            Expression minuend = ((Subtraction) expression).getMinuend();
            Expression subtrahend = ((Subtraction) expression).getSubtrahend();

            minuend = simplifyLists(minuend);
            subtrahend = simplifyLists(subtrahend);

            AdditionList additionList = new AdditionList();

            if (minuend instanceof AdditionList) {
                for (AdditionList.Addend addend : ((AdditionList) minuend).getAddends())
                    additionList.addAddend(addend);
            } else {
                additionList.addAddend(new AdditionList.Addend(minuend, false));
            }

            if (subtrahend instanceof AdditionList) {
                for (AdditionList.Addend addend : ((AdditionList) subtrahend).getAddends())
                    additionList.addAddend(new AdditionList.Addend(addend.expression, !addend.subtract));
            } else {
                additionList.addAddend(new AdditionList.Addend(subtrahend, true));
            }

            return additionList;
        } else if (expression instanceof Multiplication) {
            Expression multiplicand0 = ((Multiplication) expression).getMultiplicand0();
            Expression multiplicand1 = ((Multiplication) expression).getMultiplicand1();

            multiplicand0 = simplifyLists(multiplicand0);
            multiplicand1 = simplifyLists(multiplicand1);

            MultiplicationList multiplicationList = new MultiplicationList();

            if (multiplicand0 instanceof MultiplicationList) {
                for (Expression multiplicand : ((MultiplicationList) multiplicand0).getMultiplicands())
                    multiplicationList.addMultiplicand(multiplicand);
            } else {
                multiplicationList.addMultiplicand(multiplicand0);
            }

            if (multiplicand1 instanceof MultiplicationList) {
                for (Expression multiplicand : ((MultiplicationList) multiplicand1).getMultiplicands())
                    multiplicationList.addMultiplicand(multiplicand);
            } else {
                multiplicationList.addMultiplicand(multiplicand1);
            }

            return multiplicationList;
        } else if (expression instanceof Division) {
            Expression dividend = ((Division) expression).getDividend();
            Expression divisor = ((Division) expression).getDivisor();

            return new Division(
                    simplifyLists(dividend),
                    simplifyLists(divisor)
            );
        } else if (expression instanceof Exponentiation) {
            Expression base = ((Exponentiation) expression).getBase();
            Expression exponent = ((Exponentiation) expression).getExponent();

            return new Exponentiation(
                    simplifyLists(base),
                    simplifyLists(exponent)
            );
        } else if (expression instanceof Function) {
            Function.F function = ((Function) expression).getFunction();
            Expression parameter = ((Function) expression).getParameter();

            return new Function(
                    function,
                    simplifyLists(parameter)
            );
        } else if (expression instanceof AdditionList) {
            AdditionList additionList = new AdditionList();
            addToAdditionList(additionList, expression, false);
            return additionList;
        } else if (expression instanceof MultiplicationList) {
            MultiplicationList multiplicationList = new MultiplicationList();
            addToMultiplicationList(multiplicationList, expression);
            return multiplicationList;
        }

        return expression;
    }

    private static void addToAdditionList (AdditionList additionList, Expression expression, boolean subtract) {
        if (expression instanceof Addition) {
            addToAdditionList(additionList, simplifyLists(((Addition) expression).getAddend0()), false);
            addToAdditionList(additionList, simplifyLists(((Addition) expression).getAddend1()), false);
        } else if (expression instanceof Subtraction) {
            addToAdditionList(additionList, simplifyLists(((Subtraction) expression).getMinuend()), false);
            addToAdditionList(additionList, simplifyLists(((Subtraction) expression).getSubtrahend()), true);
        } else if (expression instanceof AdditionList) {
            AdditionList.Addend[] addends = ((AdditionList) expression).getAddends();
            for (AdditionList.Addend addend : addends)
                addToAdditionList(additionList, simplifyLists(addend.expression), subtract != addend.subtract);
        } else {
            additionList.addAddend(new AdditionList.Addend(expression, subtract));
        }
    }

    private static void addToMultiplicationList (MultiplicationList multiplicationList, Expression expression) {
        if (expression instanceof Multiplication) {
            addToMultiplicationList(multiplicationList, simplifyLists(((Multiplication) expression).getMultiplicand0()));
            addToMultiplicationList(multiplicationList, simplifyLists(((Multiplication) expression).getMultiplicand1()));
        } else if (expression instanceof MultiplicationList) {
            Expression[] multiplicands = ((MultiplicationList) expression).getMultiplicands();
            for (Expression multiplicand : multiplicands)
                addToMultiplicationList(multiplicationList, simplifyLists(multiplicand));
        } else {
            multiplicationList.addMultiplicand(expression);
        }
    }

}
