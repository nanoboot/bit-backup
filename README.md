# bit-backup

You can find more information in docs directory.

### Feature 2 : Backup of files




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
