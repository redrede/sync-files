package com.redrede.syncfiles;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
            syncFiles("/home/redrede/DEV/cpqd/saf/2.3.0/", "/home/redrede/Servers/wildfly14/standalone/tmp/vfs/deployment/", "src", "xhtml,html,css,js");
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
                            FileUtils.copyFile(srcFile, destFile);
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
