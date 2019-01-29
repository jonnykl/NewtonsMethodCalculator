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
    private final double[] yValues;

    private double xMin = Double.POSITIVE_INFINITY;
    private double xMax = Double.NEGATIVE_INFINITY;
    private double xDiff = Double.NaN;

    private Thread updateThread = new Thread(new Updater());
    private DefaultXYDataset dataset = new DefaultXYDataset();


    public NewtonsMethodPlotFrame (Expression function, Expression functionDerivative, double[] xValues, double[] yValues) {
        super("Newton's method");


        this.function = function;
        this.functionDerivative = functionDerivative;
        this.xValues = xValues;
        this.yValues = yValues;

        for (double x : xValues) {
            if (x < xMin)
                xMin = x;

            if (x > xMax)
                xMax = x;
        }

        double diff = xMax-xMin;
        if (diff > 0) {
            xMin -= diff;
            xMax += diff;

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
        plotFunction(seriesKey, function, variableName, Double.NaN, Double.NaN);
    }

    private void plotFunction (Comparable seriesKey, Expression function, String variableName, double xMin, double xMax) throws EvaluationException {
        if (Double.isNaN(xDiff))
            return;

        if (!Double.isFinite(xMin) || xMin < this.xMin)
            xMin = this.xMin;

        if (!Double.isFinite(xMax) || xMax > this.xMax)
            xMax = this.xMax;

        if (xMax < xMin)
            return;


        VariableDefinition xVariable = new VariableDefinition(variableName, 0);

        double diff = xMax - xMin;
        int numXPoints = (int) (diff/this.xDiff * NUM_X_POINTS + 0.5);
        double[] xValues = new double[numXPoints];
        double[] yValues = new double[numXPoints];

        double step = diff / numXPoints;
        for (int i=0; i<numXPoints; i++) {
            double x = xMin + i*step;
            xVariable.setValue(new Scalar(x));

            xValues[i] = x;
            yValues[i] = function.evaluate(xVariable);
        }


        dataset.removeSeries(seriesKey);
        dataset.addSeries(seriesKey, new double[][]{xValues, yValues});
    }

    private void plotVerticalLine (Comparable seriesKey, double x, double yMin, double yMax) {
        if (Double.isNaN(xDiff) || !Double.isFinite(yMin) || !Double.isFinite(yMax) || yMin > yMax)
            return;


        double diff = yMax - yMin;
        int numYPoints = (int) (diff/this.xDiff * NUM_X_POINTS + 0.5);
        double[] xValues = new double[numYPoints];
        double[] yValues = new double[numYPoints];

        double step = diff / numYPoints;
        for (int i=0; i<numYPoints; i++) {
            xValues[i] = x;
            yValues[i] = yMin + i*step;
        }


        dataset.removeSeries(seriesKey);
        dataset.addSeries(seriesKey, new double[][]{xValues, yValues});
    }


    private double calcYMax (Expression function, String variableName) throws EvaluationException {
        return calcYMax(function, variableName, Double.NaN, Double.NaN);
    }

    private double calcYMax (Expression function, String variableName, double xMin, double xMax) throws EvaluationException {
        if (Double.isNaN(xDiff))
            return Double.NaN;

        if (!Double.isFinite(xMin) || xMin < this.xMin)
            xMin = this.xMin;

        if (!Double.isFinite(xMax) || xMax > this.xMax)
            xMax = this.xMax;

        if (xMax < xMin)
            return Double.NaN;


        VariableDefinition xVariable = new VariableDefinition(variableName, 0);

        double diff = xMax - xMin;
        int numXPoints = (int) (diff/this.xDiff * NUM_X_POINTS + 0.5);

        double yMax = Double.NEGATIVE_INFINITY;

        double step = diff / numXPoints;
        for (int i=0; i<numXPoints; i++) {
            double x = xMin + i*step;
            xVariable.setValue(new Scalar(x));

            double y = function.evaluate(xVariable);
            if (y > yMax)
                yMax = y;
        }

        return yMax;
    }


    private class Updater implements Runnable {

        @Override
        public void run () {
            if (xValues.length == 0)
                return;

            double stepDuration = 1e6 * ANIMATION_DURATION / xValues.length;
            int step = 0;

            VariableDefinition xVariable = new VariableDefinition("x", 0);
            double x, y, m, xMin, xMax, yMax;

            try {
                double functionYMax = calcYMax(function, "x");
                double functionDerivativeYMax = calcYMax(functionDerivative, "x");

                yMax = Math.max(functionYMax, functionDerivativeYMax);
            } catch (EvaluationException e) {
                e.printStackTrace();
                return;
            }


            long t;
            while (true) {
                t = System.nanoTime();

                if (step == xValues.length)
                    step = 0;


                x = xValues[step];
                y = yValues[step];

                xVariable.setValue(new Scalar(x));


                try {
                    /*
                    P(a|b)
                    y = mx + k
                    b = m*a + k
                    k = b-m*a
                    y = mx + b - ma

                    y = 0
                    0 = mx + b - ma
                    ma - b = mx
                    x = a - b/m
                    */

                    m = functionDerivative.evaluate(xVariable);
                    Expression tangent = new AdditionList(
                            new AdditionList.Addend(new MultiplicationList(
                                    new Scalar(m),
                                    new Variable("x")
                            )),
                            new AdditionList.Addend(new Scalar(y - m*x))
                    );

                    double x_0 = x - y/m;
                    xMin = x_0;
                    xMax = Double.NaN;

                    plotFunction("tangent", tangent, "x", xMin, xMax);


                    if (Double.isFinite(x_0))
                        plotVerticalLine("vertical line", x_0, 0, yMax);
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
