package me.scf37.jugz.solver

import me.scf37.jugz.solver.util.DumbJugsSolver
import org.scalatest.FreeSpec

class DumbJugsSolverTest extends FreeSpec {
  val dumbSolver = new DumbJugsSolver

  "dumb solver behaves as expected" in {
    val cases = Seq(
      // trivial cases
      (1, 1, 1, "1f", 1, 0),
      (2, 2, 2, "1f", 2, 0),
      (1, 2, 1, "1f", 1, 0),
      (2, 1, 1, "2f", 0, 1),
      (2, 3, 1, "2f2t", 2, 1),
      (3, 2, 1, "1f1t", 1, 2),

      // classic problem
      (3, 5, 1, "1f1t1f1t", 1, 5),
      (3, 5, 4, "2f2t1e2t2f2t", 3, 4)
    )

    cases.foreach { case (x, y, z, expectedSolution, expectedVx, expectedVy) =>
      var r = ""
      var lastVx = 0
      var lastVy = 0
      dumbSolver.solve(x, y, z, (jug, action, vx, vy) => {
        r += (jug match {
          case Jug.First => "1"
          case Jug.Second => "2"
        })

        r += (action match {
          case JugAction.Fill => "f"
          case JugAction.Empty => "e"
          case JugAction.Transfer => "t"
        })
        lastVx = vx
        lastVy = vy
      })

      assert(r == expectedSolution)
      assert(lastVx -> lastVy == expectedVx -> expectedVy)
    }
  }

}
