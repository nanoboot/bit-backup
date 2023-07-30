# bit-inspector

Detects bit rotten files in the given directory to keep your files forever.

It is inspired by:

 * https://github.com/ambv/bitrot
 * https://github.com/laktak/chkbit-py

## How it works

You go to a directory.
You run the command bin:
 * All files in this directory are inspected, new files are added to the database.
 * If the file last modification date in database and on the disk is the same, but the content checksum is different, then a bit rot is detected.

## How to setup your environment on Linux

Example:

    alias bir='java -jar /rv/data/desktop/code/code.nanoboot.org/nanoboot/bit-inspector/target/bir-inspector-0.0.0-SNAPSHOT-jar-with-dependencies.jar'
