package io.github.sheikah45.fx2j.processor.internal.resolve;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NameResolverTest extends AbstractResolverTest {

    private final NameResolver nameResolver = resolverContainer.getNameResolver();

    @Test
    void testResolveUniqueName() {
        assertEquals("object0", nameResolver.resolveUniqueName(Object.class));
        assertEquals("object1", nameResolver.resolveUniqueName(Object.class));
        assertEquals("int0", nameResolver.resolveUniqueName(int.class));
        assertEquals(Object.class, nameResolver.resolveTypeById("object0"));
        assertEquals(Object.class, nameResolver.resolveTypeById("object1"));
        assertEquals(int.class, nameResolver.resolveTypeById("int0"));
    }

    @Test
    void testStoreIdType() {
        nameResolver.storeIdType("obj", Object.class);
        assertEquals(Object.class, nameResolver.resolveTypeById("obj"));
        assertThrows(IllegalArgumentException.class, () -> nameResolver.storeIdType("obj", Object.class));
    }

    @Test
    void testResolveTypeByIdFails() {
        assertThrows(IllegalArgumentException.class, () -> nameResolver.resolveTypeById("obj"));
    }
}
