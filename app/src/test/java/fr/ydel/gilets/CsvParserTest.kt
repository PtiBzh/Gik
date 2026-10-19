package fr.ydel.gilets

import fr.ydel.gilets.util.CsvParser
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class CsvParserTest {

    @Test
    fun testParseValidCsv() {
        val csvContent = """Modèle;ID canonique;ID club;Test;Lest;Flotta (N);Constat
Gilet Adulte;FAB001;23-001;;;200,0;
Gilet Adulte;FAB002;23-002;;;200,0;
Gilet Enfant;FAB006;23-006;;;150,0;"""

        val inputStream = ByteArrayInputStream(csvContent.toByteArray(Charsets.UTF_8))
        val gilets = CsvParser.parseCampaignCsv(inputStream)

        assertEquals(3, gilets.size)
        assertEquals("Gilet Adulte", gilets[0].modele)
        assertEquals("FAB001", gilets[0].idCanonique)
        assertEquals("23-001", gilets[0].idClub)
        assertEquals(200.0, gilets[0].flottabiliteNominale, 0.01)
        
        assertEquals("Gilet Enfant", gilets[2].modele)
        assertEquals(150.0, gilets[2].flottabiliteNominale, 0.01)
    }

    @Test
    fun testParseCsvWithDecimalComma() {
        val csvContent = """Modèle;ID canonique;ID club;Test;Lest;Flotta (N);Constat
Gilet Adulte;FAB001;23-001;;;200,5;"""

        val inputStream = ByteArrayInputStream(csvContent.toByteArray(Charsets.UTF_8))
        val gilets = CsvParser.parseCampaignCsv(inputStream)

        assertEquals(1, gilets.size)
        assertEquals(200.5, gilets[0].flottabiliteNominale, 0.01)
    }

    @Test(expected = fr.ydel.gilets.util.CsvParseException::class)
    fun testParseInvalidCsvTooFewColumns() {
        val csvContent = """Modèle;ID canonique;ID club"""

        val inputStream = ByteArrayInputStream(csvContent.toByteArray(Charsets.UTF_8))
        CsvParser.parseCampaignCsv(inputStream)
    }

    @Test(expected = fr.ydel.gilets.util.CsvParseException::class)
    fun testParseInvalidCsvNonNumeric() {
        val csvContent = """Modèle;ID canonique;ID club;Test;Lest;Flotta (N);Constat
Gilet Adulte;FAB001;23-001;;;abc;"""

        val inputStream = ByteArrayInputStream(csvContent.toByteArray(Charsets.UTF_8))
        CsvParser.parseCampaignCsv(inputStream)
    }

    @Test(expected = fr.ydel.gilets.util.CsvParseException::class)
    fun testParseEmptyCsv() {
        val csvContent = ""

        val inputStream = ByteArrayInputStream(csvContent.toByteArray(Charsets.UTF_8))
        CsvParser.parseCampaignCsv(inputStream)
    }

    @Test
    fun testParseCsvWithComments() {
        val csvContent = """# This is a comment
Modèle;ID canonique;ID club;Test;Lest;Flotta (N);Constat
Gilet Adulte;FAB001;23-001;;;200,0;
# Another comment
Gilet Adulte;FAB002;23-002;;;200,0;"""

        val inputStream = ByteArrayInputStream(csvContent.toByteArray(Charsets.UTF_8))
        val gilets = CsvParser.parseCampaignCsv(inputStream)

        assertEquals(2, gilets.size)
    }

    @Test
    fun testParseCsvWithExtraColumns() {
        val csvContent = """Modèle;ID canonique;ID club;Test;Lest;Flotta (N);Constat;Extra1;Extra2
Gilet Adulte;FAB001;23-001;test;11.0;200,0;OK;extra1;extra2"""

        val inputStream = ByteArrayInputStream(csvContent.toByteArray(Charsets.UTF_8))
        val gilets = CsvParser.parseCampaignCsv(inputStream)

        assertEquals(1, gilets.size)
        assertEquals("Gilet Adulte", gilets[0].modele)
        assertEquals("FAB001", gilets[0].idCanonique)
        assertEquals("23-001", gilets[0].idClub)
        assertEquals("test", gilets[0].test)
        assertEquals(11.0, gilets[0].lest, 0.01)
        assertEquals(200.0, gilets[0].flottabiliteNominale, 0.01)
    }
}
