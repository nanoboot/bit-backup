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

import java.io.File;
import lombok.Data;
import lombok.Getter;
import org.nanoboot.bitbackup.commands.BibIgnoreRegex;

/**
 *
 * @author <a href="mailto:mail@robertvokac.com">Robert Vokac</a>
 */
@Data
@Getter
public class BibFiles {

    private final File workingDir;
    private final String workingDirAbsolutePath;
    private final File bibSQLite3File;
    private final File bibSQLite3FileSha512;
    private final File bibIgnore;
    private final BibIgnoreRegex bibIgnoreRegex;
    private final File bibReportCsv;
    
    @Deprecated
    private final File birSQLite3File;
    @Deprecated
    private final File birSQLite3FileSha512;
    @Deprecated
    private final File birIgnore;
    

    public BibFiles(BibArgs bitInspectorArgs) {
        workingDir = new File(bitInspectorArgs.hasArgument("dir") ? bitInspectorArgs.getArgument("dir") : ".");
        workingDirAbsolutePath = workingDir.getAbsolutePath();
        bibSQLite3File = new File(workingDirAbsolutePath + "/.bib.sqlite3");
        bibSQLite3FileSha512 = new File(workingDirAbsolutePath + "/.bib.sqlite3.sha512");
        bibIgnore = new File(workingDirAbsolutePath + "/.bibignore");
        bibIgnoreRegex = new BibIgnoreRegex(bibIgnore);
        bibReportCsv = new File(workingDirAbsolutePath + "/.bibreport.csv");
        //
        birSQLite3File = new File(workingDirAbsolutePath + "/.bir.sqlite3");
        birSQLite3FileSha512 = new File(workingDirAbsolutePath + "/.bir.sqlite3.sha512");
        birIgnore = new File(workingDirAbsolutePath + "/.birignore");
    }
}
