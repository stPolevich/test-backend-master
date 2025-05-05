package mobi.sevenwinds.app.author

import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

data class CreateAuthorRequest(val fullName: String)

fun NormalOpenAPIRoute.author() {
    route("/author") {
        route("/add").post<Unit, AuthorResponse, CreateAuthorRequest>(info("Создать автора")) { _, body ->
            val author = transaction {
                AuthorEntity.new {
                    fullName = body.fullName
                    createdAt = DateTime.now()
                }.toResponse()
            }
            respond(author)
        }
    }
}