package mobi.sevenwinds.app.author

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.joda.time.DateTime

object AuthorTable : IntIdTable("author") {
    val fullName = text("full_name")
    val createdAt = datetime("created_at")
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var fullName by AuthorTable.fullName
    var createdAt by AuthorTable.createdAt

    fun toResponse(): AuthorResponse {
        return AuthorResponse(id.value, fullName, createdAt)
    }
}

data class AuthorResponse(
    val id: Int,
    val fullName: String,
    val createdAt: DateTime
)