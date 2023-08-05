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

package org.nanoboot.bitinspector.core;

import lombok.Getter;
import org.nanoboot.bitinspector.persistence.api.ConnectionFactory;
import org.nanoboot.bitinspector.persistence.api.FileRepository;
import org.nanoboot.bitinspector.persistence.api.SystemItemRepository;
import org.nanoboot.bitinspector.persistence.impl.sqlite.FileRepositoryImplSqlite;
import org.nanoboot.bitinspector.persistence.impl.sqlite.SqliteConnectionFactory;
import org.nanoboot.bitinspector.persistence.impl.sqlite.SystemItemRepositoryImplSqlite;

/**
 *
* @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 */
public class BirContext {
    private final String directoryWhereSqliteFileIs;
    private ConnectionFactory connectionFactory;
    @Getter
    private SystemItemRepository systemItemRepository;
    @Getter
    private FileRepository fileRepository;
    
    public BirContext(String directoryWhereSqliteFileIs) {
        this.directoryWhereSqliteFileIs = directoryWhereSqliteFileIs;
        this.connectionFactory = new SqliteConnectionFactory(directoryWhereSqliteFileIs);
        systemItemRepository = new SystemItemRepositoryImplSqlite((SqliteConnectionFactory) connectionFactory);
        fileRepository = new FileRepositoryImplSqlite((SqliteConnectionFactory) connectionFactory);
    }
    
}
