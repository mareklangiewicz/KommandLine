package pl.mareklangiewicz.kommand.dbus

import pl.mareklangiewicz.kommand.checkWithUser
import pl.mareklangiewicz.kommand.dbus.DBusRunSession.Option.version
import pl.mareklangiewicz.kommand.gnome.GnomeShell.Option.nested
import pl.mareklangiewicz.kommand.gnome.GnomeShell.Option.wayland
import pl.mareklangiewicz.kommand.gnome.gnomeshell
import kotlin.test.Test


class DBusStuffTests {
    @Test fun testDBusRunSession1() = dbusrunsession { -version }.checkWithUser()
    @Test fun testDBusRunSession2() = dbusrunsession(gnomeshell(nested, wayland)).checkWithUser()
}