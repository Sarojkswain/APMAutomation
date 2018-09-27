package com.ca.apm.systemtest.fld.hammond;

import java.util.Locale;

import com.wily.util.text.AStringLocalizer;

public class Localizer  extends AStringLocalizer
{
    private static Localizer instance;
    public static Localizer instance() {
        if (instance == null) {
            instance = new Localizer();
        }
        return instance;
    }

    public String getLocalizedString(String key)
    {
        return key;
    }
    
    public String getFormattedLocalizedString(String key, String[] args)
    {
        return key;
    }

    @Override
    public String getFormattedLocalizedString(Locale locale,
                                              String key,
                                              String[] parameters)
    {
        // TODO Auto-generated method stub
        return key;
    }

    @Override
    protected String getLocalizedString(Locale locale, String key)
    {
        // TODO Auto-generated method stub
        return key;
    }
}
