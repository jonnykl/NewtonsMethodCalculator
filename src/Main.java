

public class Main {

    public static void main (String[] args) {
        /*
        try {
            Expression function = Expression.parse("x^2 - 2x + 1");
            //Expression derivative = Expression.parse("2x - 2");
            NewtonsMethod newtonsMethod = new NewtonsMethod(function, "x", 0, 1e-20, 1000);
            //newtonsMethod.setFunctionDerivative(derivative);
            System.out.println("derivative: " + newtonsMethod.getFunctionDerivative());
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
        // */


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

}
