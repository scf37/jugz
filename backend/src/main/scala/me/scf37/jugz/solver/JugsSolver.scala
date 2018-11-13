package me.scf37.jugz.solver

import scala.annotation.tailrec

/**
  * Efficient solver for water jugs problem.
  * See also [[State]]
  */
class JugsSolver {
  // our solution depends on the fact that transfer does not change total amount of water in both jugs
  // therefore it boils down to fill/empty X a times, fill/empty Y b times so aX+bY=Z which is linear Diophantine equation
  // as x/y/z can be quite large and solution can be quite lengthy (billions of steps!)
  // we need to generate it recursively, from previous to next step

  /**
    * Solve jugs problem, returning total operation count and data required for State
    *
    * @param x size of jug X
    * @param y size of jug Y
    * @param z amount to measure
    * @return solution or None if there is no solution
    */
  def solve(x: Int, y: Int, z: Int): Option[Solution] = {
    if (!validate(x, y, z)) return None

    // corner cases
    if (x == z) return Some(Solution(
      count = 1,
      jugToFill = Jug.First
    ))

    if (y == z) return Some(Solution(
      count = 1,
      jugToFill = Jug.Second
    ))

    var (a, b) = solveDiophantine(x, y, z) match {
      case None => return None
      case Some((a, b)) => a -> b
    }

    // found counts of fill/empty for jugs can be suboptimal, need to check near values as well

    val gcd = this.gcd(x, y)
    var solutionLength = this.solutionLength(x, y, z, a, b)

    def updateIfBetter(da: Int, db: Int): Unit = {
      def sumOverflow(i1: Int, i2: Int): Boolean = {
        val l = i1.toLong + i2
        l < Int.MinValue || l > Int.MaxValue
      }

      if (!sumOverflow(a, da) && !sumOverflow(b, db)) {
        val newSolutionLength = this.solutionLength(x, y, z, a + da, b + db)
        if (newSolutionLength < solutionLength) {
          a += da
          b += db
          solutionLength = newSolutionLength
        }
        }
    }

    updateIfBetter(y/gcd, -x/gcd)
    updateIfBetter(-y/gcd, x/gcd)

    Some(Solution(
      count = solutionLength,
      jugToFill = if (a >= 0) Jug.First else Jug.Second
    ))
  }

  private def validate(x: Int, y: Int, z: Int): Boolean = {
    x > 0 && y > 0 && z > 0 && z <= Math.max(x, y)
  }

  /**
    * Calculates solution length for jug problem
    *
    * @param x size of jug X
    * @param y sie of jug Y
    * @param z amount to measure
    * @param a count to fill jug X
    * @param b count to fill jug Y
    * @return
    */
  private[solver] def solutionLength(x: Int, y: Int, z: Int, a: Int, b: Int): Long = {
    if (a <= 0) {
      // ensure first jug to fill, second to empty
      return solutionLength(y, x, z, b, a)
    }

    if (x == z) return 1

    val fillCount = a
    var emptyCount = -b

    // sometimes solution ends in unnecessary step - emptying unused jug
    // therefore if result is in jug A (jug we fill), correct emptyCount
    if (emptyCount > 0 && x > z) {
      emptyCount -= 1
    }

    // solution consists of following steps:
    // - fill jug X fillCount times
    // - empty jug Y emptyCount times
    // - transfer from jug X to jugY every time we fill jug X
    // - transfer again if previous transfer did not empty jug X

    // "transfer again" happens every time we empty jug Y EXCEPT when jug X is empty
    // in other words, "transfer again" does not happen if Diophantine rx=ty has roots for r<fillCount, t<emptyCount

    val firstRoot =
      if (x < y)
        y / gcd(x, y)
      else
        x / gcd(x, y)

    val countOfRoots = emptyCount / firstRoot

    fillCount.toLong + emptyCount + fillCount + (emptyCount - countOfRoots)
  }

  /**
    * Solve ax + by = c
    *
    * @param a
    * @param b
    * @param c
    * @return (x, y)
    */
  private[solver] def solveDiophantine(a: Int, b: Int, c: Int): Option[(Int, Int)] = {
    // see https://en.wikipedia.org/wiki/Diophantine_equation#Linear_Diophantine_equations
    // see https://en.wikipedia.org/wiki/Extended_Euclidean_algorithm

    if (a < b) return solveDiophantine(b, a, c).map(xy => xy._2 -> xy._1)

    val gcd = this.gcd(a, b)
    val multiplier = c / gcd

    // no solution - c is not multiple of gcd(a, b)
    if (c % gcd != 0) return None

    val (x, y) = extendedGcd(a, b)

    // solution found is not optimal if multiplier != 1, need to adjust it using
    // the fact diophantine equations have endless number of solutions
    Some(minimizeSolution(x.toLong * multiplier, y.toLong * multiplier, b / gcd, -a / gcd))
  }

  /**
    * find such k so (abs(x + k*dx) + abs(y + k*dy)) is minimal
    *
    * @param x
    * @param y
    * @param dx
    * @param dy
    * @return
    */
  private[solver] def minimizeSolution(x: Long, y: Long, dx: Int, dy: Int): (Int, Int) = {
    // this function always has single extremum
    // so we estimate it and then fine-tune by linear search
    var xx = x.toLong
    var yy = y.toLong

    def sum(a: Long, b: Long): Long = {
      Math.abs(a) + Math.abs(b)
    }

    val k = x / dx
    xx -= dx * k
    yy -= dy * k

    while (sum(xx, yy) > sum(xx + dx, yy + dy)) {
      xx += dx
      yy += dy
    }

    while (sum(xx, yy) > sum(xx - dx, yy - dy)) {
      xx -= dx
      yy -= dy
    }

    // solution can not be larger than max(a, b) therefore must fit int
    require(xx - xx.toInt == 0, "int overflow: " + xx)
    require(yy - yy.toInt == 0, "int overflow: " + yy)
    xx.toInt -> yy.toInt
  }

  /**
    * Calculates Bezout koeffs, i.e. solution to Diophantine equation ax + by = gcd(a, b)
    *
    * @param a
    * @param b
    * @return
    */
  private[solver] def extendedGcd(a: Int, b: Int): (Int, Int) = {
    // implementation taken from https://en.wikipedia.org/wiki/Extended_Euclidean_algorithm#Pseudocode
    var s = 0
    var old_s = 1
    var t = 1
    var old_t = 0
    var r = b
    var old_r = a

    while (r != 0) {
      val quotient = old_r / r

      var tmp = r
      r = old_r - quotient * r
      old_r = tmp

      tmp = s
      s = old_s - quotient * s
      old_s = tmp

      tmp = t
      t = old_t - quotient * t
      old_t = tmp
    }

    old_s -> old_t
  }

  @tailrec
  private def gcd(a: Int,b: Int): Int = {
    if(b == 0) a else gcd(b, a%b)
  }
}
