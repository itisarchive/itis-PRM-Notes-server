package pl.edu.pja.kdudek

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.Database


fun Application.configureDatabases() {
    val database = Database.connect(
        url = "jdbc:h2:./test;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE",
        user = "root",
        driver = "org.h2.Driver",
        password = ""
    )

    val noteService = NoteService(database)
    routing {
        get("/notes/samples") {
            (1..10).forEach { i ->
                noteService.create(ExposedNote(
                    id = 0,
                    title = "Sample Note $i",
                    content = """
                        Lorem ipsum dolor sit amet, consectetur adipiscing elit.
                        Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
                        Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
                        Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
                        Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
                    """.trimIndent()))
            }
            call.respond(HttpStatusCode.Created, "Sample notes created successfully")
        }

        get("/notes") {
            val notes = noteService.all()
            call.respond(HttpStatusCode.OK, notes)
        }

        post("/notes") {
            val note = call.receive<ExposedNote>()
            val id = noteService.create(note)
            call.respond(HttpStatusCode.Created, id)
        }

        get("/notes/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val note = noteService.read(id)
            if (note != null) {
                call.respond(HttpStatusCode.OK, note)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        put("/notes/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val note = call.receive<ExposedNote>()
            noteService.update(id, note)
            call.respond(HttpStatusCode.OK)
        }

        delete("/notes/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            noteService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
