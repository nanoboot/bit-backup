# bit-inspector

"Bit Inspector" (Bir) is a tool to manage files. It is inspired by Git, but its goals are slightly different.
 * Hash sum of files is tracked
 * Detects bit rots to keep your files forever
 * Files are handled in a "Bir repository"
 * You can clone a remote repository
 * You can save your local repository to remote repository.


## How to build "Bit Inspector" on your own 

```
git clone https://code.nanoboot.org/nanoboot/bit-inspector
cd bit-inspector
mvn clean install
```

## How to setup your environment on Linux


Add to your .bashrc:

    alias bir='java -jar {path to bit-inspector jar with dependencies file}/bit-inspector-0.0.0-SNAPSHOT-jar-with-dependencies.jar'

## Syntax

bir {command} [{arg1} {arg2} {argn}]

Example:
```
bir clone dir=/home/johndoe/mydir url={local path or s3 bucket or FTP server or website url}
```

### Arguments

dir={path to directory}
 * default:. (current working directory)

## Bir repository

To use Bir, a Bir repository is needed.

You can:
 * clone a remote Bir repository
 * or use an existing local Bir repository
 * or create a new empty Bir repository
 * or create a new Bir repository using an existing directory

### Cloning a remote repo

#### S3
```
bir clone {endpoint url}/{bucket name}
```
Then you will be asked for access key and secret key.

### Using an existing bir repository

### Creating a new empty Bir repository

Go to wanted directory or use argument dir={path to wanted directory} and run:

```
bir init [dir to wanted directory]
```

### Creating a Bir repository using an existing directory


Go to wanted directory or use argument dir={path to wanted directory} and run:

```
bir init [dir to wanted directory]
```

### Commands

### init

Init commands creates new directory .bir with its structure
```
bir init [dir={path to directory}]
```


bir – object storage – local/s3/ftp

 bir remote add {remote name}
bir clone {remote name}
bir save
bir push {remote name}
bir fetch
bir pull
bir status

directories:
.bir
.bir objects


Table FILE – add new columns – linux_rights, owner, group


###

## Structure of .bir directory

.bir
.bir/objects
.bir/bir.sqlite3 ... an SQLite database
.bir/.bir.sqlite3.sha512 ...last calculated hash sum of file ".bir.sqlite3".

## File .birignore

You can create file .birignore containing the names of the files/directories you wish to ignore
* each line should contain exactly one name
* lines starting with # are skipped
* you may use Unix shell-style wildcards

## Detection of bit rots

If the file last modification date in database and on the disk are the same, but the calculated checksum is different, then a bit rot was is probably detected.


New files are added to the database, deleted files are removed from the database.
 

This program cannot restore files with bitrot.
 * You have to backup up your files (do snapshots).


It is inspired by:

 * https://github.com/ambv/bitrot
 * https://github.com/laktak/chkbit-py

## Todo

New tables: FILE_HISTORY



