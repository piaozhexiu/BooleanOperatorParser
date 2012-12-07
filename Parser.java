import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Boolean operator parser

public class Parser {
    private List<String> expr = null;

    private static List<String> tokenize(String expr) {
        List<String> tokens = new ArrayList<String>();
        Pattern p = Pattern.compile("\\[([^\\]]+)\\]");
        Matcher m = p.matcher(expr);
        while (m.find()) {
            tokens.add(m.group(1));
        }
        return tokens;
    }

    private static List<String> sublist(List<String> l, int s, int e) {
        List<String> ret = new ArrayList<String>();
        for (int i = s; i < e; i++) {
            ret.add(l.get(i));
        }
        return ret;
    }

    private static List<Integer> find(List<String> l, String s) {
        List<Integer> ret = new ArrayList<Integer>();
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).equals(s)) {
                ret.add(new Integer(i));
            }
        }
        return ret;
    }

    // validate expression

    private static boolean isValidUnaryOpExpr(List<String> expr, String operator) {
        List<Integer> indices = find(expr, operator);
        for (int index : indices) {
            if (index != 0) {
                continue;
            }
            List<String> rest = sublist(expr, index + 1, expr.size());
            if (rest.size() == 0) {
                continue;
            }
            if (isValidExpr(rest)) {
                for (int i = 0; i < rest.size(); i++) {
                    expr.set(index + i + 1, rest.get(i));
                }
                expr.set(index, "!");
                return true;
            }
        }
        return false;
    }

    private static boolean isValidBinaryOpExpr(List<String> expr, String operator) {
        List<Integer> indices = find(expr, operator);
        for (int index : indices) {
            if (index == -1) {
                continue;
            }
            List<String> firstHalf = sublist(expr, 0, index);
            if (firstHalf.size() == 0) {
                continue;
            }
            List<String> secondHalf = sublist(expr, index + 1, expr.size());
            if (firstHalf.size() == 0) {
                continue;
            }
            if (isValidExpr(firstHalf) && isValidExpr(secondHalf)) {
                for (int i = 0; i < firstHalf.size(); i++) {
                    expr.set(i, firstHalf.get(i));
                }
                for (int i = 0; i < secondHalf.size(); i++) {
                    expr.set(index + i + 1, secondHalf.get(i));
                }
                if (operator.equals("and")) {
                    expr.set(index, "&&");
                }
                if (operator.equals("or")) {
                    expr.set(index, "||");
                }
                return true;
            }
        }
        return false;
    }

    private static boolean isValidAndExpr(List<String> expr) {
        return isValidBinaryOpExpr(expr, "and");
    }

    private static boolean isValidOrExpr(List<String> expr) {
        return isValidBinaryOpExpr(expr, "or");
    }

    private static boolean isValidNotExpr(List<String> expr) {
        return isValidUnaryOpExpr(expr, "not");
    }

    private static boolean isValidAlias(List<String> expr) {
        if (expr.size() == 1) {
            return true;
        }
        return false;
    }

    private static boolean isValidExpr(List<String> expr) {
        return isValidAlias(expr) || isValidNotExpr(expr) || isValidAndExpr(expr) || isValidOrExpr(expr);
    }

    // evaluate expression

    private static boolean isAndExpr(String expr) {
    	return expr.indexOf("&&") != -1;
    }

    private static boolean isOrExpr(String expr) {
    	return expr.indexOf("||") != -1;
    }

    private static boolean isNotExpr(String expr) {
    	return expr.indexOf("!") != -1;
    }

    private static boolean evalAlias(String expr) {
        return Boolean.valueOf(expr);
    }

    private static boolean evalAndExpr(String expr) {
    	int index = expr.indexOf("&&");
        String firstHalf = expr.substring(0, index);
        String secondHalf = expr.substring(index + 2);
        return evalExpr(firstHalf) && evalExpr(secondHalf);
    }

    private static boolean evalOrExpr(String expr) {
    	int index = expr.indexOf("||");
        String firstHalf = expr.substring(0, index);
        String secondHalf = expr.substring(index + 2);
        return evalExpr(firstHalf) || evalExpr(secondHalf);
    }

    private static boolean evalNotExpr(String expr) {
        return !evalExpr(expr.substring(expr.indexOf("!") + 1));
    }

    private static boolean evalExpr(String expr) {
    	if (isAndExpr(expr)) {
    		return evalAndExpr(expr);
    	} else if (isOrExpr(expr)) {
    		return evalOrExpr(expr);
    	} else if (isNotExpr(expr)) {
    		return evalNotExpr(expr);
    	} else {
    		return evalAlias(expr);
    	}
    }

    // public API

    public String toString() {
    	return expr.toString();
    }

    public void parse(String expr) {
        List<String> tokens = tokenize(expr);
        if (isValidExpr(tokens)) {
            this.expr = tokens;
        }
    }

    public boolean eval(String var, boolean val) {
        for (int i = 0; i < expr.size(); i++) {
            if (expr.get(i).equals(var)) {
                expr.set(i, Boolean.toString(val));
            }
        }
        StringBuffer sb = new StringBuffer();
        for (String s : expr) {
        	sb.append(s);
        }
        return evalExpr(sb.toString());
    }

    // Examples

    public static void main(String[] args) {
        String[] expression1 = {
                "[a][and][a]",
                "[a][or][a]",
                "[not][a]",
                "[not][not][a]",
                "[not][a][and][a]",
                "[not][a][and][not][a]",
                "[not][a][or][a]",
                "[not][a][or][not][a]",
                "[not][not][a][and][a]",
                "[not][not][a][or][a]"
        };

        for (String e : expression1) {
            Parser p = new Parser();
            p.parse(e);
            System.out.println(p.toString());
            System.out.println(p.eval("a", true));
        }

        String[] expression2 = {
                "[and][and][and]",
                "[and][or][and]",
                "[not][and]",
                "[not][not][and]",
                "[not][and][and][and]",
                "[not][and][or][and]",
                "[not][and][or][not][and]",
                "[not][and][and][not][and]",
                "[not][not][and][and][and]",
                "[not][not][and][or][and]"
        };

        for (String e : expression2) {
            Parser p = new Parser();
            p.parse(e);
            System.out.println(p.toString());
            System.out.println(p.eval("and", false));
        }
    }
}
