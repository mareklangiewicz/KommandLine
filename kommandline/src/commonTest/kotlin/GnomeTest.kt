import pl.mareklangiewicz.kommand.JournalCtl.Option.cat
import pl.mareklangiewicz.kommand.JournalCtl.Option.follow
import pl.mareklangiewicz.kommand.journalctl
import kotlin.test.Test
import kotlin.test.assertEquals

class GnomeTest {

    @Test
    fun testJournalCtl() {

        val line = journalctl(follow) {
            - cat
            + "/usr/bin/gnome-shell"
        }

        line.println()

        assertEquals("journalctl -f -o cat /usr/bin/gnome-shell", line.line())
    }
}
