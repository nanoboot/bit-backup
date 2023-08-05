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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.nanoboot.bitinspector.core.Utils;

/**
 *
 * @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 */
public class BirIgnoreRegex implements Predicate<String> {
    
    private final List<String> patterns = new ArrayList<>();
    
    public BirIgnoreRegex(File birIgnoreFile) {
        
        patterns.add(convertUnixRegexToJavaRegex("*.birreport.csv"));
        addBirIgnoreFile(birIgnoreFile);
        
    }

    public final void addBirIgnoreFile(File birIgnoreFile) {
        addBirIgnoreFile(birIgnoreFile, null);
    }
    public final void addBirIgnoreFile(File birIgnoreFile, File workingDir) {
        String[] lines = birIgnoreFile.exists() ? Utils.readTextFromFile(birIgnoreFile).split("\\R") : new String[]{};
        String addPrefix = workingDir == null ? "" : birIgnoreFile.getParentFile().getAbsolutePath().replace(workingDir.getAbsolutePath() + "/", "");
            
        for (String l : lines) {
            if (l.isBlank() || l.trim().startsWith("#")) {
                //nothing to do
                continue;
            }
            if(addPrefix == null) {
                patterns.add(convertUnixRegexToJavaRegex(l));
            } else {
                patterns.add(convertUnixRegexToJavaRegex(addPrefix + l));
                patterns.forEach(e->System.out.println("$$$" + e));
            }
            
        }
    }
    
    @Override
    public boolean test(String text) {
        if (patterns.isEmpty()) {
            //nothing to do
            return false;
        }
        boolean ignore = false;
        for (String p : patterns) {
            boolean b = Pattern.matches(p, text);
            if (b) {
                ignore = true;
            } else {
                
            }
        }
//        if (ignore) {
//            System.out.println("ignoring file: " + text);
//        } else {
//            System.out.println("accepting file: " + text);
//        }
        return ignore;
    }
    
    public static String convertUnixRegexToJavaRegex(String wildcard) {
        StringBuffer s = new StringBuffer(wildcard.length());
        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            char c = wildcard.charAt(i);
            switch (c) {
                case '*':
                    s.append(".*");
                    break;
                case '?':
                    s.append(".");
                    break;
                // escape special regexp-characters
                case '(':
                case ')':
                case '[':
                case ']':
                case '$':
                case '^':
                case '.':
                case '{':
                case '}':
                case '|':
                case '\\':
                    s.append("\\");
                    s.append(c);
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        s.append('$');
        return (s.toString());
    }
    
}
