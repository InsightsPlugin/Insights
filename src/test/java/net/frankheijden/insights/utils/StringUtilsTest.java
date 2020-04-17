package net.frankheijden.insights.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void isNewVersionRelease() {
        assertTrue(StringUtils.isNewVersion("0.0.0", "0.0.1"));
    }

    @Test
    void isNewVersionReleaseSelf() {
        assertFalse(StringUtils.isNewVersion("0.0.1", "0.0.1"));
    }

    @Test
    void isNewVersionReleaseSmaller() {
        assertFalse(StringUtils.isNewVersion("0.0.1", "0.0.0"));
    }

    @Test
    void isNewVersionMinor() {
        assertTrue(StringUtils.isNewVersion("0.0.1", "0.1.0"));
    }

    @Test
    void isNewVersionMinorSelf() {
        assertFalse(StringUtils.isNewVersion("0.1.0", "0.1.0"));
    }

    @Test
    void isNewVersionMinorSmaller() {
        assertFalse(StringUtils.isNewVersion("0.2.0", "0.1.0"));
    }

    @Test
    void isNewVersionMinorRelease() {
        assertTrue(StringUtils.isNewVersion("0.1.0", "0.1.1"));
    }

    @Test
    void isNewVersionMinorReleaseSmaller() {
        assertFalse(StringUtils.isNewVersion("0.1.1", "0.1.0"));
    }

    @Test
    void isNewVersionMajor() {
        assertTrue(StringUtils.isNewVersion("0.1.0", "1.0.0"));
    }

    @Test
    void isNewVersionMajorSelf() {
        assertFalse(StringUtils.isNewVersion("1.0.0", "1.0.0"));
    }

    @Test
    void isNewVersionMajorRelease() {
        assertTrue(StringUtils.isNewVersion("1.0.0", "1.0.1"));
    }

    @Test
    void isNewVersionMajorReleaseSmaller() {
        assertFalse(StringUtils.isNewVersion("1.0.2", "1.0.1"));
    }

    @Test
    void isNewVersionMajorMinor() {
        assertTrue(StringUtils.isNewVersion("1.0.0", "1.1.0"));
    }

    @Test
    void isNewVersionMajorMinorSmaller() {
        assertFalse(StringUtils.isNewVersion("2.3.0", "1.2.0"));
    }

    @Test
    void isNewVersionReleaseSmaller2() {
        assertFalse(StringUtils.isNewVersion("2.3.0", "2.2.0"));
    }

    @Test
    void isNewVersionMajorMinorRelease() {
        assertTrue(StringUtils.isNewVersion("1.0.0", "1.1.1"));
    }

    @Test
    void isNewVersionMMRSmaller() {
        assertFalse(StringUtils.isNewVersion("2.3.4", "2.3.3"));
    }

    @Test
    void isNewVersionMMRSmaller2() {
        assertFalse(StringUtils.isNewVersion("2.3.4", "2.2.3"));
    }

    @Test
    void isNewVersionMMRSmaller3() {
        assertFalse(StringUtils.isNewVersion("2.3.4", "1.3.3"));
    }
}