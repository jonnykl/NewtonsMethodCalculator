import math.Expression;
import math.VariableDefinition;
import math.exception.EvaluationException;
import math.exception.ParseException;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class Main {

    public static void main (String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        //*
        String functionText = requestInput(reader, "enter function: ");
        String startValueText = requestInput(reader, "enter start value: ");
        String minimumPrecisionText = requestInput(reader, "enter minimum precision: ");
        String maximumIterationCountText = requestInput(reader, "enter maximum iteration count: ");
        // */
        /*
        //String functionText = "x^3 + 4x - 4";
        //String startValueText = "12";
        //String functionText = "ln(x^4 + 5x^3 - 5)";
        //String startValueText = "1.05";
        //String functionText = "x^3 - 5x^2 - 4x + 2";
        //String startValueText = "3";
        //String startValueText = "2.5";
        //String startValueText = "-2.5";
        //String startValueText = "8";
        //String functionText = "sin(x^2)*2^-x";
        //String startValueText = "-0.8";
        //String startValueText = "-1.2";
        //String startValueText = "1.2";
        String functionText = "e^ln((sin(sqrt(x)+pi)*cos(x))+1)*x^-1-0.05";
        //String startValueText = "3";
        //String startValueText = "4";
        String startValueText = "5";
        String minimumPrecisionText = "1e-5";
        String maximumIterationCountText = "100";
        // */

        try {
            Expression function = Expression.parse(functionText);
            double startValue = Double.parseDouble(startValueText);
            double minimumPrecision = Double.parseDouble(minimumPrecisionText);
            int maximumIterationCount = Integer.parseInt(maximumIterationCountText);

            NewtonsMethod newtonsMethod = new NewtonsMethod(function, "x", startValue, minimumPrecision, maximumIterationCount);
            Expression functionDerivative = newtonsMethod.getFunctionDerivative();

            System.out.println("function: " + newtonsMethod.getFunction());
            System.out.println("derivative: " + functionDerivative);
            System.out.println();


            List<Double> xValues = new ArrayList<>();
            List<Double> yValues = new ArrayList<>();

            xValues.add(startValue);
            yValues.add(function.evaluate(new VariableDefinition("x", startValue)));

            while (true) {
                boolean end = newtonsMethod.step();

                double x = newtonsMethod.getCurrentValueX();
                double y = newtonsMethod.getCurrentValueY();

                xValues.add(x);
                yValues.add(y);

                if (end)
                    break;
            }

            for (int i=0; i<xValues.size(); i++) {
                double x = xValues.get(i);
                double y = yValues.get(i);

                System.out.println("x: " + x);
                System.out.println("y: " + y);
                System.out.println();
            }

            NewtonsMethod.Error error = newtonsMethod.getError();
            System.out.println("error: " + error);


            if (NewtonsMethod.Error.SUCCESS.equals(error)) {
                double[] xValuesArr = new double[xValues.size()];
                for (int i=0; i<xValues.size(); i++)
                    xValuesArr[i] = xValues.get(i);

                double[] yValuesArr = new double[xValues.size()];
                for (int i=0; i<xValues.size(); i++)
                    yValuesArr[i] = yValues.get(i);


                NewtonsMethodPlotFrame frame = new NewtonsMethodPlotFrame(function, functionDerivative, xValuesArr, yValuesArr);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.pack();
                frame.setVisible(true);
            }
        } catch (ParseException | NumberFormatException | EvaluationException e) {
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
