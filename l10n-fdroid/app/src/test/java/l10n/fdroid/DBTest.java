package l10n.fdroid;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import l10n.fdroid.db.DB;

public class DBTest {
    @Test void testDB(){
        DB db = new DB();
        assertTrue(db.connected);
    }
}
