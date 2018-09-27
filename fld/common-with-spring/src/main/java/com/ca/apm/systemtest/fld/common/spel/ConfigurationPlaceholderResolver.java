package com.ca.apm.systemtest.fld.common.spel;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.PropertyPlaceholderHelper;

import com.ca.apm.systemtest.fld.common.ErrorUtils;

/**
 * This class resolves placeholders/properties in strings in file transformation plugin
 * configuration.
 * Created by haiva01 on 18.6.2015.
 */
public class ConfigurationPlaceholderResolver
    implements PropertyPlaceholderHelper.PlaceholderResolver {

    private final Logger log = LoggerFactory.getLogger(ConfigurationPlaceholderResolver.class);

    private final Deque<EvaluationContext> contextsStack = new ArrayDeque<>(10);
    private EvaluationContext context;
    private final ExpressionParser parser = new SpelExpressionParser(
        new SpelParserConfiguration(
            SpelCompilerMode.IMMEDIATE, this.getClass().getClassLoader()));

    /**
     * Construct this resolver with variables passed in a Map.
     *
     * @param vars variables map
     */
    public ConfigurationPlaceholderResolver(Map<String, Object> vars) {
        StandardEvaluationContext defaultContext = new StandardEvaluationContext();
        defaultContext.setVariables(vars);
        // XXX Possible extension:
        //defaultContext.setBeanResolver(new MyBeanResolver());
        context = defaultContext;
    }


    public EvaluationContext getContext() {
        return context;
    }


    public void pushContext(EvaluationContext localContext) {
        contextsStack.push(context);
        context = localContext;
    }


    public void popContext() {
        context = contextsStack.pop();
    }


    @Override
    public String resolvePlaceholder(String exprString) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Resolving {}", exprString);
            }
            final Expression expression = parser.parseExpression(exprString);
            Object value = expression.getValue(context, String.class);
            String result = value != null ? value.toString() : "";
            if (log.isDebugEnabled()) {
                log.debug("Resolved to {}", result);
            }
            return result;
        } catch (ParseException ex) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, ex,
                "Failed to parse expression {1}. Exception: {0}",
                ex.getExpressionString() != null ? ex.getExpressionString() : exprString);
        } catch (EvaluationException ex) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, ex,
                "Failed to evaluate expression {1}. Exception: {0}",
                ex.getExpressionString() != null ? ex.getExpressionString() : exprString);
        } catch (Exception ex) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, ex,
                "Failed to resolve {1}. Exception: {0}", exprString);
        }
    }
}
