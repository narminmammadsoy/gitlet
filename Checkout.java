package gitlet;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Checkout implements Serializable {
    private Set<String> files = new HashSet<>();


    public boolean hasFile(String fileName) {
        return files.contains(fileName);
    }

    public void addFile(String fileName) {
        files.add(fileName);
    }

    public Set<String> getFiles() {
        return this.files;
    }
}
