package com.ca.apm.test.atc.common.landing;

public class LandingUtils {
    
    public static int getValueFromFormattedString(String value) {
        if ((value == null) || value.isEmpty()) {
            return 0;
        }
        
        String strValue = value.trim();
        double coef = 1;
        if (strValue.endsWith("k")) {
            strValue = strValue.substring(0, strValue.length() - 1);
            coef = 1000;
        }
        
        /* The value may be e.g. "1.5k" so we have to use Double to parse it */
        double v = Double.valueOf(strValue) * coef;
        return (int) v;
    }
}
