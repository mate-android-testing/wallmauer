package de.uni_passau.fim.auermich.instrumentation.branchcoverage.xml;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class ManifestParserTest {

    /**
     * Tests a sample manifest.
     */
    @Test
    public void testSampleManifest() {

        Path resourceDirectory = Paths.get("src","test","resources");
        Path manifestFile = resourceDirectory.resolve("AndroidManifest1.xml");
        ManifestParser manifest = new ManifestParser(manifestFile.toString());

        assertTrue(manifest.parseManifest(), "Couldn't parse manifest properly!");
        assertNotNull(manifest.getMainActivity(), "Couldn't parse MainActivity!");
        assertNotNull(manifest.getPackageName(), "Couldn't parse PackageName!");
        assertEquals(manifest.getMainActivity(), "bbc.mobile.news.v3.app.TopLevelActivity");
        assertEquals(manifest.getPackageName(), "bbc.mobile.news.ww");
    }

    /**
     * Tests a manifest where the main activity is defined through an
     * activity-alias tag.
     */
    @Test
    public void testActivityAliasManifest() {

        Path resourceDirectory = Paths.get("src","test","resources");
        Path manifestFile = resourceDirectory.resolve("AndroidManifest2.xml");
        ManifestParser manifest = new ManifestParser(manifestFile.toString());

        assertTrue(manifest.parseManifest(), "Couldn't parse manifest properly!");
        assertNotNull(manifest.getMainActivity(), "Couldn't parse MainActivity!");
        assertNotNull(manifest.getPackageName(), "Couldn't parse PackageName!");
        assertEquals(manifest.getMainActivity(), "com.simplemobiletools.calendar.activities.SplashActivity");
        assertEquals(manifest.getPackageName(), "com.simplemobiletools.calendar");
    }
}
