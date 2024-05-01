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
package org.nanoboot.bitbackup.persistence.impl.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nanoboot.bitbackup.entity.SystemItem;
import org.nanoboot.bitbackup.persistence.api.SystemItemRepository;

/**
 *
 * @author <a href="mailto:mail@robertvokac.com">Robert Vokac</a>
 */
public class SystemItemRepositoryImplSqlite implements SystemItemRepository {

    public SystemItemRepositoryImplSqlite(SqliteConnectionFactory sqliteConnectionFactory) {
        this.sqliteConnectionFactory = sqliteConnectionFactory;
    }

    private final SqliteConnectionFactory sqliteConnectionFactory;

    @Override
    public String create(SystemItem systemItem) {

        StringBuilder sb = new StringBuilder();
        sb
                .append("INSERT INTO ")
                .append(SystemItemTable.TABLE_NAME)
                .append("(")
                .append(SystemItemTable.KEY).append(",")
                //
                .append(SystemItemTable.VALUE);

        sb.append(")")
                .append(" VALUES (?,?)");

        String sql = sb.toString();
        System.err.println(sql);
        try (
                Connection connection = sqliteConnectionFactory.createConnection(); PreparedStatement stmt = connection.prepareStatement(sql);) {
            int i = 0;
            stmt.setString(++i, systemItem.getKey());
            stmt.setString(++i, systemItem.getValue());

            //
            stmt.execute();
            System.out.println(stmt.toString());

            return systemItem.getKey();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SystemItemRepositoryImplSqlite.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.err.println("Error.");
        return "";
    }

    @Override
    public List<SystemItem> list() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public SystemItem read(String key) {
        if (key == null) {
            throw new RuntimeException("key is null");
        }
        StringBuilder sb = new StringBuilder();
        sb
                .append("SELECT * FROM ")
                .append(SystemItemTable.TABLE_NAME)
                .append(" WHERE ")
                .append(SystemItemTable.KEY)
                .append("=?");

        String sql = sb.toString();
        int i = 0;
        ResultSet rs = null;
        try (
                Connection connection = sqliteConnectionFactory.createConnection(); PreparedStatement stmt = connection.prepareStatement(sql);) {

            stmt.setString(++i, key);

            rs = stmt.executeQuery();

            while (rs.next()) {
                return extractSystemItemFromResultSet(rs);
            }
            return new SystemItem(key, null);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SystemItemRepositoryImplSqlite.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(SystemItemRepositoryImplSqlite.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    @Override
    public void remove(String key) {
        if (key == null) {
            throw new RuntimeException("key is null");
        }
        StringBuilder sb = new StringBuilder();
        sb
                .append("DELETE FROM ")
                .append(SystemItemTable.TABLE_NAME)
                .append(" WHERE ")
                .append(SystemItemTable.KEY)
                .append("=?");

        String sql = sb.toString();
        try (
                Connection connection = sqliteConnectionFactory.createConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SystemItemRepositoryImplSqlite.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

        }
    }

    @Override
    public void update(SystemItem systemItem) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private SystemItem extractSystemItemFromResultSet(final ResultSet rs) throws SQLException {
        return new SystemItem(
                rs.getString(SystemItemTable.KEY),
                rs.getString(SystemItemTable.VALUE)
        );
    }
}
