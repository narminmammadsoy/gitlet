package gitlet; //serializable



import java.io.File;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.Map;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 */
public class Commit implements Serializable {
    /**
     *
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    private String uid;
    private String parentUid;
    /** The message of this Commit. */

    private String message;
    private Date timestamp;
    private Map<String, String> fileNameToBlobIdMap;
    /** The message of this Commit. */

    private String branchName;
    private File commitDir;


    //metadata
    //head branch or master
    //for very first commit
    public Commit(String message, Map <String, String> fileNameToBlobIdMap){
        this.message = message;
        this.timestamp = DateTimeUtil.createInitTimestamp();
        this.uid = this.parentUid = createCommitUid();
        this.fileNameToBlobIdMap = fileNameToBlobIdMap;
    }

    private String createCommitUid() {
        return Utils.sha1(message, timestamp.toString());
    }

    public Commit(String branchName, String message){
        this(branchName, null, message);
    }

    public Commit(String branchName, String parentUid, String message){
        this.message = message;
        this.timestamp = new Date();
        this.branchName = branchName;
        this.parentUid = parentUid;
        this.uid = createCommitUid();
    }

    public String getUid() {
        return uid;
    }

    public String getParentUid() {
        return parentUid;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getBranchName() {
        return branchName;
    }

    public File getCommitDir() {
        return commitDir;
    }

    public void setCommitDir(File commitDir) {
        this.commitDir = commitDir;
    }
}
