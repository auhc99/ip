package duke;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import duke.command.DeadlineCommand;
import duke.command.DeleteCommand;
import duke.command.DoneCommand;
import duke.command.DukeCommand;
import duke.command.EventCommand;
import duke.command.ExitCommand;
import duke.command.FindCommand;
import duke.command.HelpCommand;
import duke.command.InvalidCommand;
import duke.command.ListCommand;
import duke.command.TodoCommand;
import duke.exception.CorruptedFileException;
import duke.exception.DukeException;
import duke.exception.InvalidArgumentsException;
import duke.exception.InvalidTaskException;
import duke.task.Deadline;
import duke.task.Event;
import duke.task.Task;
import duke.task.ToDo;

/**
 * Class to deal with making sense of the user command
 */
public class Parser {
    /** Types of tasks. */
    private enum TaskType {
        TODO,
        DEADLINE,
        EVENT,
    }

    /**
     * Returns the DukeCommand parsed from input.
     *
     * @param input Input to be parsed.
     * @return DukeCommand parsed.
     */
    public static DukeCommand parseInput(String input) {
        try {
            if (input.equals("bye")) {
                return new ExitCommand();
            } else if (input.equals("list")) {
                return new ListCommand();
            } else if (input.equals("help")) {
                return new HelpCommand();
            } else if (input.startsWith("done")) {
                try {
                    // filter out doneXXXX
                    // StringIndexOutOfBoundsException thrown here if input = "done",
                    // which is caught by IndexOutOfBoundsException
                    if (input.charAt(4) != ' ') {
                        throw new InvalidTaskException();
                    }
                    // NumberFormatException thrown here if substring is a invalid integer string
                    int taskNum = Integer.parseInt(input.substring(5)) - 1;
                    return new DoneCommand(taskNum);
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    throw new InvalidArgumentsException();
                }
            } else if (input.startsWith("delete")) {
                try {
                    // filter out deleteXXXX
                    // StringIndexOutOfBoundsException thrown here if input = "delete",
                    // which is caught by IndexOutOfBoundsException
                    if (input.charAt(6) != ' ') {
                        throw new InvalidTaskException();
                    }
                    // NumberFormatException thrown here if substring is a invalid integer string
                    int taskNum = Integer.parseInt(input.substring(7)) - 1;
                    return new DeleteCommand(taskNum);
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    throw new InvalidArgumentsException();
                }
            } else if (input.startsWith("find")) {
                try {
                    // filter out findXXXX
                    // StringIndexOutOfBoundsException thrown here if input = "find",
                    // which is caught by IndexOutOfBoundsException
                    if (input.charAt(4) != ' ') {
                        throw new InvalidTaskException();
                    }
                    // filter out "find "
                    if (input.length() == 5) {
                        throw new InvalidArgumentsException();
                    }
                    String keyWord = input.substring(5);
                    return new FindCommand(keyWord);
                } catch (IndexOutOfBoundsException e) {
                    throw new InvalidArgumentsException();
                }
            } else if (input.startsWith("todo")) {
                return new TodoCommand((ToDo) parseTask(input, TaskType.TODO));
            } else if (input.startsWith("deadline")) {
                return new DeadlineCommand((Deadline) parseTask(input, TaskType.DEADLINE));
            } else if (input.startsWith("event")) {
                return new EventCommand((Event) parseTask(input, TaskType.EVENT));
            } else {
                throw new InvalidTaskException();
            }
        } catch (DukeException e) {
            return new InvalidCommand(e);
        }
    }

    /**
     * Returns the Task parsed from file line.
     *
     * @param line File line to be parsed.
     * @return Task parsed.
     * @throws CorruptedFileException If file line is invalid.
     */
    public static Task parseFileLine(String line) throws CorruptedFileException {
        String[] splitLine = line.split("%,", 4);
        String input;
        TaskType type;
        boolean isDone = Boolean.parseBoolean(splitLine[1]);
        switch (splitLine[0]) {
        case "T":
            input = "todo " + splitLine[2];
            type = TaskType.TODO;
            break;
        case "D":
            input = "deadline " + splitLine[2] + " /by " + splitLine[3];
            type = TaskType.DEADLINE;
            break;
        case "E":
            input = "event " + splitLine[2] + " /at " + splitLine[3];
            type = TaskType.EVENT;
            break;
        default:
            assert false : "Invalid task type";
            throw new CorruptedFileException();
        }
        try {
            Task task = parseTask(input, type);
            if (isDone) {
                task.markDone();
            }
            return task;
        } catch (InvalidArgumentsException | InvalidTaskException e) {
            throw new CorruptedFileException();
        }
    }

    /**
     * Returns the Task parsed from input.
     *
     * @param input Input to be parsed.
     * @param type Type of Task.
     * @return Task parsed.
     * @throws InvalidArgumentsException If arguments are missing or invalid.
     * @throws InvalidTaskException If input contains unknown commands.
     */
    private static Task parseTask(String input, TaskType type) throws InvalidArgumentsException, InvalidTaskException {
        int descriptionEnd;
        String description;
        String dateTime;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy HHmm");
        Task task;
        switch (type) {
        case TODO:
            try {
                // filter out todoXXXX
                // StringIndexOutOfBoundsException thrown here if input = "todo"
                if (input.charAt(4) != ' ') {
                    throw new InvalidTaskException();
                }
                // StringIndexOutOfBoundsException thrown here if input = "todo "
                description = input.substring(5);
                // Checks if description is all whitespace
                if (description.trim().isEmpty()) {
                    throw new InvalidArgumentsException();
                }
            } catch (StringIndexOutOfBoundsException e) {
                throw new InvalidArgumentsException();
            }
            task = new ToDo(description);
            break;
        case DEADLINE:
            try {
                // filter out deadlineXXXX
                // StringIndexOutOfBoundsException thrown here if input = "deadline"
                if (input.charAt(8) != ' ') {
                    throw new InvalidTaskException();
                }
                descriptionEnd = input.indexOf(" /by ");
                // StringIndexOutOfBoundsException thrown here if input = "deadline /by "
                // or if " /by " is not present in input
                description = input.substring(9, descriptionEnd);
                // Checks if description is all whitespace
                if (description.trim().isEmpty()) {
                    throw new InvalidArgumentsException();
                }
                // StringIndexOutOfBoundsException thrown here if input = "deadline $String /by "
                dateTime = input.substring(descriptionEnd + 5);
                // Checks if dateTime is all whitespace
                if (dateTime.trim().isEmpty()) {
                    throw new InvalidArgumentsException();
                }
                task = new Deadline(description, LocalDateTime.parse(dateTime, formatter));
            } catch (StringIndexOutOfBoundsException | DateTimeParseException e) {
                throw new InvalidArgumentsException();
            }

            break;
        case EVENT:
            try {
                // filter out eventXXXX
                // StringIndexOutOfBoundsException thrown here if input = "event"
                if (input.charAt(5) != ' ') {
                    throw new InvalidTaskException();
                }
                descriptionEnd = input.indexOf(" /at ");
                // StringIndexOutOfBoundsException thrown here if input = "event /at"
                // or if " /at " is not present in input
                description = input.substring(6, descriptionEnd);
                // Checks if description is all whitespace
                if (description.trim().isEmpty()) {
                    throw new InvalidArgumentsException();
                }
                // StringIndexOutOfBoundsException thrown here if input = "event $String /at "
                dateTime = input.substring(descriptionEnd + 5);
                // Checks if dateTime is all whitespace
                if (dateTime.trim().isEmpty()) {
                    throw new InvalidArgumentsException();
                }
                task = new Event(description, LocalDateTime.parse(dateTime, formatter));
            } catch (StringIndexOutOfBoundsException | DateTimeParseException e) {
                throw new InvalidArgumentsException();
            }
            break;
        default:
            assert false : "Invalid task type";
            throw new InvalidTaskException();
        }
        return task;
    }
}
