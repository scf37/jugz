package me.scf37.jugz.controller

import com.google.inject.Guice
import com.google.inject.Key
import com.google.inject.TypeLiteral
import com.twitter.finagle.Service
import com.twitter.finagle.http.Method
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Response
import com.twitter.util.Await
import com.twitter.util.Future
import me.scf37.jugz.module.ApiModule
import me.scf37.jugz.module.WebModule
import me.scf37.jugz.rest.JsonHelper
import org.scalatest.FreeSpec

class ControllerTest extends FreeSpec{
  val service = Guice.createInjector(
    new ApiModule, new WebModule
  ).getInstance(Key.get(new TypeLiteral[Service[Request, Response]]{}))

  "http behavior" - {
    "fail on missing request body fields" in {
      val r = service(post("/v1/jugs/solve",
        Map("x" -> 3, "y" -> 3)
      ))

      assert(r.code == 400)
      assert(r.json("code") == "invalid_request")
    }

    "fail on unknown fields" in {
      val r = service(post("/v1/jugs/solve",
        Map("x" -> 3, "y" -> 3, "z" -> 3, "unknownfield" -> 0)
      ))

      assert(r.code == 400)
      assert(r.json("code") == "invalid_request")
    }

    "returns X-Request-Id header" in {
      val r = service(post("/v1/jugs/solve",
        Map("x" -> 3, "y" -> 3, "z" -> 3, "unknownfield" -> 0)
      ))

      assert(r.response.headerMap.contains("X-Request-Id"))
    }
  }

  "solve" - {
    "works for valid input" in {
      val r = service(post("/v1/jugs/solve",
        Map("x" -> 3, "y" -> 5, "z" -> 4)
      ))

      assert(r.code == 200)
      assert(r.json == Map("count" -> 6, "firstJug" -> false))
    }

    "returns error for unsolvable input" in {
      val r = service(post("/v1/jugs/solve",
        Map("x" -> 3, "y" -> 3, "z" -> 2)
      ))

      assert(r.code == 400)
      assert(r.json("code") == "jug_no_solution")
    }

    "validates requests" in {
      val r = service(post("/v1/jugs/solve",
        Map("x" -> 3, "y" -> 3, "z" -> -1)
      ))

      assert(r.code == 400)
      assert(r.json("code") == "invalid_request")
    }
  }

  "next" - {
    "works for valid input" in {
      val r = service(post("/v1/jugs/next",
        Map("x" -> 3, "y" -> 5, "z" -> 4, "firstJug" -> false, "state"-> Map("vx" -> 0, "vy" -> 0))
      ))

      assert(r.code == 200)
      assert(r.json == Map("nextState" -> Map("vx" -> 0, "vy" -> 5), "action" -> "Fill Jug Y"))
    }

    "validates requests" in {
      val r = service(post("/v1/jugs/next",
        Map("x" -> 3, "y" -> 5, "z" -> 4, "firstJug" -> false, "state"-> Map("vx" -> -4, "vy" -> 0))
      ))

      assert(r.code == 400)
      assert(r.json("code") == "invalid_request")
    }
  }


  private def post(url: String, body: Map[String, Any]): Request = {
    val r = Request(Method.Post, url)
    r.contentString = JsonHelper.toJson(body)
    r
  }

  private implicit class ResponseOps(r: Future[Response]) {
    def json: Map[String, Any] = {
      val resp = Await.result(r)
      JsonHelper.parseJson[Map[String, Any]](resp.contentString)
    }

    def code: Int = {
      val resp = Await.result(r)
      resp.statusCode
    }

    def response: Response = Await.result(r)
  }
}
