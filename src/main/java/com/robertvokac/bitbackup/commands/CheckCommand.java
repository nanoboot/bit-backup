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
package com.robertvokac.bitbackup.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.robertvokac.bitbackup.core.BitBackupContext;
import com.robertvokac.bitbackup.core.Command;
import com.robertvokac.bitbackup.core.BitBackupArgs;
import com.robertvokac.bitbackup.core.BitBackupException;
import com.robertvokac.bitbackup.core.BitBackupFiles;
import com.robertvokac.bitbackup.core.ListSet;
import com.robertvokac.bitbackup.core.Utils;
import com.robertvokac.bitbackup.entity.FsFile;
import com.robertvokac.bitbackup.entity.SystemItem;
import com.robertvokac.bitbackup.files.FileEntry;
import com.robertvokac.bitbackup.persistence.api.FileRepository;
import com.robertvokac.bitbackup.persistence.impl.sqlite.SqliteDatabaseMigration;
import com.robertvokac.dbmigration.core.main.MigrationResult;
import com.robertvokac.powerframework.time.duration.Duration;
import com.robertvokac.powerframework.time.moment.LocalDateTime;
import com.robertvokac.powerframework.time.utils.ProgressTracker;
import com.robertvokac.powerframework.time.utils.TimeUnit;

/**
 *
 * @author r
 */
public class CheckCommand implements Command {

    private static final Logger LOG = LogManager.getLogger(CheckCommand.class);
    public static final String NAME = "check";

    public CheckCommand() {
    }

    @Override
    public String getName() {
        return NAME;
    }

    enum CheckCommandPart {

        CHECK_OLD_DB_CHECKSUM(1),
        MIGRATE_DB_SCHEMA_IF_NEEDED(2),
        UPDATE_VERSION(3),
        FOUND_FILES_IN_FILESYSTEM(4),
        FOUND_FILES_IN_DB(5),
        ADD_NEW_FILES_TO_DB(6),
        REMOVE_DELETED_FILES_FROM_DB(7),
        COMPARE_CONTENT_AND_LAST_MODTIME(8),
        CREATE_REPORT_CSV_IF_NEEDED(9),
        CHECK_NEW_DB_CHECKSUM(10);

        private int number;

        CheckCommandPart(int number) {
            this.number = number;
        }

        public String toText() {
            return "Part " + number + ": ";
        }
    }

    static int iStatic = 0;

    @Override
    public String run(BitBackupArgs bitBackupArgs) {
        BitBackupFiles bitBackupFiles = new BitBackupFiles(bitBackupArgs);
        BitBackupContext bitBackupContext = new BitBackupContext(bitBackupFiles.getWorkingDirAbsolutePath());
        //
        //part 1:
        part1CheckDbHasExpectedHashSum(bitBackupFiles);
        //part 2:
        boolean part2Result = part2MigrateDbSchemaIfNeeded(bitBackupFiles);
        if (!part2Result) {
            return "part 2 failed";
        }
        //part 3:
        part3UpdateVersionInDbIfNeeded(bitBackupContext, bitBackupFiles);

        ListSet<File> filesInFileSystem = part4FoundFilesInFileSystem(bitBackupFiles, bitBackupArgs);
        ListSet<FsFile> filesInDb = part5FoundFilesInDb(bitBackupContext.getFileRepository(), bitBackupArgs);

        LocalDateTime now = part6AddNewFilesToDb(filesInFileSystem, bitBackupFiles, filesInDb, bitBackupContext);

        List<FsFile> filesToBeRemovedFromDb = part7RemoveDeletedFilesFromDb(filesInDb, filesInFileSystem, bitBackupContext);

        List<FsFile> filesWithBitRot = part8CompareContentAndLastModificationDate(filesInDb, filesToBeRemovedFromDb, bitBackupContext, now);

        part9CreateReportCsvIfNeeded(bitBackupArgs, bitBackupFiles, filesWithBitRot);
        part10CalculateCurrentHashSumOfDbFile(bitBackupFiles);

        LOG.info("==========");
        LOG.info("Summary");

        if (filesWithBitRot.isEmpty()) {
            LOG.info("Summary: OK : No files with bit rot were found.");
        } else {
            LOG.error("Summary: KO : Some files {} with bit rot were found.", filesWithBitRot.size());
            filesWithBitRot.stream().forEach(f
                    -> LOG.error("Bit rot detected: \"" + f.getAbsolutePath() + "\"" + " expected_sha512=" + f.getHashSumValue() + " returned_sha512=" + Utils.calculateSHA512Hash(new File("./" + f.getAbsolutePath())))
            );
        }

        System.out.println("foundFiles=" + foundFiles);
        System.out.println("foundDirs=" + foundDirs);
        return filesWithBitRot.isEmpty() ? "" : filesWithBitRot.stream().map(FsFile::getAbsolutePath).collect(Collectors.joining("\n"));

    }

    /**
     * Checks, if SQLite DB file has the expected SHA-512 hash sum
     *
     * @param bitBackupSQLite3File
     * @param bitBackupSQLite3FileSha512
     * @throws BitBackupException - if this check fails.
     */
    private void part1CheckDbHasExpectedHashSum(BitBackupFiles bitBackupFiles) throws BitBackupException {
        LOG.info("** Part {}: Checking DB, if has expected check sum.", CheckCommandPart.CHECK_OLD_DB_CHECKSUM.number);
        final File bitBackupSQLite3FileSha512 = bitBackupFiles.getBitBackupSQLite3FileSha512();
        
        final boolean dbExists = bitBackupFiles.getBitBackupSQLite3File().exists();
        final boolean checkSumExists = bitBackupFiles.getBitBackupSQLite3FileSha512().exists();

        if (dbExists && checkSumExists) {

            String expectedHash = Utils.readTextFromFile(bitBackupSQLite3FileSha512);
            String returnedHash = Utils.calculateSHA512Hash(bitBackupFiles.getBitBackupSQLite3File());
            if (!returnedHash.equals(expectedHash)) {
                String msg
                        = "Part {}: KO. "
                        + "Unexpected hash "
                        + returnedHash
                        + ". Expected SHA-512 hash sum was: "
                        + expectedHash
                        + " for file "
                        + bitBackupFiles.getBitBackupSQLite3File().getAbsolutePath();
                LOG.error(msg, CheckCommandPart.CHECK_OLD_DB_CHECKSUM.number);
                LOG.info("Exiting because of the previous error.");
                throw new BitBackupException(msg);
            }
        } else {
            LOG.info("Part {}: OK. Nothing to do: {}",
                    CheckCommandPart.CHECK_OLD_DB_CHECKSUM.number,
                    !dbExists ? "DB does not yet exist." : "Check sum file does not exist.");
        }
    }

    private boolean part2MigrateDbSchemaIfNeeded(BitBackupFiles bitBackupFiles) {
        LOG.info("** Part {}: Migrating schema, if needed.", CheckCommandPart.MIGRATE_DB_SCHEMA_IF_NEEDED.number);
        try {

            MigrationResult migrationResult = SqliteDatabaseMigration.getInstance().migrate(bitBackupFiles.getWorkingDirAbsolutePath());
            if (migrationResult == MigrationResult.SUCCESS) {
                LOG.info("Part {}: OK. Success.", CheckCommandPart.MIGRATE_DB_SCHEMA_IF_NEEDED.number);
                return true;
            } else {
                LOG.error("Part {}: KO. Failed.", CheckCommandPart.MIGRATE_DB_SCHEMA_IF_NEEDED.number);
                throw new RuntimeException("Part " + CheckCommandPart.MIGRATE_DB_SCHEMA_IF_NEEDED.number + ": KO. Failed.");
            }
        } catch (RuntimeException e) {
            LOG.error("Part {}: KO. {}", CheckCommandPart.MIGRATE_DB_SCHEMA_IF_NEEDED.number, e.getMessage());
            return false;
        }
    }

    private void part3UpdateVersionInDbIfNeeded(BitBackupContext bitBackupContext, BitBackupFiles bitBackupFiles) {
        LOG.info("** Part {}: Updating version, if needed.", CheckCommandPart.UPDATE_VERSION.number);

        String bitBackupVersion = bitBackupContext.getSystemItemRepository().read(BIBVERSION).getValue();
        System.out.println("Before: bib.version=" + bitBackupVersion);
        if (bitBackupVersion == null) {
            bitBackupContext.getSystemItemRepository().create(new SystemItem("bib.version", "0.0.0-SNAPSHOT"));
        }
        System.out.println("Updating version in DB.");
        bitBackupVersion = bitBackupContext.getSystemItemRepository().read("bib.version").getValue();
        System.out.println("After: bib.version=" + bitBackupVersion);
        LOG.info("Part {}: OK.", CheckCommandPart.UPDATE_VERSION.number);
    }
    public static final String BIBVERSION = "bib.version";

    private ListSet<File> part4FoundFilesInFileSystem(BitBackupFiles bitBackupFiles, BitBackupArgs bitBackupArgs) {
        LOG.info("** Part {}: Loading files in filesystem", CheckCommandPart.FOUND_FILES_IN_FILESYSTEM.number);
        String workingDir = bitBackupFiles.getWorkingDirAbsolutePath();
        List<File> filesAlreadyFound = new ArrayList<>();
        List<File> filesInDirList = foundFilesInCurrentDir(bitBackupFiles.getWorkingDir(), filesAlreadyFound, bitBackupFiles);

        Utils.writeTextToFile(bitbackupindexSB.toString(), bitBackupFiles.getBitbackupindex());
        ListSet<File> listSet = new ListSet<>(filesInDirList, f -> loadPathButOnlyTheNeededPart(bitBackupFiles.getWorkingDir(), f));

        LOG.info("Part {}: Found {} files.", CheckCommandPart.FOUND_FILES_IN_FILESYSTEM.number, listSet.size());
        if (bitBackupArgs.isVerboseLoggingEnabled()) {
            filesInDirList.stream().forEach((f -> LOG.info("#" + (++iStatic) + " " + f.getAbsolutePath().substring(workingDir.length() + 1))));
        }
        return listSet;
    }

    private String loadPathButOnlyTheNeededPart(File currentDir, File file) {
        return file.getAbsolutePath().substring(currentDir.getAbsolutePath().length() + 1);
    }
    private final StringBuilder bitbackupindexSB = new StringBuilder();
    static int iii = 0;

    private int foundFiles;
    private int foundDirs;

    private List<File> foundFilesInCurrentDir(File currentDir, List<File> filesAlreadyFound, BitBackupFiles bitBackupFiles) {

        for (File f : currentDir.listFiles()) {
            

            boolean isBitBackupIgnore = f.getName().equals(bitBackupFiles.getBitBackupIgnore().getName());
            
            if (isBitBackupIgnore && !f.getAbsolutePath().equals(bitBackupFiles.getBitBackupIgnore().getAbsolutePath())) {
                bitBackupFiles.getBitBackupIgnoreRegex().addBitBackupIgnoreFile(f, bitBackupFiles.getWorkingDir());
            }
            if (f.isDirectory()) {
                ++foundDirs;
                try {
                    bitbackupindexSB.append(new FileEntry(f).toCsvLine()).append("\n");
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(CheckCommand.class.getName()).log(Level.SEVERE, null, ex);
                }
                foundFilesInCurrentDir(f, filesAlreadyFound, bitBackupFiles);
            } else {
                ++foundFiles;
                if (f.getAbsolutePath().equals(bitBackupFiles.getBitBackupSQLite3File().getAbsolutePath())) {
                    continue;
                }
                if (f.getAbsolutePath().equals(bitBackupFiles.getBitBackupSQLite3FileSha512().getAbsolutePath())) {
                    continue;
                }

                ++iii;
                //System.out.println("Testing file: " + iii + "#" + " " + loadPathButOnlyTheNeededPart(currentDirRoot, f));
                if (bitBackupFiles.getBitBackupIgnoreRegex().test(loadPathButOnlyTheNeededPart(bitBackupFiles.getWorkingDir(), f))) {
                    continue;
                }
                try {
                    bitbackupindexSB.append(new FileEntry(f).toCsvLine()).append("\n");
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(CheckCommand.class.getName()).log(Level.SEVERE, null, ex);
                }
                filesAlreadyFound.add(f);
            }
        }
        return filesAlreadyFound;
    }

    private ListSet<FsFile> part5FoundFilesInDb(FileRepository fileRepository, BitBackupArgs bitBackupArgs) {
        LOG.info("** Part {}: Loading files in DB", CheckCommandPart.FOUND_FILES_IN_DB.number);
        List<FsFile> filesInDb = fileRepository.list();

        ListSet<FsFile> listSet = new ListSet<>(filesInDb, f -> f.getAbsolutePath());
        LOG.info("Part {}: Found {} files.", CheckCommandPart.FOUND_FILES_IN_DB.number, listSet.size());
        iStatic = 0;
        if (bitBackupArgs.isVerboseLoggingEnabled()) {
            filesInDb.stream().forEach((f -> System.out.println("#" + (++iStatic) + " " + f.toString())));
        }
        return listSet;
    }

    private LocalDateTime part6AddNewFilesToDb(ListSet<File> filesInFileSystem, BitBackupFiles bitBackupFiles, ListSet<FsFile> filesInDb, BitBackupContext bitBackupContext) {
        LOG.info("** Part {}: Adding new files to DB", CheckCommandPart.ADD_NEW_FILES_TO_DB.number);
        Date lastChecked = new Date();
        com.robertvokac.powerframework.time.moment.LocalDateTime now = com.robertvokac.powerframework.time.moment.LocalDateTime.convertJavaUtilDateToPowerLocalDateTime(lastChecked);
        int processedCount0 = 0;
        List<FsFile> filesMissingInDb = new ArrayList<>();
        for (File fileInDir : filesInFileSystem.getList()) {
            processedCount0 = processedCount0 + 1;
            if (processedCount0 % 100 == 0) {
                double progress = ((double) processedCount0) / filesInFileSystem.getList().size() * 100;
                LOG.info("Part {}: Add - Progress: {}/{} {} %",
                        CheckCommandPart.ADD_NEW_FILES_TO_DB.number,
                        processedCount0,
                        filesInFileSystem.getList().size(),
                        String.format("%,.2f", progress));
            }

            String absolutePathOfFileInDir = loadPathButOnlyTheNeededPart(bitBackupFiles.getWorkingDir(), fileInDir);
            if (!filesInDb.doesSetContains(absolutePathOfFileInDir)) {
                Date lastModified = new Date(fileInDir.lastModified());
                com.robertvokac.powerframework.time.moment.LocalDateTime ldt = com.robertvokac.powerframework.time.moment.LocalDateTime.convertJavaUtilDateToPowerLocalDateTime(lastModified);

                FsFile fsFile = new FsFile(
                        UUID.randomUUID().toString(),
                        fileInDir.getName(),
                        absolutePathOfFileInDir,
                        ldt.toString(),
                        now.toString(),
                        Utils.calculateSHA512Hash(fileInDir),
                        "SHA-512",
                        fileInDir.length(),
                        "OK"
                );
                filesMissingInDb.add(fsFile);
            }

        }
        LOG.info("Adding new files: {}", filesMissingInDb.size());
        bitBackupContext.getFileRepository().create(filesMissingInDb);
        return now;
    }

    private List<FsFile> part7RemoveDeletedFilesFromDb(ListSet<FsFile> filesInDb, ListSet<File> filesInFileSystem, BitBackupContext bitBackupContext) {
        LOG.info("** Part {}: Removing deleted files from DB", CheckCommandPart.REMOVE_DELETED_FILES_FROM_DB.number);
        List<FsFile> filesToBeRemovedFromDb = new ArrayList<>();
        int processedCount = 0;

        for (FsFile fileInDb : filesInDb.getList()) {
            processedCount = processedCount + 1;
            if (processedCount % 100 == 0) {
                double progress = ((double) processedCount) / filesInDb.getList().size() * 100;
                LOG.info(
                        "Part {}: Remove - Progress: {}/{} {}%",
                        CheckCommandPart.REMOVE_DELETED_FILES_FROM_DB.number,
                        processedCount,
                        filesInDb.getList().size(),
                        String.format("%,.2f", progress)
                );
            }

            String absolutePathOfFileInDb = fileInDb.getAbsolutePath();
            if (!filesInFileSystem.doesSetContains(absolutePathOfFileInDb)) {

                filesToBeRemovedFromDb.add(fileInDb);
            }

        }
        LOG.info("Part {}: Removing files: {}",
                CheckCommandPart.REMOVE_DELETED_FILES_FROM_DB.number,
                filesToBeRemovedFromDb.size());
        for (FsFile f : filesToBeRemovedFromDb) {
            bitBackupContext.getFileRepository().remove(f);
        }
        return filesToBeRemovedFromDb;
    }

    private List<FsFile> part8CompareContentAndLastModificationDate(
            ListSet<FsFile> filesInDb, List<FsFile> filesToBeRemovedFromDb, BitBackupContext bitBackupContext, LocalDateTime now) {
        LOG.info("** Part {}: Comparing Content and last modification date", CheckCommandPart.COMPARE_CONTENT_AND_LAST_MODTIME.number);
        double countOfFilesToCalculateHashSum = filesInDb.size() - filesToBeRemovedFromDb.size();
        int processedCount = 0;
        //// Update modified files with same last modification date
        List<FsFile> filesWithBitRot = new ArrayList<>();
        List<FsFile> filesToUpdateLastCheckDate = new ArrayList<>();
        int contentAndModTimeWereChanged = 0;

        ProgressTracker progressTracker = new ProgressTracker(filesInDb.size() - filesToBeRemovedFromDb.size());
        for (FsFile fileInDb : filesInDb) {
            String absolutePathOfFileInDb = fileInDb.getAbsolutePath();
            if (filesToBeRemovedFromDb.contains(fileInDb)) {
                //nothing to do
                continue;

            }
            progressTracker.nextDone();
            processedCount = processedCount + 1;
            if (processedCount % 100 == 0) {
                double progress = ((double) processedCount) / countOfFilesToCalculateHashSum * 100;
                LOG.info("Update - Progress: " + processedCount + "/" + countOfFilesToCalculateHashSum + " " + String.format("%,.2f", progress) + "%");
                LOG.info("Remains: " + Duration.of(progressTracker.getRemainingSecondsUntilEnd(), TimeUnit.SECOND).toHumanString());
            }
            File file = new File("./" + absolutePathOfFileInDb);

            Date lastModified = new Date(file.lastModified());
            com.robertvokac.powerframework.time.moment.LocalDateTime ldt = com.robertvokac.powerframework.time.moment.LocalDateTime.convertJavaUtilDateToPowerLocalDateTime(lastModified);

            String calculatedHash = Utils.calculateSHA512Hash(file);
            if (ldt.toString().equals(fileInDb.getLastModificationDate()) && !calculatedHash.equals(fileInDb.getHashSumValue())) {
                filesWithBitRot.add(fileInDb);
                fileInDb.setLastCheckDate(now.toString());
                fileInDb.setLastCheckResult("KO");
                bitBackupContext.getFileRepository().updateFile(fileInDb);
                continue;
            }
            if (!ldt.toString().equals(fileInDb.getLastModificationDate())) {
                fileInDb.setLastCheckDate(now.toString());
                fileInDb.setLastModificationDate(ldt.toString());
                fileInDb.setHashSumValue(calculatedHash);
                fileInDb.setHashSumAlgorithm("SHA-512");
                fileInDb.setSize(file.length());
                fileInDb.setLastCheckResult("OK");
                bitBackupContext.getFileRepository().updateFile(fileInDb);
                //System.out.println(fileInDb.toString());
                contentAndModTimeWereChanged++;
                continue;
            }
            if (ldt.toString().equals(fileInDb.getLastModificationDate())) {
                fileInDb.setLastCheckResult("OK");

                if (fileInDb.getSize() == 0) {
                    fileInDb.setSize(file.length());
                    bitBackupContext.getFileRepository().updateFile(fileInDb);
                } else {
                    filesToUpdateLastCheckDate.add(fileInDb);
                }
                continue;
            }

        }
        LOG.info("Part {}: Updating files - " + (filesWithBitRot.isEmpty() ? "no" : "some") + " files with bit rots - content was changed and last modification is the same: {}",
                CheckCommandPart.COMPARE_CONTENT_AND_LAST_MODTIME.number,
                filesWithBitRot.size());
        LOG.info("Part {}: Updating files - content and last modification date were changed: {}",
                CheckCommandPart.COMPARE_CONTENT_AND_LAST_MODTIME.number,
                contentAndModTimeWereChanged);
        LOG.info("Part {}: Updating files - content and last modification date were not changed: {}",
                CheckCommandPart.COMPARE_CONTENT_AND_LAST_MODTIME.number,
                filesToUpdateLastCheckDate.size());
        bitBackupContext.getFileRepository().updateLastCheckDate(now.toString(), filesToUpdateLastCheckDate);

        return filesWithBitRot;
    }

    private void part9CreateReportCsvIfNeeded(BitBackupArgs bitBackupArgs, BitBackupFiles bitBackupFiles, List<FsFile> filesWithBitRot) {
        LOG.info("** Part {}: Creating csv report, if needed", CheckCommandPart.CREATE_REPORT_CSV_IF_NEEDED.number);
        if (!bitBackupArgs.hasArgument("report")) {
            LOG.info(" Part {}: OK. Nothing to do. No option report was passed.", CheckCommandPart.CREATE_REPORT_CSV_IF_NEEDED.number);
            return;
        }
        if (!bitBackupArgs.getArgument("report").equals("true")) {
            LOG.info("Part {}: Nothing to do. Option report={}",
                    CheckCommandPart.CREATE_REPORT_CSV_IF_NEEDED.number,
                    bitBackupArgs.getArgument("report"));
            return;
        }

        File bibReportCsv = bitBackupFiles.getBitBackupReportCsv();
        if (bibReportCsv.exists()) {
            Long nowLong = com.robertvokac.powerframework.time.moment.UniversalDateTime.now().toLong();

            File backup = new File(bibReportCsv.getParentFile().getAbsolutePath() + "/" + nowLong + "." + bibReportCsv.getName());
            bibReportCsv.renameTo(backup);
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
        Utils.writeTextToFile(sb.toString(), bibReportCsv);
        LOG.info("Part {}: OK.",
                CheckCommandPart.CREATE_REPORT_CSV_IF_NEEDED.number);
    }

    private void part10CalculateCurrentHashSumOfDbFile(BitBackupFiles bitBackupFiles) {
        LOG.info("** Part {}: Calculating current hash sum of DB file", CheckCommandPart.CHECK_NEW_DB_CHECKSUM.number);
        Utils.writeTextToFile(Utils.calculateSHA512Hash(bitBackupFiles.getBitBackupSQLite3File()), bitBackupFiles.getBitBackupSQLite3FileSha512());
    }

}
