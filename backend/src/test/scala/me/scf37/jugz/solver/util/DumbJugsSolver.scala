package me.scf37.jugz.solver.util

import me.scf37.jugz.solver.Jug
import me.scf37.jugz.solver.JugAction

/**
  * inefficient but correct solution
  *
  */
class DumbJugsSolver {

  /**
    * Count of operations required
    *
    * @param x volume of jug X
    * @param y volume of jug X
    * @param z amount to measure
    * @return
    */
  def solveCount(x: Int, y: Int, z: Int): Int = {
    val solution1 = buildDumb(x, y, Jug.First, z, (_, _, _, _) => ())
    val solution2 = buildDumb(y, x, Jug.Second, z, (_, _, _, _) => ())

    Math.min(solution1, solution2)
  }

  /**
    * Solve jugs puzzle
    *
    * @param x volume of jug X
    * @param y volume of jug X
    * @param z amount to measure
    * @param action callback for every action
    */

  def solve(x: Int, y: Int, z: Int, action: (Jug, JugAction, Int, Int) => Unit): Unit = {
    if (x < 1 || y < 1 || z < 1 || z > Math.max(x, y)) return

    val solution1 = buildDumb(x, y, Jug.First, z, (_, _, _, _) => ())
    val solution2 = buildDumb(y, x, Jug.Second, z, (_, _, _, _) => ())

    if (solution1 <= solution2)
      buildDumb(x, y, Jug.First, z, action)
    else
      buildDumb(y, x, Jug.Second, z, action)
  }

  private def buildDumb(toFill: Int, toEmpty: Int, jugToFill: Jug, target: Int, action: (Jug, JugAction, Int, Int) => Unit): Int = {
    var operationCount = 0

    if (toFill == target) {
      if (jugToFill == Jug.First)
        action(jugToFill, JugAction.Fill, toFill, 0)
      else
        action(jugToFill, JugAction.Fill, 0, toFill)
      return 1
    }

    // fill first, empty second
    var volumeFill = 0
    var volumeEmpty = 0
    while (volumeEmpty != target && volumeFill != target) {
      // fill first jug if empty
      if (volumeFill == 0) {
        require(volumeFill == 0)
        volumeFill = toFill
        operationCount += 1

        if (jugToFill == Jug.First)
          action(jugToFill, JugAction.Fill, volumeFill, volumeEmpty)
        else
          action(jugToFill, JugAction.Fill, volumeEmpty, volumeFill)
      }

      // empty second jug if full
      if (volumeEmpty == toEmpty) {
        volumeEmpty = 0
        operationCount += 1
        if (jugToFill == Jug.First)
          action(jugToFill.otherJug, JugAction.Empty, volumeFill, volumeEmpty)
        else
          action(jugToFill.otherJug, JugAction.Empty, volumeEmpty, volumeFill)
      }

      // move contents from first to second jug
      val delta = Math.min(volumeFill, toEmpty - volumeEmpty)
      volumeFill -= delta
      volumeEmpty += delta
      operationCount += 1
      if (jugToFill == Jug.First)
        action(jugToFill, JugAction.Transfer, volumeFill, volumeEmpty)
      else
        action(jugToFill, JugAction.Transfer, volumeEmpty, volumeFill)
    }

    operationCount
  }

}
