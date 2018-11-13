package me.scf37.jugz.controller.model

case class PuzzleStateRequest(
  x: Int,
  y: Int,
  z: Int,
  state: PuzzleState,
  firstJug: Boolean
) {

  def validate(): Seq[String] = {
    val errors = Seq.newBuilder[String]

    if (x < 1) errors += "x must be >= 1"
    if (y < 1) errors += "y must be >= 1"
    if (z < 1) errors += "z must be >= 1"

    errors ++= state.validate()

    errors.result()
  }
}
