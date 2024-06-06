/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods related to parsing parameters.
 */
/*
 * As there is no common dependency between domain and standalone, this class was duplicated.
 */
public class ParameterUtils {

    private static final Pattern WHITESPACE_IF_NOT_QUOTED = Pattern.compile("\"([^\"]*)\"|(\\S+)");

    /**
     * Splits the given string at quotes oder, if unquoted, at whitespaces.
     *
     * @param parameterString
     * @return
     */
    public static List<String> splitParams(String parameterString) {

        ArrayList<String> params = new ArrayList<>();
        Matcher m = WHITESPACE_IF_NOT_QUOTED.matcher(parameterString);
        while (m.find()) {
            if (m.group(1) != null) {
                // Add double-quoted string without the quotes
                params.add(m.group(1));
            } else {
                // Add unquoted word
                params.add(m.group(2));
            }
        }
        return params;
    }

    private ParameterUtils() {
        // Do not initialize this class
    }
}
