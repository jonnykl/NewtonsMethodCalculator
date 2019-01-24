package math;

import math.exception.EvaluationException;
import math.exception.ParseException;


public abstract class Expression {

    public static Expression parse (String text) throws ParseException {
        return ExpressionParser.parse(text);
    }

    public String toSimplifiedString () {
        return simplify(toString());
    }


    private static String simplify (String expression) {
        // TODO simplify expression
        return null;
    }


    public abstract double evaluate(VariableDefinition ...variables) throws EvaluationException;
    public abstract String toString();
}
