/*
    Copyright 2009 Dmitry Naumenko (dm.naumenko@gmail.com)
    
    This file is part of Java Diff Utills Library.

    Java Diff Utills Library is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Java Diff Utills Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Java Diff Utills Library.  If not, see <http://www.gnu.org/licenses/>.
 */
package difflib;

/**
 * Thrown whenever a delta cannot be applied as a patch to a given text.
 *
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 */
public class PatchFailedException extends DiffException {
    
    private static final long serialVersionUID = 1L;
    
    public PatchFailedException() {
    }
    
    public PatchFailedException(String msg) {
        super(msg);
    }
}