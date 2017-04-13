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
package difflib.algorithm.xdiff;

import difflib.algorithm.DiffAlgorithm;
import difflib.algorithm.DiffException;
import difflib.patch.Patch;
import java.util.List;

/**
 * Histogram diff implementation.
 * @author tw
 */
public class XHistogram<T> implements DiffAlgorithm<T> {

    XPParam xpp;
    final XDFEnv env = new XDFEnv();
    
    @Override
    public Patch<T> diff(List<T> original, List<T> revised) throws DiffException {
        XPrepare.prepareEnvironment(original, revised, xpp, env);
        
        histogramDiff(xpp, env, 
                env.xdf1.dstart + 1, env.xdf1.dend - env.xdf1.dstart + 1,
                env.xdf2.dstart + 1, env.xdf2.dend - env.xdf2.dstart + 1);
        return null;
    }

    private void histogramDiff(XPParam xpp, XDFEnv env, int line1, int count1, int line2, int count2) {
        
    }
}
