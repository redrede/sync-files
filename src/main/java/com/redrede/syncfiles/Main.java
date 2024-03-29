package com.redrede.syncfiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

/**
 *
 * @author marcelo
 */
public class Main {

    public static void main(String[] args) {
        try {
            if (args.length == 4) {
                System.out.println("Parameters");
                System.out.println("src: "+args[0]);
                System.out.println("dest: "+args[1]);
                System.out.println("dir-filter: "+args[2]);
                System.out.println("extensions-filter: "+args[3]);
                syncFiles(args[0], args[1], args[2], args[3]);
            } else {
                System.err.println("Error:");
                System.err.println("Enter the parameters");
                System.out.println("java -jar syncfiles.jar <src> <dest> <dir-filter> <extensions-filter>");
                System.out.println("Example: ");                
                System.out.println("java -jar syncfiles.jar \"/home/redrede/DEV/teste/2.0/\" \"/home/redrede/Servers/wildfly14/standalone/tmp/vfs/deployment/\" \"src\" \"xhtml,html,css,js\"");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void syncFiles(String dirFrom, String dirTo, String subdirFilter, String extensions) throws IOException {
        //get the file object

        List<String> extensionsList = Arrays.asList(extensions.split(","));

        File toDir = new File(dirTo);

        File fromDir = FileUtils.getFile(dirFrom);

        FileAlterationObserver observer = new FileAlterationObserver(fromDir);

        observer.addListener(new FileAlterationListenerAdaptor() {

            @Override
            public void onDirectoryCreate(File file) {
                System.err.println("Not supported folder created : " + file.getName());
            }

            @Override
            public void onDirectoryDelete(File file) {
                System.err.println("Not supported folder deleted: " + file.getName());
            }

            @Override
            public void onFileCreate(File srcFile) {
                if (srcFile.getAbsolutePath().contains(subdirFilter) && extensionsList.contains(FilenameUtils.getExtension(srcFile.getName()))) {
                    System.out.println("File create: " + srcFile);
                    Collection<File> dirs = FileUtils.listFilesAndDirs(toDir, DirectoryFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
                    File destDir = null;
                    for (File posDir : dirs) {
                        if (posDir.getName().equals(srcFile.getParentFile().getName()) && posDir.getParentFile() != null && srcFile.getParentFile().getParentFile() != null && posDir.getParentFile().getName().equals(srcFile.getParentFile().getParentFile().getName())) {
                            destDir = posDir;
                        } else if (destDir == null && posDir.isDirectory() && posDir.getName().equals(srcFile.getParentFile().getName())) {
                            destDir = posDir;
                        }
                    }
                    if (destDir != null) {
                        try {
                            File destFile = new File(destDir.getAbsolutePath() + File.separator + srcFile.getName());
                            FileUtils.copyFile(srcFile, destFile);
                            System.out.println("File create sync: " + destFile);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFileDelete(File srcFile) {
                if (srcFile.getAbsolutePath().contains(subdirFilter) && extensionsList.contains(FilenameUtils.getExtension(srcFile.getName()))) {
                    System.out.println("File delete: " + srcFile);
                    Collection<File> files = FileUtils.listFiles(toDir, new NameFileFilter(srcFile.getName()), TrueFileFilter.INSTANCE);
                    File destFile = null;
                    for (File destAux : files) {
                        if (files.size() > 1) {
                            if (destAux.getParentFile() != null && srcFile.getParentFile() != null && destAux.getParentFile().getName().equals(srcFile.getParentFile().getName())) {
                                destFile = destAux;
                            } else if (destFile == null) {
                                destFile = destAux;
                            }
                        } else if (files.size() == 1) {
                            destFile = destAux;
                        }
                    }
                    if (destFile != null) {
                        System.out.println("File delete sync: " + destFile);
                        FileUtils.deleteQuietly(destFile);
                    }
                }
            }

            @Override
            public void onFileChange(File srcFile) {
                if (srcFile.getAbsolutePath().contains(subdirFilter) && extensionsList.contains(FilenameUtils.getExtension(srcFile.getName()))) {
                    System.out.println("File change: " + srcFile);
                    Collection<File> files = FileUtils.listFiles(toDir, new NameFileFilter(srcFile.getName()), TrueFileFilter.INSTANCE);
                    File destFile = null;
                    for (File destAux : files) {
                        if (files.size() > 1) {
                            if (destAux.getParentFile() != null && srcFile.getParentFile() != null && destAux.getParentFile().getName().equals(srcFile.getParentFile().getName())) {
                                destFile = destAux;
                            } else if (destFile == null) {
                                destFile = destAux;
                            }
                        } else if (files.size() == 1) {
                            destFile = destAux;
                        }
                    }
                    if (destFile != null) {
                        try {
                            System.out.println("File sync: " + destFile);
                            
                            String content = IOUtils.toString(new FileInputStream(srcFile), StandardCharsets.UTF_8);
                            IOUtils.write(content, new FileOutputStream(destFile), StandardCharsets.UTF_8);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }

        });

        FileAlterationMonitor monitor = new FileAlterationMonitor(1000, observer);

        try {
            monitor.start();
            System.out.println("Start file sync");

        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
