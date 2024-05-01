# bit-backup

"Bit Backup" (Bib) is a tool to manage files.

## How to run

Requirements to run "Bit Backup":
* Java 21

### How to setup your environment on Linux

Add to your .bashrc:

    alias bib='java -jar {path to bit-backup jar with dependencies file}/bit-backup-0.0.0-SNAPSHOT-jar-with-dependencies.jar'

## How to build

Requirements to build "Bit Backup:
 * Java 21
 * Maven

```
git clone https://code.nanoboot.org/nanoboot/bit-backup
cd bit-backup
mvn clean install
```

## Features

### Feature 1 : Bit Rot Detection (to keep your files forever)

 * Hash sum of files is tracked.
 * If last modification date of file in database and on the disk are the same, but the calculated checksum is different, then a bit rot was detected.
 * New files are added to the database, deleted files are removed from the database.

Found bit rots can be probably repaired, but only in case, you use also "Feature 2 : Backup of files"

Inspired by:

 * https://github.com/ambv/bitrot
 * https://github.com/laktak/chkbit-py

#### File .bibignore

You can create file .bibignore containing the names of the files/directories you wish to ignore
* .bibignore is similar to Git file .gitignore
* each line should contain exactly one name
* lines starting with # are skipped
* you may use Unix shell-style wildcards

#### File .bib.sqlite3

An SQLite database created and managed automatically by bit-backup.

#### File .bib.sqlite3.sha512

Last calculated hash sum of file ".bib.sqlite3"

### Feature 2 : Backup of files

Inspired by Git, but goals are slightly different.
 * https://github.com/joshnh/Git-Commands
 * https://git-scm.com/docs

#### File .bibbackup

File .bibbackup is stored in local directory, which you are going to backup.

Content:

```
remotes=remote1,remote2,remote3
```

#### Features

 * Backing up directory to another location like: local, FTP(S), SFTP, HTTP(S), S3, Bib server and others
 * Optionally you can set a password to protect your files (if a not trusted external location like FTP or S3 is used)
 
 * Files are handled in a "Bib repository"

#### What is not supported

 * Conflict resolution is not supported. (If you wish something not yet supported to be added, please, let's start a discussion here or at forum.nanoboot.org)
   * "Bib" is not intended to be used by many read/write users.
   * Several people changing one Bib repository must be avoided.
   * "Bib" is not intended to be used by many read users and only one read/write user.  
   * One Bib repository can be used by more users, but only one user can change it.
 * Branches are not supported

## Bib repository

* Your files are versioned. You can travel in history and return to an older version of the whole repo or only a changed/deleted directory or file.

You can:
 * clone a remote Bib repository
 * or use an existing local Bib repository
 * or create a new empty Bib repository
 * or create a new Bib repository using an existing directory
 * or save your local repository to a remote repository.

### Structure

#### Directory .bib/objects

.bib/objects/{??}/{?????...}

#### Directory .bib/pack
.bib/pack/pack-{sha-512}.pack

{sha-512}::::{length in bytes}::::{sha-512}::::{length in bytes}::::{sha-512}::::{length in bytes}::::{sha-512}::::{length in bytes}::::{sha-512}::::{length in bytes}::::::::{bytes -base}{bytes-incremental}{bytes-incremental}{bytes-incremental}{bytes-incremental}{bytes-incremental}

binary diffs

#### File .bib/bibindex.{number}

https://stackabuse.com/linux-display-file-properties-via-terminal/

##### Content

```
DATE=20231105
TIME=102950
VERSION=1
MESSAGE=
TAG=
-----
PATH	FILE_NAME	TYPE	LINK_TARGET	PERMISSIONS	SUID	SGID	STICKY_BIR	EXECUTABLE	UID	GID	OWNER	GROUP	LAST_MOD_TIME	LAST_CHANGE_TIME	LAST_ACCESS_TIME	SIZE	HASH	PACKAGE_UUID	ACL	ATTRS	IMMUTABLE 	UNDELETABLE
/home/robertvokac       doc.txt -              rwxrw-r--                                                           2             2             robertvokac                robertvokac       20220110151617999      20220110151617999       20220110151617999      263         {}                PACKAGE_UUID                                              1             1
```

```
TYPE	DIR d, REGULAR -, LINK l
LINK_TARGET	The target is in the content of binary object
PERMISSIONS	rwxrw-r--
HASH	SHA-512:{}
```

https://superuser.com/questions/283008/binary-diff-patch-for-large-files-on-linux

https://stackabuse.com/guide-to-understanding-chmod/

https://www.guiffy.com/Binary-Diff-Tool.html

```
Permission  Symbolic Representation Octal Representation
read    r   4
write   w   2
execute x   1
no permission   -   0
```

```
Creates local .bibindex

Downloads remote .bibindex – if does not exist, empty file will be returned.

Compares these indexes, uploads new blobs to object addressed system (SHA-512) – packaged to 7z archives (compression=ultra + other settings)

The most reliable way would be to make md5 hashes of all the local files you care about and store it in a file. So the file will contain a list of filenames and their md5 hashes. Store that file on your ftp server. When you want to update the files on your ftp server, download the file containing the list, compare that against all your local files, and upload the files that have changed (or are new). That way you don't have to worry about archive bits, modified date, or looking at file sizes, the use of which can never be 100% reliable.

Using file sizes isn't reliable for the obvious reason - a file could change but have the same size. I'm not a fan of using the archive bit or modified date because either of those could be confused if you backup or restore your local directory with another backup program.
```

#### File .bib/biblog

Contains index number of last bib index.

#### file .bib/description

#### File .bib/config

```
pack-file.files-until-size.mb=100
pack-file.max-size.mb=1000
```

## Commands

bib {command} [{arg1} {arg2} {argn}]

Example:
```
bib clone path=/home/johndoe/mydir url={local path or s3 bucket or FTP server or website url}
```
Arguments

path={path to directory}
 * default:. (current working directory)


### bitrot

Checks for bitrots in current directory

### clone : Cloning a remote repo

```
bib clone {url} [[--bare]] [[revision number|tag]]
```

#### Local

```
bib clone {path to another local Bib repository - path to directory}
```

#### S3
```
bib clone s3://http[s]://{endpoint url}/{bucket name}
```
Then you will be asked for access key and secret key.

#### FTP/FTPS/SFTP

```
bib clone {protocol}://[{user}:{password}]@{host url}:{port}/{directory}
```

#### HTTP/HTTPS

```
bib clone http[s]://[{user}:{password}]@{host url}:{port}/{directory}
```

#### Bib server (via Rest api)

```
bib clone bib:://[{user}:{password}]@{host url}:{port}/[path to repository/]{repository name}
```

### init

Init commands creates new directory .bib with its structure

```
bib init [[--bare]]
```

 * Creating a new empty Bib repository
 * Creating a Bib repository using an existing directory

### help

### version

### config

### restore

### reset

### tag

### revert

### blame

```
bib blame {file} {remote}
```

### clean

### gc

```
bib gc abc
```

### fsck

### check

### bundle

### remote add {remote name}

```
bib remote add {remote name}
```

```
bib remote add abc protocol://user:pw|{}@host:port/directory[::password=encryption_password|{}::duplicate_count=1::exclude=::include=::]
bib remote add wedos_disk_100gb_backup user:pw@host:port/directory::password=123::duplicate_count=2
bib remote add abc user:pw|{}@host:port/directory::password=encryption_password|{}::duplicate_count={1, 0 is default}::compression_level={0-9,5 is default}
```

{} placeholders means, that user will be asked in console (to avoid the password to be in console history)

### remote remove {remote name}

```
bib remote remove abc
```

### bib commit

```
bib commit [-m "{message}"]
```

### bib mirror {remote name}

```
bib mirror abc def ghi [-m message -t TAG]
bib mirror @all
```

### bib fetch

### bib pull

### bib log

### bib diff

### bib prune

```
bib prune origin --since 2021-10-04
bib prune abc --since "2 months ago" | 10 … does not delete anything, only marks objects to be deleted
```

### verify

```
bib verify abc
```

### repack

## Todo

Table FILE – add new columns – linux_rights, owner, group
