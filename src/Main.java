import math.Expression;
import math.exception.EvaluationException;
import math.exception.ParseException;


public class Main {

    public static void main (String[] args) {
        //test("3 + (sin(2 * pi))  (3)");
        //test("(3 + ((3 * 3)))");
        //test("(3) + 3 * 3");
        //test("(3 + 3) * 3");
        //test("-(3 * (((e)))^(-5.2 + 2*4) + tan(2*pi^-2)^(-1/2))");
        test("3x^2");
        test("3x^2 + 1/2x - 5 + 1/(2x*5y) + 2sin(pi/2)");

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
            System.out.print(expression + " = ");
            System.out.println(expression.evaluate());
        } catch (ParseException | EvaluationException e) {
            e.printStackTrace();
        }
    }

}
