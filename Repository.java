package gitlet;

import java.io.*;
import java.util.*;


/** Represents a gitlet repository.
 *  does at a high level.
 *  Professor Hilfinger
 *
 */
public class Repository {
    /**
     //add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");
    public static final File META_DATA_DIR = Utils.join(CWD, ".gitlet/.meta");
    public static final File COMMIT_PARENT_DIR = Utils.join(CWD, ".gitlet/Commits");

    public static final String STA_PARENT_DIR_NAME = "Staging";
    public static final File BRANCH_PARENT_DIR = Utils.join(CWD, ".gitlet/Branches");
    public static final String INITIAL_COMMIT_MESSAGE = "initial commit";
    public static final String MASTER_BRANCH_NAME = "master";
    public static final String OBJECT_BRANCH_LIST = "object-branch-list";
    private static final String OBJECT_COMMIT = "object-commit";
    private static final String OBJECT_BRANCH = "object-branch";

    public static final File FILE_BRANCH_LIST = Utils.join(META_DATA_DIR, OBJECT_BRANCH_LIST);
    public static final File FILE_REMOVED_FILES = Utils.join(META_DATA_DIR, "REMOVED-FILES.txt");
//    public static final File FILE_CURRENT_HEAD_COMMIT = Utils.join(META_DATA_DIR, "CURRENT-HEAD-COMMIT.txt");
    public static final File FILE_CURRENT_BRANCH = Utils.join(META_DATA_DIR, "CURRENT-BRANCH.txt");
    private static final String COMMIT_PARENT_DIR_NAME = "Commits";

    String GITLET_DIR_ABSOLUTE_PATH = null;
    String WORKING_DIR_ABSOLUTE_PATH = null;


    private Branch currentBranch = null;

    private List<Branch> branchList = new ArrayList<>();

    private static final List<String> removedFilesList = new ArrayList<>();

    private  Map<String, String> fileNameToBlobIdMap = new HashMap<>();


    /**
     * The current Staging
     */
//    private Staging staging = new Staging();


    /**
     * The current Checkout
     */
    private Checkout checkout = new Checkout();
    private Map<String, Commit> commitMap = new HashMap<>();

    //Constructor
    public Repository() {

    }

    private Branch createBranch(String branchName) {
        Branch branch = new Branch(branchName);
        File branchDir = getBranchDir(branch.getName());
        makeDirectory(branchDir);
        branch.setBranchDir(branchDir);

        //make branch subdirectory structure
//        makeDirectory(branch.getCommitParentDir());
        makeDirectory(branch.getStagingParentDir());
        makeDirectory(branch.getWorkingParentDir());

        branchList.add(branch);
        //save
        saveBranch(branch);
        saveBranchList();
        return branch;
    }

    private void saveBranchList() {

        Integer branchCount =   branchList.size();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_BRANCH_LIST));

            oos.writeObject(branchCount);

        int i = 1;
        for (Branch branch: branchList) {
            oos.writeObject(branch);
        }
        oos.flush();
        oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBranchList() {
        this.branchList = new ArrayList<>();

        Integer branchCount = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_BRANCH_LIST));
            branchCount = (Integer) ois.readObject();
            if(branchCount == 0){
                return;
            }

            for (int i = 0; i < branchCount; i++) {
                Branch branch = (Branch) ois.readObject();
                branchList.add(branch);
            }
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void createOrLoadMasterBranchOLD() {
        //initial commit
        if (GITLET_DIR.exists()) {
            if (GITLET_DIR.isDirectory()) {
                //READ THE BRANCH object from file.
                Branch masterBranch = readBranch(MASTER_BRANCH_NAME);
                //Set the master branch as the current branch
                this.currentBranch = masterBranch;
                return;
            } else {
                throw new GitletException("EXISTING .gitlet is Must be a directory. Existing .gitlet a file.");
            }
        } else {
            createMasterBranch();
        }

    }

    private void createMasterBranch() {

        Branch masterBranch = createBranch(MASTER_BRANCH_NAME);

        //create META DATA Files
        createFile(FILE_REMOVED_FILES);
//            createFile(FILE_CURRENT_HEAD_COMMIT);
        createFile(FILE_CURRENT_BRANCH);

        //Set the master branch as the current branch
        setCurrentBranch(masterBranch);

        //make first commit
        Commit commit = new Commit(currentBranch.getName(), INITIAL_COMMIT_MESSAGE);
        addNewCommit(commit);
    }

    private void setCurrentBranch(Branch branch) {
        this.currentBranch = branch;
        Utils.writeObject(FILE_CURRENT_BRANCH, branch.getName());
    }

    private void abort() {
        System.exit(0);
    }

    private File getBranchDir(String branchName) {
        File branchDir = Utils.join(BRANCH_PARENT_DIR, branchName);
        return branchDir;
    }

    private File getCommitDirOLD(Branch branch, Commit commit) {
        return getCommitDirOLD(branch, commit.getUid());
    }

    private File getCommitMatchShortUid(Branch branch, String commitUid) {
//        File[] files = branch.getCommitParentDirOLD().listFiles();
        File[] files = COMMIT_PARENT_DIR.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                if (file.getName().equals(commitUid) || file.getName().startsWith(commitUid)) {
//                    return Utils.join(branch.getCommitParentDirOLD(), file.getName());
                    return Utils.join(COMMIT_PARENT_DIR, file.getName());
                }
            }
        }
        return null;
    }

    private File getCommitDirOLD(Branch branch, String commitUid) {
        return Utils.join(branch.getCommitParentDirOLD(), commitUid);
    }


    public File getCommitDir(String commitUid) {
        return Utils.join(COMMIT_PARENT_DIR, commitUid);
    }

    public void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            abort();
        }
        if(!GITLET_DIR.mkdir()){
            throw new GitletException("Unable to create directory: " + GITLET_DIR);
        }
        makeDirectory(META_DATA_DIR);
        makeDirectory(COMMIT_PARENT_DIR);
        //create branch parent directory
        makeDirectory(BRANCH_PARENT_DIR);

        //Create MASTER Branch
        createMasterBranch();


    }

    private void createFile(File fileName) {
        //create
        try {
            fileName.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            throw new GitletException("Could not create file: " + fileName);
        }
    }

    private void addNewCommit(Commit commit) {
        //Create commit directory File object by combining the directory names.
        File commitDir = getCommitDir(commit.getUid());
        makeDirectory(commitDir);
        commit.setCommitDir(commitDir);

        //Write commits to file
        File outFile = new File(commitDir,OBJECT_COMMIT);
        Utils.writeObject(outFile, commit);

        //Add commit to branch
        currentBranch.addCommit(commit);

        //Save Branch info
        saveBranch(currentBranch);
        commitMap.put(commit.getUid(), commit);
    }


    private void makeDirectory(File dir) {
        if(dir.exists()){
            return;
        }
        if(!dir.mkdir()){
            throw new GitletException("Unable to create directory: " + dir);
        }
    }


    private void saveBranch(Branch branch) {
        File branchDir = branch.getBranchDir();

        File outFile = getBranchObjectFile(branchDir);
        Utils.writeObject(outFile, branch);
    }

    private File getBranchObjectFile(File branchDir) {
        return new File(branchDir, OBJECT_BRANCH);
    }


    private void loadCurrentBranch() {
        String currentBranchName = Utils.readObject(FILE_CURRENT_BRANCH, String.class);
        File branchDir = getBranchDir(currentBranchName);
        File file = getBranchObjectFile(branchDir);
        this.currentBranch = Utils.readObject(file, Branch.class);
    }


    private Branch readBranch(String branchName) {

        File branchDir = getBranchDir(branchName);

        File file = getBranchObjectFile(branchDir);
        Branch branch = Utils.readObject(file, Branch.class);
        return branch;
    }
    private Commit readCommit(File commitDir) {

        File file = new File(commitDir, OBJECT_COMMIT);
        Commit branch = Utils.readObject(file, Commit.class);
        return branch;
    }

    public void addToStaging(String fileName) {

        File file = new File(fileName); // creates File object to represent file to be added.
        if(!file.exists()){
            System.out.println("File does not exist.");
            abort();
        }

        loadCurrentBranch();

        //if file is  in the removed list
        //do not add to staging

        loadRemovedFiles();

        int count = removedFilesList.size();
        boolean removed = false;
        for(int i = 0; i < count; i++){
            String removedFileName = removedFilesList.get(i);
            if(removedFileName.equals(fileName)){
                removedFilesList.remove(i);
                removed = true;
                break;
            }
        }
        if(removed){
            StringBuilder sb = new StringBuilder();
            for (String removedFileName: removedFilesList){
                sb.append(removedFileName);
                sb.append("\n");
            }
            overwriteRemovedFilesTextFile(sb.toString());
            return;
        }

//        adding a tracked, unchanged file has no effect
        if(isTrackedInHeadCommit(fileName)){
            //check if the file contents are the same
            File f1 = new File(fileName);
            File f2 = Utils.join(currentBranch.getHeadCommitDir(), fileName);
            if(isSameFileContent(f1, f2)){
                return;
            }
        }

        //Add to Staging
        //read file content.

        byte[] content = Utils.readContents(file);

        //copy file to staging folder (overwrite if exists)
            File destFile = new File(currentBranch.getStagingParentDir(), fileName);
            Utils.writeContents(destFile, content);
    }

    private boolean isSameFileContent(File f1, File f2) {
        byte[] bytes1 = Utils.readContents(f1);
        byte[] bytes2 = Utils.readContents(f2);
        return Arrays.equals(bytes1, bytes2);
    }

    private boolean isTrackedInHeadCommit(String fileName) {
        List<String> files = Utils.plainFilenamesIn(currentBranch.getHeadCommitDir());
        return files.contains(fileName);
    }


    public void removeFromStaging(String fileName) {
        //if file is in the staging
        //delete from staging
        File stagingFile = new File(currentBranch.getStagingParentDir(), fileName);
        if(stagingFile.exists()){
            stagingFile.delete();
            appendToRemovedFilesTextFile(fileName);
            System.out.println("Removed from staging: " + fileName);
        }
    }

    public void removeFromWorkingDir(String fileName) {
        //if file is in the working directory
        //delete it.
        File file = new File(fileName);
        if(file.exists()){
            file.delete();
            appendToRemovedFilesTextFile(fileName);
        }
    }

    public void commit(String commitMessage) {
        if(commitMessage == null || commitMessage.trim().equals("")){
            System.out.println("Please enter a commit message.");
            abort();
        }
//        System.out.println(">>>>>>commitMessage: " + commitMessage);
        loadCurrentBranch();

        //getStagingFiles
        File[] stagedForAddFiles = getStagingFiles();
        //getRemovedFiles
        loadRemovedFiles();

        if((stagedForAddFiles == null || stagedForAddFiles.length == 0) && removedFilesList.isEmpty()){
            System.out.println("No changes added to the commit.");
            abort();
        }

        //Get PARENT commit hash.
        String parentUid = currentBranch.getHeadCommitHash();
        System.out.println();

        Commit commit = new Commit(currentBranch.getName(), parentUid, commitMessage); // creates object commit
        //add the new commit to the file
        addNewCommit(commit);

        //Move staging files to commit folder
        File commitDir = commit.getCommitDir();

        for (File file : stagedForAddFiles) {
            commitFile(commitDir, file.getName());
        }
        /* needs to update the myCommits hashmap with the new Commit object, empty the staging-area, and redirect the
	 current branch pointer and current branch name to the newly made commit.*/

        //EMPTY the REMOVED Files.
        FILE_REMOVED_FILES.delete();
//        System.out.println("DELETED: " + FILE_REMOVED_FILES);
//        System.out.println("exists: " + FILE_REMOVED_FILES.exists());
        //create File RemovedFilesList;
        createFile(FILE_REMOVED_FILES);
    }

    private File[] getStagingFiles() {
        File stagingParentDir = currentBranch.getStagingParentDir();
        return stagingParentDir.listFiles();
    }

    private void commitFile(File commitDir, String fileName) {
//        MOVE from staging to commit
        File stagingFile = new File(currentBranch.getStagingParentDir(), fileName);
//        System.out.println(">>>>>stagingFile: " + stagingFile);

        byte[] content = Utils.readContents(stagingFile);

//        copy file to:
//        commit/x/

        File destFile = new File(commitDir, fileName);
//        System.out.println(">>>>>destFile: " + destFile);

        Utils.writeContents(destFile, content);
        //Delete staging File: staging/file
        stagingFile.delete();
    }

   /** public void checkout(){ //file name , commit id, branch name
        if (!file){ //file doesn't exits
            throw new Exception("File doesn't exist in that commit.");

        }

    }*/

   //

           //COMPILE ISSUE - COMMENTED.
//        File commits = Utils.plainFilenamesIn(); //reading from persistence //how commits are being stored
    //Utils.readObject()

       public void checkoutBranch(String branchName) {
           loadCurrentBranch();
           loadBranchList();

           Branch branchToCheckout = findBranchByName(branchName);
           if (branchToCheckout == null) {
               System.out.println("No such branch exists.");
               abort();;
           }

           if(currentBranch.getName().equals(branchToCheckout.getName())){
               System.out.println("No need to checkout the current branch.");
               abort();
           }
//
//           List<Commit> commits = null; //COMPILE ISSUE - create TEMP variable.
//           Commit branchHeadCommit = findCommitByUid(commits, branchToCheckout.getHeadCommitUid());
//           if (branchHeadCommit == null) {
//               throw new GitletException("commit not found");
//           }


           GITLET_DIR_ABSOLUTE_PATH = GITLET_DIR.getAbsolutePath();
           WORKING_DIR_ABSOLUTE_PATH = GITLET_DIR_ABSOLUTE_PATH.substring(0, GITLET_DIR_ABSOLUTE_PATH.indexOf("/.gitlet"));

           //check if any untracked files present
           if(isUntrackedFilesPresent()){
               System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
               abort();
           }

           //Perform branch checkout
           branchToCheckout = readBranch(branchToCheckout.getName());

           saveCurrentBranchWorkingDirectory();
           restoreCheckoutBranchWorkingDirectory(branchToCheckout);
           setCurrentBranch(branchToCheckout);
       }

    private boolean isUntrackedFilesPresent() {
        File workingDir = new File(WORKING_DIR_ABSOLUTE_PATH);
        File[] files = workingDir.listFiles();
        for(File srcFile: files){
            if(srcFile.isDirectory()){
                continue;
            }
            String fileName = srcFile.getName();
            if (!isTrackedInHeadCommit(fileName)) {
                //untracked file found.
                return true;
            } else {
                //check if the file contents are the same
                File f1 = new File(fileName);
                File f2 = Utils.join(currentBranch.getHeadCommitDir(), fileName);
                if(!isSameFileContent(f1, f2)){
                    //File changed but not committed
                    return true;
                }
            }
        }
        return false;
    }

    private void restoreCheckoutBranchWorkingDirectory(Branch branchToCheckout) {
        File[] files = branchToCheckout.getWorkingParentDir().listFiles();
        for(File srcFile: files){
            if(srcFile.isDirectory()){
                continue;
            }
            File destFile = Utils.join(WORKING_DIR_ABSOLUTE_PATH, srcFile.getName());

            copyFile(srcFile, destFile);
        }
    }

    private void saveCurrentBranchWorkingDirectory() {
        File workingDir = new File(WORKING_DIR_ABSOLUTE_PATH);
        File[] files = workingDir.listFiles();
        for(File srcFile: files){
            if(srcFile.isDirectory()){
                continue;
            }
            File destFile = Utils.join(currentBranch.getWorkingParentDir(), srcFile.getName());

            copyFile(srcFile, destFile);
            //clearCurrentBranchWorkingDirectory files
            srcFile.delete();
        }

    }

    private void copyFile(File srcFile, File destFile) {
        byte[] bytes = Utils.readContents(srcFile);
        Utils.writeContents(destFile, bytes);
    }

    public Branch findBranchByName(String branchName) {
           if(branchList == null){
               loadBranchList();
           }
            for (Branch branch : branchList) {
                if (branch.getName().equals(branchName)){
                    return branch;
                }
            }
            return null;
        }


       public static Commit findCommitByUid(List<Commit> commits, String uid) {
           for (Commit commit : commits) {
               if (commit.getUid().equals(uid)) {
                   return commit;
               }
           }
           return null;
       }


    private void checkoutHeadCommitFile(String fileName) {
        loadCurrentBranch();
        Commit headCommit = currentBranch.getHeadCommit();
        checkoutCommitFile(headCommit, fileName);
    }

    public void checkoutCommitFile(String commitUid, String fileName) {
        loadCurrentBranch();

//        Commit commit = commitMap.get(commitUid);
//        File commitDir = getCommitDir(currentBranch, commitUid);
        File commitDir = getCommitMatchShortUid(currentBranch, commitUid);

        if (commitDir == null || !commitDir.exists() || !commitDir.isDirectory()) {
            System.out.println("No commit with that id exists.");
            abort();
        }
        Commit commit = readCommit(commitDir);
        checkoutCommitFile(commit, fileName);
    }


    public void checkoutCommitFile(Commit commit, String fileName) {
//        COPY from Commits to checkout

        //if file is not in the checkout
        //add to checkout
//        System.out.println(">>>>>>checkout files: "+checkout);

//        if(!checkout.hasFile(fileName)){
//            checkout.addFile(fileName);
//        }
        File commitDir = commit.getCommitDir();
        File[] files = commitDir.listFiles();
        File checkoutFile = null;
        for (File file: files){
            if(file.getName().matches(fileName)){
                checkoutFile = file;
            }
        }
        if(checkoutFile == null){
            System.out.println("File does not exist in that commit.");
            abort();
        }
//        File commitFile = new File(commitDir, fileName);
//        System.out.println(">>>>>>"+checkoutFile.getAbsolutePath());
        byte[] content = Utils.readContents(checkoutFile);

//        copy file to:
//        Checkout/
        //copy file to WORKING DIRECTORY (overwrite if exists? PENDING)
        File destFile = new File(fileName);
//        System.out.println(">>>>>>Checkout out to: "+destFile.getAbsolutePath());

        Utils.writeContents(destFile, content);
    }

    public void log() {
        loadCurrentBranch();

        List<Commit> commits = currentBranch.getCommits();
        int lastIndex = commits.size() - 1 ;
        for(int i = lastIndex; i >= 0; i--){
            Commit commit = commits.get(i);
            printLog(commit);
        }
    }


    private void printLog(Commit commit) {
       /*
       FORMAT:

       ===
commit a0da1ea5a15ab613bf9961fd86f010cf74c7ee48
Date: Thu Nov 9 20:00:05 2017 -0800
A commit message.

===
commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff
Date: Thu Nov 9 17:01:33 2017 -0800
Another commit message.

===
commit e881c9575d180a215d1a636545b8fd9abfb1d2bb
Date: Wed Dec 31 16:00:00 1969 -0800
initial commit

        */

        System.out.println("===");
        System.out.println("commit " + commit.getUid());
        System.out.println("Date: " + DateTimeUtil.formatForLog(commit.getTimestamp()));
        System.out.println(commit.getMessage());
        System.out.println();

    }


    public static boolean isRepoInitialized() { //making sure repo is not null
        return GITLET_DIR.exists() && GITLET_DIR.isDirectory();
    }

    public void processCheckoutCommand(String[] args) {

        if (args.length == 2) {
            // `gitlet checkout <branch_name>` -- Checkout to branch command
            checkoutBranch(args[1]); //then go to checkout
        }
        else if (args.length == 3) {
            // `gitlet checkout <branch_name>` -- Checkout to branch command
//            repository.checkoutBranch(args[1]); //then go to checkout
            if("--".equals(args[1])) {
                checkoutHeadCommitFile(args[2]);
            }
            else{
                throw new GitletException("Unsupported command: " + Arrays.toString(args));
            }
        }
        else if (args.length == 4) {
            //FORMAT:
            //checkout ${UID1} -- wug.txt

            // `gitlet checkout <branch_name>` -- Checkout to branch command
//            repository.checkoutBranch(args[1]); //then go to checkout
            if("--".equals(args[2])) {
                checkoutCommitFile(args[1], args[3]);
            }
            else{
                System.out.println("Incorrect operands");
                abort();
            }
        }
    }


    public void status() {
        if (!Repository.isRepoInitialized()) { //checking if repo is not initialized
            System.out.println("Not in an initialized Gitlet directory.");
            abort();
        }
        loadCurrentBranch();

/*
FORMAT:

=== Branches ===
*master

=== Staged Files ===

=== Removed Files ===

=== Modifications Not Staged For Commit ===

=== Untracked Files ===

 */

        System.out.println("=== Branches ===");
        loadBranchList();
        for(Branch branch: branchList){
            if(branch.getName().equals(currentBranch.getName())) {
                System.out.print("*");
            }
            System.out.println(branch.getName());
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        //getStagingFiles
        File[] files = getStagingFiles();
        for (File file : files) {
            System.out.println(file.getName());
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        //getRemovedFiles
        loadRemovedFiles();
        for (String fileName : removedFilesList) {
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
//PENDING
        System.out.println();

        System.out.println("=== Untracked Files ===");
//PENDING
        System.out.println();

    }

    private void loadCurrentState() {
//        try {
//
//            String currentHeadCommit = Utils.readContentsAsString(FILE_CURRENT_HEAD_COMMIT);
//            BufferedReader br = new BufferedReader(new FileReader(FILE_CURRENT_HEAD_COMMIT));
//
//            String line;
//            while((line = br.readLine()) != null) {
//                removedFilesList.add(line);
//            }
//            br.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void loadRemovedFiles() {
           removedFilesList.clear();
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_REMOVED_FILES));

            String line;
            while((line = br.readLine()) != null) {
                removedFilesList.add(line);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void appendToRemovedFilesTextFile(String fileName) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_REMOVED_FILES, true));
            bw.write(fileName);
            bw.write("\n");
            bw.flush();
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void overwriteRemovedFilesTextFile(String content) {
        overwriteFile(FILE_REMOVED_FILES, content);
    }

    private void overwriteFile(File fileName , String content) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false));
            bw.write(content);
            bw.flush();
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rm(String fileName) {
        loadCurrentBranch();
        //Check if removal of unstaged, untracked file.
        if(!isTrackedInHeadCommit(fileName) && !isStagedForAddition(fileName)){
            System.out.println("No reason to remove the file.");
            abort();
        }

        //Check that we can unstage a file we have deleted with plain Unix 'rm'
        File file = new File(fileName);

        if(!file.exists()){
            if(isTrackedInHeadCommit(fileName)){
                appendToRemovedFilesTextFile(fileName);
                return;
            }
        }

        //Do remove operation
        removeFromStaging(fileName);
        removeFromWorkingDir(fileName);
    }

    private boolean isStagedForAddition(String fileName) {
        //getStagingFiles
        File[] files = getStagingFiles();
        for (File file : files) {
            if(file.getName().equals(fileName)){
                return true;
            }
        }
           return false;
    }

    public void globalLog() {
        loadCurrentBranch();

        List<Commit> commits = currentBranch.getCommits();
        int lastIndex = commits.size() - 1 ;
        for(int i = lastIndex; i >= 0; i--){
            Commit commit = commits.get(i);
            printLog(commit);
//            System.out.println("commit " + commit.getUid());
        }
    }

    public void find(String message) {
        loadCurrentBranch();

        List<Commit> commits = currentBranch.getCommits();
        int lastIndex = commits.size() - 1 ;
        boolean found = false;
        for(Commit commit: commits){
            if(commit.getMessage().contains(message)) {
                System.out.println(commit.getUid());
                found = true;
            }
        }
        if(!found){
            System.out.println("Found no commit with that message.");
        }
//        for(int i = lastIndex; i >= 0; i--){
//            Commit commit = commits.get(i);
//            if(commit.getMessage().contains(message)) {
//                System.out.println(commit.getUid());
//            }
//        }

    }


    public void branch(String branchName) {
           loadCurrentBranch();
        if(isExistingBranch(branchName)){
            System.out.println("A branch with that name already exists.");
            abort();
        }

        Branch branch = createBranch(branchName);
        Commit currentBranchHeadCommit = this.currentBranch.getHeadCommit();
        branch.addCommit(currentBranchHeadCommit);
        saveBranch(branch);
    }

    public void rmBranch(String branchName) {
           loadCurrentBranch();
           if(currentBranch.getName().equals(branchName)){
               System.out.println("Cannot remove the current branch.");
               abort();
           }

        if(!isExistingBranch(branchName)){
            System.out.println("A branch with that name does not exist.");
            abort();

        }


        int count = branchList.size();
        boolean removed = false;
        for(int i = 0; i < count; i++){
            Branch branch = branchList.get(i);
            if(branch.getName().equals(branchName)){
                branchList.remove(i);
                removed = true;
                break;
            }
        }

        if(removed){
            saveBranchList();
        }
    }

    private boolean isExistingBranch(String branchName) {

        loadBranchList();
        for (Branch branch: branchList) {
            if (branch.getName().equals(branchName)) {
                return true;
            }
        }

//        if(MASTER_BRANCH_NAME.equals(branchName)){
//            return true;
//        }
        return false;
    }
}
