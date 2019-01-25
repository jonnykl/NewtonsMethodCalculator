import math.Expression;
import math.ExpressionSimplifier;
import math.FunctionDerivative;
import math.exception.ParseException;


public class Main {

    public static void main (String[] args) {
        //test("3 + (sin(2 * pi))  (3)");
        //test("(3 + ((3 * 3)))");
        //test("(3) + 3 * 3");
        //test("(3 + 3) * 3");
        test("-(3 * (((e)))^(-5.2 + 2*4) + tan(2*pi^-2)^(-1/2))");
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

        //test("(((ln(tan(((pi^(-2.0)) * 2.0))) * (1.0 / 4.0)) * tan(((pi^(-2.0)) * 2.0))) * (-1.0))");


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

        /*
        try {
            Expression function = Expression.parse("x^2 - 2x + 1");
            //Expression derivative = Expression.parse("2x - 2");
            NewtonsMethod newtonsMethod = new NewtonsMethod(function, "x", 0, 1e-20, 1000);
            //newtonsMethod.setFunctionDerivative(derivative);
            System.out.println("derivative: " + newtonsMethod.getFunctionDerivative());
            while (true) {
                boolean end = newtonsMethod.step();

                System.out.println("x: " + newtonsMethod.getCurrentValueX());
                System.out.println("y: " + newtonsMethod.getCurrentValueY());
                System.out.println();

                if (end)
                    break;
            }
        } catch (ParseException | UnknownVariableException e) {
            e.printStackTrace();
        }
        // */


        /*
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String functionText;
        while (true) {
            System.out.print("enter function: ");

            try {
                functionText = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
                return;
            }

            if (functionText == null)
                System.exit(0);



        }
        */
    }

    private static void test (String text) {
        try {
            Expression expression = Expression.parse(text);
            System.out.println(expression.toString());

            Expression simplified = ExpressionSimplifier.simplify(expression);
            System.out.println(simplified.toString());


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
