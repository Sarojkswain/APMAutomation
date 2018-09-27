/*
 * Copyright (c) 2016 CA. All rights reserved.
 * 
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.classes.from.appmap.plugin;


/**
 * Problem Zone Identifier:
 * 
 * Heuristic that figures out the deepest component in given
 * context and places blame on it. This is the default analyst
 * that is guaranteed to produce a result even if other analysts
 * fail to produce one. Because if we have a context even with one
 * actor we have one deepest component to blame.
 */
public class ZoneIdentifierAnalyst {

    public static final String FRONTEND_ZONE = "Frontend";
    public static final String BUSINESS_TRANSACTION_ZONE = "Business transaction";
    public static final String BACKEND_ZONE = "Backend";
    public static final String INTERNAL_COMPONENT_ZONE = "Internal component";
}
