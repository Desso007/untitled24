import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Task1Test {

    @Test
    void testMyClass() throws Exception {
        RandomObjectGenerator rog = new RandomObjectGenerator();

        MyClass myClass = rog.nextObject(MyClass.class, "create");

        assertNotNull(myClass);
        assertEquals(42, myClass.getIntValue());
    }


}
