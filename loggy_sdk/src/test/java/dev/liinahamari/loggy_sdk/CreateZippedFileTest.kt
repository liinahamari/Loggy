/*
Copyright 2021 liinahamari

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/


package dev.liinahamari.loggy_sdk

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import dev.liinahamari.loggy_sdk.db.Log
import dev.liinahamari.loggy_sdk.db.MyObjectBox
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import dev.liinahamari.loggy_sdk.helper.yellow
import dev.liinahamari.loggy_sdk.rules.ImmediateSchedulersRule
import dev.liinahamari.loggy_sdk.screens.logs.CreateZipLogsFileResult
import dev.liinahamari.loggy_sdk.screens.logs.CreateZippedLogFileUseCase
import dev.liinahamari.loggy_sdk.screens.logs.SHARED_LOGS_DIR_NAME
import dev.liinahamari.loggy_sdk.screens.logs.SHARED_LOGS_ZIP_FILE_NAME
import io.objectbox.Box
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.*
import java.util.zip.ZipInputStream

@Suppress("SpellCheckingInspection")
const val LOREM = """Lorem ipsum dolor sit amet, persius efficiendi sea an, vim te nusquam luptatum dissentias. Fabulas omittam sed an, eirmod facilisis iudicabit ne vis. Ad quo praesent vituperata adversarium, ea mei quot ullamcorper. Usu quis facilisi et.
Omnium mentitum quaestio et eos, feugait nominavi qui an, tamquam praesent id has. At ius eruditi efficiendi, an assum viris instructior pro. Facer inermis honestatis est eu, mazim eirmod copiosae in cum, ei admodum efficiendi quo. Ut omnesque deleniti nominati vel, salutandi scriptorem in usu. Cu causae consectetuer sit, eos ex utroque consulatu. Ut quo bonorum nostrum, mucius definiebas no mea.
Et everti dissentiet cum, nec eu primis pericula. Maiestatis assueverit vis no. Id eos probatus senserit, has tale probo cu. Est mazim doming causae et, vix ex odio mediocrem.
Ea nonumes mentitum ponderum vel, cum paulo scriptorem ea. Tollit tacimates consectetuer ne vis. His omittam nominati iracundia ei, movet complectitur mel ei. Vis no unum maluisset. Perfecto delicata iudicabit vix te, inermis copiosae rationibus mel an, nam te enim lorem.
Et possit labitur eligendi has, no mutat consul theophrastus vel, quo vidit tincidunt ea. Esse inimicus qui ei. Sale tantas moderatius vim id, usu et summo tibique vivendum. Luptatum nominati ei vel. Ea nam augue virtute fabellas, laudem tractatos pri ei, modus ornatus mei eu.
Sed animal laboramus ex. Ei ius lorem iuvaret qualisque, pertinax vulputate ex ius. Quas tincidunt dissentias ex has, vidit pertinacia omittantur an his. No idque utinam vis, eum ut error quodsi sensibus, in vidit iusto perfecto ius. Quis commune an eos, sed ei ferri falli iudicabit. Ad sea solum voluptua percipit.
Nec ut dolores inciderint, usu et atqui nonumy definiebas, id autem illum eleifend mei. Mei an justo error numquam, ne tation aliquam consequat cum, pro in persius erroribus. Ei usu delenit definitionem, ea has saperet dissentiunt. No vix illud utamur laoreet. Pri at solum exerci vulputate, eos epicurei adversarium ne. Vim detraxit sadipscing ei, vel te solum graece.
Mei id consulatu laboramus. Albucius disputationi nec no, graeci democritum te cum, mea cu omnes sapientem. Eam ea partem integre suscipiantur, atqui assentior id quo. An lucilius facilisis mei. Sea duis option eu, facilisis aliquando no per, nec vide aeterno atomorum at. Ius id alii regione, esse animal appareat ut eos. Sed et everti cetero suavitate, mei ei autem graeci moderatius, id labores quaestio vis.
Cu nisl iudico dolorum vix, mei amet noster cu. Ius ex probatus accusata, quis tota placerat cu sed, qui tation omnesque corrumpit ex. Eos eu alii habemus pericula. Ad saperet copiosae qualisque nec.
Ex omnium iuvaret patrioque vis. Ea pri aliquam nonumes comprehensam, cu nam mutat salutatus, ei qui oratio dissentiet. Ad qui summo eruditi. Wisi idque fierent est et, libris epicurei cum ex. Sumo equidem feugait ex vis, commune singulis no sit."""

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class CreateZippedFileTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().context
    private val application = InstrumentationRegistry.getInstrumentation().context.applicationContext as Application
    private lateinit var logBox: Box<Log>

    @get:Rule
    val immediateSchedulersRule = ImmediateSchedulersRule()

    @Before
    fun `setup DB`() {
        Loggy.initForTest(application)
        logBox = MyObjectBox.builder()
            .androidContext(context)
            .build()
            .boxFor(Log::class.java)
    }

    @After
    fun `tear down`() {
        logBox.removeAll()
    }

    @Test
    fun `create zipped file test`() {
        val createZippedLogFileUseCase = CreateZippedLogFileUseCase(logBox)

        val zippedLogsFile = File(File(context.filesDir, SHARED_LOGS_DIR_NAME), SHARED_LOGS_ZIP_FILE_NAME)
        assert(zippedLogsFile.exists().not())

        val title = "some_title"
        val body = LOREM
        val time = System.currentTimeMillis()
        val thread = "some_thread"
        val priority = FlightRecorder.Priority.W
        logBox.put(Log(timestamp = time, title = title, body = body, thread = thread, priority = priority.ordinal))

        createZippedLogFileUseCase.execute(context)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueCount(2)
            .assertValueAt(0, CreateZipLogsFileResult.InProgress)
            .assertValueAt(1) { it is CreateZipLogsFileResult.Success }

        assert(zippedLogsFile.exists())
        assert(zippedLogsFile.length() > 0)

        unzip(zippedLogsFile)
            .readLines()
            .joinToString("\n")
            .also {
                println(it.yellow())

                assert(it.contains(title))
                assert(it.contains(thread))
                assert(it.trim().contains(body.trim()))
                assert(it.contains(priority.toString()))
            }
    }

    private fun unzip(zipFile: File): File {
        val tempFile = File.createTempFile("test", ".tmp")
        ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
            while (zis.nextEntry != null) {
                val outputStream = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var count: Int
                FileOutputStream(tempFile).use { fileOutputStream ->
                    while (zis.read(buffer).also { count = it } != -1) {
                        outputStream.write(buffer, 0, count)
                        val bytes: ByteArray = outputStream.toByteArray()
                        fileOutputStream.write(bytes)
                        outputStream.reset()
                    }
                }
                zis.closeEntry()
            }
        }
        return tempFile
    }
}