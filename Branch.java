package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Branch implements Serializable{
    private static final String COMMIT_PARENT_DIR_NAME = "Commits";
    private static final String STAGING_PARENT_DIR_NAME = "Staging";
    private static final String WORKING_PARENT_DIR_NAME = "Working";

    //each branch has a name and a reference to the head commit
    private String name;
    private String headCommitUid;
    private List<Commit> commitList = new ArrayList<>();
    private File branchDir;


    public Branch(String name) {
        this.name = name;
    }

    public Branch(String name, String headCommitUid) {
        this.name = name;
        this.headCommitUid = headCommitUid;
    }

    public String getName(){
        return name;
    }

    public String getHeadCommitUid(){
        return headCommitUid;
    }

    @Override
    public String toString() {
        return "Branch{" +
                "name='" + name + '\'' +
                ", headCommitUid='" + headCommitUid + '\'' +
                '}';
    }

    public void addCommit(Commit commit) {
        this.commitList.add(commit);
    } //

    public String getHeadCommitHash() {
        Commit commit = getHeadCommit();
        return commit.getUid();
    }

    public Commit getHeadCommit() {
        //The commit at the LAST index is the recent commit.
        int lastIndex = commitList.size() -1;
        Commit commit = commitList.get(lastIndex);
        return commit;
    }


    public File getHeadCommitDir() {
        return getHeadCommit().getCommitDir();
    }

    public void setBranchDir(File branchDir) {

        this.branchDir = branchDir;
    }

    public File getBranchDir() {
        return branchDir;
    }

    public File getCommitParentDirOLD() {
        return Utils.join(getBranchDir(), COMMIT_PARENT_DIR_NAME);
    }

    public File getStagingParentDir() {
        return Utils.join(getBranchDir(), STAGING_PARENT_DIR_NAME);
    }
    public File getWorkingParentDir() {
        return Utils.join(getBranchDir(), WORKING_PARENT_DIR_NAME);
    }

    public List<Commit> getCommits() {
        return commitList;
    }


}
