package com.infasoft.antiguard;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Main {

    private static boolean logging = true;
    private static File workingDir = null;
    private static File srcFolder;
    private static ArrayList<String> renamedPaths = new ArrayList<> ();
    private static ArrayList<File> smaliFiles = new ArrayList<> ();

    public static void main(String[] args) throws IOException {

        srcFolder = new File (args[0]).getAbsoluteFile ();

        /* Check working directory exists. if not exit. */
        if (srcFolder.exists ()){
            makeLog ("Working on " + srcFolder.getParent ());
            workingDir = new File (srcFolder.getParent ());
        } else {
            makeLog ("Directory doesn't exists!");
            System.exit (1);
        }

        makeLog ("Creating backup directory...");

        if (FileUtils.getFile (workingDir, srcFolder.getName () + "_AG").exists ()) {
            makeLog ("Skipping... Backup folder already exists.");
            srcFolder = new File (srcFolder + "_AG");

        } else {
            createBackupFolder ();
        }

        doWork (srcFolder);

        makeLog ("Package complication success!");
        makeLog ("Collecting changed paths...");

//        for (int i=0; i<renamedPaths.size(); i++) {
//            String s = renamedPaths.get(i);
//            s = s.replace("\\", "/");
//            renamedPaths.add(i, s);
//            makeLog(s);
//        }

        makeLog ("Starting source change...");
        makeLog ("This task will take more time...");

        float totalFiles = Float.parseFloat(String.valueOf(smaliFiles.size()));
        float completedFiles = 0;

        for (File f : smaliFiles){
            fixFile (f);
            completedFiles++;
            float percent = (completedFiles/totalFiles) * 100;
            makeLog ("\t\t\t\t||||| " + String.valueOf(percent).substring(0, String.valueOf(percent).lastIndexOf(".")) + "% |||||");
        }

    }

    private static void fixFile(File smali){

        int replacements = 0;

        makeLog ("Changing source file " +
                smali.getAbsolutePath ().replace (srcFolder.getAbsolutePath () + "\\", ""));

        StringBuilder fileContent = new StringBuilder ();
        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {

            reader = new BufferedReader (new FileReader (smali.getAbsolutePath ()));
            String currReadLine = reader.readLine ();

            while (currReadLine != null) {

                for (String s : renamedPaths){
                    if (currReadLine.contains("L" + s + "/")){
                        String[] pathArr = s.split("/");
                        String out = "L";
                        for (int i=0; i<pathArr.length; i++) {
                            if (i < (pathArr.length - 1)) {
                                out += pathArr[i] + "/";
                            } else {
                                out += "pkg_" + pathArr[i] + "/";
                            }
                        }
                        currReadLine = currReadLine.replace("L" + s + "/", out);
                    }
                }
                fileContent.append (currReadLine).append (System.lineSeparator ());
                currReadLine = reader.readLine ();
            }

            writer = new BufferedWriter (new FileWriter (smali.getAbsolutePath ()));

            writer.write (fileContent.toString ());

        } catch (IOException e) {
            e.printStackTrace ();

        } finally {
            makeLog ("- Task completed. " + replacements + " replacements made on " + smali.getName ());

            try {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                makeErr (e.getMessage ());
            }
        }

    }

    private static void createBackupFolder(){
        try {
            FileUtils.copyDirectory (
                    srcFolder, /* Source*/
                    FileUtils.getFile (workingDir, srcFolder.getName () + "_AG")
            );

            makeLog ("Backup directory creation success");
            makeLog ("Moving to new work directory...");
            srcFolder = new File (srcFolder + "_AG");

        } catch (IOException e) {
            makeErr ("Could't create backup for directory");
            makeErr (e.getMessage ());
            System.exit (1);
        }
    }


    private static void doWork (File src) {

        makeLog ("Analyzing " + src.getAbsolutePath ());

        ArrayList<File> files = new ArrayList<> ();

        files.addAll (Arrays.asList (Objects.requireNonNull (src.listFiles ())));

        for (File cf : files){
            if (cf.isDirectory ()){
                handleDirectory (new File (cf.getAbsolutePath ()));
            } else {
                if (cf.getName ().contains (".smali")){
                    smaliFiles.add (new File (cf.getAbsolutePath ()));
                }
            }
        }

    }

    private static void handleDirectory(File dir){

        for (File f : Objects.requireNonNull (dir.listFiles ())){

            if (f.isDirectory ()){

                if (FileUtils.getFile (f.getParentFile (), f.getName () + ".smali").exists ()) {

                    makeLog ("Fixing conflict for package " +
                            f.getAbsolutePath ().replace (srcFolder.getAbsolutePath () + "\\", ""));

                    boolean renameTask = f.renameTo (new File (f.getParentFile (),"pkg_" + f.getName ()));
                    String renamedPkg = FileUtils.getFile (f.getParentFile (), f.getName ())
                            .getAbsolutePath ().replace (srcFolder.getAbsolutePath () + "\\", "")
                            .replace("\\", "/");
                    renamedPaths.add (renamedPkg);
                    handleDirectory (new File (f.getParentFile (), "pkg_" + f.getName ()));

                } else {
                    handleDirectory (new File (f.getAbsolutePath ()));
                }
            } else {
                if (f.getName ().contains (".smali")){
                    smaliFiles.add (new File (f.getAbsolutePath ()));
                }
            }
        }
    }

    private static void makeLog(String s){
        if (!logging) return;
        System.out.println(s);
    }

    private static void makeErr(String s){
        if (!logging) return;
        System.err.println(s);
    }
}
