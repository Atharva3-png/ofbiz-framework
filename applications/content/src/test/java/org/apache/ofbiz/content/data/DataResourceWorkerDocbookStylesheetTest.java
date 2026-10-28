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
package org.apache.ofbiz.content.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

/**
 * Regression tests for {@link DataResourceWorker#getDocbookStylesheetPath(Object)} (OFBIZ-13712).
 * Rendering an XSLT DataResource used to fail with a StringIndexOutOfBoundsException when the
 * visual theme did not set VT_DOCBOOKSTYLESHEET, because the empty property value was cut with substring.
 */
class DataResourceWorkerDocbookStylesheetTest {

    @Test
    void emptyStringGivesNull() {
        assertNull(DataResourceWorker.getDocbookStylesheetPath(""));
    }

    @Test
    void nullGivesNull() {
        assertNull(DataResourceWorker.getDocbookStylesheetPath(null));
    }

    @Test
    void singleItemListGivesThatPath() {
        assertEquals("/common-theme/webapp/common/css/docbook.css",
                DataResourceWorker.getDocbookStylesheetPath(Collections.singletonList("/common-theme/webapp/common/css/docbook.css")));
    }

    @Test
    void twoItemListGivesFirstPath() {
        assertEquals("/a/docbook.css", DataResourceWorker.getDocbookStylesheetPath(Arrays.asList("/a/docbook.css", "/b/docbook.css")));
    }

    @Test
    void listWithEmptyFirstItemGivesNextNonEmptyPath() {
        assertEquals("/b/docbook.css", DataResourceWorker.getDocbookStylesheetPath(Arrays.asList("", "/b/docbook.css")));
    }

    @Test
    void emptyListGivesNull() {
        assertNull(DataResourceWorker.getDocbookStylesheetPath(Collections.emptyList()));
    }

    @Test
    void plainStringGivesSameString() {
        assertEquals("/x/docbook.css", DataResourceWorker.getDocbookStylesheetPath("/x/docbook.css"));
    }
}
