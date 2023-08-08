# bit-inspector

"Bit Inspector" (Bir) is a tool to manage files.
 * It is inspired by Git, but its goals are slightly different.

Features
 * Tracking hash sum of files
 * Backing up directory to another location like: local, FTP(S), SFTP, HTTP(S), S3, Bir server
 * Optionally you can set a password to protect your files (if a not trusted external location like FTP or S3 is used)
 * Detecting bit rots (to keep your files forever) - found bit rots can be repaired.
 * Files are handled in a "Bir repository"
 * You can clone a remote repository
 * You can save your local repository to a remote repository.
 * Your files are versioned. You can travel in history and return to an older version of the whole repo or only a changed/deleted directory or file.

What is not supported:
 * Conflict resolution is not supported. (If you wish something not yet suuported to be added, please, let's start a discussion here or at forum.nanoboot.org)
   * "Bir" is not intended to be used by many read/write users.
   * Several people changing one Bir repository must be avoided.
   * "Bir" is not intended to be used by many read users and only one read/write user.  
   * One Bir repository can be used by more users, but only one user can change it.
 * Branches are not supported

## Requirements

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

## Syntax

bir {command} [{arg1} {arg2} {argn}]

Example:
```
bir clone path=/home/johndoe/mydir url={local path or s3 bucket or FTP server or website url}
```

### Arguments

path={path to directory}
 * default:. (current working directory)

## Bir repository

To use Bir, a Bir repository is needed.

You can:
 * clone a remote Bir repository
 * or use an existing local Bir repository
 * or create a new empty Bir repository
 * or create a new Bir repository using an existing directory

### Command : clone : Cloning a remote repo

#### Local

```
bir clone {path to local directory = Bir repository}
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

### Using an existing Bir repository

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

Inspired by: 
 * https://github.com/joshnh/Git-Commands
 * https://git-scm.com/docs

### help

### version

### config

### restore

### reset

### mv

### tag

### revert

### blame

### clean

### gc

### clone

### fsck

### check

### bundle

```
bir clone {url} [[--bare
```

### init

Init commands creates new directory .bir with its structure
```
bir init [dir={path to directory}]
```

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

### bir status

### bir add

### bir rm

### bir stash

### bir log

### bir diff



Table FILE – add new columns – linux_rights, owner, group


###

## Structure of .bir directory

.bir
.bir/objects
.bir/remotes/{remote name}
.bir/bir.sqlite3 ... an SQLite database
.bir/bir.sqlite3.sha512 ...last calculated hash sum of file ".bir.sqlite3".
.bir/config
.bir/description


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



