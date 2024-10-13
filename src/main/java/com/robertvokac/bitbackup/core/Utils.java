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

import dev.mccue.guava.hash.Hashing;
import dev.mccue.guava.io.Files;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author <a href="mailto:mail@robertvokac.com">Robert Vokac</a>
 */
public class Utils {

    private static final String UNDERSCORE = "_";

    private Utils() {
        //Not meant to be instantiated.
    }

    public static String replaceUnderscoresBySpaces(String s) {
        if (!s.contains(UNDERSCORE)) {
            //nothing to do
            return s;
        }
        return s.replace(UNDERSCORE, " ");
    }

    public static String makeFirstLetterUppercase(String s) {
        if (Character.isLetter(s.charAt(0)) && Character.isLowerCase(s.charAt(0))) {
            return Character.toUpperCase(s.charAt(0))
                    + (s.length() == 1 ? "" : s.substring(1));
        } else {
            return s;
        }
    }

    public static List<File> listAllFilesInDir(File dir) {
        return listAllFilesInDir(dir, new ArrayList<>());
    }

    private static List<File> listAllFilesInDir(File dir, List<File> files) {
        files.add(dir);
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                listAllFilesInDir(f, files);
            } else {
                files.add(f);
            }
        }
        return files;
    }

    public static void copyFile(File originalFile, File copiedFile) throws BitBackupException {
        Path originalPath = originalFile.toPath();
        Path copied = new File(copiedFile, originalFile.getName()).toPath();

        try {
            java.nio.file.Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new BitBackupException("Copying file failed: " + originalFile.getAbsolutePath());
        }
    }

    public static void writeTextToFile(String text, File file) {
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(file);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new BitBackupException("Writing to file failed: " + file.getName(), ex);
        }
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(text);
        printWriter.close();
    }

    public static String readTextFromFile(File file) {
        if (!file.exists()) {
            return "";
        }
        try {
            return new String(java.nio.file.Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        } catch (IOException ex) {
            throw new BitBackupException("Reading file failed: " + file.getAbsolutePath(), ex);
        }
    }

    public static String readTextFromResourceFile(String fileName) {
        try {
            Class clazz = Main.class;
            InputStream inputStream = clazz.getResourceAsStream(fileName);
            return readFromInputStream(inputStream);
        } catch (IOException ex) {
            throw new BitBackupException("Reading file failed: " + fileName, ex);
        }

    }

    public static String readFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    public static String calculateSHA512Hash(File file) {
        try {
            return Files.hash(file, Hashing.sha512()).toString();
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            throw new BitBackupException(ex);
        }
    }

    public static String calculateSHA256Hash(File file) {
        if(file.isDirectory()) {return "";}
        try {
            return Files.hash(file, Hashing.sha256()).toString();
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            throw new BitBackupException(ex);
        }
    }

    public static String createJdbcUrl(String directoryWhereSqliteFileIs) {
        return "jdbc:sqlite:" + directoryWhereSqliteFileIs + "/" + ".bitbackup.sqlite3?foreign_keys=on;";
    }

    public static String encodeBase64(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes());

    }

    public static String decodeBase64(String s) {

        byte[] decodedBytes = Base64.getDecoder().decode(s);
        return new String(decodedBytes);
    }
}
