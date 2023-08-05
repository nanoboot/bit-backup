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

import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nanoboot.bitinspector.commands.CheckCommand;
import org.nanoboot.bitinspector.commands.HelpCommand;
import org.nanoboot.bitinspector.commands.VersionCommand;

/**
 *
 * @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 */
public class BitInspector {

    private static final Logger LOG = LogManager.getLogger(BitInspector.class);
    
    private final Set<Command> commandImplementations;
    public BitInspector() {
        commandImplementations = new HashSet<>();
        commandImplementations.add(new CheckCommand());
        commandImplementations.add(new HelpCommand());
        commandImplementations.add(new VersionCommand());
    }
       
    public void run(String[] args) {
        run(new BirArgs(args));
    }
    
    public void run(BirArgs bitInspectorArgs) {
        String command = bitInspectorArgs.getCommand();
        Command foundCommand = null;
        for(Command e:commandImplementations) {
            if(e.getName().equals(command)) {
                foundCommand = e;
                break;
            }
        }
        if(foundCommand == null) {
            String msg = "Command \"" + command + "\" is not supported.";
            LOG.error(msg);
            
            new HelpCommand().run(bitInspectorArgs);
            throw new BitInspectorException(msg);
        }
        foundCommand.run(bitInspectorArgs);
        
    }
}