package me.scf37.jugz.service

import me.scf37.jugz.solver.Jug
import me.scf37.jugz.solver.JugAction
import me.scf37.jugz.solver.JugsSolver
import me.scf37.jugz.solver.Solution
import me.scf37.jugz.solver.State

class PuzzleService() {
  private val solver = new JugsSolver

  def solve(x: Int, y: Int, z: Int): Option[Solution] = {
    solver.solve(x, y, z)
  }

  def next(x: Int, y: Int, z: Int, vx: Int, vy: Int, fillJug: Jug): (State, JugAction) = {
    State(vx, vy).nextState(x, y, z, fillJug)
  }
}
