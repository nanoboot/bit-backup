///////////////////////////////////////////////////////////////////////////////////////////////
// bit-backup: Tool detecting bit rots in files.
// Copyright (C) 2023-2023 the original author or authors.
//
// This program is free software: you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation, either version 3
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see 
// <https://www.gnu.org/licenses/> or write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
///////////////////////////////////////////////////////////////////////////////////////////////
package com.robertvokac.bitbackup.files;

import java.nio.file.attribute.BasicFileAttributes;
import lombok.Getter;

/**
 *
 * @author robertvokac
 */
public enum FileType {
    DIR("d"), REGULAR("-"), LINK("l"), OTHER("z");

    @Getter
    private String ch;

    FileType(String ch) {
        this.ch = ch;
    }

    static FileType forFile(BasicFileAttributes basicFileAttributes) {
        if (basicFileAttributes.isDirectory()) {
            return DIR;
        }
        if (basicFileAttributes.isRegularFile()) {
            return REGULAR;
        }
        if (basicFileAttributes.isSymbolicLink()) {
            return LINK;
        }
        else return OTHER;
    }
}
