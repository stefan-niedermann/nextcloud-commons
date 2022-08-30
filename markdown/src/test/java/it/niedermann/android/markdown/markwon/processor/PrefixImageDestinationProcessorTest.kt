package it.niedermann.android.markdown.markwon.processor

import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PrefixImageDestinationProcessorTest : TestCase() {
    private lateinit var processor: PrefixImageDestinationProcessor

    @Before
    fun reset() {
        processor = PrefixImageDestinationProcessor()
    }

    @Test
    fun `should not prepend prefix when destination has valid scheme`() {
        processor.setPrefix("example.com?url=")

        assertEquals("http://bar", processor.process("http://bar"))
        assertEquals("https://bar", processor.process("https://bar"))
        assertEquals("ftp://bar", processor.process("ftp://bar"))
        assertEquals("ftps://bar", processor.process("ftps://bar"))
        assertEquals("sftp://bar", processor.process("sftp://bar"))
    }

    @Test
    fun `should prepend prefix when destination is relative path`() {
        processor.setPrefix("example.com?url=")
        assertEquals("example.com?url=bar", processor.process("bar"))
    }

    @Test
    fun `should prepend prefix when destination is absolute path`() {
        processor.setPrefix("example.com?url=")
        assertEquals("example.com?url=/bar", processor.process("/bar"))
    }

    @Test
    fun `should not touch the actual destination when prepending a prefix`() {
        processor.setPrefix("example.com?url=")
        assertEquals("example.com?url= /%20 & strange values", processor.process(" /%20 & strange values"))
    }
}