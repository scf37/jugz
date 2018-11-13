package me.scf37.jugz.controller.model

case class PuzzleState(
  vx: Int,
  vy: Int
) {

  def validate(): Seq[String] = {
    val errors = Seq.newBuilder[String]

    if (vx < 0) errors += "vx must be >= 0"
    if (vy < 0) errors += "vx must be >= 0"

    errors.result()

  }
}
