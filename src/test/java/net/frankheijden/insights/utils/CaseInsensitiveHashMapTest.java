package net.frankheijden.insights.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CaseInsensitiveHashMapTest {

    @Test
    void testKeySet() {
        CaseInsensitiveHashMap<Integer> map = new CaseInsensitiveHashMap<>();
        map.put("key", 1);
        map.put("aBc", 2);

        Set<String> keySet = map.keySet();
        assertThat(keySet).containsExactlyInAnyOrder("KEY", "ABC");
        assertTrue(keySet.contains("key"));
        assertTrue(keySet.containsAll(Arrays.asList("kEy", "abC")));
    }

}