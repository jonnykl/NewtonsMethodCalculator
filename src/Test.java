import math.Expression;
import math.ExpressionSimplifier;
import math.FunctionDerivative;
import math.exception.ParseException;


public class Test {

    public static void main (String[] args) {
        //test("3 + (sin(2 * pi))  (3)");
        //test("(3 + ((3 * 3)))");
        //test("(3) + 3 * 3");
        //test("(3 + 3) * 3");
        //test("-(3 * (((e)))^(-5.2 + 2*4) + tan(2*pi^-2)^(-1/2))");
        //test("3x^2");
        //test("3(x+2)^2 + 1/2x + 2");
        //test("ln(x)^2");
        //test("3x^2 + 1/2x - 5 + 1/(2x*5y) + 2sin(pi/2)");
        //test("x^x");
        //test("(x - 1)^2");
        //test("x^(2*1)/1 + 0 * y");
        //test("2^x");
        //test("(x-1)(x-2) * 3x^(2 + x - 2*3pi - (3 + 2 - 8))");
        //test("(4x-3)^2");
        //test("(3x^3 * 8 * e^ln(x)) / (x^(pi-2) * 2)");
        //test("((((x * 4.0) - 3.0)^2.0) * 8.0)");
        //test("8 / (4x - 3)");
        //test("(x - 1)^sqrt(x)");
        //test("x^2 - 2x + 1");
        //test("(((((1.0 * (1.0 / x)) * (2.0)) + (ln(x) * 0.0)) * (e^(ln(x) * 2.0))) - ((0.0 * (x)) + (2.0 * 1.0)) + 0.0)");
        //test("(((ln(tan(((pi^(-2.0)) * 2.0))) * (1.0 / 4.0)) * tan(((pi^(-2.0)) * 2.0))) * (-1.0))");
        //test("(2x + 1)^sin(x/2)");
        test("abs(sin(x^3)-0.2)");


        /*
        Expression expression0 = new MultiplicationList(
                new Scalar(1),
                new Constant(Constant.C.pi),
                new Function(Function.F.sin, new Division(
                        new Constant(Constant.C.pi),
                        new Scalar(2)
                )),
                new Scalar(-5)
        );
        Expression expression1 = new MultiplicationList(
                new Scalar(1),
                new Constant(Constant.C.pi),
                new Function(Function.F.sin, new Division(
                        new Constant(Constant.C.pi),
                        new Scalar(2)
                )),
                new Scalar(-5)
        );
        Expression expression2 = new MultiplicationList(
                new Scalar(1),
                new Constant(Constant.C.pi),
                new Function(Function.F.sin, new Division(
                        new Constant(Constant.C.pi),
                        new Scalar(2)
                )),
                new Scalar(-6)
        );

        System.out.println(expression0.equals(expression1) + ", " + expression0.equals(expression2));
        */
    }

    private static void test (String text) {
        try {
            Expression expression = Expression.parse(text);
            System.out.println(expression.toString());

            //Expression simplified = ExpressionSimplifier.simplify(expression);
            //System.out.println(simplified.toString());


            //*
            System.out.println();


            Expression derivative = FunctionDerivative.compute(expression, "x");
            System.out.println(derivative.toString());

            Expression simplifiedDerivative = ExpressionSimplifier.simplify(derivative);
            System.out.println(simplifiedDerivative.toString());
            // */
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
