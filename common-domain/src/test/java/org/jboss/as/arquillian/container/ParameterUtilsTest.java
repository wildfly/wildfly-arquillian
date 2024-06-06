/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container;

import java.util.List;

import org.jboss.as.arquillian.container.domain.ParameterUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * TestCases for {@link ParameterUtils}.
 *
 */
public class ParameterUtilsTest {

    private static final String PARAM_WITH_WHITESPACE = "-Djboss.server.base.dir=Does contain whitespaces";
    private static final String QUOTED_PARAM_WITH_WHITESPACE = "\"" + PARAM_WITH_WHITESPACE + "\"";
    private static final String PARAM_WITHOUT_WHITESPACE = "-Djboss.http.port=8080";

    @Test
    public void mixedParams() {
        List<String> splitParams = ParameterUtils
                .splitParams(QUOTED_PARAM_WITH_WHITESPACE.concat(" ").concat(PARAM_WITHOUT_WHITESPACE).concat(" ")
                        .concat(QUOTED_PARAM_WITH_WHITESPACE).concat(" ").concat(QUOTED_PARAM_WITH_WHITESPACE).concat(" ")
                        .concat(PARAM_WITHOUT_WHITESPACE).concat(" ").concat(QUOTED_PARAM_WITH_WHITESPACE));
        Assert.assertArrayEquals(new String[] { PARAM_WITH_WHITESPACE, PARAM_WITHOUT_WHITESPACE, PARAM_WITH_WHITESPACE,
                PARAM_WITH_WHITESPACE, PARAM_WITHOUT_WHITESPACE, PARAM_WITH_WHITESPACE }, splitParams.toArray(new String[0]));
    }

    @Test
    public void singleParamWithWS() {
        List<String> splitParams = ParameterUtils.splitParams(QUOTED_PARAM_WITH_WHITESPACE);
        Assert.assertArrayEquals(new String[] { PARAM_WITH_WHITESPACE }, splitParams.toArray(new String[0]));
    }

    @Test
    public void singleParamWithoutWS() {
        List<String> splitParams = ParameterUtils.splitParams(PARAM_WITHOUT_WHITESPACE);
        Assert.assertArrayEquals(new String[] { PARAM_WITHOUT_WHITESPACE }, splitParams.toArray(new String[0]));
    }

    @Test
    public void onlyParamsWithWhitespaces() {
        List<String> splitParams = ParameterUtils.splitParams(QUOTED_PARAM_WITH_WHITESPACE.concat(" ")
                .concat(QUOTED_PARAM_WITH_WHITESPACE).concat(QUOTED_PARAM_WITH_WHITESPACE));
        Assert.assertArrayEquals(new String[] { PARAM_WITH_WHITESPACE, PARAM_WITH_WHITESPACE, PARAM_WITH_WHITESPACE },
                splitParams.toArray(new String[0]));
    }

    @Test
    public void onlyParamsWithoutWhitespaces() {
        List<String> splitParams = ParameterUtils.splitParams(PARAM_WITHOUT_WHITESPACE.concat(" ")
                .concat(PARAM_WITHOUT_WHITESPACE).concat(" ").concat(PARAM_WITHOUT_WHITESPACE));
        Assert.assertArrayEquals(new String[] { PARAM_WITHOUT_WHITESPACE, PARAM_WITHOUT_WHITESPACE, PARAM_WITHOUT_WHITESPACE },
                splitParams.toArray(new String[0]));
    }
}
