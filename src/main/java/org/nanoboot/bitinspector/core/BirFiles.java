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

import java.io.File;
import lombok.Data;
import lombok.Getter;
import org.nanoboot.bitinspector.commands.BirIgnoreRegex;

/**
 *
 * @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 */
@Data
@Getter
public class BirFiles {

    private final File workingDir;
    private final String workingDirAbsolutePath;
    private final File birSQLite3File;
    private final File birSQLite3FileSha512;
    private final File birIgnore;
    private final BirIgnoreRegex birIgnoreRegex;
    private final File birReportCsv;
    

    public BirFiles(BirArgs bitInspectorArgs) {
        workingDir = new File(bitInspectorArgs.hasArgument("dir") ? bitInspectorArgs.getArgument("dir") : ".");
        workingDirAbsolutePath = workingDir.getAbsolutePath();
        birSQLite3File = new File(workingDirAbsolutePath + "/.bir.sqlite3");
        birSQLite3FileSha512 = new File(workingDirAbsolutePath + "/.bir.sqlite3.sha512");
        birIgnore = new File(workingDirAbsolutePath + "/.birignore");
        birIgnoreRegex = new BirIgnoreRegex(birIgnore);
        birReportCsv = new File(workingDirAbsolutePath + "/.birreport.csv");
    }
}
