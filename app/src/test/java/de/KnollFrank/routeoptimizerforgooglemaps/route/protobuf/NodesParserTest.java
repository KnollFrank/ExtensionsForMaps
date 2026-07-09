package de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.List;

public class NodesParserTest {

    @Test
    public void testParseAllNodes_SimpleFlat() {
        // Given
        final List<String> tokens = List.of("1sabc", "2d1.23");

        // When
        final List<Node> nodes = NodesParser.parseAllNodes(tokens);

        // Then
        assertEquals(2, nodes.size());
        assertEquals("1sabc", nodes.get(0).getToken());
        assertEquals(1, nodes.get(0).fieldId);
        assertEquals('s', nodes.get(0).dataType);
        assertEquals("2d1.23", nodes.get(1).getToken());
        assertEquals(2, nodes.get(1).fieldId);
        assertEquals('d', nodes.get(1).dataType);
    }

    @Test
    public void testParseAllNodes_Nested() {
        // Given: !1m2!2sabc!3d4.56
        final List<String> tokens = List.of("1m2", "2sabc", "3d4.56");

        // When
        final List<Node> nodes = NodesParser.parseAllNodes(tokens);

        // Then
        assertEquals(1, nodes.size());
        final Node container = nodes.get(0);
        assertEquals("1m2", container.getToken());
        assertEquals(1, container.fieldId);
        assertEquals('m', container.dataType);
        assertEquals(2, container.children.size());
        assertEquals("2sabc", container.children.get(0).getToken());
        assertEquals("3d4.56", container.children.get(1).getToken());
    }

    @Test
    public void testParseAllNodes_DeeplyNested() {
        final List<String> tokens = List.of("1m3", "2m2", "3sabc", "4d1.2");

        // When
        final List<Node> nodes = NodesParser.parseAllNodes(tokens);

        // Then
        assertEquals(1, nodes.size());
        final Node outer = nodes.get(0);
        assertEquals(1, outer.children.size());
        final Node inner = outer.children.get(0);
        assertEquals("2m2", inner.getToken());
        assertEquals(2, inner.children.size());
        assertEquals("3sabc", inner.children.get(0).getToken());
        assertEquals("4d1.2", inner.children.get(1).getToken());
    }

    @Test
    public void testParseAllNodes_RealWorldFull() {
        // Snippet from GoogleMapsRouteExtractorTest:
        // !4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999
        final List<String> tokens =
                List.of("4m22", "4m21", "1m5", "1m4", "1s0x4799fc4b13515dd5:0x345201aaff119b3a", "8m2", "3d48.4765345", "4d8.934900899999999");

        // When
        final List<Node> nodes = NodesParser.parseAllNodes(tokens);

        // Then
        assertEquals(1, nodes.size());
        final Node root = nodes.get(0);
        assertEquals("4m22", root.getToken());
        assertEquals(1, root.children.size());

        final Node child4m21 = root.children.get(0);
        assertEquals("4m21", child4m21.getToken());
        assertEquals(1, child4m21.children.size());

        final Node child1m5 = child4m21.children.get(0);
        assertEquals("1m5", child1m5.getToken());
        assertEquals(1, child1m5.children.size());

        final Node child1m4 = child1m5.children.get(0);
        assertEquals("1m4", child1m4.getToken());
        assertEquals(2, child1m4.children.size());

        assertEquals("1s0x4799fc4b13515dd5:0x345201aaff119b3a", child1m4.children.get(0).getToken());
        final Node child8m2 = child1m4.children.get(1);
        assertEquals("8m2", child8m2.getToken());
        assertEquals(2, child8m2.children.size());
        assertEquals("3d48.4765345", child8m2.children.get(0).getToken());
        assertEquals("4d8.934900899999999", child8m2.children.get(1).getToken());
    }
}
