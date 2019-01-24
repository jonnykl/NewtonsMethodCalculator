package math.exception;


public class UnknownVariableException extends EvaluationException {

    private String variableName;


    public UnknownVariableException (String variableName) {
        super("unknown variable '" + variableName + "'");
        this.variableName = variableName;
    }


    public String getVariableName () {
        return variableName;
    }

}
