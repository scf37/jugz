package me.scf37.jugz.controller.model

case class Puzzle(
  x: Int,
  y: Int,
  z: Int
) {
  def validate(): Seq[String] = {
    val errors = Seq.newBuilder[String]

    if (x < 1) errors += "x must be >= 1"
    if (y < 1) errors += "y must be >= 1"
    if (z < 1) errors += "z must be >= 1"

    errors.result()
  }
}
