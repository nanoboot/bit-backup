///////////////////////////////////////////////////////////////////////////////////////////////
// bit-backup: Tool detecting bit rots in files.
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
package org.nanoboot.bitbackup.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nanoboot.bitbackup.core.BibContext;
import org.nanoboot.bitbackup.core.Command;
import org.nanoboot.bitbackup.core.BibArgs;
import org.nanoboot.bitbackup.core.BitBackupException;
import org.nanoboot.bitbackup.core.BibFiles;
import org.nanoboot.bitbackup.core.Constants;
import org.nanoboot.bitbackup.core.ListSet;
import org.nanoboot.bitbackup.core.Utils;
import org.nanoboot.bitbackup.entity.FsFile;
import org.nanoboot.bitbackup.entity.SystemItem;
import org.nanoboot.bitbackup.persistence.api.FileRepository;
import org.nanoboot.bitbackup.persistence.impl.sqlite.SqliteDatabaseMigration;
import org.nanoboot.dbmigration.core.main.MigrationResult;
import org.nanoboot.powerframework.time.duration.Duration;
import org.nanoboot.powerframework.time.moment.LocalDateTime;
import org.nanoboot.powerframework.time.utils.ProgressTracker;
import org.nanoboot.powerframework.time.utils.TimeUnit;

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
    public String run(BibArgs bibArgs) {
        BibFiles bibFiles = new BibFiles(bibArgs);
        BibContext bibContext = new BibContext(bibFiles.getWorkingDirAbsolutePath());
        //
        //part 1:
        part1CheckDbHasExpectedHashSum(bibFiles);
        //part 2:
        boolean part2Result = part2MigrateDbSchemaIfNeeded(bibFiles);
        if (!part2Result) {
            return "part 2 failed";
        }
        //part 3:
        part3UpdateVersionInDbIfNeeded(bibContext);

        ListSet<File> filesInFileSystem = part4FoundFilesInFileSystem(bibFiles, bibArgs);
        ListSet<FsFile> filesInDb = part5FoundFilesInDb(bibContext.getFileRepository(), bibArgs);

        LocalDateTime now = part6AddNewFilesToDb(filesInFileSystem, bibFiles, filesInDb, bibContext);

        List<FsFile> filesToBeRemovedFromDb = part7RemoveDeletedFilesFromDb(filesInDb, filesInFileSystem, bibContext);

        List<FsFile> filesWithBitRot = part8CompareContentAndLastModificationDate(filesInDb, filesToBeRemovedFromDb, bibContext, now);

        part9CreateReportCsvIfNeeded(bibArgs, bibFiles, filesWithBitRot);
        part10CalculateCurrentHashSumOfDbFile(bibFiles);

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
     * @param bibSQLite3File
     * @param bibSQLite3FileSha512
     * @throws BitBackupException - if this check fails.
     */
    private void part1CheckDbHasExpectedHashSum(BibFiles bitBackupFiles) throws BitBackupException {
        LOG.info("** Part {}: Checking DB, if has expected check sum.", CheckCommandPart.CHECK_OLD_DB_CHECKSUM.number);
        final File bibSQLite3File = bitBackupFiles.getBibSQLite3File();
        final File birSQLite3File = bitBackupFiles.getBirSQLite3File();
        final File bibSQLite3FileSha512 = bitBackupFiles.getBibSQLite3FileSha512();
        final File birSQLite3FileSha512 = bitBackupFiles.getBirSQLite3FileSha512();
        if (Constants.MIGRATE_FROM_BIT_INSPECTOR_TO_BIT_BACKUP_IF_NEEDED && !bibSQLite3File.exists() && birSQLite3File.exists()) {
            //apply for migration to Bit Backup
            birSQLite3File.renameTo(bibSQLite3File);

            if (!bibSQLite3FileSha512.exists() && birSQLite3FileSha512.exists()) {
                //apply for migration to Bit Backup
                birSQLite3FileSha512.renameTo(bibSQLite3FileSha512);
            }
        }
        final boolean dbExists = bitBackupFiles.getBibSQLite3File().exists();
        final boolean checkSumExists = bitBackupFiles.getBibSQLite3FileSha512().exists();

        if (dbExists && checkSumExists) {

            String expectedHash = Utils.readTextFromFile(bibSQLite3FileSha512);
            String returnedHash = Utils.calculateSHA512Hash(bitBackupFiles.getBibSQLite3File());
            if (!returnedHash.equals(expectedHash)) {
                String msg
                        = "Part {}: KO. "
                        + "Unexpected hash "
                        + returnedHash
                        + ". Expected SHA-512 hash sum was: "
                        + expectedHash
                        + " for file "
                        + bitBackupFiles.getBibSQLite3File().getAbsolutePath();
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

    private boolean part2MigrateDbSchemaIfNeeded(BibFiles bibFiles) {
        LOG.info("** Part {}: Migrating schema, if needed.", CheckCommandPart.MIGRATE_DB_SCHEMA_IF_NEEDED.number);
        try {

            MigrationResult migrationResult = SqliteDatabaseMigration.getInstance().migrate(bibFiles.getWorkingDirAbsolutePath());
            if (migrationResult == MigrationResult.SUCCESS) {
                LOG.info("Part {}: OK. Success.", CheckCommandPart.MIGRATE_DB_SCHEMA_IF_NEEDED.number);
                return true;
            } else {
                LOG.error("Part {}: KO. Failed.", CheckCommandPart.MIGRATE_DB_SCHEMA_IF_NEEDED.number);
                throw new RuntimeException("Part " + CheckCommandPart.MIGRATE_DB_SCHEMA_IF_NEEDED.number + ": KO. Failed.");
            }
        } catch (Exception e) {
            LOG.error("Part {}: KO. {}", CheckCommandPart.MIGRATE_DB_SCHEMA_IF_NEEDED.number, e.getMessage());
            return false;
        }
    }

    private void part3UpdateVersionInDbIfNeeded(BibContext bibContext) {
        LOG.info("** Part {}: Updating version, if needed.", CheckCommandPart.UPDATE_VERSION.number);

        if(Constants.MIGRATE_FROM_BIT_INSPECTOR_TO_BIT_BACKUP_IF_NEEDED){
            //Migrating from bit-inspector to bit-backup:
            String birVersion = bibContext.getSystemItemRepository().read("bir.version").getValue();
            if (birVersion != null) {
                bibContext.getSystemItemRepository().remove(BIRVERSION);
            }
        }
        String bibVersion = bibContext.getSystemItemRepository().read(BIBVERSION).getValue();
        System.out.println("Before: bib.version=" + bibVersion);
        if (bibVersion == null) {
            bibContext.getSystemItemRepository().create(new SystemItem("bib.version", "0.0.0-SNAPSHOT"));
        }
        System.out.println("Updating version in DB.");
        bibVersion = bibContext.getSystemItemRepository().read("bib.version").getValue();
        System.out.println("After: bib.version=" + bibVersion);
        LOG.info("Part {}: OK.", CheckCommandPart.UPDATE_VERSION.number);
    }
    public static final String BIBVERSION = "bib.version";
    public static final String BIRVERSION = "bir.version";

    private ListSet<File> part4FoundFilesInFileSystem(BibFiles bibFiles, BibArgs bibArgs) {
        LOG.info("** Part {}: Loading files in filesystem", CheckCommandPart.FOUND_FILES_IN_FILESYSTEM.number);
        String workingDir = bibFiles.getWorkingDirAbsolutePath();
        List<File> filesAlreadyFound = new ArrayList<>();
        List<File> filesInDirList = foundFilesInCurrentDir(bibFiles.getWorkingDir(), filesAlreadyFound, bibFiles);

        ListSet<File> listSet = new ListSet<>(filesInDirList, f -> loadPathButOnlyTheNeededPart(bibFiles.getWorkingDir(), f));

        LOG.info("Part {}: Found {} files.", CheckCommandPart.FOUND_FILES_IN_FILESYSTEM.number, listSet.size());
        if (bibArgs.isVerboseLoggingEnabled()) {
            filesInDirList.stream().forEach((f -> LOG.info("#" + (++iStatic) + " " + f.getAbsolutePath().substring(workingDir.length() + 1))));
        }
        return listSet;
    }

    private String loadPathButOnlyTheNeededPart(File currentDir, File file) {
        return file.getAbsolutePath().substring(currentDir.getAbsolutePath().length() + 1);
    }
    static int iii = 0;

    private int foundFiles;
    private int foundDirs;

    private List<File> foundFilesInCurrentDir(File currentDir, List<File> filesAlreadyFound, BibFiles bibFiles) {

        for (File f : currentDir.listFiles()) {
            
            if(Constants.MIGRATE_FROM_BIT_INSPECTOR_TO_BIT_BACKUP_IF_NEEDED) {
            //Migrating from bit-inspector to bit-backup:
            boolean isAlsoBirIgnore = f.getName().equals(bibFiles.getBirIgnore().getName());
            if(isAlsoBirIgnore) {
                File bibIgnoreFile = new File(f.getParentFile(), bibFiles.getBibIgnore().getName());
                if(!bibIgnoreFile.exists()) {
                    f.renameTo(bibIgnoreFile);
                }
            }
            
            }
            boolean isAlsoBibIgnore = f.getName().equals(bibFiles.getBibIgnore().getName());
            if (isAlsoBibIgnore && !f.getAbsolutePath().equals(bibFiles.getBibIgnore().getAbsoluteFile())) {
                bibFiles.getBibIgnoreRegex().addBibIgnoreFile(f, bibFiles.getWorkingDir());
            }
            if (f.isDirectory()) {
                ++foundDirs;
                foundFilesInCurrentDir(f, filesAlreadyFound, bibFiles);
            } else {
                ++foundFiles;
                if (f.getAbsolutePath().equals(bibFiles.getBirSQLite3File().getAbsolutePath())) {
                    continue;
                }
                if (f.getAbsolutePath().equals(bibFiles.getBirSQLite3FileSha512().getAbsolutePath())) {
                    continue;
                }

                ++iii;
                //System.out.println("Testing file: " + iii + "#" + " " + loadPathButOnlyTheNeededPart(currentDirRoot, f));
                if (bibFiles.getBibIgnoreRegex().test(loadPathButOnlyTheNeededPart(bibFiles.getWorkingDir(), f))) {
                    continue;
                }
                filesAlreadyFound.add(f);
            }
        }
        return filesAlreadyFound;
    }

    private ListSet<FsFile> part5FoundFilesInDb(FileRepository fileRepository, BibArgs bibArgs) {
        LOG.info("** Part {}: Loading files in DB", CheckCommandPart.FOUND_FILES_IN_DB.number);
        List<FsFile> filesInDb = fileRepository.list();

        ListSet<FsFile> listSet = new ListSet<>(filesInDb, f -> f.getAbsolutePath());
        LOG.info("Part {}: Found {} files.", CheckCommandPart.FOUND_FILES_IN_DB.number, listSet.size());
        iStatic = 0;
        if (bibArgs.isVerboseLoggingEnabled()) {
            filesInDb.stream().forEach((f -> System.out.println("#" + (++iStatic) + " " + f.toString())));
        }
        return listSet;
    }

    private LocalDateTime part6AddNewFilesToDb(ListSet<File> filesInFileSystem, BibFiles bibFiles, ListSet<FsFile> filesInDb, BibContext bibContext) {
        LOG.info("** Part {}: Adding new files to DB", CheckCommandPart.ADD_NEW_FILES_TO_DB.number);
        Date lastChecked = new Date();
        org.nanoboot.powerframework.time.moment.LocalDateTime now = org.nanoboot.powerframework.time.moment.LocalDateTime.convertJavaUtilDateToPowerLocalDateTime(lastChecked);
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

            String absolutePathOfFileInDir = loadPathButOnlyTheNeededPart(bibFiles.getWorkingDir(), fileInDir);
            if (!filesInDb.doesSetContains(absolutePathOfFileInDir)) {
                Date lastModified = new Date(fileInDir.lastModified());
                org.nanoboot.powerframework.time.moment.LocalDateTime ldt = org.nanoboot.powerframework.time.moment.LocalDateTime.convertJavaUtilDateToPowerLocalDateTime(lastModified);

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
        bibContext.getFileRepository().create(filesMissingInDb);
        return now;
    }

    private List<FsFile> part7RemoveDeletedFilesFromDb(ListSet<FsFile> filesInDb, ListSet<File> filesInFileSystem, BibContext bibContext) {
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
            bibContext.getFileRepository().remove(f);
        }
        return filesToBeRemovedFromDb;
    }

    private List<FsFile> part8CompareContentAndLastModificationDate(
            ListSet<FsFile> filesInDb, List<FsFile> filesToBeRemovedFromDb, BibContext bibContext, LocalDateTime now) {
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
            org.nanoboot.powerframework.time.moment.LocalDateTime ldt = org.nanoboot.powerframework.time.moment.LocalDateTime.convertJavaUtilDateToPowerLocalDateTime(lastModified);

            String calculatedHash = Utils.calculateSHA512Hash(file);
            if (ldt.toString().equals(fileInDb.getLastModificationDate()) && !calculatedHash.equals(fileInDb.getHashSumValue())) {
                filesWithBitRot.add(fileInDb);
                fileInDb.setLastCheckDate(now.toString());
                fileInDb.setLastCheckResult("KO");
                bibContext.getFileRepository().updateFile(fileInDb);
                continue;
            }
            if (!ldt.toString().equals(fileInDb.getLastModificationDate())) {
                fileInDb.setLastCheckDate(now.toString());
                fileInDb.setLastModificationDate(ldt.toString());
                fileInDb.setHashSumValue(calculatedHash);
                fileInDb.setHashSumAlgorithm("SHA-512");
                fileInDb.setSize(file.length());
                fileInDb.setLastCheckResult("OK");
                bibContext.getFileRepository().updateFile(fileInDb);
                //System.out.println(fileInDb.toString());
                contentAndModTimeWereChanged++;
                continue;
            }
            if (ldt.toString().equals(fileInDb.getLastModificationDate())) {
                fileInDb.setLastCheckResult("OK");

                if (fileInDb.getSize() == 0) {
                    fileInDb.setSize(file.length());
                    bibContext.getFileRepository().updateFile(fileInDb);
                } else {
                    filesToUpdateLastCheckDate.add(fileInDb);
                }
                continue;
            }

        }
        LOG.info("Part {}: Updating files - found bit rots - content was changed and last modification is the same): {}",
                CheckCommandPart.COMPARE_CONTENT_AND_LAST_MODTIME.number,
                filesWithBitRot.size());
        LOG.info("Part {}: Updating files - content and last modification date were changed): {}",
                CheckCommandPart.COMPARE_CONTENT_AND_LAST_MODTIME.number,
                contentAndModTimeWereChanged);
        LOG.info("Part {}: Updating files - content and last modification date were not changed): {}",
                CheckCommandPart.COMPARE_CONTENT_AND_LAST_MODTIME.number,
                filesToUpdateLastCheckDate.size());
        bibContext.getFileRepository().updateLastCheckDate(now.toString(), filesToUpdateLastCheckDate);

        return filesWithBitRot;
    }

    private void part9CreateReportCsvIfNeeded(BibArgs bibArgs, BibFiles bibFiles, List<FsFile> filesWithBitRot) {
        LOG.info("** Part {}: Creating csv report, if needed", CheckCommandPart.CREATE_REPORT_CSV_IF_NEEDED.number);
        if (!bibArgs.hasArgument("report")) {
            LOG.info(" Part {}: OK. Nothing to do. No option report was passed.", CheckCommandPart.CREATE_REPORT_CSV_IF_NEEDED.number);
            return;
        }
        if (!bibArgs.getArgument("report").equals("true")) {
            LOG.info("Part {}: Nothing to do. Option report={}",
                    CheckCommandPart.CREATE_REPORT_CSV_IF_NEEDED.number,
                    bibArgs.getArgument("report"));
            return;
        }

        File bibReportCsv = bibFiles.getBibReportCsv();
        if (bibReportCsv.exists()) {
            Long nowLong = org.nanoboot.powerframework.time.moment.UniversalDateTime.now().toLong();

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

    private void part10CalculateCurrentHashSumOfDbFile(BibFiles bibFiles) {
        LOG.info("** Part {}: Calculating current hash sum of DB file", CheckCommandPart.CHECK_NEW_DB_CHECKSUM.number);
        Utils.writeTextToFile(Utils.calculateSHA512Hash(bibFiles.getBirSQLite3File()), bibFiles.getBirSQLite3FileSha512());
    }

}
