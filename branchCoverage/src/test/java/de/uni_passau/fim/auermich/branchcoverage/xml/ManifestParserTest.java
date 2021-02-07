package de.uni_passau.fim.auermich.branchcoverage.xml;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class ManifestParserTest {

    @Test
    public void testValidManifest() {

        Path resourceDirectory = Paths.get("src","test","resources");
        Path manifestFile = resourceDirectory.resolve("AndroidManifest1.xml");
        ManifestParser manifest = new ManifestParser(manifestFile.toString());

        assertTrue("Couldn't parse manifest properly!", manifest.parseManifest());
        assertNotNull("Couldn't parse MainActivity!", manifest.getMainActivity());
        assertNotNull("Couldn't parse PackageName!", manifest.getPackageName());
    }

    /**
     * This test fails because the supplied AndroidManifest.xml contains some corruption
     * not yet identified. It seems like the main activity couldn't be extracted.
     */
    @Test
    public void testFaultyManifest() {

        Path resourceDirectory = Paths.get("src","test","resources");
        Path manifestFile = resourceDirectory.resolve("AndroidManifest2.xml");
        ManifestParser manifest = new ManifestParser(manifestFile.toString());

        // the parsing fails for the supplied AndroidManifest.xml
        assertFalse(manifest.parseManifest());
        manifest.getPackageName();
        manifest.getMainActivity();
    }
}
