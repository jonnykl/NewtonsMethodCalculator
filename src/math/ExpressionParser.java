package math;


import math.exception.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ExpressionParser {

    private static final Pattern NUMBER_START_PATTERN = Pattern.compile("^([0-9]+(\\.[0-9]*)?|\\.[0-9]+).*$");
    private static final char[] ALL_OPERATORS = new char[]{'+', '-', '*', '/', '^'};


    public static Expression parse (String text) throws ParseException {
        // remove all whitespaces
        text = text.replaceAll("\\s", "");

        /*

        - parseBrackets:        parse brackets and create list with the text splitted by brackets
        - parseAllOperators:    split text elements of the list by operators
        - parseExpressions:     parse all expressions within each brackets
        - simplifyLists         simplify concatenated additions/subtractions and multiplications

         */

        List<ParseItem> items = parseBrackets(text);
        items = parseAllOperators(items);
        //printList(items, 0);
        Expression expression = parseExpressions(items);
        expression = ExpressionSimplifier.simplifyLists(expression);

        return expression;
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

    // method for debugging purposes
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
            } else if (item instanceof ExpressionItem) {
                for (int i=0; i<depth; i++)
                    System.out.print('\t');

                System.out.println("expression: " + ((ExpressionItem) item).expression);
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


        /*

        steps in this function:
        - parse brackets recursively (results in a almost flat list (maximum depth = 1))
        - parse scalars
        - parse functions
        - remove all brackets (results in a flat list)
        - parse constants, variables
        - parse unary use of sign operator
        - check operators
        - parse exponentiation
        - parse multiplication/division
        - merge expressions without operator between them (multiplication)
        - parse addition/subtraction

         */



        // parse brackets recursively
        for (ParseItem item : list) {
            if (!(item instanceof BracketsItem))
                continue;


            BracketsItem bracketsItem = (BracketsItem) item;

            List<ParseItem> newList = new ArrayList<>();
            newList.add(new ExpressionItem(parseExpressions(bracketsItem.list)));

            bracketsItem.list = newList;
        }

        // parse scalars
        for (int i=0; i<list.size(); i++) {
            ParseItem item = list.get(i);
            if (!(item instanceof RawItem))
                continue;


            String raw = ((RawItem) item).raw;
            //System.out.println(": item: " + raw);

            Matcher m = NUMBER_START_PATTERN.matcher(raw);
            if (m.matches()) {
                String scalar = m.group(1);
                String tmp = raw.substring(scalar.length());

                list.add(i, new ExpressionItem(new Scalar(Double.parseDouble(scalar))));
                if (tmp.length() > 0) {
                    list.add(i+1, new OperatorItem(Operator.MULTIPLICATION));
                    list.add(i+2, new RawItem(tmp));
                    list.remove(i+3);
                } else {
                    list.remove(i+1);
                }

            }
        }

        //System.out.println("-----------------------------------------------------------");
        //printList(list, 0);
        //System.out.println("-----------------------------------------------------------");


        // parse functions
        for (int i=0; i<list.size(); i++) {
            ParseItem item = list.get(i);
            if (!(item instanceof RawItem))
                continue;

            if (i == list.size()-1)
                continue;

            ParseItem nextItem = list.get(i+1);
            if (!(nextItem instanceof BracketsItem))
                continue;

            String name = ((RawItem) item).raw;
            for (Function.F function : Function.F.values()) {
                if  (name.equals(function.name())) {
                    Expression parameter = parseExpressions(((BracketsItem) nextItem).list);

                    list.add(i, new ExpressionItem(new Function(function, parameter)));
                    list.remove(i+1);
                    list.remove(i+1);

                    break;
                }
            }
        }


        // remove all brackets
        for (int i=0; i<list.size(); i++) {
            ParseItem item = list.get(i);
            if (!(item instanceof BracketsItem))
                continue;

            List<ParseItem> tmp = ((BracketsItem) item).list;
            list.addAll(i, tmp);
            list.remove(i+tmp.size());
            i--;
        }


        // parse constants, variables
        for (int i=0; i<list.size(); i++) {
            ParseItem item = list.get(i);
            if (!(item instanceof RawItem))
                continue;


            Expression replacement = null;

            String raw = ((RawItem) item).raw;
            //System.out.println(": item: " + raw);
            for (Constant.C constant : Constant.C.values()) {
                if (raw.equals(constant.name()))
                    replacement = new Constant(constant);
            }

            if (replacement == null && VariableDefinition.checkName(raw))
                replacement = new Variable(raw);


            if (replacement != null) {
                list.add(i, new ExpressionItem(replacement));
                list.remove(i+1);
            }
        }


        // parse unary use of sign operator
        for (int i=0; i<list.size(); i++) {
            ParseItem item = list.get(i);
            if (!(item instanceof OperatorItem))
                continue;

            if (!Operator.SUBTRACTION.equals(((OperatorItem) item).operator))
                continue;

            ParseItem replacementItem = null;
            if (i == 0) {
                if (list.size() > 1) {
                    ParseItem nextItem = list.get(i+1);
                    if (nextItem instanceof OperatorItem)
                        continue;

                    replacementItem = nextItem;
                }
            } else if (i < list.size()-1) {
                ParseItem prevItem = list.get(i-1);
                if (prevItem instanceof OperatorItem) {
                    Operator operator = ((OperatorItem) prevItem).operator;
                    if (Operator.EXPONENTIATION.equals(operator))
                        replacementItem = list.get(i+1);
                }
            }

            if (replacementItem != null) {
                Expression replacementExpression = parseExpression(replacementItem);

                list.add(i, new ExpressionItem(new Multiplication(new Scalar(-1), replacementExpression)));
                list.remove(i+1);
                list.remove(i+1);
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
            i--;
        }


        // parse multiplication/division
        for (int i=0; i<list.size(); i++) {
            ParseItem item = list.get(i);
            if (!(item instanceof OperatorItem))
                continue;


            Expression expression;

            Operator operator = ((OperatorItem) item).operator;
            if (Operator.MULTIPLICATION.equals(operator)) {
                if (i == 0 || i == list.size()-1)
                    throw new ParseException("expected multiplicand", -1);

                ParseItem multiplicand0Item = list.get(i-1);
                ParseItem multiplicand1Item = list.get(i+1);

                Expression multiplicand0 = parseExpression(multiplicand0Item);
                Expression multiplicand1 = parseExpression(multiplicand1Item);

                expression = new Multiplication(multiplicand0, multiplicand1);
            } else if (Operator.DIVISION.equals(operator)) {
                if (i == 0 || i == list.size()-1)
                    throw new ParseException("expected dividend or divisor", -1);

                ParseItem dividendItem = list.get(i-1);
                ParseItem divisorItem = list.get(i+1);

                Expression dividend = parseExpression(dividendItem);
                Expression divisor = parseExpression(divisorItem);

                expression = new Division(dividend, divisor);
            } else {
                continue;
            }

            list.add(i, new ExpressionItem(expression));

            list.remove(i+1);
            list.remove(i+1);
            list.remove(i-1);
            i--;
        }


        // merge expressions without operator between them (multiplication)
        for (int i=0; i<(list.size()-1); i++) {
            ParseItem item = list.get(i);
            if (!(item instanceof ExpressionItem))
                continue;

            ParseItem nextItem = list.get(i+1);
            if (!(nextItem instanceof ExpressionItem))
                continue;

            list.add(i, new ExpressionItem(new Multiplication(((ExpressionItem) item).expression, ((ExpressionItem) nextItem).expression)));
            list.remove(i+1);
            list.remove(i+1);
            i--;
        }


        // parse addition/subtraction
        for (int i=0; i<list.size(); i++) {
            ParseItem item = list.get(i);
            if (!(item instanceof OperatorItem))
                continue;


            Operator operator = ((OperatorItem) item).operator;
            if (Operator.ADDITION.equals(operator)) {
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
                i--;
            } else if (Operator.SUBTRACTION.equals(operator)) {
                if (i == list.size()-1)
                    throw new ParseException("expected subtrahend", -1);

                ParseItem minuendItem = i != 0 ? list.get(i-1) : null;
                ParseItem subtrahendItem = list.get(i+1);

                Expression minuend = minuendItem != null ? parseExpression(minuendItem) : new Scalar(0);
                Expression subtrahend = parseExpression(subtrahendItem);
                list.add(i, new ExpressionItem(new Subtraction(minuend, subtrahend)));

                list.remove(i+1);
                list.remove(i+1);
                if (i != 0) {
                    list.remove(i - 1);
                    i--;
                }
            }
        }



        /*
        System.out.println("list.size(): " + list.size());
        printList(list, 0);
        /*
        for (int i=0; i<list.size(); i++) {
            System.out.println("lst + " + i + ": " + list.get(i));
        }
        */
        // */

        if (list.size() != 1)
            throw new ParseException("invalid syntax", -1);


        ParseItem item = list.get(0);
        //System.out.println("item: " + item);
        if (!(item instanceof ExpressionItem))
            throw new ParseException("invalid syntax", -1);

        return ((ExpressionItem) item).expression;
    }


    private static List<ParseItem> parseOperators (String text) throws ParseException {
        List<ParseItem> parsed = new ArrayList<>();

        int pos = 0;
        while (pos < text.length()) {
            int nextOperatorPos = -1;
            for (char operator : ALL_OPERATORS) {
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
