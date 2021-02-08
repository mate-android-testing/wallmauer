package de.uni_passau.fim.auermich.branchcoverage.xml;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class ManifestParserTest {

    /**
     * Tests a sample manifest.
     */
    @Test
    public void testSampleManifest() {

        Path resourceDirectory = Paths.get("src","test","resources");
        Path manifestFile = resourceDirectory.resolve("AndroidManifest1.xml");
        ManifestParser manifest = new ManifestParser(manifestFile.toString());

        assertTrue("Couldn't parse manifest properly!", manifest.parseManifest());
        assertNotNull("Couldn't parse MainActivity!", manifest.getMainActivity());
        assertNotNull("Couldn't parse PackageName!", manifest.getPackageName());
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

        assertTrue("Couldn't parse manifest properly!", manifest.parseManifest());
        assertNotNull("Couldn't parse MainActivity!", manifest.getMainActivity());
        assertNotNull("Couldn't parse PackageName!", manifest.getPackageName());
    }
}
