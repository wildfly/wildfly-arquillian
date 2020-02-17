/*
 * Copyright 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
