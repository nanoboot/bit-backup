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
package com.robertvokac.bitbackup.files;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.ToString;
import com.robertvokac.bitbackup.core.Utils;

/**
 *
 * @author robertvokac
 */
@ToString
public class FileEntry {

    private String path;
    private String fileName;
    private FileType fileType;
    private String linkTarget;

    private int uid;
    private int gid;
    private String owner;
    private String group;
    //https://www.redhat.com/sysadmin/suid-sgid-sticky-bit
    private UnixPermissions unixPermissions = new UnixPermissions();
    //private String acl;

    private long creationTime;
    private long lastModTime;
    private long lastChangeTime;
    private long lastAccessTime;
    private long size;
    private String hashAlgorithm;
    private String hashSum;
    private Map<String, String> attrs = null;

    public FileEntry(File file) throws IOException {
        fileName = file.getName();
        fileName = fileName.replace("\t", " ");
        path = fileName.equals(SLASH) ? "" : file.getParentFile().getAbsolutePath();
        final Path toPath = file.toPath();
        //https://www.baeldung.com/java-nio2-file-attribute
        BasicFileAttributeView basicFileAttributeView = Files.getFileAttributeView(toPath, BasicFileAttributeView.class);
        BasicFileAttributes basicFileAttributes = basicFileAttributeView.readAttributes();
        fileType = FileType.forFile(basicFileAttributes);
        linkTarget = fileType == FileType.LINK ? Files.readSymbolicLink(toPath).toString() : "";

        uid = (Integer) Files.getAttribute(toPath, "unix:uid");
        gid = (Integer) Files.getAttribute(toPath, "unix:gid");
        FileOwnerAttributeView ownerView = Files.getFileAttributeView(toPath, FileOwnerAttributeView.class);
        UserPrincipal owner_ = ownerView.getOwner();
        PosixFileAttributes posixFileAttributes = Files.readAttributes(toPath, PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        GroupPrincipal group_ = posixFileAttributes.group();
        owner = owner_.getName();
        group = group_.getName();
        //https://stackoverflow.com/questions/13241967/change-file-owner-group-under-linux-with-java-nio-files
        Set<PosixFilePermission> posixFilePermissions = posixFileAttributes.permissions();
        unixPermissions.getUser().setRead(posixFilePermissions.contains(PosixFilePermission.OWNER_READ));
        unixPermissions.getUser().setWrite(posixFilePermissions.contains(PosixFilePermission.OWNER_WRITE));
        unixPermissions.getUser().setExecute(posixFilePermissions.contains(PosixFilePermission.OWNER_EXECUTE));
        //
        unixPermissions.getGroup().setRead(posixFilePermissions.contains(PosixFilePermission.GROUP_READ));
        unixPermissions.getGroup().setWrite(posixFilePermissions.contains(PosixFilePermission.GROUP_WRITE));
        unixPermissions.getGroup().setExecute(posixFilePermissions.contains(PosixFilePermission.GROUP_EXECUTE));
        //
        unixPermissions.getOthers().setRead(posixFilePermissions.contains(PosixFilePermission.OTHERS_READ));
        unixPermissions.getOthers().setWrite(posixFilePermissions.contains(PosixFilePermission.OTHERS_WRITE));
        unixPermissions.getOthers().setExecute(posixFilePermissions.contains(PosixFilePermission.OTHERS_EXECUTE));
        //
        FileTime created = basicFileAttributes.creationTime();
        FileTime modified = basicFileAttributes.lastModifiedTime();
        FileTime accessed = basicFileAttributes.lastAccessTime();
        creationTime = created.to(TimeUnit.MILLISECONDS);
        lastModTime = modified.to(TimeUnit.MILLISECONDS);
        lastChangeTime = ((FileTime) Files.getAttribute(toPath, "unix:ctime")).toMillis();
        lastAccessTime = accessed.to(TimeUnit.MILLISECONDS);
        size = basicFileAttributes.size();
        hashAlgorithm = SH_A256;
        hashSum = Utils.calculateSHA256Hash(file);
        UserDefinedFileAttributeView userDefView = Files.getFileAttributeView(
                toPath, UserDefinedFileAttributeView.class);
        List<String> attributeNames = userDefView.list();

        for (String key : attributeNames) {
            if (attrs == null) {
                attrs = new HashMap<>();
            }

            ByteBuffer attributeValue = ByteBuffer.allocate(userDefView.size(key));
            userDefView.read(key, attributeValue);
            attributeValue.flip();
            String value = Charset.defaultCharset().decode(attributeValue).toString();
            attrs.put(key, value);

        }

    }

    public String attrsAsBase64EncodedString() {
        StringBuilder attrsSb = new StringBuilder();
        Set<String> keys = attrs.keySet();
        List<String> keysList = new ArrayList<>();
        keys.stream().forEach(e -> keysList.add(e));
        
        Collections.sort(keysList);

        for (String key : keysList) {
            String value = attrs.get(key);
            attrsSb.append(key).append("=").append(value).append("\n");
        }
        return Utils.encodeBase64(attrsSb.toString());
    }
    private static final String SH_A256 = "SHA-256";

    private static final String SLASH = "/";

    public String toCsvLine() {
        StringBuilder sb = new StringBuilder();
        sb
                .append(path).append('\t')
                .append(fileName).append('\t')
                .append(fileType.getCh()).append('\t')
                .append(linkTarget).append('\t')
                .append(uid).append('\t')
                .append(gid).append('\t')
                .append(owner).append('\t')
                .append(group).append('\t')
                .append(unixPermissions.toString()).append('\t')
                .append(creationTime).append('\t')
                .append(lastModTime).append('\t')
                .append(lastChangeTime).append('\t')
                .append(lastAccessTime).append('\t')
                .append(size).append('\t')
                .append(hashAlgorithm).append('\t')
                .append(hashSum).append('\t')
                .append(attrs == null ? "" : attrsAsBase64EncodedString());
        return sb.toString();
    }
    public static void main(String[] args) throws IOException {
        final FileEntry fileEntry = new FileEntry(new File("/home/robertvokac/Downloads/zim_0.75.1-1_all.deb"));
        System.out.println(fileEntry.toCsvLine());
    }

}
