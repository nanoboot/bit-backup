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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nanoboot.bitbackup.entity.FsFile;
import org.nanoboot.bitbackup.persistence.api.FileRepository;

/**
 *
 * @author <a href="mailto:mail@robertvokac.com">Robert Vokac</a>
 */
public class FileRepositoryImplSqlite implements FileRepository {

    private SqliteConnectionFactory sqliteConnectionFactory;

    public FileRepositoryImplSqlite(SqliteConnectionFactory sqliteConnectionFactory) {
        this.sqliteConnectionFactory = sqliteConnectionFactory;
    }

    @Override
    public void create(List<FsFile> files) {
        if (files.isEmpty()) {
            return;
        }
        if (files.size() > 100) {
            List<FsFile> tmpList = new ArrayList<>();
            for (int i = 0; i < files.size(); i++) {
                FsFile e = files.get(i);
                tmpList.add(e);
                if (tmpList.size() >= 100) {
                    create(tmpList);
                    tmpList.clear();
                }
            }
            if (!tmpList.isEmpty()) {
                create(tmpList);
                tmpList.clear();
            }
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb
                .append("INSERT INTO ")
                .append(FileTable.TABLE_NAME)
                .append("(")
                .append(FileTable.ID).append(",")
                .append(FileTable.NAME).append(",")
                .append(FileTable.ABSOLUTE_PATH).append(",")
                .append(FileTable.LAST_MODIFICATION_DATE).append(",")
                .append(FileTable.LAST_CHECK_DATE).append(",")
                //
                .append(FileTable.HASH_SUM_VALUE).append(",")
                .append(FileTable.HASH_SUM_ALGORITHM).append(",")
                .append(FileTable.SIZE).append(",")
                .append(FileTable.LAST_CHECK_RESULT).append("");

        sb.append(") VALUES ");

        int index = 0;
        for (FsFile f : files) {
            sb.append(" (?,?,?,?,?, ?,?,?,?)");
            boolean lastFile = index == (files.size() - 1);
            if (!lastFile) {
                sb.append(",");
            }
            index = index + 1;
        }

        String sql = sb.toString();
        //System.err.println(sql);
        try (
                Connection connection = createConnection(); PreparedStatement stmt = connection.prepareStatement(sql);) {
            int i = 0;

            for (FsFile f : files) {
                stmt.setString(++i, f.getId());
                stmt.setString(++i, f.getName());
                stmt.setString(++i, f.getAbsolutePath());
                stmt.setString(++i, f.getLastModificationDate());
                //
                stmt.setString(++i, f.getLastCheckDate());
                //
                stmt.setString(++i, f.getHashSumValue());
                stmt.setString(++i, f.getHashSumAlgorithm());
                stmt.setLong(++i, f.getSize());
                stmt.setString(++i, f.getLastCheckResult());

            }
            //
            stmt.execute();
            //System.out.println(stmt.toString());

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FileRepositoryImplSqlite.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public List<FsFile> list() {

        List<FsFile> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb
                .append("SELECT * FROM ")
                .append(FileTable.TABLE_NAME);

        String sql = sb.toString();
//        System.err.println(sql);
        int i = 0;
        ResultSet rs = null;
        try (
                Connection connection = createConnection(); PreparedStatement stmt = connection.prepareStatement(sql);) {

            System.err.println(stmt.toString());
            rs = stmt.executeQuery();

            while (rs.next()) {
                result.add(extractFileFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FileRepositoryImplSqlite.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(FileRepositoryImplSqlite.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    @Override
    public void remove(FsFile file) {

        StringBuilder sb = new StringBuilder();
        sb
                .append("DELETE FROM ")
                .append(FileTable.TABLE_NAME);
        sb.append(" WHERE ");

        sb.append(FileTable.ID);
        sb.append("=?");
        String sql = sb.toString();
        //System.err.println("SQL::" + sql);
        int i = 0;

        try (
                Connection connection = createConnection(); PreparedStatement stmt = connection.prepareStatement(sql);) {

            stmt.setString(++i, file.getId());

            //System.err.println(stmt.toString());
            stmt.execute();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FileRepositoryImplSqlite.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Connection createConnection() throws ClassNotFoundException {
        return sqliteConnectionFactory.createConnection();
    }

    @Override
    public void updateFile(FsFile file) {

        StringBuilder sb = new StringBuilder();
        sb
                .append("UPDATE ")
                .append(FileTable.TABLE_NAME)
                .append(" SET ")
                .append(FileTable.LAST_MODIFICATION_DATE).append("=?, ")
                .append(FileTable.LAST_CHECK_DATE).append("=?, ")
                .append(FileTable.HASH_SUM_VALUE).append("=?, ")
                .append(FileTable.HASH_SUM_ALGORITHM).append("=?, ")
                .append(FileTable.SIZE).append("=?, ")
                .append(FileTable.LAST_CHECK_RESULT).append("=? ")
                .append(" WHERE ").append(FileTable.ID).append("=?");

        String sql = sb.toString();
        //System.err.println(sql);
        try (
                Connection connection = createConnection(); PreparedStatement stmt = connection.prepareStatement(sql);) {
            int i = 0;
            stmt.setString(++i, file.getLastModificationDate());
            stmt.setString(++i, file.getLastCheckDate());
            stmt.setString(++i, file.getHashSumValue());
            stmt.setString(++i, file.getHashSumAlgorithm());
            stmt.setLong(++i, file.getSize());
            stmt.setString(++i, file.getLastCheckResult());

            stmt.setString(++i, file.getId());

            int numberOfUpdatedRows = stmt.executeUpdate();
            //System.out.println("numberOfUpdatedRows=" + numberOfUpdatedRows);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FileRepositoryImplSqlite.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private FsFile extractFileFromResultSet(final ResultSet rs) throws SQLException {
        return new FsFile(
                rs.getString(FileTable.ID),
                rs.getString(FileTable.NAME),
                rs.getString(FileTable.ABSOLUTE_PATH),
                rs.getString(FileTable.LAST_MODIFICATION_DATE),
                rs.getString(FileTable.LAST_CHECK_DATE),
                rs.getString(FileTable.HASH_SUM_VALUE),
                rs.getString(FileTable.HASH_SUM_ALGORITHM),
                rs.getLong(FileTable.SIZE),
                rs.getString(FileTable.LAST_CHECK_RESULT)
        );
    }

    @Override
    public void updateLastCheckDate(String lastCheckDate, List<FsFile> files) {

        if (files.isEmpty()) {
            return;
        }
        if (files.size() > 100) {
            List<FsFile> tmpList = new ArrayList<>();
            for (int i = 0; i < files.size(); i++) {
                FsFile e = files.get(i);
                tmpList.add(e);
                if (tmpList.size() >= 100) {
                    updateLastCheckDate(lastCheckDate, tmpList);
                    tmpList.clear();
                }
            }
            if (!tmpList.isEmpty()) {
                updateLastCheckDate(lastCheckDate, tmpList);
                tmpList.clear();
            }
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb
                .append("UPDATE ")
                .append(FileTable.TABLE_NAME)
                .append(" SET ")
                .append(FileTable.LAST_CHECK_DATE).append("=?, ")
                .append(FileTable.LAST_CHECK_RESULT).append("='OK' ")
                .append(" WHERE ").append(FileTable.ID).append(" IN (");
        int index = 0;
        for (FsFile f : files) {
            index = index + 1;
            sb.append("?");
            if (index < files.size()) {
                sb.append(",");
            }
        }
        sb.append(")");

        String sql = sb.toString();
        //System.err.println(sql);
        try (
                Connection connection = createConnection(); PreparedStatement stmt = connection.prepareStatement(sql);) {
            int i = 0;

            stmt.setString(++i, lastCheckDate);

            for (FsFile f : files) {
                stmt.setString(++i, f.getId());
            }

            int numberOfUpdatedRows = stmt.executeUpdate();
            //System.out.println("numberOfUpdatedRows=" + numberOfUpdatedRows);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FileRepositoryImplSqlite.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
