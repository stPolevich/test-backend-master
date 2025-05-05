package mobi.sevenwinds.app.author

import io.restassured.RestAssured
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.joda.time.DateTime

class AuthorApiTest : ServerTest() {

    @BeforeEach
    internal fun setUp() {
        transaction { AuthorTable.deleteAll() }
    }

    @Test
    fun testCreateAuthor() {
        val request = CreateAuthorRequest(fullName = "Андрей Андрей")

        RestAssured.given()
            .jsonBody(request)
            .post("/author/add")
            .toResponse<AuthorResponse>().let { response ->
                println(response)

                Assert.assertTrue(response.id > 0)
                Assert.assertEquals(request.fullName, response.fullName)
                Assert.assertTrue(response.createdAt.isBeforeNow)
            }
    }



    @Test
    fun testCreatedAtField() {
        val beforeTest = DateTime.now().minusSeconds(1)

        addAuthor("Михаил Михаил").let { response ->
            Assert.assertTrue(response.createdAt.isAfter(beforeTest))
            Assert.assertTrue(response.createdAt.isBeforeNow)
        }
    }

    private fun addAuthor(fullName: String): AuthorResponse {
        return RestAssured.given()
            .jsonBody(CreateAuthorRequest(fullName = fullName))
            .post("/author/add")
            .toResponse<AuthorResponse>().also { response ->
                Assert.assertEquals(fullName, response.fullName)
            }
    }
}