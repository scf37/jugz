package me.scf37.jugz.solver

/**
  * Jugs enum, we only have two jugs
  */
sealed trait Jug {
  def otherJug: Jug
}

object Jug {
  case object First extends Jug {
    override def otherJug: Jug = Second
  }

  case object Second extends Jug {
    override def otherJug: Jug = First
  }
}
