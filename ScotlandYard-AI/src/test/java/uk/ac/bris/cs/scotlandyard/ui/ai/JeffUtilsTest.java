package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.Sets;
import org.junit.Test;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.io.IOException;

import static org.junit.Assert.*;

public class JeffUtilsTest {
    Graph<Integer, Transport> graph = StandardGame.standardGraph();

    public JeffUtilsTest() throws IOException { }

    private int distanceBetween(int from, int to) {
        return JeffUtils.generateDistances(graph, Sets.newHashSet(from)).get(to);
    }

    @Test
    public void distanceBetweenTest() {
        assertEquals(distanceBetween(1, 1), 0);
        assertEquals(distanceBetween(128, 140), 1);
        assertEquals(distanceBetween(182, 165), 3);
        assertEquals(distanceBetween(163, 29), 5);
        assertEquals(distanceBetween(119, 199), 3);
    }
}
