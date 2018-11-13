package me.scf37.jugz.solver

/**
  * Jug puzzle state
  */
case class State(
  /**
    * Current volume of jug X
    */
  vx: Int,

  /**
    * Current volume of jug Y
    */
  vy: Int
) {

  def nextState(x: Int, y: Int, z: Int, jugToFill: Jug): (State, JugAction) = {
    val ((vvx, vvy), action) =
      if (jugToFill == Jug.First)
        calcNextState(x, y, z, vx, vy)
      else {
        val (vv, action) = calcNextState(y, x, z, vy, vx)
        vv.swap -> action
      }

    State(vx = vvx, vy = vvy) -> action
  }

  private def calcNextState(toFill: Int, toEmpty: Int, target: Int, vFill: Int, vEmpty: Int): ((Int, Int), JugAction) = {
    if (vFill == 0)
      toFill -> vEmpty -> JugAction.Fill
    else if (vEmpty == toEmpty)
      vFill -> 0 -> JugAction.Empty
    else {
      val delta = Math.min(vFill, toEmpty - vEmpty)
      (vFill - delta) -> (vEmpty + delta) -> JugAction.Transfer
    }
  }
}