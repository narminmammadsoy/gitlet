package gitlet;

import java.io.File;

public class MainTest {
    public static void main(String[] args) {

//        File file = new File(".test");
//
//        file.mkdir();
//        System.out.println(file.getAbsolutePath());
//        System.out.println(file.getParent());

        branchAndRmBranch();
    }

    private static void remove() {
        String[] input;
        input = new String[]{"init"};
        Main.processArguments(input);
        input = new String[]{"rm", "abc.txt"};
        Main.processArguments(input);

    }

    public static void find() {

        String[] input;
        input = new String[]{"init"};
        Main.processArguments(input);
        input = new String[]{"add", "wug.txt"};
        Main.processArguments(input);
        input = new String[]{"commit", "added file"};
        Main.processArguments(input);
        input = new String[]{"add", "abc.txt"};
        Main.processArguments(input);
        input = new String[]{"commit", "added file"};
        Main.processArguments(input);

        input = new String[]{"find", "added file"};

        Main.processArguments(input);
    }

    public static void checkout() {

        String[] input;
        input = new String[]{"init"};
        Main.processArguments(input);
        input = new String[]{"add", "wug.txt"};
        Main.processArguments(input);
        input = new String[]{"commit", "added wug"};
        Main.processArguments(input);

        input = new String[]{"checkout", "--", "wug.txt"};
        Main.processArguments(input);
    }

    public static void branchAndRmBranch() {

        String[] input;
        input = new String[]{"init"};
        Main.processArguments(input);
        input = new String[]{"add", "wug.txt"};
        Main.processArguments(input);
        input = new String[]{"commit", "added wug"};
        Main.processArguments(input);

        input = new String[]{"branch", "other"};
        Main.processArguments(input);

        input = new String[]{"checkout", "other"};
        Main.processArguments(input);

        input = new String[]{"add", "abc.txt"};
        Main.processArguments(input);
        input = new String[]{"commit", "added abc"};
        Main.processArguments(input);

        input = new String[]{"checkout", "master"};
        Main.processArguments(input);


        input = new String[]{"rm-branch", "other"};
        Main.processArguments(input);


        input = new String[]{"checkout", "other"};
        Main.processArguments(input);

        input = new String[]{"log"};
        Main.processArguments(input);
    }
}
