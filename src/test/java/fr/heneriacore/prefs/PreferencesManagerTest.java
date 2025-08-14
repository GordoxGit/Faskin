package fr.heneriacore.prefs;

import fr.heneriacore.db.SQLiteManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PreferencesManagerTest {
    private SQLiteManager sqlite;

    @AfterEach
    public void cleanup() {
        if (sqlite != null) {
            sqlite.close();
        }
    }

    @Test
    public void testOptOutToggle() throws Exception {
        sqlite = new SQLiteManager(1);
        File tmp = File.createTempFile("prefs",".db");
        sqlite.init(tmp);
        PreferencesManager pm = new PreferencesManager(sqlite, false, true);
        UUID u = UUID.randomUUID();
        assertFalse(pm.isOptedOut(u).get());
        pm.setOptOut(u, true).get();
        assertTrue(pm.isOptedOut(u).get());
        pm.setOptOut(u, false).get();
        assertFalse(pm.isOptedOut(u).get());
        tmp.delete();
    }
}
