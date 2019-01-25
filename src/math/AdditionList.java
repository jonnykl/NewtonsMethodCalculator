package math;


import math.exception.EvaluationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AdditionList extends Expression {

    private List<Addend> addends = new ArrayList<>();


    public AdditionList (Addend ...addends) {
        setAddends(addends);
    }

    public AdditionList (Expression[] addends, boolean[] subtract) {
        setAddends(addends, subtract);
    }


    public Addend[] getAddends () {
        return addends.toArray(new Addend[0]);
    }


    public void setAddends (Addend ...addends) {
        this.addends.clear();
        Collections.addAll(this.addends, addends);
    }

    public void setAddends (Expression[] addends, boolean[] subtract) {
        if (subtract != null && addends.length != subtract.length)
            throw new IllegalArgumentException("different number of addends and subtract entries");

        this.addends.clear();
        for (int i=0; i<addends.length; i++)
            addAddend(new Addend(addends[i], subtract != null && subtract[i]));
    }

    public void addAddend (Addend addend) {
        this.addends.add(addend);
    }

    public void addAddend (Expression addend, boolean subtract) {
        this.addends.add(new Addend(addend, subtract));
    }

    public void removeAddend (int i) {
        this.addends.remove(i);
    }


    @Override
    public boolean equals (Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof AdditionList))
            return false;


        AdditionList additionList0 = this;
        AdditionList additionList1 = (AdditionList) obj;

        if (additionList0.addends.size() != additionList1.addends.size())
            return false;

        for (int i=0; i<additionList0.addends.size(); i++) {
            Addend addend0 = additionList0.addends.get(i);
            Addend addend1 = additionList1.addends.get(i);

            if (addend0.subtract != addend1.subtract)
                return false;

            if (!addend0.expression.equals(addend1.expression))
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
        double res = 0;
        for (Addend addend : this.addends) {
            double val = addend.expression.evaluate(variables);
            if (!addend.subtract)
                res += val;
            else
                res -= val;
        }

        return res;
    }

    @Override
    public String toString () {
        StringBuilder str = new StringBuilder("(");

        for (int i=0; i<this.addends.size(); i++) {
            Addend addend = this.addends.get(i);
            if (addend.subtract)
                str.append(" - ");
            else if (i > 0)
                str.append(" + ");

            str.append(addend.expression.toString());
        }

        return str + ")";
    }


    public static class Addend {

        public Expression expression;
        boolean subtract;


        public Addend (Expression expression) {
            this(expression, false);
        }

        public Addend (Expression expression, boolean subtract) {
            this.expression = expression;
            this.subtract = subtract;
        }

    }

}
