package math;

import math.exception.EvaluationException;
import math.exception.UnknownVariableException;


public class Variable extends Expression {

    private String name;


    public Variable (String name) {
        setName(name);
    }


    public String getName () {
        return name;
    }

    public void setName (String name) {
        if (name == null)
            throw new NullPointerException("name may not be null");

        if (!VariableDefinition.checkName(name))
            throw new IllegalArgumentException("variable name contains illegal characters");


        this.name = name;
    }


    @Override
    public double evaluate (VariableDefinition ...variables) throws EvaluationException {
        if (variables != null) {
            for (VariableDefinition var : variables) {
                if (name.equals(var.getName())) {
                    return var.getValue().evaluate(variables);
                }
            }
        }

        throw new UnknownVariableException(name);
    }

    @Override
    public String toString () {
        return name;
    }

}
