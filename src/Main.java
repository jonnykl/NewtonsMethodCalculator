import math.Expression;


public class Main {

    public static void main (String[] args) {
        Expression.parse("3 + sin(2 * pi) * 3");
        //Expression.parse("(3 + ((3 * 3)))");
        //Expression.parse("-3 * e^(-5.2 + 2*4) +2*pi^-2");

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
