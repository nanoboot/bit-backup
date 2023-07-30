# bit-inspector

Detects bit rotten files in the given directory to keep your files forever.

It is inspired by:

 * https://github.com/ambv/bitrot
 * https://github.com/laktak/chkbit-py

## How it works

You go to a directory.
You run the command bir:
 * All files in this directory are inspected, new files are added to the database, deleted files are removed from the database.
 * If the file last modification date in database and on the disk is the same, but the content checksum is different, then a bit rot is detected.

This program cannot restore files with bitrot.
 * You have to backup up your files (do snapshots).

## How to setup your environment on Linux

Example:

    alias bir='java -jar /rv/data/desktop/code/code.nanoboot.org/nanoboot/bit-inspector/target/bit-inspector-0.0.0-SNAPSHOT-jar-with-dependencies.jar'

## Usage

"Bit Inspector" creates an SQLite database - file ".bir.sqlite3" in the current directory.

There is also created the  file ".bir.sqlite3.sha512", which contains the current hash sum of file ".bir.sqlite3".

You can create file .birignore containing the names of the files/directories you wish to ignore
* each line should contain exactly one name
* lines starting with # are skipped
* you may use Unix shell-style wildcards

