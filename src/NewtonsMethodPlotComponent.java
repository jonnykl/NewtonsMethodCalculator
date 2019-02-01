import math.*;
import math.exception.EvaluationException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultXYDataset;

import javax.swing.*;
import java.awt.*;
import java.util.List;


public class NewtonsMethodPlotComponent extends JComponent {

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


    private boolean stopped = false;
    private JLabel lCurrentStepInfo;


    public NewtonsMethodPlotComponent (Expression function, Expression functionDerivative, double[] xValues, double[] yValues, JLabel lCurrentStepInfo) {
        this.functionDerivative = functionDerivative;
        this.xValues = xValues;
        this.yValues = yValues;

        this.lCurrentStepInfo = lCurrentStepInfo;


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


        for (double y : yValues) {
            if (y < yMin)
                yMin = y;

            if (y > yMax)
                yMax = y;
        }

        diff = yMax-yMin;
        if (diff > 0) {
            yMin -= diff;
            yMax += diff;
        }


        try {
            plotFunction("Funktion", function, "x");
            plotFunction("Ableitung", functionDerivative, "x");
            plotFunction("x-Achse", new Scalar(0), "x");
        } catch (EvaluationException e) {
            e.printStackTrace();
        }


        JFreeChart chart = ChartFactory.createXYLineChart(null, "x", "y", dataset);

        setLayout(new BorderLayout());
        add(new ChartPanel(chart), BorderLayout.CENTER);
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
        if (!Double.isFinite(yMin) || !Double.isFinite(yMax) || yMin > yMax)
            return;


        double[] xValues = new double[]{x, x};
        double[] yValues = new double[]{yMin, yMax};

        dataset.removeSeries(seriesKey);
        dataset.addSeries(seriesKey, new double[][]{xValues, yValues});
    }


    public void stop () {
        stopped = true;
    }


    private class UpdaterWorker extends SwingWorker<Void, UpdaterWorker.PlotObject> {

        private PlotObject[][] plotObjects;


        @Override
        protected Void doInBackground () {
            if (xValues.length == 0)
                return null;


            double stepDuration = 1e6 * ANIMATION_DURATION / xValues.length;

            VariableDefinition xVariable = new VariableDefinition("x", 0);
            double x, y, m, xMin, xMax;


            plotObjects = new PlotObject[xValues.length][];
            for (int i=0; i<plotObjects.length; i++) {
                x = xValues[i];
                y = yValues[i];

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


                    PlotObject[] objs = new PlotObject[Double.isFinite(x_0) ? 3 : 2];

                    objs[0] = new PlotInfo(i, x, y);
                    objs[1] = new PlotFunctionObject("Tangente", tangent, "x", xMin, xMax);

                    if (Double.isFinite(x_0))
                        objs[2] = new PlotVerticalLineObject("vertikale Linie", x_0, yMin, yMax);


                    plotObjects[i] = objs;
                } catch (EvaluationException e) {
                    e.printStackTrace();
                }
            }


            long t;
            int step = 0;

            while (!stopped) {
                t = System.nanoTime();

                for (PlotObject obj : plotObjects[step])
                    publish(obj);

                step++;
                if (step == plotObjects.length)
                    step = 0;

                try {
                    long sleepTime = (long) ((t+stepDuration-System.nanoTime())/1e6);
                    if (sleepTime > 0)
                        Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            return null;
        }


        @Override
        protected void process (List<PlotObject> chunks) {
            if (chunks == null)
                return;

            for (PlotObject obj : chunks) {
                try {
                    if (obj instanceof PlotInfo)
                        lCurrentStepInfo.setText("<html>Iteration #" + (((PlotInfo) obj).iteration+1) + "<br>x: " + ((PlotInfo) obj).x + "<br>y: " + ((PlotInfo) obj).y + "</html>");
                    else if (obj instanceof PlotFunctionObject)
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

        private class PlotInfo extends PlotObject {

            int iteration;
            double x, y;


            public PlotInfo (int iteration, double x, double y) {
                super(null);

                this.iteration = iteration;
                this.x = x;
                this.y = y;
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
