package de.knollfrank.extensionsformaps.route.protobuf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.List;

import de.knollfrank.extensionsformaps.route.url.TokenProvider;

public class NodesParserTest {

    @Test
    public void testParseNodes_SimpleFlat() {
        // Given
        final List<String> tokens = List.of("1sabc", "2d1.23");

        // When
        final List<Node> nodes = NodesParser.parseNodes(tokens);

        // Then
        assertEquals(2, nodes.size());
        assertEquals("1sabc", nodes.get(0).getToken());
        assertEquals(1, nodes.get(0).fieldId());
        assertEquals(Datatype.STRING, nodes.get(0).datatype());
        assertEquals("2d1.23", nodes.get(1).getToken());
        assertEquals(2, nodes.get(1).fieldId());
        assertEquals(Datatype.DOUBLE, nodes.get(1).datatype());
    }

    @Test
    public void testParseNodes_Nested() {
        // Given: !1m2!2sabc!3d4.56
        final List<String> tokens = List.of("1m2", "2sabc", "3d4.56");

        // When
        final List<Node> nodes = NodesParser.parseNodes(tokens);

        // Then
        assertEquals(1, nodes.size());
        final Node container = nodes.get(0);
        assertEquals("1m2", container.getToken());
        assertEquals(1, container.fieldId());
        assertEquals(Datatype.CONTAINER, container.datatype());
        assertEquals(2, container.children().size());
        assertEquals("2sabc", container.children().get(0).getToken());
        assertEquals("3d4.56", container.children().get(1).getToken());
    }

    @Test
    public void testParseNodes_DeeplyNested() {
        final List<String> tokens = List.of("1m3", "2m2", "3sabc", "4d1.2");

        // When
        final List<Node> nodes = NodesParser.parseNodes(tokens);

        // Then
        assertEquals(1, nodes.size());
        final Node outer = nodes.get(0);
        assertEquals(1, outer.children().size());
        final Node inner = outer.children().get(0);
        assertEquals("2m2", inner.getToken());
        assertEquals(2, inner.children().size());
        assertEquals("3sabc", inner.children().get(0).getToken());
        assertEquals("4d1.2", inner.children().get(1).getToken());
    }

    @Test
    public void testParseNodes_RealWorldFull() {
        // Snippet from GoogleMapsRouteExtractorTest:
        final String dataPart = "!4m22!4m21!1m5!1m4!1s0x4799fc4b13515dd5:0x345201aaff119b3a!8m2!3d48.4765345!4d8.934900899999999!1m5!1m4!1s0x47b161837e1813b9:0x4263df27bd63aa0!8m2!3d53.548828199999996!4d9.987170299999999!1m5!1m4!1s0x4799f35ec85b80b1:0xe432d2a55bc3cd11!8m2!3d48.430628399999996!4d9.2546378!2m1!11b1!3e0";
        final List<String> tokens = TokenProvider.getTokens(dataPart);

        // When
        final List<Node> nodes = NodesParser.parseNodes(tokens);

        // Then
        assertEquals(1, nodes.size());
        final Node root = nodes.get(0);
        assertEquals("4m22", root.getToken());
        assertEquals(1, root.children().size());

        final Node child4m21 = root.children().get(0);
        assertEquals("4m21", child4m21.getToken());
        assertEquals(5, child4m21.children().size());

        final Node child1m5 = child4m21.children().get(0);
        assertEquals("1m5", child1m5.getToken());
        assertEquals(1, child1m5.children().size());

        final Node child1m4 = child1m5.children().get(0);
        assertEquals("1m4", child1m4.getToken());
        assertEquals(2, child1m4.children().size());

        assertEquals("1s0x4799fc4b13515dd5:0x345201aaff119b3a", child1m4.children().get(0).getToken());
        final Node child8m2 = child1m4.children().get(1);
        assertEquals("8m2", child8m2.getToken());
        assertEquals(2, child8m2.children().size());
        assertEquals("3d48.4765345", child8m2.children().get(0).getToken());
        assertEquals("4d8.934900899999999", child8m2.children().get(1).getToken());
    }
}
