///////////////////////////////////////////////////////////////////////////////////////////////
// bit-backup: Tool detecting bit rots in files.
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

package org.nanoboot.bitbackup.core;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 *
 * @author robertvokac
 */
public class ListSet<T> implements Iterable<T>  {
    @Getter
    private final List<T> list;
    @Getter
    private final Set<String> set;

    public ListSet(List<T> list, Function<? super T, String> mapper) {
        this.list = Collections.unmodifiableList(list);
        this.set = Collections.unmodifiableSet(list.stream().map(mapper).collect(Collectors.toSet()));
    }
    public boolean doesSetContains(String s) {
        return set.contains(s);
    }
    public int size() {
        return list.size();
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }
    
    
}
