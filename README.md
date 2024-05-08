# bit-backup

"Bit Backup" is a tool to manage files.

## How to run

Requirements to run "Bit Backup":
* Java 21

### How to setup your environment on Linux

Add to your .bashrc:

    alias bitbackup='java -jar {path to bit-backup jar with dependencies file}/bit-backup-0.0.0-SNAPSHOT-jar-with-dependencies.jar'

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

#### File .bitbackupignore

You can create file .bitbackupignore containing the names of the files/directories you wish to ignore
* .bitbackupignore is similar to Git file .gitignore
* each line should contain exactly one name
* lines starting with # are skipped
* you may use Unix shell-style wildcards

#### File .bitbackup.sqlite3

An SQLite database created and managed automatically by bit-backup.

#### File .bitbackup.sqlite3.sha512

Last calculated hash sum of file ".bitbackup.sqlite3"

### Feature 2 : Backup of files

Inspired by Git, but goals are slightly different.
 * https://github.com/joshnh/Git-Commands
 * https://git-scm.com/docs

#### File .bitbackup

File .bitbackup is stored in local directory, which you are going to backup.

Content:

```
remotes=remote1,remote2,remote3
```

#### Features

 * Backing up directory to another location like: local, FTP(S), SFTP, HTTP(S), S3, Bit Backup server and others
 * Optionally you can set a password to protect your files (if a not trusted external location like FTP or S3 is used)
 
 * Files are handled in a "Bit Backup repository"

#### What is not supported

 * Conflict resolution is not supported. (If you wish something not yet supported to be added, please, let's start a discussion here or at forum.nanoboot.org)
   * "Bit Backup" is not intended to be used by many read/write users.
   * Several people changing one Bit Backup repository must be avoided.
   * "Bit Backup" is not intended to be used by many read users and only one read/write user.  
   * One Bit Backup repository can be used by more users, but only one user can change it.
 * Branches are not supported

## Bit Backup repository

* Your files are versioned. You can travel in history and return to an older version of the whole repo or only a changed/deleted directory or file.

You can:
 * clone a remote Bit Backup repository
 * or use an existing local Bit Backup repository
 * or create a new empty Bit Backup repository
 * or create a new Bit Backup repository using an existing directory
 * or save your local repository to a remote repository.

### Structure

#### Directory .bitbackup/objects

.bitbackup/objects/{??}/{?????...}

#### Directory .bitbackup/pack
.bitbackup/pack/pack-{sha-512}.pack

{sha-512}::::{length in bytes}::::{sha-512}::::{length in bytes}::::{sha-512}::::{length in bytes}::::{sha-512}::::{length in bytes}::::{sha-512}::::{length in bytes}::::::::{bytes -base}{bytes-incremental}{bytes-incremental}{bytes-incremental}{bytes-incremental}{bytes-incremental}

binary diffs

#### File .bitbackup/bitbackupindex.{number}

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
Creates local .bitbackupindex

Downloads remote .bitbackupindex – if does not exist, empty file will be returned.

Compares these indexes, uploads new blobs to object addressed system (SHA-512) – packaged to 7z archives (compression=ultra + other settings)

The most reliable way would be to make md5 hashes of all the local files you care about and store it in a file. So the file will contain a list of filenames and their md5 hashes. Store that file on your ftp server. When you want to update the files on your ftp server, download the file containing the list, compare that against all your local files, and upload the files that have changed (or are new). That way you don't have to worry about archive bits, modified date, or looking at file sizes, the use of which can never be 100% reliable.

Using file sizes isn't reliable for the obvious reason - a file could change but have the same size. I'm not a fan of using the archive bit or modified date because either of those could be confused if you backup or restore your local directory with another backup program.
```

#### File .bitbackup/bitbackuplog

Contains index number of last bitbackup index.

#### file .bitbackup/description

#### File .bitbackup/config

```
pack-file.files-until-size.mb=100
pack-file.max-size.mb=1000
```

## Commands

bitbackup {command} [{arg1} {arg2} {argn}]

Example:
```
bitbackup clone path=/home/johndoe/mydir url={local path or s3 bucket or FTP server or website url}
```
Arguments

path={path to directory}
 * default:. (current working directory)


### bitrot

Checks for bitrots in current directory

### clone : Cloning a remote repo

```
bitbackup clone {url} [[--bare]] [[revision number|tag]]
```

#### Local

```
bitbackup clone {path to another local Bit Backup repository - path to directory}
```

#### S3
```
bitbackup clone s3://http[s]://{endpoint url}/{bucket name}
```
Then you will be asked for access key and secret key.

#### FTP/FTPS/SFTP

```
bitbackup clone {protocol}://[{user}:{password}]@{host url}:{port}/{directory}
```

#### HTTP/HTTPS

```
bitbackup clone http[s]://[{user}:{password}]@{host url}:{port}/{directory}
```

#### Bit Backup server (via Rest api)

```
bitbackup clone bitbackup:://[{user}:{password}]@{host url}:{port}/[path to repository/]{repository name}
```

### init

Init commands creates new directory .bitbackup with its structure

```
bitbackup init [[--bare]]
```

 * Creating a new empty Bit Backup repository
 * Creating a Bit Backup repository using an existing directory

### help

### version

### config

### restore

### reset

### tag

### revert

### blame

```
bitbackup blame {file} {remote}
```

### clean

### gc

```
bitbackup gc abc
```

### fsck

### check

### bundle

### remote add {remote name}

```
bitbackup remote add {remote name}
```

```
bitbackup remote add abc protocol://user:pw|{}@host:port/directory[::password=encryption_password|{}::duplicate_count=1::exclude=::include=::]
bitbackup remote add wedos_disk_100gb_backup user:pw@host:port/directory::password=123::duplicate_count=2
bitbackup remote add abc user:pw|{}@host:port/directory::password=encryption_password|{}::duplicate_count={1, 0 is default}::compression_level={0-9,5 is default}
```

{} placeholders means, that user will be asked in console (to avoid the password to be in console history)

### remote remove {remote name}

```
bitbackup remote remove abc
```

### bitbackup commit

```
bitbackup commit [-m "{message}"]
```

### bitbackup mirror {remote name}

```
bitbackup mirror abc def ghi [-m message -t TAG]
bitbackup mirror @all
```

### bitbackup fetch

### bitbackup pull

### bitbackup log

### bitbackup diff

### bitbackup prune

```
bitbackup prune origin --since 2021-10-04
bitbackup prune abc --since "2 months ago" | 10 … does not delete anything, only marks objects to be deleted
```

### verify

```
bitbackup verify abc
```

### repack

## Todo

Table FILE – add new columns – linux_rights, owner, group
