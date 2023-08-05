///////////////////////////////////////////////////////////////////////////////////////////////
// bit-inspector: Tool detecting bit rots in files.
// Copyright (C) 2023-2023 the original author or authors.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; version 2
// of the License only.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
///////////////////////////////////////////////////////////////////////////////////////////////
package org.nanoboot.bitinspector.persistence.impl.sqlite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *
* @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 */
@Getter
@Setter
@AllArgsConstructor
class SystemItemTable {
    public static final String TABLE_NAME = "SYSTEM_ITEM";
    
    public static final String KEY = "KEY";
    public static final String VALUE = "VALUE";

}
