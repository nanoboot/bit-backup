///////////////////////////////////////////////////////////////////////////////////////////////
// dog: Tool generating documentation.
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
package org.nanoboot.dog.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.nanoboot.dog.Command;
import org.nanoboot.dog.DogArgs;
import org.nanoboot.dog.Menu;
import org.nanoboot.dog.Utils;
import org.asciidoctor.Asciidoctor;
import static org.asciidoctor.Asciidoctor.Factory.create;
import org.nanoboot.dog.DogException;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author pc00289
 */
public class GenCommand implements Command {

    private static final String ADOC_EXTENSION = ".adoc";
    
    public GenCommand() {
    }

    @Override
    public String getName() {
        return "gen";
    }

    @Override
    public void run(DogArgs dogArgs) {
        if (!dogArgs.hasArgument("in")) {
            dogArgs.addArgument("in", new File(".").getAbsolutePath());
        }

        if (dogArgs.getArgument("in") == null) {
            throw new DogException("Argument in must have a value (must not be empty).");
        }
        if (dogArgs.hasArgument("out") && !(new File(dogArgs.getArgument("out")).exists())) {
            throw new DogException("Argument out must be an existing directory.");
        }

        File inDir = new File(dogArgs.getArgument("in"));
        if (!inDir.exists()) {
            throw new DogException("Argument in must be an existing directory, but that directory does not exist.");
        }
        File dogConfFile = new File(inDir, "dog.conf");
        if (!dogConfFile.exists()) {
            throw new DogException("File dog.conf was not found.");
        }
        File generatedDir = new File((dogArgs.hasArgument("out") ? new File(dogArgs.getArgument("out")) : inDir), "generated");

        if (generatedDir.exists()) {
            try {
                FileUtils.deleteDirectory(generatedDir);
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new DogException("Deleting generated directory failed.", ex);
            }
        }
        generatedDir.mkdir();
        if (!generatedDir.exists()) {
            throw new DogException("Argument out must be an existing directory, but that directory does not exist.");
        }

        //
        Properties dogConfProperties = null;
        try (InputStream input = new FileInputStream(dogConfFile.getAbsolutePath())) {
            dogConfProperties = new Properties();
            dogConfProperties.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
            throw new DogException("Loading file dog.conf failed.", ex);
        }
        Utils.writeTextToFile(Utils.readTextFromResourceFile("/dog.css"), new File(generatedDir, "dog.css"));
        File contentDir = new File(inDir, "content");
        Menu menuInstance = new Menu(contentDir);
        File templateDir = new File(contentDir.getParentFile().getAbsolutePath() + "/templates");
        String headerTemplate = Utils.readTextFromFile(new File(templateDir, "header.html"));
        String footerTemplate = Utils.readTextFromFile(new File(templateDir, "footer.html"));
        processContentDir(contentDir, generatedDir, contentDir, dogConfProperties, menuInstance, headerTemplate, footerTemplate);
    }

    private static void processContentDir(File dir, File generatedDir, File contentDir, Properties dogConfProperties, Menu menuInstance, String headerTemplate, String footerTemplate) {
        for (File inFile : dir.listFiles()) {
            if (inFile.isFile()) {
                processFileInContentDir(inFile, dir, contentDir, dogConfProperties, headerTemplate, menuInstance, footerTemplate, generatedDir);
            }
            if (inFile.isDirectory()) {
                processDirInContentDir(generatedDir, inFile, contentDir, dogConfProperties, menuInstance, headerTemplate, footerTemplate);
            }
        }
    }

    public static void processDirInContentDir(File generatedDir, File inFile, File contentDir, Properties dogConfProperties, Menu menuInstance, String headerTemplate, String footerTemplate) {
        File generatedDir2 = new File(generatedDir, inFile.getName());
        generatedDir2.mkdir();
        processContentDir(inFile, generatedDir2, contentDir, dogConfProperties, menuInstance, headerTemplate, footerTemplate);
    }

    public static void processFileInContentDir(File inFile, File dir, File contentDir, Properties dogConfProperties, String headerTemplate, Menu menuInstance, String footerTemplate, File generatedDir) {
        if (inFile.getName().endsWith(ADOC_EXTENSION)) {
            
            Asciidoctor asciidoctor = create();
            String asciidocText = Utils.readTextFromFile(inFile);
            
            String asciidocCompiled = asciidoctor
                    .convert(asciidocText, new HashMap<String, Object>());
            String pathToRoot = dir.getAbsolutePath().replace(contentDir.getAbsolutePath(), "");
            
            if (!pathToRoot.trim().isEmpty()) {
                int count = 0;
                for (char ch : pathToRoot.toCharArray()) {
                    if (ch == '/') {
                        count++;
                    }
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= count; i++) {
                    sb.append("../");
                }
                pathToRoot = sb.toString();
                
            }
            String titleSeparator = dogConfProperties.containsKey("titleSeparator") ? dogConfProperties.getProperty("titleSeparator") : "::";
            final String humanName = createHumanName(inFile, dogConfProperties);
            
            String start
                    = """
                                                  <!DOCTYPE html>
                                                  <html lang="en">
                                                  <head>
                                                  <meta charset="UTF-8">
                                                  <meta http-equiv="X-UA-Compatible" content="IE=edge">
                                                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                  <meta name="generator" content="Asciidoctor 2.0.16">
                                                  <title>
                                                  """
                    + humanName
                    + (dogConfProperties.containsKey("title")
                    ? (" " + titleSeparator + " " + dogConfProperties.getProperty("title")) : "")
                    + """
                                                  </title>
                                                  <link rel="stylesheet" href="
                                                    """
                    + pathToRoot
                    + """
                                                  dog.css">
                                                          </head>
                                                          <body class="article">
                                                            """
                    + headerTemplate
                    + """
                                                  <div id="header">
                                                    """
                    + createNavigation(inFile, contentDir, dogConfProperties)
                    + "<h1>"
                    + humanName
                    + """
                                                  </h1>
                                                  </div>"""
                    + createMenu(menuInstance, inFile)
                    + """
                                                          <div id="content">
                                                  """;
            String end
                    = """
                                                  </div>
                                                  <div id="footer">
                                                     <div id="footer-text">
                                                        Last updated """
                    + " "
                    + (org.nanoboot.dog.Constants.YYYYMMDDHHMMSSZ_DATE_FORMAT.format(new Date()))
                    + "<br>" + footerTemplate
                    + """
                                                           </div>
                                                        </div>
                                                     </body>
                                                  </html>
                                                  """;
            
            List<String> dirs = new ArrayList<>();
            File currentFile = inFile;
            String rootContentDirPath = contentDir.getAbsolutePath();
            while (!currentFile.getAbsolutePath().equals(rootContentDirPath)) {
                dirs.add(currentFile.getName());
                currentFile = currentFile.getParentFile();
            }
            StringBuilder sb = new StringBuilder();
            for (int i = dirs.size() - 1; i >= 0; i--) {
                String d = dirs.get(i);
                sb.append(d);
                if (i > 0) {
                    sb.append("/");
                }
            }
            String editThisPage = "<hr><a href=\"" + dogConfProperties.getProperty("editURL") + sb.toString() + "\">Edit this page</a>";
            String htmlOutput = start + asciidocCompiled + editThisPage + end;
            
            File htmlFile = new File(generatedDir, inFile.getName().replace(ADOC_EXTENSION, ".html"));
            Utils.writeTextToFile(htmlOutput, htmlFile);
            
        } else {
            Utils.copyFile(inFile, generatedDir);
        }
    }
    

    private static String createMenu(Menu menu, File currentFile) {

        return //"<pre>" + menu.toAsciidoc(currentFile.getAbsolutePath().split("content")[1]) + "</pre>" +
                menu.toHtml(currentFile.getAbsolutePath().split("content")[1]);

    }

    private static String createNavigation(File adocFile, File rootContentDir, Properties dogConfProperties) {
        List<File> files = new ArrayList<>();
        File currentFile = adocFile;
        while (!currentFile.getAbsolutePath().equals(rootContentDir.getAbsolutePath())) {

            if (currentFile.getName().equals("content")) {
                continue;
            }
            files.add(currentFile);
            currentFile = currentFile.getParentFile();
        }

        StringBuilder sb = new StringBuilder("<div class=\"navigation\" style=\"margin-top:20px;\"><a href=\"" + /*path +*/ Utils.createDoubleDotSlash(files.size() - 1) + "index.html\">Home</a>");
        if (files.size() > 1 || !currentFile.getName().equals("index.adoc")) {
            sb.append(" > ");
        }
        for (int i = (files.size() - 1); i >= 0; i--) {
            File file = files.get(i);
            if (file.getName().equals("index.adoc")) {
                continue;
            }
            sb
                    .append("<a href=\"")
                    .append(Utils.createDoubleDotSlash(i - 1))
                    .append(i == 0 ? (file.getName().replace(ADOC_EXTENSION, "")) : "index")
                    .append(".html\">")
                    .append(createHumanName(file, dogConfProperties))
                    .append("</a>\n");

            sb.append(" > ");

        }
        sb.append("</div>");
        String result = sb.toString();
        if (result.endsWith(" > </div>")) {
            result = result.substring(0, result.length() - 9);
            result = result + "</div>";
        }
        return result + "<hr>";
    }

    private static String createHumanName(File inFile, Properties dogConfProperties) {

        String result = inFile.getName();
        if (result.endsWith(ADOC_EXTENSION)) {
            result = result.substring(0, inFile.getName().length() - ADOC_EXTENSION.length());
        }
        result = result.replace("_", " ");
        if (Character.isLetter(result.charAt(0))) {
            result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
        }
        if (result.equals("Index")) {
            File parentFile = inFile.getParentFile();
            if (parentFile.getName().equals("content")) {
                String frontPageName = dogConfProperties.getProperty("frontPageName", "");
                return frontPageName.isBlank() ? "Home" : frontPageName;
            }
            return createHumanName(parentFile, dogConfProperties);
        }
        return result;
    }
}
