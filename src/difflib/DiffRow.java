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
 * Describes the diff row in form [tag, oldLine, newLine) for showing the
 * difference between two texts
 * 
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public class DiffRow {
    private Tag tag;
    private String oldLine;
    private String newLine;
    
    public DiffRow(Tag tag, String oldLine, String newLine) {
        this.tag = tag;
        this.oldLine = oldLine;
        this.newLine = newLine;
    }
    
    public enum Tag {
        INSERT, DELETE, CHANGE, EQUAL
    }
    
    /**
     * @return the tag
     */
    public Tag getTag() {
        return tag;
    }
    
    /**
     * @param tag the tag to set
     */
    public void setTag(Tag tag) {
        this.tag = tag;
    }
    
    /**
     * @return the oldLine
     */
    public String getOldLine() {
        return oldLine;
    }
    
    /**
     * @param oldLine the oldLine to set
     */
    public void setOldLine(String oldLine) {
        this.oldLine = oldLine;
    }
    
    /**
     * @return the newLine
     */
    public String getNewLine() {
        return newLine;
    }
    
    /**
     * @param newLine the newLine to set
     */
    public void setNewLine(String newLine) {
        this.newLine = newLine;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((newLine == null) ? 0 : newLine.hashCode());
        result = prime * result + ((oldLine == null) ? 0 : oldLine.hashCode());
        result = prime * result + ((tag == null) ? 0 : tag.hashCode());
        return result;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DiffRow other = (DiffRow) obj;
        if (newLine == null) {
            if (other.newLine != null)
                return false;
        } else if (!newLine.equals(other.newLine))
            return false;
        if (oldLine == null) {
            if (other.oldLine != null)
                return false;
        } else if (!oldLine.equals(other.oldLine))
            return false;
        if (tag == null) {
            if (other.tag != null)
                return false;
        } else if (!tag.equals(other.tag))
            return false;
        return true;
    }
    
    public String toString() {
        return "[" + this.tag + "," + this.oldLine + "," + this.newLine + "]";
    }
}
