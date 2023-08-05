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
import org.nanoboot.bitinspector.core.BirArgs;

/**
 *
 * @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 */
public class HelpCommand implements Command {

    public static final String NAME = "help";

    public HelpCommand() {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String run(BirArgs bitInspectorArgs) {
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
                            dir={working directory to be checked for bit rot}
                                Optional. Default=. (current working directory)
                            report=true or false
                                Optional. Default= false (nothing will be reported to file .birreport.csv).
        help        Display help information
        version     Display version information                           
""";
        System.out.println(str);
        return str;
    }

}
