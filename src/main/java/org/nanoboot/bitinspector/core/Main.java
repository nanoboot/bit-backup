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

import org.nanoboot.bitinspector.commands.HelpCommand;
import org.nanoboot.bitinspector.commands.CheckCommand;
import org.nanoboot.bitinspector.commands.VersionCommand;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 * @since 0.0.0
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Bir - Detects bit rotten files in the given directory to keep your files forever.\n");
        
        BitInspectorArgs BitInspectorArgs = new BitInspectorArgs(args);
        String command = BitInspectorArgs.getCommand();
        
        Set<Command> commandImplementations = new HashSet<>();
        commandImplementations.add(new CheckCommand());
        commandImplementations.add(new HelpCommand());
        commandImplementations.add(new VersionCommand());
        Command foundCommand = null;
        for(Command e:commandImplementations) {
            if(e.getName().equals(command)) {
                foundCommand = e;
                break;
            }
        }
        if(foundCommand == null) {
            System.err.println("Error: Command \"" + command + "\" is not supported.\n");
            
            new HelpCommand().run(BitInspectorArgs);
            System.exit(1);
        }
        foundCommand.run(BitInspectorArgs);
    }


}
