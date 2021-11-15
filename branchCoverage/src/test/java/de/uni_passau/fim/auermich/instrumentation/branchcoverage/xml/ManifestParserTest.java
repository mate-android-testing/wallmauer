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
        assertEquals("bbc.mobile.news.v3.app.TopLevelActivity", manifest.getMainActivity());
        assertEquals("bbc.mobile.news.ww", manifest.getPackageName());
    }

    /**
     * Tests a manifest where the main activity is defined through an activity-alias tag.
     */
    @Test
    public void testActivityAliasManifest() {

        Path resourceDirectory = Paths.get("src","test","resources");
        Path manifestFile = resourceDirectory.resolve("AndroidManifest2.xml");
        ManifestParser manifest = new ManifestParser(manifestFile.toString());

        assertTrue(manifest.parseManifest(), "Couldn't parse manifest properly!");
        assertNotNull(manifest.getMainActivity(), "Couldn't parse MainActivity!");
        assertNotNull(manifest.getPackageName(), "Couldn't parse PackageName!");
        assertEquals("com.simplemobiletools.calendar.activities.SplashActivity", manifest.getMainActivity());
        assertEquals("com.simplemobiletools.calendar", manifest.getPackageName());
    }

    /**
     * Tests a manifest where the main activity is defined through an activity-alias tag.
     */
    @Test
    public void testActivityAliasManifest2() {

        Path resourceDirectory = Paths.get("src","test","resources");
        Path manifestFile = resourceDirectory.resolve("AndroidManifest3.xml");
        ManifestParser manifest = new ManifestParser(manifestFile.toString());

        assertTrue(manifest.parseManifest(), "Couldn't parse manifest properly!");
        assertNotNull(manifest.getMainActivity(), "Couldn't parse MainActivity!");
        assertNotNull(manifest.getPackageName(), "Couldn't parse PackageName!");
        assertEquals("de.schildbach.wallet.ui.WalletActivity", manifest.getMainActivity());
        assertEquals("hashengineering.groestlcoin.wallet_test", manifest.getPackageName());
    }
}
