package pl.edu.pja.kdudek

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

@Serializable
data class ExposedNote(
    val id: Int,
    val title: String,
    val content: String
)

class NoteService(database: Database) {
    object Notes : Table() {
        val id = integer("id").autoIncrement()
        val title = varchar("title", length = 50)
        val content = text("content")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Notes)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(note: ExposedNote): Int = dbQuery {
        Notes.insert {
            it[Notes.title] = note.title
            it[Notes.content] = note.content
        }[Notes.id]
    }

    suspend fun all(): List<ExposedNote> = dbQuery {
        Notes.selectAll()
            .map {
                ExposedNote(
                    id = it[Notes.id],
                    title = it[Notes.title],
                    content = it[Notes.content]
                )
            }
    }

    suspend fun read(id: Int): ExposedNote? = dbQuery {
        Notes.selectAll()
            .where { Notes.id eq id }
            .map {
                ExposedNote(
                    id = it[Notes.id],
                    title = it[Notes.title],
                    content = it[Notes.content]
                )
            }
            .singleOrNull()
    }


    suspend fun update(id: Int, note: ExposedNote) = dbQuery {
        Notes.update({ Notes.id eq id }) {
            it[title] = note.title
            it[content] = note.content
        }
    }


    suspend fun delete(id: Int) = dbQuery {
        Notes.deleteWhere { Notes.id eq id }
    }

}

