package math;


import math.exception.EvaluationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MultiplicationList extends Expression {

    private List<Expression> multiplicands = new ArrayList<>();


    public MultiplicationList (Expression ...multiplicands) {
        setMultiplicands(multiplicands);
    }


    public Expression[] getMultiplicands () {
        return multiplicands.toArray(new Expression[0]);
    }


    public void setMultiplicands (Expression ...multiplicands) {
        this.multiplicands.clear();
        Collections.addAll(this.multiplicands, multiplicands);
    }

    public void addMultiplicand (Expression multiplicand) {
        this.multiplicands.add(multiplicand);
    }

    public void removeMultiplicand (int i) {
        this.multiplicands.remove(i);
    }


    @Override
    public boolean equals (Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof MultiplicationList))
            return false;


        MultiplicationList multiplicationList0 = this;
        MultiplicationList multiplicationList1 = (MultiplicationList) obj;

        if (multiplicationList0.multiplicands.size() != multiplicationList1.multiplicands.size())
            return false;

        for (int i=0; i<multiplicationList0.multiplicands.size(); i++) {
            Expression multiplicand0 = multiplicationList0.multiplicands.get(i);
            Expression multiplicand1 = multiplicationList1.multiplicands.get(i);

            if (!multiplicand0.equals(multiplicand1))
                return false;
        }

        return true;
    }

    @Override
    public int hashCode () {
        return 0;
    }


    @Override
    public double evaluate (VariableDefinition... variables) throws EvaluationException {
        double res = 1;
        for (Expression multiplicand : this.multiplicands)
            res *= multiplicand.evaluate(variables);

        return res;
    }

    @Override
    public String toString () {
        StringBuilder str = new StringBuilder("(");

        for (int i=0; i<this.multiplicands.size(); i++) {
            Expression multiplicand = this.multiplicands.get(i);
            if (i > 0)
                str.append(" * ");

            str.append(multiplicand.toString());
        }

        return str + ")";
    }

}
