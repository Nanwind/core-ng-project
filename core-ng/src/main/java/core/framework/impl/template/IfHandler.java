package core.framework.impl.template;

import core.framework.api.util.Exceptions;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.expression.Expression;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionParser;
import core.framework.impl.template.expression.Token;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author neo
 */
public class IfHandler extends CompositeHandler implements FragmentHandler {
    private static final Pattern STATEMENT_PATTERN = Pattern.compile("if ((not )?)([#a-zA-Z1-9\\.\\(\\)]+)");
    final Expression expression;
    final boolean reverse;

    public IfHandler(String statement, CallTypeStack stack, String location) {
        Matcher matcher = STATEMENT_PATTERN.matcher(statement);
        if (!matcher.matches())
            throw Exceptions.error("statement must match \"if (not) condition\", location={}", location);

        reverse = "not ".equals(matcher.group(2));
        String condition = matcher.group(3);

        Token expression = new ExpressionParser().parse(condition);
        this.expression = new ExpressionBuilder().build(expression, stack, Boolean.class);
    }

    @Override
    public void process(StringBuilder builder, CallStack stack) {
        Boolean result = (Boolean) expression.eval(stack);
        Boolean expected = reverse ? Boolean.FALSE : Boolean.TRUE;
        if (expected.equals(result)) {
            for (FragmentHandler handler : handlers) {
                handler.process(builder, stack);
            }
        }
    }
}
