package math;


import java.util.regex.Pattern;


public class VariableDefinition {

    public static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z_]+$");

    public String name;
    public Expression value;


    public VariableDefinition (String name, Expression value) {
        setName(name);
        setValue(value);
    }


    public String getName () {
        return name;
    }

    public Expression getValue () {
        return value;
    }

    public void setName (String name) {
        if (name == null)
            throw new NullPointerException("name may not be null");

        if (!checkName(name))
            throw new IllegalArgumentException("variable name contains illegal characters");


        this.name = name;
    }

    public void setValue (Expression value) {
        if (value == null)
            throw new NullPointerException("value may not be null");

        this.value = value;
    }


    public static boolean checkName (String name) {
        return NAME_PATTERN.matcher(name).matches();
    }

}
