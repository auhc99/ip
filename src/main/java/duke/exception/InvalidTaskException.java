package duke.exception;

public class InvalidTaskException extends DukeException{
    public InvalidTaskException() {
        super("Invalid Command");
    }
}