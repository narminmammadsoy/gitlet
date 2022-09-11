package gitlet;

import java.io.File;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *
 */
public class Main {

    /**
     * The Repository.
     */
    private static Repository repository = new Repository();

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if(args.length < 1){
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        processArguments(args);
    }

    public static void processArguments(String[] args) {

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                //handle the `init` command
                initializeRepository();
                break;
            case "add":
                //handle the `add [filename]` command
                addToStaging(args);
                break;
            case "commit":
                handleCommitCommand(args);
                break;
            case "branch":
                handleBranchCommand(args);
                break;
            case "rm-branch":
                handleRmBranchCommand(args);
                break;
            case "checkout":
                handleCheckoutCommand(args);
                break;
            case "log":
                handleLogCommand(args);
                break;
            case "global-log":
                handleGlobalLogCommand(args);
                break;
           case "status":
                handleStatusCommand(args);
                break;
            case "rm":
                handleRmCommand(args);
                break;
            case "find":
                handleFindCommand(args);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    private static void handleBranchCommand(String[] args) {
        if(args.length != 2){ //
            throw new GitletException("Invalid command.");
        }
        repository.branch(args[1]);
    }

    private static void handleRmBranchCommand(String[] args) {
        if(args.length != 2){ //
            throw new GitletException("Invalid command.");
        }
        repository.rmBranch(args[1]);
    }

    private static void handleFindCommand(String[] args) {
        if(args.length != 2){ //
            throw new GitletException("Invalid command.");
        }
        repository.find(args[1]);
    }

    private static void handleGlobalLogCommand(String[] args) {
        if(args.length != 1){ //
            throw new GitletException("Invalid command.");
        }
        repository.globalLog();
    }

    private static void handleRmCommand(String[] args) {
        if(args.length != 2){ //
            throw new GitletException("Invalid command.");
        }
        repository.rm(args[1]);
    }

    private static void handleStatusCommand(String[] args) {
        if(args.length != 1){ //
            throw new GitletException("Invalid command.");
        }
        repository.status();
    }

    private static void handleLogCommand(String[] args) {
        if(args.length != 1){ //
            throw new GitletException("Invalid command.");
        }
        repository.log();
    }


    private static void handleCommitCommand(String[] args) {
        if(args.length < 2){ //
            throw new GitletException("Invalid command.");
        }
        String commitMessage = args[1];
       repository.commit(commitMessage);

    }


    private static void handleCheckoutCommand(String[] args){

        repository.processCheckoutCommand(args);

    }

    private static void addToStaging(String[] args) {

        if(args.length < 2){
            throw new GitletException("Invalid command.");
        }
        String fileName = args[1];
        repository.addToStaging(fileName);
    }

    public static void initializeRepository(){
        repository.init();
    }


}


