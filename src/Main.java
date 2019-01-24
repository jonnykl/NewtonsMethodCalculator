import math.Expression;
import math.FunctionDerivative;
import math.exception.ParseException;
import math.exception.UnknownVariableException;


public class Main {

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

        try {
            Expression function = Expression.parse("x^2 - 2x + 1");
            Expression derivative = Expression.parse("2x - 2");
            NewtonsMethod newtonsMethod = new NewtonsMethod(function, "x", 0, 1e-20, 1000);
            newtonsMethod.setFunctionDerivative(derivative);
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

            Expression derivation = FunctionDerivative.compute(expression, "x");
            System.out.println(derivation.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
