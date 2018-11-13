package me.scf37.jugz.controller

import com.google.inject.Inject
import com.twitter.util.Future
import me.scf37.jugz.controller.model.Puzzle
import me.scf37.jugz.controller.model.PuzzleSolution
import me.scf37.jugz.controller.model.PuzzleState
import me.scf37.jugz.controller.model.PuzzleStateRequest
import me.scf37.jugz.controller.model.PuzzleStateResponse
import me.scf37.jugz.logging.Logging
import me.scf37.jugz.rest.JsonHelper
import me.scf37.jugz.rest.RestRespond
import me.scf37.jugz.rest.Route
import me.scf37.jugz.rest.exception.JugNoSolutionException
import me.scf37.jugz.rest.exception.ValidationException
import me.scf37.jugz.service.PuzzleService
import me.scf37.jugz.solver.Jug
import me.scf37.jugz.solver.JugAction

class ApiEndpoint @Inject() (service: PuzzleService) extends RestRespond with Logging {
  import Route._

  private val solve: Route = post("/v1/jugs/solve"){ req =>
      logAuditCallback(Some(req), "solve") {

        val p = JsonHelper.parseJson[Puzzle](req.contentString)
        val validationErrors = p.validate()
        if (validationErrors.nonEmpty) {
          throw new ValidationException(validationErrors.mkString(", "))
        }

        service.solve(p.x, p.y, p.z) match {
          case None =>
            Future exception new JugNoSolutionException

          case Some(solution) =>
            respondOk(PuzzleSolution(
              count = solution.count,
              firstJug = solution.jugToFill == Jug.First
            ))
        }
      }
    }

  private val next: Route = post("/v1/jugs/next") { req =>
    logAuditCallback(Some(req), "next") {
      val r = JsonHelper.parseJson[PuzzleStateRequest](req.contentString)
      val validationErrors = r.validate()
      if (validationErrors.nonEmpty) {
        throw new ValidationException(validationErrors.mkString(", "))
      }

      val fillJug = if (r.firstJug) Jug.First else Jug.Second

      val (nextState, action) = service.next(x = r.x, y = r.y, z = r.z, vx = r.state.vx, vy = r.state.vy,
        fillJug = fillJug)

      val result = PuzzleStateResponse(
        nextState = PuzzleState(
          vx = nextState.vx,
          vy = nextState.vy
        ),
        action = action match {
          case JugAction.Fill => "Fill " + jugToString(fillJug)
          case JugAction.Empty => "Empty " + jugToString(fillJug.otherJug)
          case JugAction.Transfer => "Transfer from " + jugToString(fillJug) + " to " + jugToString(fillJug.otherJug)
        }
      )

      respondOk(result)
    }
  }

  private def jugToString(jug: Jug): String = jug match {
    case Jug.First => "Jug X"
    case Jug.Second => "Jug Y"
  }

  val endpoint = solve.andThen(next)
}
