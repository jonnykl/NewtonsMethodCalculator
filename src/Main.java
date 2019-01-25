import math.Expression;
import math.exception.ParseException;
import math.exception.UnknownVariableException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Main {

    public static void main (String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String functionText = requestInput(reader, "enter function: ");
        String startValueText = requestInput(reader, "enter start value: ");
        String minimumPrecisionText = requestInput(reader, "enter minimum precision: ");
        String maximumIterationCountText = requestInput(reader, "enter maximum iteration count: ");

        try {
            Expression function = Expression.parse(functionText);
            double startValue = Double.parseDouble(startValueText);
            double minimumPrecision = Double.parseDouble(minimumPrecisionText);
            int maximumIterationCount = Integer.parseInt(maximumIterationCountText);

            NewtonsMethod newtonsMethod = new NewtonsMethod(function, "x", startValue, minimumPrecision, maximumIterationCount);

            System.out.println("function: " + newtonsMethod.getFunction());
            System.out.println("derivative: " + newtonsMethod.getFunctionDerivative());
            System.out.println();


            while (true) {
                boolean end = newtonsMethod.step();

                System.out.println("x: " + newtonsMethod.getCurrentValueX());
                System.out.println("y: " + newtonsMethod.getCurrentValueY());
                System.out.println();

                if (end)
                    break;
            }

            boolean success = newtonsMethod.success();
            System.out.println("success: " + success);
        } catch (ParseException | UnknownVariableException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private static String requestInput (BufferedReader in, String message) {
        String inputText;

        System.out.print(message);

        try {
            inputText = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;  // just to make IntelliJ happy
        }

        if (inputText == null)
            System.exit(0);

        return inputText;
    }

}
