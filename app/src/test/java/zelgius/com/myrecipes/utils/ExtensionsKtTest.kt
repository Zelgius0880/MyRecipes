package zelgius.com.myrecipes.utils

import org.junit.Assert.*
import org.junit.Test
import zelgius.com.myrecipes.entities.Ingredient

class ExtensionsKtTest{
    @Test
    fun extAnyAsMap(){
        val i = Ingredient("1234", "Name", "Image")

        val map = i.asMap()
        assertFalse(map.isEmpty())

        val fields = listOf("key", "name", "imageURL")
        val values = listOf("1234", "Name", "Image")
        map.forEach { t, u ->
            assertTrue(fields.contains(t))
            assertTrue(values.contains(u))
        }
    }
}