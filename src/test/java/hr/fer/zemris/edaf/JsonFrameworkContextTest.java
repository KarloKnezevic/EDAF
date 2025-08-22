package hr.fer.zemris.edaf;

import hr.fer.zemris.edaf.json.JsonFrameworkContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

public class JsonFrameworkContextTest {

    @Test
    public void testJsonFrameworkContext() throws IOException {
        IFrameworkContext context = new JsonFrameworkContext("EDAFParametersB.json");

        assertNotNull(context);
        assertEquals("pbil", context.getAlgorithmName());
        assertEquals("coco.TEST", context.getWorkEnvironment());
        assertEquals("B", context.getGenotype());
        assertEquals(50, context.getPopulationSize());
    }
}
