package com.heneria.skinview.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonUtilsTest {

    private static final String SAMPLE = "{\"id\":\"abc\",\"name\":\"Steve\",\"properties\":[{\"name\":\"textures\",\"value\":\"valB64\",\"signature\":\"sig\"}]}";

    @Test
    void findValueAndSignature() {
        assertEquals("valB64", JsonUtils.findFirstValuePropertyBase64(SAMPLE).orElse(null));
        assertEquals("sig", JsonUtils.findFirstValuePropertySignature(SAMPLE).orElse(null));
    }
}
