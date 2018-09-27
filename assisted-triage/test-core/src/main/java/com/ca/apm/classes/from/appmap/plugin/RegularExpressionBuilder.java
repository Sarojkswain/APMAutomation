package com.ca.apm.classes.from.appmap.plugin;


//  Not really for general reg exp, really for our uses for our semantics with path & metric
//  & separators

// FIX ME we need to sniff out special characters and escape them

public class    RegularExpressionBuilder
    {
    public static   final   String      kKleeneClosureString        = "*";      // zero or more //$NON-NLS-1$
    public static   final   String      kPositiveClosureString      = "+";      // one or more //$NON-NLS-1$
    public static   final   String      kOrString                   = "|"; //$NON-NLS-1$

    private static  final   String      kAllLegalCharactersExceptSeparatorsGroup        = "[^" + KESESpecifierConstants.kSegmentSeparatorString + KESESpecifierConstants.kAttributeSeparatorString + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    private static  final   String      kAllLegalCharactersWithSegmentSeparatorGroup    = "[^" + KESESpecifierConstants.kAttributeSeparatorString + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    private static  final   String      kAllLegalCharactersWithMetricSeparatorGroup     = "[^" + KESESpecifierConstants.kSegmentSeparatorString + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    private static  final   String      kAllLegalCharactersWithBothSeparatorsGroup      = "."; //$NON-NLS-1$
    
    // Note: these special characters are the ones which have special meaning OUTSIDE a character range.
    // For instance, a dash '-' has no special meaning except when inside brackets [], so it isn't included here.
    private static final String       kSpecialChars = "^$[]().?*{},+|&"; //$NON-NLS-1$

    private     StringBuffer        fBuffer;

    public static RegularExpressionBuilder
    or(     String regex1,
            String regex2)
        {
        return or(  new String[] { regex1, regex2 });
        }

    public static RegularExpressionBuilder
    or(     String[] regExp)
        {
        RegularExpressionBuilder builder = new RegularExpressionBuilder();
        for (int i = 0; i < regExp.length; i++)
            {
            if (i > 0)
                {
                builder.append(kOrString);
                }
            builder.append("("); //$NON-NLS-1$
            builder.append(regExp[i]);
            builder.append(")"); //$NON-NLS-1$
            }
        return builder;
        }

    public
    RegularExpressionBuilder()
        {
        fBuffer = new StringBuffer();
        }
    
    public
    RegularExpressionBuilder(String initialRegExp)
        {
        this();
        append(initialRegExp);
        }
    
    public void
    append(String text)
        {
        fBuffer.append(text);
        }
    
    public String
    toString()
        {
        return fBuffer.toString();
        }
    
    public void
    appendSegmentSeparator()
        {
        append("\\"); //$NON-NLS-1$
        append(KESESpecifierConstants.kSegmentSeparatorString);
        }
    
    public void
    appendMetricSeparator()
        {
        append(KESESpecifierConstants.kAttributeSeparatorString);
        }
    
    public void
    appendKleeneClosure(String text)
        {
        append("("); //$NON-NLS-1$
        append(text);
        append(kKleeneClosureString);
        append(")"); //$NON-NLS-1$
        }
    
    public void
    appendPositiveClosure(String text)
        {
        append("("); //$NON-NLS-1$
        append(text);
        append(kPositiveClosureString);
        append(")"); //$NON-NLS-1$
        }

    public static String
    getAllLegalCharactersNoSeparatorsExpression()
        {
        return kAllLegalCharactersExceptSeparatorsGroup;
        }
    
    public static String
    getAllLegalCharactersWithSegmentSeparatorExpression()
        {
        return kAllLegalCharactersWithSegmentSeparatorGroup;
        }
    
    public static String
    getAllLegalCharactersWithMetricSeparatorExpression()
        {
        return kAllLegalCharactersWithMetricSeparatorGroup;
        }
    
    public static String
    getAllLegalCharactersIncludingSeparatorsExpression()
        {
        return kAllLegalCharactersWithBothSeparatorsGroup;
        }
    
    public static String
    getAllLegalCharactersExpression()
        {
        return kAllLegalCharactersWithBothSeparatorsGroup;
        }
    
    public static String
    getMatchAny()
        {   
        return "(.*)"; //$NON-NLS-1$
        }
    
    public static String
    getMatchNone()
        {
        return ""; //$NON-NLS-1$
        }   

    public void
    appendConvertingEscapeSequences(String input)
        {
        String  converted = convertClearTextToRegExp(input);
        append(converted);
        }
    
    // clear text can contain characters that need to be escaped
    public static String
    convertClearTextToRegExp(String clearText)
        {
        StringBuffer    result = new StringBuffer();
        
        for ( int x = 0; x < clearText.length(); x++ )
            {
            char    currentChar = clearText.charAt(x);
            if ( requiresEscape(currentChar) )
                {
                result.append("\\"); //$NON-NLS-1$
                }
            result.append(currentChar);
            }
        return result.toString();
        }
    

    private static boolean
    requiresEscape(char inputCharacter)
        {
        switch( inputCharacter )
            {
            case '(':
            case ')':
            case '[':
            case ']':
            case '*':
            case '+':
            case '-':
            case '|':
            case '$':
            case '{':
            case '}':
            case '.':
            case '?':
            case '\\':
            case '&':
            case '^':

            // FIX ME I have no idea if this list is exhaustive

                /*
                 * Hint: from the Pattern javadoc:
                 *  
                 * A backslash may be used prior to a non-alphabetic character
                 * regardless of whether that character is part of an unescaped
                 * construct.
                 */
            
                return true;
                
            default:
                
                return false;
            }
        
        }
    }
