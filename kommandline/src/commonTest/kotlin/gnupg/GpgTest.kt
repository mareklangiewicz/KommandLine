package pl.mareklangiewicz.kommand.gnupg

import kotlin.test.*
import kotlin.test.Test
import pl.mareklangiewicz.annotations.DelicateApi
import pl.mareklangiewicz.annotations.NotPortableApi
import pl.mareklangiewicz.interactive.runBlockingWithCLIAndULogOnJvmOnly
import pl.mareklangiewicz.interactive.tryInteractivelyCheckBlockingOrErr
import pl.mareklangiewicz.kground.io.P
import pl.mareklangiewicz.kommand.*
import pl.mareklangiewicz.kommand.core.*
import pl.mareklangiewicz.kommand.gnupg.GpgCmd.*
import pl.mareklangiewicz.kommand.gnupg.GpgOpt.*
import pl.mareklangiewicz.udata.strf


// FIXME NOW: refactor as samples

@OptIn(DelicateApi::class, NotPortableApi::class)
class GpgTest {
  @Test fun testGpgHelp() = gpg { -Help }
    .tryInteractivelyCheckBlockingOrErr("gpg --help")

  @Test fun testGpgListKeys() = gpg(ListPublicKeys)
    .tryInteractivelyCheckBlockingOrErr("gpg --list-public-keys")

  @Test fun testGpgListKeysVerbose() = gpg(ListPublicKeys) { -Verbose }
    .tryInteractivelyCheckBlockingOrErr("gpg --list-public-keys --verbose")

  @Test fun testGpgListSecretKeysVerbose() = gpg(ListSecretKeys) { -Verbose }
    .tryInteractivelyCheckBlockingOrErr("gpg --list-secret-keys --verbose")

  @Suppress("DEPRECATION")
  @Test fun testGpgEncryptDecrypt() {
    runBlockingWithCLIAndULogOnJvmOnly {
      val inFile = mktemp(prefix = "testGED").ax()
      val encFile = "$inFile.enc".P
      val decFile = "$inFile.dec".P
      writeFileWithDD(inLines = listOf("some plain text 667"), outFile = inFile).ax()
      gpgEncryptPass("correct pass", inFile, encFile).ax()
      gpgDecryptPass("correct pass", encFile, decFile).ax()
      val decrypted = readFileWithCat(decFile).ax().single()
      assertEquals("some plain text 667", decrypted)
      val errCode = localCLI().lx(gpgDecryptPass("incorrect pass", encFile, "$decFile.err".P)).waitForExit()
      assertEquals(2, errCode)
      rm { +inFile.strf; +encFile.strf; +decFile.strf }.ax()
    }
  }
}
