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
import lombok.Setter;
import com.robertvokac.bitbackup.core.BitBackupException;

/**
 *
 * @author robertvokac
 */
@Setter
@Getter
public class UnixPermission {

    private static final char R = 'r';
    private static final char W = 'w';
    private static final char X = 'x';
    private static final char LOWER_S = 's';
    private static final char UPPER_S = 'S';
    private static final char LOWER_T = 't';
    private static final char UPPER_T = 'T';
    private static final char DASH = '-';

    public UnixPermission(com.robertvokac.bitbackup.files.UnixPermissionType linuxPermissionType) {
        this.unixPermissionType = linuxPermissionType;
    }
    private final UnixPermissionType unixPermissionType;
    private boolean read;
    private boolean write;
    private boolean execute;
    private boolean special;

    public void setFromString(String s) {
        if (s.length() != 3) {
            throw new BitBackupException("Cannot parse UnixPermission, because the expected lenght is 9, but given is " + s.length());
        }
        char[] charArray = s.toCharArray();
        final char ch1 = charArray[0];
        final char ch2 = charArray[1];
        final char ch3 = charArray[2];
        switch(ch1) {
            case R : read = true; break;
            case DASH : read = false; break;
            default: throw new BitBackupException("Cannot parse read UnixPermission from character: " + ch1);
        }
        
        switch(ch2) {
            case W : write = true; break;
            case DASH : write = false; break;
            default: throw new BitBackupException("Cannot parse write UnixPermission from character: " + ch1);
        }
        
        switch(ch3) {
            case X : execute = true; special = false; break;
            case LOWER_S: if(unixPermissionType == UnixPermissionType.OTHERS) {throw new BitBackupException("Invalid letter " + ch3);} execute = true; special = true; break;
            case UPPER_S: if(unixPermissionType == UnixPermissionType.OTHERS) {throw new BitBackupException("Invalid letter " + ch3);} execute = false; special = true; break;
            case LOWER_T: if(unixPermissionType != UnixPermissionType.OTHERS) {throw new BitBackupException("Invalid letter " + ch3);} execute = true; special = true; break;
            case UPPER_T: if(unixPermissionType != UnixPermissionType.OTHERS) {throw new BitBackupException("Invalid letter " + ch3);} execute = false; special = true; break;
            case DASH : execute = false; special = false;break;
            default: throw new BitBackupException("Cannot parse execute UnixPermission from character: " + ch1);
        }

    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(read ? R : DASH);
        sb.append(write ? W : DASH);
        if (execute) {
            if (special) {
                sb.append(unixPermissionType == UnixPermissionType.OTHERS ? LOWER_T : LOWER_S);
            } else {
                sb.append(X);
            }
        } else {
            if (special) {
                sb.append(unixPermissionType == UnixPermissionType.OTHERS ? UPPER_T : UPPER_S);
            } else {
                sb.append(DASH);
            }
        }
        return sb.toString();
    }

}
