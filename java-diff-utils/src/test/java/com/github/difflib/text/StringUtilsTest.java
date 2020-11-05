/*
 * Copyright 2017 java-diff-utils.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.difflib.text;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author tw
 */
public class StringUtilsTest {

    /**
     * Test of htmlEntites method, of class StringUtils.
     */
    @Test
    public void testHtmlEntites() {
        assertEquals("&lt;test&gt;", StringUtils.htmlEntites("<test>"));
    }

    /**
     * Test of normalize method, of class StringUtils.
     */
    @Test
    public void testNormalize_String() {
        assertEquals("    test", StringUtils.normalize("\ttest"));
    }

    /**
     * Test of wrapText method, of class StringUtils.
     */
    @Test
    public void testWrapText_String_int() {
        assertEquals("te<br/>st", StringUtils.wrapText("test", 2));
        assertEquals("tes<br/>t", StringUtils.wrapText("test", 3));
        assertEquals("test", StringUtils.wrapText("test", 10));
        assertEquals(".\uD800\uDC01<br/>.", StringUtils.wrapText(".\uD800\uDC01.", 2));
        assertEquals("..<br/>\uD800\uDC01", StringUtils.wrapText("..\uD800\uDC01", 3));
    }

    @Test
    public void testWrapText_String_int_zero() {
        Assertions.assertThrows(IllegalArgumentException.class, 
                () -> StringUtils.wrapText("test", -1));
    }

}
