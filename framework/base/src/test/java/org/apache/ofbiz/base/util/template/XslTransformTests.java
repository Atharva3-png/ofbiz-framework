/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.apache.ofbiz.base.util.template;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Regression tests for {@link XslTransform#renderTemplate(String, String)} (OFBIZ-13712).
 * The XML data file must be parsed with XInclude disabled, so an xi:include element
 * cannot pull a local file into the rendered output.
 */
public final class XslTransformTests {

    private static final String SECRET = "OFBIZ13712_SECRET_CONTENT";
    private static final String XSL = "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
            + "<xsl:output method=\"text\"/>"
            + "<xsl:template match=\"/\">BEGIN[<xsl:value-of select=\"/doc\"/>]END</xsl:template>"
            + "</xsl:stylesheet>";

    @TempDir
    private Path tempDir;

    private String write(String name, String content) throws IOException {
        Path file = tempDir.resolve(name);
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file.toUri().toString();
    }

    private String renderWithInclude(String parse, String secretContent) throws Exception {
        String secretUri = write("secret.txt", secretContent);
        String data = write("data.xml", "<?xml version=\"1.0\"?>"
                + "<doc xmlns:xi=\"http://www.w3.org/2001/XInclude\">"
                + "<xi:include href=\"" + secretUri + "\" parse=\"" + parse + "\"/>"
                + "</doc>");
        String template = write("template.xsl", XSL);
        return XslTransform.renderTemplate(template, data);
    }

    @Test
    public void xincludeIsNotExpanded() throws Exception {
        String result = renderWithInclude("text", SECRET);
        assertNotNull(result);
        assertFalse(result.contains(SECRET), "xi:include with parse=text must not be expanded");
    }

    @Test
    public void xincludeXmlIsNotExpanded() throws Exception {
        String result = renderWithInclude("xml", "<secret>" + SECRET + "</secret>");
        assertNotNull(result);
        assertFalse(result.contains(SECRET), "xi:include with parse=xml must not be expanded");
    }

    @Test
    public void rendersPlainXml() throws Exception {
        String data = write("plain.xml", "<?xml version=\"1.0\"?><doc>hello</doc>");
        String template = write("template.xsl", XSL);
        String result = XslTransform.renderTemplate(template, data);
        assertNotNull(result);
        assertTrue(result.contains("BEGIN[hello]END"), "plain XML must still be rendered, got: " + result);
    }
}
