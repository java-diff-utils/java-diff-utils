/*
 * Copyright 2009-2017 java-diff-utils.
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

import java.io.Serializable;
import java.util.Objects;

/**
 * Describes the diff row in form [tag, oldLine, newLine) for showing the difference between two texts
 *
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public final class DiffRow implements Serializable {

    private Tag tag;
    private final String oldLine;
    private final String newLine;

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
     * @return the newLine
     */
    public String getNewLine() {
        return newLine;
    }

    @Override
    public int hashCode() {
        return Objects.hash(newLine, oldLine, tag);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DiffRow other = (DiffRow) obj;
        if (newLine == null) {
            if (other.newLine != null) {
                return false;
            }
        } else if (!newLine.equals(other.newLine)) {
            return false;
        }
        if (oldLine == null) {
            if (other.oldLine != null) {
                return false;
            }
        } else if (!oldLine.equals(other.oldLine)) {
            return false;
        }
        if (tag == null) {
            if (other.tag != null) {
                return false;
            }
        } else if (!tag.equals(other.tag)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[" + this.tag + "," + this.oldLine + "," + this.newLine + "]";
    }
}
