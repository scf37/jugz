package me.scf37.jugz.solver

import me.scf37.jugz.solver.util.DumbJugsSolver
import org.scalatest.FreeSpec

import scala.annotation.tailrec

class JugsSolverTest extends FreeSpec {
  val solver = new JugsSolver
  val dumbSolver = new DumbJugsSolver

  "diophantine solver behaves as expected" in {
    val cases = Seq(
      // ax + by = c
      // a, b, c, x, y

      // trivial cases
      (1, 1, 1, 0, 1),
      (1, 1, 2, 0, 2),

      // classic problem
      (3, 5, 1, 2, -1),
      (3, 5, 4, 3, -1),

      //classic problem - inverted
      (5, 3, 1, -1, 2),
      (5, 3, 4, -1, 3),

      // gcd != 1
      (10, 6, 2, -1, 2),
      (10, 18, 6, -3, 2),

      // extreme cases
      (Int.MaxValue, Int.MaxValue, Int.MaxValue, 0, 1),
      (Int.MaxValue, Int.MaxValue - 2, 42, 21, -21),
      (Int.MaxValue, Int.MaxValue - 2, Int.MaxValue - 3, 1073741822, -1073741822),
      (2000000000, 2000000001, 12345667, -12345667, 12345667)

    )

    cases.foreach { case (a, b, c, x, y) =>
      val solution = solver.solveDiophantine(a, b, c)
      val expected = if (a < 0) None else Some(x -> y)
      assert(solution == expected, s"${a}x + ${b}y = $c")
    }
  }

  "extended gcd calculation behaves as expected" in {
    val cases = Seq(
      // a, b -> Bezout koeffs, i.e. ax + by = gcd(a, b)

      // trivial cases
      (1, 1, 0, 1),
      (1, 2, 1, 0),
      (3, 3, 0, 1),

      // large argument
      (1, 99, 1, 0),

      // non-trivial cases
      (2, 3, -1, 1),

      // gcd != ab
      (4, 6, -1, 1),

      // extreme cases
      (1, Int.MaxValue, 1, 0),
      (Int.MaxValue, Int.MaxValue, 0, 1),
      (Int.MaxValue, Int.MaxValue - 1, 1, -1),
      (Int.MaxValue, Int.MaxValue - 2, -1073741822, 1073741823), // gcd=1
      (Int.MaxValue - 1, Int.MaxValue - 3, 1, -1) // gcd=2
    )
    cases.foreach { case (a, b, x, y) =>
      val solution = solver.extendedGcd(a, b)
      val expected = x -> y
      assert(solution == expected, s"$a, $b")

      if (a != b) {
        val solution2 = solver.extendedGcd(b, a)
        val expected2 = y -> x
        assert(solution2 == expected2, s"$b, $a")
      }
    }
  }

  "verify solution length calculation" in {
    everyTask(100) { case (x, y, z) =>
      val solutionLen = dumbSolver.solveCount(x, y, z)
      val s = solver.solve(x, y, z).get

      assert(s.count == solutionLen)
    }
  }

  "verify solution length calculation (2)" in {
    val (x, y, z) = (2000000000, 2000000001, 12345667)
    val solutionLen = dumbSolver.solveCount(x, y, z)
    val s = solver.solve(x, y, z).get

    assert(s.count == solutionLen)

  }

  "verify State implementation" in {

    everyTask(100) { case (x, y, z) =>
      val s = solver.solve(x, y, z).get

      var state = State(0, 0)

      dumbSolver.solve(x, y, z, (jug, action, vx, vy) => {
        val (s1, a) = state.nextState(x, y, z, s.jugToFill)

        assert(a == action)
        assert(s1.vx -> s1.vy == vx -> vy)

        state = s1
      })

    }
  }

  private def everyTask(max: Int)(f: (Int, Int, Int) => Unit): Unit = {
    for (x <- 1 to max) {
      for (y <- x to max) {
        val z1 = gcd(x, y)
        val z2 = gcd(x, y) * 4

        if (z1 <= Math.max(x, y)) {
          f(x, y, z1)
        }

        if (z2 <= Math.max(x, y)) {
          f(x, y, z2)
        }
      }
    }
  }


  @tailrec
  private def gcd(a: Int,b: Int): Int = {
    if(b == 0) a else gcd(b, a%b)
  }
}
