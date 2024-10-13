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

import com.robertvokac.bitbackup.persistence.api.ConnectionFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.robertvokac.bitbackup.core.Utils;

/**
 *
 * @author <a href="mailto:mail@robertvokac.com">Robert Vokac</a>
 */
public class SqliteConnectionFactory implements ConnectionFactory {
    private final String jdbcUrl;
    public SqliteConnectionFactory(String directoryWhereSqliteFileIs) {
        this.jdbcUrl = Utils.createJdbcUrl(directoryWhereSqliteFileIs);
    }
    public Connection createConnection() throws ClassNotFoundException {
        try {
            Class.forName("org.sqlite.JDBC");

            Connection conn = DriverManager.getConnection(jdbcUrl);

            return conn;

        } catch (SQLException ex) {
            if (true) {
                throw new RuntimeException(ex);
            }
            System.err.println(ex.getMessage());
            return null;
        }
    }

}
