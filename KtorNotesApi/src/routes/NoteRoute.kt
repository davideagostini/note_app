package com.androiddev.routes

import com.androiddev.data.*
import com.androiddev.data.collections.Note
import com.androiddev.data.requests.AddOwnerRequest
import com.androiddev.data.responses.SimpleResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.noteRoutes() {
    route("/notes") {
        authenticate {
            get {
                val email = call.principal<UserIdPrincipal>()!!.name
                val notes = getNotesForUser(email)
                call.respond(OK, notes)
            }
        }
    }
    route("/add-owner") {
        authenticate {
            post {
                val request = try {
                    call.receive<AddOwnerRequest>()
                } catch(e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if(!checkIfUserExists(request.owner)) {
                    call.respond(
                        OK,
                        SimpleResponse(false, "No user with this E-Mail exists")
                    )
                    return@post
                }
                if(isOwnerOfNote(request.noteID, request.owner)) {
                    call.respond(
                        OK,
                        SimpleResponse(false, "This user is already an owner of this note")
                    )
                    return@post
                }
                if(addOwnerToNote(request.noteID, request.owner)) {
                    call.respond(
                        OK,
                        SimpleResponse(true, "${request.owner} can now see this note")
                    )
                } else {
                    call.respond(Conflict)
                }
            }
        }
    }
    route("/note") {
        authenticate {
            post {
                val note = try {
                    call.receive<Note>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if(saveNote(note)) {
                    call.respond(OK)
                } else {
                    call.respond(Conflict)
                }
            }
        }
    }
    route("/note") {
        authenticate {
            delete("/{id}") {
                val email = call.principal<UserIdPrincipal>()!!.name
                val noteId = call.parameters["id"] ?: return@delete
                if(deleteNoteForUser(email, noteId)) {
                    call.respond(OK)
                } else {
                    call.respond(Conflict)
                }
            }
        }
    }
}