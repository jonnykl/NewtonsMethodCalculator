import math.*;
import math.exception.EvaluationException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultXYDataset;

import javax.swing.*;
import java.util.List;


public class NewtonsMethodPlotFrame extends JFrame {

    private static final int NUM_X_POINTS = 1000;
    private static final int ANIMATION_DURATION = 5 * 1000;

    private final Expression functionDerivative;
    private final double[] xValues;
    private final double[] yValues;

    private double xMin = Double.POSITIVE_INFINITY;
    private double xMax = Double.NEGATIVE_INFINITY;
    private double xDiff = Double.NaN;
    private double yMin = Double.POSITIVE_INFINITY;
    private double yMax = Double.NEGATIVE_INFINITY;

    private DefaultXYDataset dataset = new DefaultXYDataset();


    public NewtonsMethodPlotFrame (Expression function, Expression functionDerivative, double[] xValues, double[] yValues) {
        super("Newton's method");


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
            double[] functionYMinMax = calcYMinMax(function, "x");
            if (functionYMinMax != null) {
                yMin = functionYMinMax[0];
                yMax = functionYMinMax[1];
            }
        } catch (EvaluationException e) {
            e.printStackTrace();
            return;
        }


        try {
            plotFunction("function", function, "x");
            plotFunction("derivative", functionDerivative, "x");
            plotFunction("x axis", new Scalar(0), "x");
        } catch (EvaluationException e) {
            e.printStackTrace();
        }


        JFreeChart chart = ChartFactory.createXYLineChart(null, "x", "y", dataset);

        setContentPane(new ChartPanel(chart));
        (new UpdaterWorker()).execute();
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
            double y = function.evaluate(xVariable);

            xValues[i] = x;
            yValues[i] = Math.min(Math.max(y, yMin), yMax);
        }


        dataset.removeSeries(seriesKey);
        dataset.addSeries(seriesKey, new double[][]{xValues, yValues});
    }

    private void plotVerticalLine (Comparable seriesKey, double x, double yMin, double yMax) {
        if (Double.isNaN(xDiff) || !Double.isFinite(yMin) || !Double.isFinite(yMax) || yMin > yMax)
            return;


        double diff = yMax - yMin;
        int numYPoints = (int) (diff/this.xDiff * NUM_X_POINTS + 0.5);
        if (numYPoints > 5*NUM_X_POINTS)
            numYPoints = 5*NUM_X_POINTS;


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


    private double[] calcYMinMax (Expression function, String variableName) throws EvaluationException {
        return calcYMinMax(function, variableName, Double.NaN, Double.NaN);
    }

    private double[] calcYMinMax (Expression function, String variableName, double xMin, double xMax) throws EvaluationException {
        if (Double.isNaN(xDiff))
            return null;

        if (!Double.isFinite(xMin) || xMin < this.xMin)
            xMin = this.xMin;

        if (!Double.isFinite(xMax) || xMax > this.xMax)
            xMax = this.xMax;

        if (xMax < xMin)
            return null;


        VariableDefinition xVariable = new VariableDefinition(variableName, 0);

        double diff = xMax - xMin;
        int numXPoints = (int) (diff/this.xDiff * NUM_X_POINTS + 0.5);

        double yMin = Double.POSITIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;

        double step = diff / numXPoints;
        for (int i=0; i<numXPoints; i++) {
            double x = xMin + i*step;
            xVariable.setValue(new Scalar(x));

            double y = function.evaluate(xVariable);
            if (y < yMin)
                yMin = y;

            if (y > yMax)
                yMax = y;
        }

        return new double[]{yMin, yMax};
    }


    private class UpdaterWorker extends SwingWorker<Void, UpdaterWorker.PlotObject> {

        @Override
        protected Void doInBackground () {
            if (xValues.length == 0)
                return null;

            double stepDuration = 1e6 * ANIMATION_DURATION / xValues.length;
            int step = 0;

            VariableDefinition xVariable = new VariableDefinition("x", 0);
            double x, y, m, xMin, xMax;


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

                    y = c
                    c = mx + b - ma
                    c + ma - b = mx
                    x = c/m + a - b/m
                    x = a + (c-b)/m
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
                    if (m != 0) {
                        xMin = x + (yMin - y) / m;
                        xMax = x + (yMax - y) / m;

                        if (m < 0) {
                            double tmp = xMin;
                            xMin = xMax;
                            xMax = tmp;
                        }
                    } else {
                        xMin = Double.NaN;
                        xMax = Double.NaN;
                    }

                    publish(new PlotFunctionObject("tangent", tangent, "x", xMin, xMax));


                    if (Double.isFinite(x_0))
                        publish(new PlotVerticalLineObject("vertical line", x_0, yMin, yMax));
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


        @Override
        protected void process (List<PlotObject> chunks) {
            if (chunks == null)
                return;

            for (PlotObject obj : chunks) {
                try {
                    if (obj instanceof PlotFunctionObject)
                        plotFunction(obj.seriesKey, ((PlotFunctionObject) obj).function, ((PlotFunctionObject) obj).variableName, ((PlotFunctionObject) obj).xMin, ((PlotFunctionObject) obj).xMax);
                    else if (obj instanceof  PlotVerticalLineObject)
                        plotVerticalLine(obj.seriesKey, ((PlotVerticalLineObject) obj).x, ((PlotVerticalLineObject) obj).yMin, ((PlotVerticalLineObject) obj).yMax);
                } catch (EvaluationException e) {
                    e.printStackTrace();
                }
            }
        }


        private abstract class PlotObject {

            Comparable seriesKey;


            PlotObject (Comparable seriesKey) {
                this.seriesKey = seriesKey;
            }

        }

        private class PlotFunctionObject extends PlotObject {

            Expression function;
            String variableName;
            double xMin, xMax;


            PlotFunctionObject (Comparable seriesKey, Expression function, String variableName, double xMin, double xMax) {
                super(seriesKey);

                this.function = function;
                this.variableName = variableName;
                this.xMin = xMin;
                this.xMax = xMax;
            }

        }

        private class PlotVerticalLineObject extends PlotObject {

            double x;
            double yMin, yMax;


            PlotVerticalLineObject (Comparable seriesKey, double x, double yMin, double yMax) {
                super(seriesKey);

                this.x = x;
                this.yMin = yMin;
                this.yMax = yMax;
            }

        }

    }

}
