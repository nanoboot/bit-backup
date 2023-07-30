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
package org.nanoboot.bitinspector.commands;

import org.nanoboot.bitinspector.core.Command;
import org.nanoboot.bitinspector.core.BitInspectorArgs;

/**
 *
 * @author pc00289
 */
public class HelpCommand implements Command {

    public HelpCommand() {
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void run(BitInspectorArgs bitInspectorArgs) {
        String str = """
    NAME
        bir - " Bit Inspector"
                           
    SYNOPSIS
        bir [command] [options]
        If no command is provided, then the default command check is used. This means, if you run "bit-inspector", it is the same, as to run "bir check".
                           
    DESCRIPTION
        Detects bit rotten files in the given directory to keep your files forever.
                           
    COMMAND
        check       Generates the static website
                        OPTIONS
                            reportid={unique name for this report, usually `date +'%Y%m%d_%H%M%S'`}
                                Optional. Default= (nothing will be reported to file report.{reportid}.bitreport.txt).
        help        Display help information
        version     Display version information                           
""";
        System.out.println(str);

    }

}
