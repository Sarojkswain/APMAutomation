package com.ca.apm.tests.test.selenium;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ca.apm.browseragent.testsupport.collector.util.AbstractPayloadType;
import com.ca.apm.browseragent.testsupport.collector.util.AbstractPayloadType.AnyType;
import com.ca.apm.browseragent.testsupport.collector.util.AbstractPayloadType.TimeOutType;
import com.ca.apm.browseragent.testsupport.collector.util.MetricPostRecord;
import com.ca.apm.eum.datamodel.Page;
import com.ca.apm.eum.datamodel.Resource;

/**
 * This class has the class and methods needs to create a List<AbstractPayloadType>
 * needed for EUMValidationUtils.waitAndBasicValidate methods
 */

public class PayloadUtils {


    public static class PageType extends AbstractPayloadType {
        public int calculateTypeCount(MetricPostRecord recordToTest) {
            return EUMValidationUtils.hasPageMetrics(EUMValidationUtils.createList(recordToTest))
                ? 1
                : 0;
        }
    }

    public static class JSFunctionType extends AbstractPayloadType {
        public int calculateTypeCount(MetricPostRecord recordToTest) {
            return EUMValidationUtils.hasJSFunctionMetrics(EUMValidationUtils
                .createList(recordToTest)) ? 1 : 0;
        }
    }

    public static class AJAXType extends AbstractPayloadType {
        public int calculateTypeCount(MetricPostRecord recordToTest) {
            List<Resource> resourceList =
                EUMValidationUtils.extractAjax(EUMValidationUtils.createList(recordToTest));
            return resourceList.size();
        }
    }

    public static class JSErrorType extends AbstractPayloadType {
        public int calculateTypeCount(MetricPostRecord recordToTest) {
            return EUMValidationUtils.hasJSError(EUMValidationUtils.createList(recordToTest))
                ? 1
                : 0;
        }
    }

    public static class ThinkTimeType extends AbstractPayloadType {
        public int calculateTypeCount(MetricPostRecord recordToTest) {
            return EUMValidationUtils.hasThinkTime(EUMValidationUtils.createList(recordToTest))
                ? 1
                : 0;
        }
    }

    public static class SoftPageType extends AbstractPayloadType {
        public int calculateTypeCount(MetricPostRecord recordToTest) {
            List<Page> softPages =
                EUMValidationUtils.extractPagesByType(EUMValidationUtils.createList(recordToTest),
                    EUMValidationUtils.PageType.SOFT);
            return softPages.size();
        }
    }

    public static class HardPageType extends AbstractPayloadType {
        public int calculateTypeCount(MetricPostRecord recordToTest) {
            List<Page> hardPages =
                EUMValidationUtils.extractPagesByType(EUMValidationUtils.createList(recordToTest),
                    EUMValidationUtils.PageType.HARD);
            return hardPages.size();
        }
    }



    /**
     * Generates a list of types for the wait collector method. If you want to configure the
     * actual count on each type use the generateTypesMap. This method assumes 1 for each
     * type passed in.
     * 
     * @param types a variable argument parameter to take in any number of types
     * @return
     */


    public static List<AbstractPayloadType> generateTypesList(PayloadTypes... types) {
        Map<PayloadTypes, AbstractPayloadType> typesMap = generateTypesMap(types);
        return convertToList(typesMap);
    }

    /**
     * Generates the map of passed types, useful to set specific counts on each as needed.
     * 
     * Example: Expecting page and 3 AJAX
     * 
     * Map<PayloadTypes, AbstractPayloadType> typesMap =
     * PayloadUtils.generateTypesMap(PayloadTypes.PAGE_TYPE, PayloadTypes.AJAX_TYPE);
     * AbstractPayloadType ajaxType = typesMap.get(PayloadTypes.AJAX_TYPE);
     * ajaxType.setCount(3);
     * 
     * 
     * @param types a variable argument parameter to take in any number of types
     * @return
     */

    public static Map<PayloadTypes, AbstractPayloadType> generateTypesMap(PayloadTypes... types) {
        Map<PayloadTypes, AbstractPayloadType> testTypeMap =
            new HashMap<PayloadTypes, AbstractPayloadType>();

        for (PayloadTypes type : types) {

            if (type.equals(PayloadTypes.PAGE_TYPE)) {
                testTypeMap.put(PayloadTypes.PAGE_TYPE, new PageType());
            }

            if (type.equals(PayloadTypes.JS_FUNCTION_TYPE)) {
                testTypeMap.put(PayloadTypes.JS_FUNCTION_TYPE, new JSFunctionType());
            }

            if (type.equals(PayloadTypes.AJAX_TYPE)) {
                testTypeMap.put(PayloadTypes.AJAX_TYPE, new AJAXType());
            }

            if (type.equals(PayloadTypes.JS_ERROR_TYPE)) {
                testTypeMap.put(PayloadTypes.JS_ERROR_TYPE, new JSErrorType());
            }

            if (type.equals(PayloadTypes.THINK_TIME_TYPE)) {
                testTypeMap.put(PayloadTypes.THINK_TIME_TYPE, new ThinkTimeType());
            }

            if (type.equals(PayloadTypes.ANY_TYPE)) {
                testTypeMap.put(PayloadTypes.ANY_TYPE, new AnyType());
            }

            if (type.equals(PayloadTypes.TIME_OUT_TYPE)) {
                testTypeMap.put(PayloadTypes.TIME_OUT_TYPE, new TimeOutType());
            }

            if (type.equals(PayloadTypes.SOFT_PAGE_TYPE)) {
                testTypeMap.put(PayloadTypes.SOFT_PAGE_TYPE, new SoftPageType());
            }

            if (type.equals(PayloadTypes.HARD_PAGE_TYPE)) {
                testTypeMap.put(PayloadTypes.HARD_PAGE_TYPE, new HardPageType());
            }
        }

        return testTypeMap;
    }

    /**
     * Helper to convert the passed map into a list
     * 
     * @param Map<PayloadTypes, AbstractPayloadType> typesMap to convert
     * @return List<AbstractPayloadType> from the map
     */

    public static List<AbstractPayloadType> convertToList(
        Map<PayloadTypes, AbstractPayloadType> typesMap) {
        List<AbstractPayloadType> returnList = new ArrayList<AbstractPayloadType>();

        returnList.addAll(typesMap.values());

        return returnList;
    }

}
