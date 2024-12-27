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
package com.robertvokac.bitbackup.core;

import java.io.File;
import lombok.Data;
import lombok.Getter;
import com.robertvokac.bitbackup.commands.BitBackupIgnoreRegex;

/**
 *
 * @author <a href="mailto:mail@robertvokac.com">Robert Vokac</a>
 */
@Data
@Getter
public class BitBackupFiles {

    private final File workingDir;
    private final String workingDirAbsolutePath;
    private final File bitBackupSQLite3File;
    private final File bitBackupSQLite3FileSha512;
    private final File bitBackupIgnore;
    private final BitBackupIgnoreRegex bitBackupIgnoreRegex;
    private final File bitBackupReportCsv;
    private final File bitbackupindex;
    
    public BitBackupFiles(BitBackupArgs bitInspectorArgs) {
        workingDir = new File(bitInspectorArgs.hasArgument("dir") ? bitInspectorArgs.getArgument("dir") : ".");
        workingDirAbsolutePath = workingDir.getAbsolutePath();
        bitBackupSQLite3File = new File(workingDirAbsolutePath + "/.bitbackup.sqlite3");
        bitBackupSQLite3FileSha512 = new File(workingDirAbsolutePath + "/.bitbackup.sqlite3.sha512");
        bitBackupIgnore = new File(workingDirAbsolutePath + "/.bitbackupignore");
        bitBackupIgnoreRegex = new BitBackupIgnoreRegex(bitBackupIgnore);
        bitBackupReportCsv = new File(workingDirAbsolutePath + "/.bitbackupreport.csv");
        bitbackupindex = new File(workingDirAbsolutePath + "/.bitbackupindex.csv");
    }
}
