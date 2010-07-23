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
 * Base class for all exceptions emanating from this package.
 * 
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 */
public class DiffException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    public DiffException() {
    }
    
    public DiffException(String msg) {
        super(msg);
    }
}
