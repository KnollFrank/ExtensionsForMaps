package de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.List;

public class NodeParserTest {

    // 11b1 3d48.4765345 1sabc 1s0x4799fc4b13515dd5:0x345201aaff119b3a
    @Test
    public void testParseNodeWithoutChildren() {
        // Given
        final String token = "11b1";

        // When
        final Node node = NodeParser.parseNodeWithoutChildren(token);

        // Then
        assertEquals(
                new Node(11, Datatype.BOOLEAN, "1", List.of()),
                node);
    }
}