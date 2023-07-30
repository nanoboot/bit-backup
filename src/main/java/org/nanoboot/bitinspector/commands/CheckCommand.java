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
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.nanoboot.bitinspector.core.Command;
import org.nanoboot.bitinspector.core.BitInspectorArgs;
import org.nanoboot.bitinspector.core.BitInspectorException;
import org.nanoboot.bitinspector.core.Utils;
import org.nanoboot.bitinspector.entity.FsFile;
import org.nanoboot.bitinspector.entity.SystemItem;
import org.nanoboot.bitinspector.persistence.api.FileRepository;
import org.nanoboot.bitinspector.persistence.api.SystemItemRepository;
import org.nanoboot.bitinspector.persistence.impl.sqlite.FileRepositoryImplSqlite;
import org.nanoboot.bitinspector.persistence.impl.sqlite.SqliteDatabaseMigration;
import org.nanoboot.bitinspector.persistence.impl.sqlite.SystemItemRepositoryImplSqlite;

/**
 *
 * @author pc00289
 */
public class CheckCommand implements Command {

    private final File currentDirRoot = new File(".");
    private final File birSQLite3File = new File("./.bir.sqlite3");
    private final File birSQLite3FileSha512 = new File("./.bir.sqlite3.sha512");
    private final File birIgnore = new File("./.birignore");
    BirIgnoreRegex birIgnoreRegex = new BirIgnoreRegex(birIgnore);

    public CheckCommand() {
    }

    @Override
    public String getName() {
        return "check";
    }

    static int i = 0;

    @Override
    public void run(BitInspectorArgs bitInspectorArgs) {
        SqliteDatabaseMigration sqliteDatabaseMigration = new SqliteDatabaseMigration();
        sqliteDatabaseMigration.migrate();

        SystemItemRepository systemItemRepository = new SystemItemRepositoryImplSqlite();
        FileRepository fileRepository = new FileRepositoryImplSqlite();
        ////
        String version = systemItemRepository.read("bir.version").getValue();
        System.out.println("bir.version=" + version);
        if (version == null) {
            systemItemRepository.create(new SystemItem("bir.version", "0.0.0-SNAPSHOT"));
        }
        System.out.println("Updating version in DB.");
        version = systemItemRepository.read("bir.version").getValue();
        System.out.println("bir.version=" + version);
        ////

        //// Check ,SQLite DB file has the expected SHA-512 hash sum
        if (birSQLite3File.exists() && birSQLite3FileSha512.exists()) {
            String expectedHash = Utils.readTextFromFile(birSQLite3FileSha512);
            String returnedHash = Utils.calculateSHA512Hash(birSQLite3File);
            if (!returnedHash.equals(expectedHash)) {
                throw new BitInspectorException("Unexpected hash " + returnedHash + ". Expected SHA-512 hash sum was: " + expectedHash + " for file " + birSQLite3File.getAbsolutePath());
            }
        }
        //// Found files in directory

        List<File> filesInDir = foundFilesInCurrentDir(currentDirRoot, new ArrayList<>());
        Set<String> filesInDirSet = filesInDir.stream().map(f -> loadPathButOnlyTheNeededPart(currentDirRoot, f)).collect(Collectors.toSet());
        System.out.println("Found files:");
        filesInDir.stream().forEach((f -> System.out.println("#" + (++i) + " " + f.getAbsolutePath().substring(currentDirRoot.getAbsolutePath().length() + 1))));

        //// Found files in DB
        List<FsFile> filesInDb = fileRepository.list();
        Set<String> filesInDbSet = filesInDb.stream().map(f -> f.getAbsolutePath()).collect(Collectors.toSet());
        System.out.println("Files in DB:");
        i = 0;
        filesInDb.stream().forEach((f -> System.out.println("#" + (++i) + " " + f.toString())));

        //// Add new files
        Date lastChecked = new Date();
        org.nanoboot.powerframework.time.moment.LocalDateTime now = org.nanoboot.powerframework.time.moment.LocalDateTime.convertJavaUtilDateToPowerLocalDateTime(lastChecked);

        int processedCount0 = 0;

        List<FsFile> filesMissingInDb = new ArrayList<>();
        for (File fileInDir : filesInDir) {
            processedCount0 = processedCount0 + 1;
            if (processedCount0 % 100 == 0) {
                double progress = ((double) processedCount0) / filesInDir.size() * 100;
                System.out.println("Add - Progress: " + processedCount0 + "/" + filesInDir.size() + " " + String.format("%,.2f", progress) + "%");
            }

            String absolutePathOfFileInDir = loadPathButOnlyTheNeededPart(currentDirRoot, fileInDir);
            if (!filesInDbSet.contains(absolutePathOfFileInDir)) {
                Date lastModified = new Date(fileInDir.lastModified());
                org.nanoboot.powerframework.time.moment.LocalDateTime ldt = org.nanoboot.powerframework.time.moment.LocalDateTime.convertJavaUtilDateToPowerLocalDateTime(lastModified);

                FsFile fsFile = new FsFile(
                        UUID.randomUUID().toString(),
                        fileInDir.getName(),
                        absolutePathOfFileInDir,
                        ldt.toString(),
                        now.toString(),
                        Utils.calculateSHA512Hash(fileInDir),
                        "SHA-512"
                );
                filesMissingInDb.add(fsFile);
            }

        }
        fileRepository.create(filesMissingInDb);

        //// Remove deleted files
        List<FsFile> filesToBeRemovedFromDb = new ArrayList<>();
        int processedCount1 = 0;

        for (FsFile fileInDb : filesInDb) {
            processedCount1 = processedCount1 + 1;
            if (processedCount1 % 100 == 0) {
                double progress = ((double) processedCount1) / filesInDb.size() * 100;
                System.out.println("Remove - Progress: " + processedCount1 + "/" + filesInDb.size() + " " + String.format("%,.2f", progress) + "%");
            }

            String absolutePathOfFileInDb = fileInDb.getAbsolutePath();
            if (!filesInDirSet.contains(absolutePathOfFileInDb)) {

                filesToBeRemovedFromDb.add(fileInDb);
            }

        }
        for (FsFile f : filesToBeRemovedFromDb) {
            fileRepository.remove(f);
        }

        double countOfFilesToCalculateHashSum = filesInDb.size() - filesToBeRemovedFromDb.size();
        int processedCount = 0;
        //// Update modified files with same last modification date
        List<FsFile> filesWithBitRot = new ArrayList<>();
        List<FsFile> filesToUpdateLastCheckDate = new ArrayList<>();
        for (FsFile fileInDb : filesInDb) {
            String absolutePathOfFileInDb = fileInDb.getAbsolutePath();
            if (filesToBeRemovedFromDb.contains(fileInDb)) {
                //nothing to do
                continue;

            }
            processedCount = processedCount + 1;
            if (processedCount % 100 == 0) {
                double progress = ((double) processedCount) / countOfFilesToCalculateHashSum * 100;
                System.out.println("Update - Progress: " + processedCount + "/" + countOfFilesToCalculateHashSum + " " + String.format("%,.2f", progress) + "%");
            }
            File file = new File("./" + absolutePathOfFileInDb);

            Date lastModified = new Date(file.lastModified());
            org.nanoboot.powerframework.time.moment.LocalDateTime ldt = org.nanoboot.powerframework.time.moment.LocalDateTime.convertJavaUtilDateToPowerLocalDateTime(lastModified);

            String calculatedHash = Utils.calculateSHA512Hash(file);
            if (ldt.toString().equals(fileInDb.getLastModificationDate()) && !calculatedHash.equals(fileInDb.getHashSumValue())) {
                filesWithBitRot.add(fileInDb);
                fileInDb.setLastCheckDate(now.toString());
                fileRepository.updateFile(fileInDb);
                continue;
            }
            if (!ldt.toString().equals(fileInDb.getLastModificationDate())) {
                fileInDb.setLastCheckDate(now.toString());
                fileInDb.setLastModificationDate(ldt.toString());
                fileInDb.setHashSumValue(calculatedHash);
                fileInDb.setHashSumAlgorithm("SHA-512");
                fileRepository.updateFile(fileInDb);
                continue;
            }
            if (ldt.toString().equals(fileInDb.getLastModificationDate())) {
                filesToUpdateLastCheckDate.add(fileInDb);
                continue;
            }

        }
        fileRepository.updateLastCheckDate(now.toString(), filesToUpdateLastCheckDate);

        //// Report files, which may have a bitrot and will have to be restored from a backup
        System.out.println("\n\n");

        if (filesWithBitRot.isEmpty()) {
            System.out.println("No files with bit rot were found.");
        }
        filesWithBitRot.stream().forEach(f
                -> System.out.println("Bit rot detected: \"" + f.getAbsolutePath() + "\"" + " expected_sha512=" + f.getHashSumValue() + " returned_sha512=" + Utils.calculateSHA512Hash(new File("./" + f.getAbsolutePath())))
        );
        if (bitInspectorArgs.hasArgument("reportid")) {
            String reportId = bitInspectorArgs.getArgument("reportid");
            File reportIdFile = new File("./" + reportId + ".birreport.csv");
            if (reportIdFile.exists()) {
                Long nowLong = org.nanoboot.powerframework.time.moment.UniversalDateTime.now().toLong();

                File backup = new File(reportIdFile.getParentFile().getAbsolutePath() + "/" + nowLong + "." + reportIdFile.getName());
                System.out.println("backup=" + backup);
                reportIdFile.renameTo(backup);
            }
            StringBuilder sb = new StringBuilder();
            if (!filesWithBitRot.isEmpty()) {
                sb.append("file;expected;calculated\n");
            }
            filesWithBitRot.stream().forEach(f
                    -> sb.append(f.getAbsolutePath())
                            .append(";")
                            .append(f.getHashSumValue())
                            .append(";")
                            .append(Utils.calculateSHA512Hash(new File("./" + f.getAbsolutePath())))
                            .append("\n")
            );
            Utils.writeTextToFile(sb.toString(), reportIdFile);
        }
        //// Calculate current checksum of DB file.
        Utils.writeTextToFile(Utils.calculateSHA512Hash(birSQLite3File), birSQLite3FileSha512);

        System.out.println("foundFiles=" + foundFiles);
        System.out.println("foundDirs=" + foundDirs);

    }

    private String loadPathButOnlyTheNeededPart(File currentDir, File file) {
        return file.getAbsolutePath().substring(currentDir.getAbsolutePath().length() + 1);
    }
    static int iii = 0;

    private int foundFiles;
    private int foundDirs;

    private List<File> foundFilesInCurrentDir(File currentDir, List<File> files) {

        for (File f : currentDir.listFiles()) {
            if (f.isDirectory()) {
                ++foundDirs;
                foundFilesInCurrentDir(f, files);
            } else {
                ++foundFiles;
                if (f.getAbsolutePath().equals(birSQLite3File.getAbsolutePath())) {
                    continue;
                }
                if (f.getAbsolutePath().equals(birSQLite3FileSha512.getAbsolutePath())) {
                    continue;
                }
                if (f.getAbsolutePath().equals(birIgnore.getAbsolutePath())) {
                    continue;
                }
                ++iii;
                //System.out.println("Testing file: " + iii + "#" + " " + loadPathButOnlyTheNeededPart(currentDirRoot, f));
                if (birIgnoreRegex.test(loadPathButOnlyTheNeededPart(currentDirRoot, f))) {
                    continue;
                }
                files.add(f);
            }
        }
        return files;
    }

}
