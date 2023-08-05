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

package org.nanoboot.bitinspector.persistence.api;

import java.util.List;
import org.nanoboot.bitinspector.entity.SystemItem;

/**
 *
* @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 */
public interface SystemItemRepository {
    String create(SystemItem systemItem);
    List<SystemItem> list();
    SystemItem read(String key);
    void remove(String key);
    void update(SystemItem systemItem);
    
}
