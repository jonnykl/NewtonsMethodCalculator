import math.Expression;
import math.VariableDefinition;
import math.exception.EvaluationException;
import math.exception.ParseException;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class MainGUI {

    private JPanel pMain;
    private JTextField tfFunction;
    private JTextField tfStartValue;
    private JTextField tfMinimumPrecision;
    private JTextField tfMaximumIterationCount;
    private JButton btnUpdate;
    private JTextArea taRawValues;
    private JPanel pPlot;
    private JLabel lCurrentStepInfo;

    private NewtonsMethodPlotComponent newtonsMethodPlotComponent;


    public MainGUI () {
        tfFunction.setText("e^ln((sin(sqrt(x)+pi)*cos(x))+1)*x^-1-0.05");
        tfStartValue.setText("3");
        tfMinimumPrecision.setText("1e-5");
        tfMaximumIterationCount.setText("100");

        btnUpdate.addActionListener(actionEvent -> update());


        pPlot.setLayout(new BorderLayout());
    }


    private void update () {
        taRawValues.setText("");
        lCurrentStepInfo.setText("Wird geladen ...");
        pPlot.removeAll();

        if (newtonsMethodPlotComponent != null) {
            newtonsMethodPlotComponent.stop();
            newtonsMethodPlotComponent = null;
        }


        String functionText = tfFunction.getText();
        String startValueText = tfStartValue.getText();
        String minimumPrecisionText = tfMinimumPrecision.getText();
        String maximumIterationCountText = tfMaximumIterationCount.getText();

        try {
            Expression function = Expression.parse(functionText);
            double startValue = Double.parseDouble(startValueText);
            double minimumPrecision = Double.parseDouble(minimumPrecisionText);
            int maximumIterationCount = Integer.parseInt(maximumIterationCountText);

            NewtonsMethod newtonsMethod = new NewtonsMethod(function, "x", startValue, minimumPrecision, maximumIterationCount);
            Expression functionDerivative = newtonsMethod.getFunctionDerivative();

            taRawValues.append("\n");
            taRawValues.append("Funktion: " + newtonsMethod.getFunction() + "\n");
            taRawValues.append("Ableitung: " + functionDerivative + "\n");
            taRawValues.append("\n\n");


            List<Double> xValues = new ArrayList<>();
            List<Double> yValues = new ArrayList<>();

            double startValueY = function.evaluate(new VariableDefinition("x", startValue));
            if (!Double.isFinite(startValueY))
                throw new EvaluationException("value is not finite");

            xValues.add(startValue);
            yValues.add(startValueY);

            while (true) {
                boolean end = newtonsMethod.step();

                double x = newtonsMethod.getCurrentValueX();
                double y = newtonsMethod.getCurrentValueY();

                xValues.add(x);
                yValues.add(y);

                if (end)
                    break;
            }

            for (int i = 0; i < xValues.size(); i++) {
                double x = xValues.get(i);
                double y = yValues.get(i);

                taRawValues.append("#" + (i+1) + ": x: " + x + "\n");
                taRawValues.append("#" + (i+1) + ": y: " + y + "\n");
                taRawValues.append("\n");
            }

            taRawValues.append("\n");


            NewtonsMethod.Error error = newtonsMethod.getError();
            switch (error) {
                case SUCCESS:
                    taRawValues.append("Die Berechnung war erfolgreich!\n");
                    break;

                case FUNCTION_DERIVATIVE_ZERO:
                    taRawValues.append("Fehler: Die Ableitung ist im letzten Schritt 0!\n");
                    break;

                case EVALUATE_FUNCTION:
                    taRawValues.append("Fehler: Konnte Funktion im letzten Schritt nicht berechnen!\n");
                    break;

                case EVALUATE_FUNCTION_DERIVATIVE:
                    taRawValues.append("Fehler: Konnte Ableitungsfunktion im letzten Schritt nicht berechnen!\n");
                    break;

                case UNKNOWN_VARIABLE:
                    taRawValues.append("Fehler: Unbekannte Variable gefunden! Nur x ist erlaubt!\n");
                    break;

                case MAX_ITERATIONS_REACHED:
                    taRawValues.append("Fehler: Die maximale Anzahl der Iterationen wurde erreicht!\n");
                    break;
            }


            if (NewtonsMethod.Error.SUCCESS.equals(error)) {
                double[] xValuesArr = new double[xValues.size()];
                for (int i = 0; i < xValues.size(); i++)
                    xValuesArr[i] = xValues.get(i);

                double[] yValuesArr = new double[xValues.size()];
                for (int i = 0; i < xValues.size(); i++)
                    yValuesArr[i] = yValues.get(i);


                newtonsMethodPlotComponent = new NewtonsMethodPlotComponent(function, functionDerivative, xValuesArr, yValuesArr, lCurrentStepInfo);
                pPlot.add(newtonsMethodPlotComponent);
            }
        } catch (NumberFormatException e) {
            taRawValues.append("\n");
            taRawValues.append("Fehler: Mindestens eine Eingabe ist ungültig!\n");
        } catch (ParseException e) {
            taRawValues.append("\n");
            taRawValues.append("Fehler: Die eingegebene Formel ist ungültig!\n");
        } catch (EvaluationException e) {
            taRawValues.append("\n");
            taRawValues.append("Fehler: Konnte Funktion mit dem Startwert nicht berechnen!\n");
        } finally {
            if (newtonsMethodPlotComponent == null)
                lCurrentStepInfo.setText("");
        }


        pMain.invalidate();
        pMain.repaint();
    }


    public static void main (String[] args) {
        JFrame frame = new JFrame("Newton-Verfahren");
        frame.setContentPane((new MainGUI()).pMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.pack();
        frame.setVisible(true);
    }

}
