package com.ca.apm.classes.from.appmap.plugin;

/**
*
*  @author Jeff Cobb 1999
*  @copyright 1999-2001 Wily Technology, Inc.  All rights reserved.
*
* $NoKeywords: $
*/

//
// NOTE: if you change any of these values, you need to fix up the sets in ESERegularExpressionBuilder


public abstract class KESESpecifierConstants
{
   public  final static char       kSegmentSeparatorChar = '|';
   public  final static String     kSegmentSeparatorString = "|";
   public  final static String     kSegmentSeparatorDelimiter = "\\|";
   public  final static char       kResourceSeparatorChar = kSegmentSeparatorChar;
   public  final static String     kResourceSeparatorString = kSegmentSeparatorString;

   public  final static char       kAttributeSeparatorChar = ':';
   public  final static String     kAttributeSeparatorString = ":";
   public  final static char       kMetricSeparatorChar = kAttributeSeparatorChar;
   public  final static String     kMetricSeparatorString = kAttributeSeparatorString;

   public  final static char       kAttributeSubstituteSeparatorChar = '%';
   public  final static String     kAttributeSubstituteSeparatorString = "%";
   public  final static char       kMetricSubstituteSeparatorChar = kAttributeSeparatorChar;
   public  final static String     kMetricSubstituteSeparatorString = kAttributeSeparatorString;
   
   public  static final String     kESETokenSeparators =       kSegmentSeparatorString
                                                           +   kAttributeSeparatorString;
}