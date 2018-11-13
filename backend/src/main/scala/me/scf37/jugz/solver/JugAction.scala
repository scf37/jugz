package me.scf37.jugz.solver

/**
  * Actions for selected jug
  */
sealed trait JugAction

object JugAction {

  /**
    * Fully fill this jug
    */
  case object Fill extends JugAction

  /**
    * Fully empty this jug
    */
  case object Empty extends JugAction

  /**
    * Transfer contents of this jug to another jug
    */
  case object Transfer extends JugAction

}