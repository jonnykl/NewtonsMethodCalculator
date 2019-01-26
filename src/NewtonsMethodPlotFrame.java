import math.*;
import math.exception.EvaluationException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultXYDataset;

import javax.swing.*;
import java.awt.*;


public class NewtonsMethodPlotFrame extends JFrame {

    private static final int NUM_X_POINTS = 1000;
    private static final int ANIMATION_DURATION = 10 * 1000;

    private final Expression function, functionDerivative;
    private final double[] xValues;

    private double xMin = Double.POSITIVE_INFINITY;
    private double xMax = Double.NEGATIVE_INFINITY;
    private double xDiff = Double.NaN;

    private Thread updateThread = new Thread(new Updater());
    private DefaultXYDataset dataset = new DefaultXYDataset();


    public NewtonsMethodPlotFrame (Expression function, Expression functionDerivative, double[] xValues) {
        super("Newton's method");


        this.function = function;
        this.functionDerivative = functionDerivative;
        this.xValues = xValues;

        for (double x : xValues) {
            if (x < xMin)
                xMin = x;

            if (x > xMax)
                xMax = x;
        }

        double diff = xMax-xMin;
        if (diff > 0) {
            xMin -= diff * 5;
            xMax += diff * 5;

            xDiff = xMax - xMin;
        }


        try {
            plotFunction("function", function, "x");
            plotFunction("derivative", functionDerivative, "x");
            plotFunction("x axis", new Scalar(0), "x");
        } catch (EvaluationException e) {
            e.printStackTrace();
        }


        JFreeChart chart = ChartFactory.createXYLineChart(null, "x", "y", dataset);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 640));

        setContentPane(chartPanel);
        updateThread.start();
    }


    private void plotFunction (Comparable seriesKey, Expression function, String variableName) throws EvaluationException {
        if (Double.isNaN(xDiff))
            return;


        VariableDefinition xVariable = new VariableDefinition(variableName, 0);

        double[] xValues = new double[NUM_X_POINTS];
        double[] yValues = new double[NUM_X_POINTS];

        double step = xDiff / NUM_X_POINTS;
        for (int i = 0; i< NUM_X_POINTS; i++) {
            double x = xMin + i*step;
            xVariable.setValue(new Scalar(x));

            xValues[i] = x;
            yValues[i] = function.evaluate(xVariable);
        }


        dataset.removeSeries(seriesKey);
        dataset.addSeries(seriesKey, new double[][]{xValues, yValues});
    }


    private class Updater implements Runnable {

        @Override
        public void run () {
            if (xValues.length == 0)
                return;

            double stepDuration = 1e6 * ANIMATION_DURATION / xValues.length;
            int step = 0;

            VariableDefinition xVariable = new VariableDefinition("x", 0);
            double x, y, m;

            long t;
            while (true) {
                t = System.nanoTime();

                if (step == xValues.length)
                    step = 0;


                x = xValues[step];
                xVariable.setValue(new Scalar(x));

                try {
                    /*
                    P(a|b)
                    y = mx + k
                    b = m*a + k
                    k = b-m*a
                    */

                    y = function.evaluate(xVariable);
                    m = functionDerivative.evaluate(xVariable);
                    Expression tangent = new AdditionList(
                            new AdditionList.Addend(new MultiplicationList(
                                    new Scalar(m),
                                    new Variable("x")
                            )),
                            new AdditionList.Addend(new Scalar(y - m*x))
                    );

                    plotFunction("tangent", tangent, "x");
                } catch (EvaluationException e) {
                    e.printStackTrace();
                }




                step++;

                try {
                    long sleepTime = (long) ((t+stepDuration-System.nanoTime())/1e6);
                    if (sleepTime > 0)
                        Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
