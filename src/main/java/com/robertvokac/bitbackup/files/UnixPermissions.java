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
package com.robertvokac.bitbackup.files;

import lombok.Getter;
import com.robertvokac.bitbackup.core.BitBackupException;

/**
 *
 * @author robertvokac
 */
@Getter
public class UnixPermissions {

    private UnixPermission user = new UnixPermission(UnixPermissionType.USER);
    private UnixPermission group = new UnixPermission(UnixPermissionType.GROUP);
    private UnixPermission others = new UnixPermission(UnixPermissionType.OTHERS);

    public UnixPermissions() {

    }

    public UnixPermissions(String s) {
        if (s.length() != 9) {
            throw new BitBackupException("Cannot parse UnixPermissions, because the expected lenght is 9, but given is " + s.length());
        }
        user.setFromString(s.substring(0, 3));
        group.setFromString(s.substring(3, 6));
        others.setFromString(s.substring(6, 9));
    }

    public void setSUID(boolean b) {
        user.setSpecial(b);
    }

    public void setSGID(boolean b) {
        group.setSpecial(b);
    }

    public void setStickyBit(boolean b) {
        others.setSpecial(b);
    }

    public boolean isSUID(boolean b) {
        return user.isSpecial();
    }

    public boolean isSGID(boolean b) {
        return group.isSpecial();
    }

    public boolean isStickyBit(boolean b) {
        return others.isSpecial();
    }

    public String toString() {
        return user.toString() + group.toString() + others.toString();
    }

}
