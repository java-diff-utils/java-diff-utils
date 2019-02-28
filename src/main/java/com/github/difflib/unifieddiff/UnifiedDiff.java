/*
 * Copyright 2019 java-diff-utils.
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
package com.github.difflib.unifieddiff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 *
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
public final class UnifiedDiff {

    private UnifiedDiff(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream));) {
            while (reader.ready()) {
                String line = reader.readLine();
                LOG.info(line);
            }
        }

    }
    private static final Logger LOG = Logger.getLogger(UnifiedDiff.class.getName());

    public static UnifiedDiff parse(InputStream stream) throws IOException {
        return new UnifiedDiff(stream);
    }
}
