# bit-inspector

"Bit Inspector" (Bir) is a tool to manage files.

## Requirements to run "Bit Inspector"

Requirements to run "Bit Inspector":
* Java 19

## How to build "Bit Inspector" on your own 

Requirements to build "Bit Inspector:
 * Java 19
 * Maven

```
git clone https://code.nanoboot.org/nanoboot/bit-inspector
cd bit-inspector
mvn clean install
```

## How to setup your environment on Linux

Add to your .bashrc:

    alias bir='java -jar {path to bit-inspector jar with dependencies file}/bit-inspector-0.0.0-SNAPSHOT-jar-with-dependencies.jar'

## Features

### Feature 1 : Bit Rot Detection (to keep your files forever)

 * Hash sum of files is tracked.
 * If last modification date of file in database and on the disk are the same, but the calculated checksum is different, then a bit rot was detected.
 * New files are added to the database, deleted files are removed from the database.

Found bit rots can be probably repaired, but only in case, you use also "Feature 2 : Backup of files"

Inspired by:

 * https://github.com/ambv/bitrot
 * https://github.com/laktak/chkbit-py

#### File .birignore

You can create file .birignore containing the names of the files/directories you wish to ignore
* each line should contain exactly one name
* lines starting with # are skipped
* you may use Unix shell-style wildcards

#### File .bir.sqlite3

An SQLite database

#### File .bir.sqlite3.sha512

Last calculated hash sum of file ".bir.sqlite3"

### Feature 2 : Backup of files

Inspired by Git, but goals are slightly different.
 * https://github.com/joshnh/Git-Commands
 * https://git-scm.com/docs

#### File .birbackup
remotes=remote1,remote2,remote3

#### Features

 * Backing up directory to another location like: local, FTP(S), SFTP, HTTP(S), S3, Bir server and others
 * Optionally you can set a password to protect your files (if a not trusted external location like FTP or S3 is used)
 
 * Files are handled in a "Bir repository"

#### What is not supported

 * Conflict resolution is not supported. (If you wish something not yet supported to be added, please, let's start a discussion here or at forum.nanoboot.org)
   * "Bir" is not intended to be used by many read/write users.
   * Several people changing one Bir repository must be avoided.
   * "Bir" is not intended to be used by many read users and only one read/write user.  
   * One Bir repository can be used by more users, but only one user can change it.
 * Branches are not supported

## Bir repository

* Your files are versioned. You can travel in history and return to an older version of the whole repo or only a changed/deleted directory or file.

You can:
 * clone a remote Bir repository
 * or use an existing local Bir repository
 * or create a new empty Bir repository
 * or create a new Bir repository using an existing directory
 * or save your local repository to a remote repository.

### Structure

#### Directory .bir/objects

.bir/objects/{??}/{?????...}

#### Directory .bir/pack
.bir/pack/pack-{sha-512}.pack

#### File .bir/birindex.{number}

#### File .bir/birlog

Contains index number of last bir index.

#### file .bir/description

#### File .bir/config

```
pack-file.files-until-size.mb=100
pack-file.max-size.mb=1000
```

## Commands

bir {command} [{arg1} {arg2} {argn}]

Example:
```
bir clone path=/home/johndoe/mydir url={local path or s3 bucket or FTP server or website url}
```
Arguments

path={path to directory}
 * default:. (current working directory)


### bitrot

Checks for bitrots in current directory

### clone : Cloning a remote repo

```
bir clone {url} [[--bare
```

#### Local

```
bir clone {path to another local Bir repository - path to directory}
```

#### S3
```
bir clone s3://http[s]://{endpoint url}/{bucket name}
```
Then you will be asked for access key and secret key.

#### FTP/FTPS/SFTP

```
bir clone {protocol}://[{user}:{password}]@{host url}:{port}/{directory}
```

#### HTTP/HTTPS

```
bir clone http[s]://[{user}:{password}]@{host url}:{port}/{directory}
```

#### Bir server (via Rest api)

```
bir clone bir:://[{user}:{password}]@{host url}:{port}/[path to repository/]{repository name}
```

### init

Init commands creates new directory .bir with its structure
```
bir init
```

 * Creating a new empty Bir repository
 * Creating a Bir repository using an existing directory

### help

### version

### config

### restore

### reset

### tag

### revert

### blame

### clean

### gc

### fsck

### check

### bundle

### remote add {remote name}

```
bir remote add {remote name}
```

### bir commit

```
bir commit [-m "{message}"]
```

### bir push {remote name}

### bir fetch

### bir pull

### bir log

### bir diff

### bir prune

bir prune origin --since 2021-10-04

## Todo

Table FILE – add new columns – linux_rights, owner, group
