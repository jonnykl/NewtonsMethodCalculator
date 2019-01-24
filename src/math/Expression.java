package math;

import math.exception.EvaluationException;
import math.exception.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public abstract class Expression {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("^([0-9]+(\\.[0-9]*)?|\\.[0-9]+)$");


    public static void parse (String text) {
        text = text.replaceAll("\\s", "");

        try {
            List<ParseItem> items = parseBrackets(text);
            items = parseAllOperators(items);
            //printList(items, 0);
            Expression expression = parseExpressions(items);
            System.out.println(expression + " = " + expression.evaluate());
        } catch (ParseException | EvaluationException e) {
            e.printStackTrace();
        }
    }

    private static List<ParseItem> parseAllOperators (List<ParseItem> list) throws ParseException {
        List<ParseItem> newList = new ArrayList<>();
        for (ParseItem item : list) {
            if (item instanceof RawItem) {
                //System.out.println(">" + ((RawItem) item).raw);
                newList.addAll(parseOperators(((RawItem) item).raw));
            } else if (item instanceof BracketsItem) {
                //System.out.println("#bracket start");
                newList.add(new BracketsItem(parseAllOperators(((BracketsItem) item).list)));
                //newList.addAll(parseAllOperators(((BracketsItem) item).list));  // only for testing/debugging !!!
                //System.out.println("#bracket end: " + ((BracketsItem) newList.get(newList.size()-1)).list.size());
            } else {
                throw new UnknownError();
            }
        }

        return newList;
    }

    private static void printList (List<ParseItem> list, int depth) {
        System.out.println("printList: " + list.size() + ", " + depth);
        for (ParseItem item : list) {
            if (item instanceof RawItem) {
                for (int i=0; i<depth; i++)
                    System.out.print('\t');

                //System.out.print(":");
                System.out.println(((RawItem) item).raw.trim());
            } else if (item instanceof OperatorItem) {
                for (int i=0; i<depth; i++)
                    System.out.print('\t');

                System.out.println("operator: " + ((OperatorItem) item).operator.name());
            } else if (item instanceof BracketsItem) {
                printList(((BracketsItem) item).list, depth+1);
            } else {
                System.out.println("???");
            }
        }
    }


    private static void addSubstring (List<ParseItem> list, String str, int beginIndex) {
        addSubstring(list, str, beginIndex, -1);
    }

    private static void addSubstring (List<ParseItem> list, String str, int beginIndex, int endIndex) {
        if (endIndex == -1)
            endIndex = str.length();

        String substr = str.substring(beginIndex, endIndex);
        if (substr.length() > 0)
            list.add(new RawItem(substr));
    }


    private static Expression parseExpression (ParseItem item) throws ParseException {
        List<ParseItem> tmp = new ArrayList<>();
        tmp.add(item);
        return parseExpressions(tmp);
    }

    private static Expression parseExpressions (List<ParseItem> list) throws ParseException {
        if (list.size() == 0)
            throw new ParseException("unknown error", -1);


        // parse brackets recursively
        for (ParseItem item : list) {
            if (!(item instanceof BracketsItem))
                continue;


            BracketsItem bracketsItem = (BracketsItem) item;

            List<ParseItem> newList = new ArrayList<>();
            newList.add(new ExpressionItem(parseExpressions(bracketsItem.list)));

            bracketsItem.list = newList;
        }


        // TODO function


        // parse unary use of sign operator
        for (int i=0; i<list.size(); i++) {
            ParseItem item = list.get(i);
            if (!(item instanceof OperatorItem))
                continue;

            if (!Operator.SUBTRACTION.equals(((OperatorItem) item).operator))
                continue;

            if (i > 0 && i < list.size()-1) {
                ParseItem prevItem = list.get(i-1);
                if (prevItem instanceof OperatorItem) {
                    Operator operator = ((OperatorItem) prevItem).operator;
                    if (Operator.EXPONENTIATION.equals(operator)) {
                        ParseItem nextItem = list.get(i+1);

                        List<ParseItem> subList = new ArrayList<>();
                        subList.add(new ExpressionItem(new Scalar(-1)));
                        subList.add(new OperatorItem(Operator.MULTIPLICATION));
                        subList.add(nextItem);

                        list.add(i, new BracketsItem(subList));
                        list.remove(i+1);
                        list.remove(i+1);
                    }
                }
            }
        }

        // check operators
        for (int i=0; i<list.size(); i++) {
            ParseItem item = list.get(i);
            if (!(item instanceof OperatorItem))
                continue;

            if (i == 0) {
                if (list.size() == 1)
                    throw new ParseException("invalid syntax", -1);
            } else if (i == list.size()-1) {
                throw new ParseException("invalid syntax", -1);
            } else {
                ParseItem prevItem = list.get(i-1);
                if (prevItem instanceof OperatorItem)
                    throw new ParseException("invalid syntax", -1);
            }
        }


        // parse exponentiation
        for (int i=0; i<list.size(); i++) {
            ParseItem item = list.get(i);
            if (!(item instanceof OperatorItem))
                continue;

            if (!Operator.EXPONENTIATION.equals(((OperatorItem) item).operator))
                continue;

            if (i == 0 || i == list.size()-1)
                throw new ParseException("expected exponent", -1);

            ParseItem baseItem = list.get(i-1);
            ParseItem exponentItem = list.get(i+1);

            Expression base = parseExpression(baseItem);
            Expression exponent = parseExpression(exponentItem);
            list.add(i, new ExpressionItem(new Exponentiation(base, exponent)));

            list.remove(i+1);
            list.remove(i+1);
            list.remove(i-1);
        }


        // parse multiplication
        for (int i=0; i<list.size(); i++) {
            ParseItem item = list.get(i);
            if (!(item instanceof OperatorItem))
                continue;

            if (!Operator.MULTIPLICATION.equals(((OperatorItem) item).operator))
                continue;

            if (i == 0 || i == list.size()-1)
                throw new ParseException("expected multiplicand", -1);

            ParseItem multiplicand0Item = list.get(i-1);
            ParseItem multiplicand1Item = list.get(i+1);

            Expression multiplicand0 = parseExpression(multiplicand0Item);
            Expression multiplicand1 = parseExpression(multiplicand1Item);
            list.add(i, new ExpressionItem(new Multiplication(multiplicand0, multiplicand1)));

            list.remove(i+1);
            list.remove(i+1);
            list.remove(i-1);
        }


        // parse division
        for (int i=0; i<list.size(); i++) {
            ParseItem item = list.get(i);
            if (!(item instanceof OperatorItem))
                continue;

            if (!Operator.DIVISION.equals(((OperatorItem) item).operator))
                continue;

            if (i == 0 || i == list.size()-1)
                throw new ParseException("expected dividend or divisor", -1);

            ParseItem dividendItem = list.get(i-1);
            ParseItem divisorItem = list.get(i+1);

            Expression dividend = parseExpression(dividendItem);
            Expression divisor = parseExpression(divisorItem);
            list.add(i, new ExpressionItem(new Division(dividend, divisor)));

            list.remove(i+1);
            list.remove(i+1);
            list.remove(i-1);
        }


        // parse addition
        for (int i=0; i<list.size(); i++) {
            ParseItem item = list.get(i);
            if (!(item instanceof OperatorItem))
                continue;

            if (!Operator.ADDITION.equals(((OperatorItem) item).operator))
                continue;

            if (i == 0 || i == list.size()-1)
                throw new ParseException("expected addend", -1);

            ParseItem addend0Item = list.get(i-1);
            ParseItem addend1Item = list.get(i+1);

            Expression addend0 = parseExpression(addend0Item);
            Expression addend1 = parseExpression(addend1Item);
            list.add(i, new ExpressionItem(new Addition(addend0, addend1)));

            list.remove(i+1);
            list.remove(i+1);
            list.remove(i-1);
        }


        // parse subtraction
        for (int i=0; i<list.size(); i++) {
            ParseItem item = list.get(i);
            if (!(item instanceof OperatorItem))
                continue;

            if (!Operator.SUBTRACTION.equals(((OperatorItem) item).operator))
                continue;

            if (i == list.size()-1)
                throw new ParseException("expected subtrahend", -1);

            ParseItem minuendItem = i != 0 ? list.get(i-1) : null;
            ParseItem subtrahendItem = list.get(i+1);

            Expression minuend = minuendItem != null ? parseExpression(minuendItem) : new Scalar(0);
            Expression subtrahend = parseExpression(subtrahendItem);
            list.add(i, new ExpressionItem(new Substraction(minuend, subtrahend)));

            list.remove(i+1);
            list.remove(i+1);
            if (i != 0)
                list.remove(i-1);
        }



        return parseExpressions2(list);
    }

    private static Expression parseExpressions2 (List<ParseItem> list) throws ParseException {
        if (list.size() == 1) {
            ParseItem item = list.get(0);
            //System.out.println("item: " + item);
            if (item instanceof ExpressionItem) {
                return ((ExpressionItem) item).expression;
            } else if (item instanceof BracketsItem) {
                List<ParseItem> tmp = ((BracketsItem) item).list;
                //System.out.println("item->list: " + tmp.size());
                return parseExpressions2(tmp);
            } else if (item instanceof RawItem) {
                String raw = ((RawItem) item).raw;
                //System.out.println("item: " + raw);
                for (Constant.C constant : Constant.C.values()) {
                    if (raw.equals(constant.name()))
                        return new Constant(constant);
                }

                if (NUMBER_PATTERN.matcher(raw).matches())
                    return new Scalar(Double.parseDouble(raw));

                if (VariableDefinition.checkName(raw))
                    return new Variable(raw);
            }
        }

        /*
        System.out.println("list.size(): " + list.size());
        for (int i=0; i<list.size(); i++) {
            System.out.println("lst + " + i + ": " + list.get(i));
        }
        // */

        throw new ParseException("invalid syntax", -1);
    }

    private static List<ParseItem> parseOperators (String text) throws ParseException {
        List<ParseItem> parsed = new ArrayList<>();


        char[] operators = new char[]{'+', '-', '*', '/', '^'};

        int pos = 0;
        while (pos < text.length()) {
            int nextOperatorPos = -1;
            for (char operator : operators) {
                int tmp = text.indexOf(operator, pos);
                if (tmp == -1)
                    continue;

                if (nextOperatorPos == -1 || tmp < nextOperatorPos)
                    nextOperatorPos = tmp;
            }

            if (nextOperatorPos == -1)
                break;


            addSubstring(parsed, text, pos, nextOperatorPos);

            char operator = text.charAt(nextOperatorPos);
            if (operator == '+')
                parsed.add(new OperatorItem(Operator.ADDITION));
            else if (operator == '-')
                parsed.add(new OperatorItem(Operator.SUBTRACTION));
            else if (operator == '*')
                parsed.add(new OperatorItem(Operator.MULTIPLICATION));
            else if (operator == '/')
                parsed.add(new OperatorItem(Operator.DIVISION));
            else if (operator == '^')
                parsed.add(new OperatorItem(Operator.EXPONENTIATION));
            else
                throw new UnknownError();

            pos = nextOperatorPos+1;
        }

        addSubstring(parsed, text, pos);


        return parsed;
    }

    private static List<ParseItem> parseBrackets (String text) throws ParseException {
        /*
        operations with operators:
        - addition: +
        - subtraction: -
        - multiplication: *
        - division: /
        - exponentiation: ^

        TODO: var func

        precedence (high to low):
        1. ( )
        2. ^
        3. * /
        4. + -

        */

        List<ParseItem> parsed = new ArrayList<>();

        int pos = 0;

        while (pos < text.length()) {
            int openingBracketPos = text.indexOf('(', pos);
            int closingBracketPos = text.indexOf(')', pos);

            int bracketPos;
            boolean openingBracket;

            if (openingBracketPos != -1 && closingBracketPos != -1) {
                if (openingBracketPos < closingBracketPos) {
                    bracketPos = openingBracketPos;
                    openingBracket = true;
                } else {
                    bracketPos = closingBracketPos;
                    openingBracket = false;
                }
            } else if (openingBracketPos != -1 || closingBracketPos != -1) {
                throw new ParseException(openingBracketPos != -1 ? openingBracketPos : closingBracketPos);
            } else {
                break;
            }

            if (!openingBracket)
                throw new ParseException(bracketPos);

            int depth = 1;
            for (int i=openingBracketPos+1; i<text.length(); i++) {
                char c = text.charAt(i);
                if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    depth--;
                    if (depth == 0) {
                        closingBracketPos = i;
                        break;
                    }
                }
            }

            if (depth != 0)
                throw new ParseException(bracketPos);


            List<ParseItem> tmp = parseBrackets(text.substring(openingBracketPos+1, closingBracketPos));
            if (tmp.size() == 0)
                throw new ParseException(openingBracketPos);

            addSubstring(parsed, text, pos, openingBracketPos);
            parsed.add(new BracketsItem(tmp));
            pos = closingBracketPos+1;
        }

        addSubstring(parsed, text, pos);

        return parsed;
    }

    public String toSimplifiedString () {
        return simplify(toString());
    }


    private static String simplify (String expression) {
        // TODO simplify expression
        return null;
    }


    public abstract double evaluate(VariableDefinition ...variables) throws EvaluationException;
    public abstract String toString();


    private static abstract class ParseItem { }

    private static class ExpressionItem extends ParseItem {

        public Expression expression;


        public ExpressionItem (Expression expression) {
            this.expression = expression;
        }

    }

    private static class RawItem extends ParseItem {

        public String raw;


        public RawItem (String raw) {
            this.raw = raw;
        }

    }

    private static class BracketsItem extends ParseItem {

        public List<ParseItem> list;


        public BracketsItem (List<ParseItem> list) {
            this.list = list;
        }

    }

    private static class OperatorItem extends ParseItem {

        public Operator operator;


        public OperatorItem (Operator operator) {
            this.operator = operator;
        }

    }


    private enum Operator {
        ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION, EXPONENTIATION
    }

}
