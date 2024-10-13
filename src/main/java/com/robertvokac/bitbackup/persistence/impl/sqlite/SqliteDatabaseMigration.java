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
package com.robertvokac.bitbackup.persistence.impl.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.robertvokac.bitbackup.core.Constants;
import com.robertvokac.bitbackup.core.Utils;
import com.robertvokac.dbmigration.core.main.DBMigration;
import com.robertvokac.dbmigration.core.main.MigrationResult;

/**
 *
* @author <a href="mailto:mail@robertvokac.com">Robert Vokac</a>
 */
public class SqliteDatabaseMigration {

    private static SqliteDatabaseMigration INSTANCE;

    private SqliteDatabaseMigration() {
        //Not meant to be instantiated
    }

    public static SqliteDatabaseMigration getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SqliteDatabaseMigration();
        }
        return INSTANCE;
    }

    public MigrationResult migrate(String directoryWhereSqliteFileIs) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
            throw new RuntimeException(ex);
        }
        String jdbcUrl = Utils.createJdbcUrl(directoryWhereSqliteFileIs);
        System.err.println("jdbcUrl=" + jdbcUrl);
        String clazz = this.getClass().getName();

        DBMigration dbMigration = DBMigration
                .configure()
                .dataSource(jdbcUrl)
                .installedBy("bitbackup-persistence-impl-sqlite")
                .name("bitbackup")
                .sqlDialect("sqlite", "com.robertvokac.dbmigration.core.persistence.impl.sqlite.DBMigrationPersistenceSqliteImpl")
                .sqlMigrationsClass(clazz)
                .load();
        return dbMigration.migrate();
    }

}
