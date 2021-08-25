package duke;

import duke.task.ToDo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ToDoTest {

    @Test
    public void toDo_toString_correct() {
        ToDo t = new ToDo("Test ToDo");
        assertEquals("[T][ ] Test ToDo", t.toString());
    }

    @Test
    public void toDo_formatSave_correct() {
        ToDo t = new ToDo("Test ToDo");
        assertEquals("T%,false%,Test ToDo", t.formatSave());
    }

}