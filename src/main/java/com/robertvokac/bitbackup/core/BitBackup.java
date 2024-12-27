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

import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.robertvokac.bitbackup.commands.CheckCommand;
import com.robertvokac.bitbackup.commands.HelpCommand;
import com.robertvokac.bitbackup.commands.VersionCommand;

/**
 *
 * @author <a href="mailto:mail@robertvokac.com">Robert Vokac</a>
 */
public class BitBackup {

    private static final Logger LOG = LogManager.getLogger(BitBackup.class);
    
    private final Set<Command> commandImplementations;
    public BitBackup() {
        commandImplementations = new HashSet<>();
        commandImplementations.add(new CheckCommand());
        commandImplementations.add(new HelpCommand());
        commandImplementations.add(new VersionCommand());
    }
       
    public void run(String[] args) {
        run(new BitBackupArgs(args));
    }
    
    public void run(BitBackupArgs bitBackupArgs) {
        String command = bitBackupArgs.getCommand();
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
            
            new HelpCommand().run(bitBackupArgs);
            throw new BitBackupException(msg);
        }
        foundCommand.run(bitBackupArgs);
        
    }
}
