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

import java.util.List;

/**
 *
 * @author tw
 */
public final class XPrepare {

    private static <T> void prepareContext(List<T> data, XPParam xpp, XDLClassifier cf, XDFile xdf) {      
        xdf.data = data;
        xdf.ha = new int[data.size()];
        for (int i=0;i<data.size();i++) {
            xdf.ha[i] = data.get(i).hashCode();
        }
        xdf.dstart = 0;
        xdf.dend = data.size()-1;
    }
    
    
    public static <T> void prepareEnvironment(List<T> original, List<T> revised, XPParam xpp, XDFEnv env) {
        //sample calculation not needed, due to already List
        long enl1 = original.size() + 1;
        long enl2 = revised.size() + 1;
        XDLClassifier cf = new XDLClassifier();
        
        prepareContext(original, xpp, cf, env.xdf1);
        prepareContext(revised, xpp, cf, env.xdf2);
    }
}
