import math.Expression;
import math.exception.ParseException;
import math.exception.UnknownVariableException;

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
        String functionText = "(x-1)^2 - 0.2";
        String startValueText = "15";
        String minimumPrecisionText = "1e-10";
        String maximumIterationCountText = "1000";
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

            while (true) {
                boolean end = newtonsMethod.step();

                double x = newtonsMethod.getCurrentValueX();
                double y = newtonsMethod.getCurrentValueY();

                xValues.add(x);
                yValues.add(y);

                System.out.println("x: " + x);
                System.out.println("y: " + y);
                System.out.println();

                if (end)
                    break;
            }

            boolean success = newtonsMethod.success();
            System.out.println("success: " + success);


            if (success) {
                double[] xValuesArr = new double[xValues.size()];
                for (int i=0; i<xValues.size(); i++)
                    xValuesArr[i] = xValues.get(i);

                double[] yValuesArr = new double[xValues.size()];
                for (int i=0; i<xValues.size(); i++)
                    yValuesArr[i] = yValues.get(i);


                NewtonsMethodPlotFrame frame = new NewtonsMethodPlotFrame(function, functionDerivative, xValuesArr, yValuesArr);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
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
