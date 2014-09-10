package ella

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._

class MyServiceSpec extends Specification with Specs2RouteTest with EllaService {
  def actorRefFactory = system
  
  "Ella" should {

    "return a greeting for GET requests to the root path" in {
      Get() ~> route ~> check {
        responseAs[String] must be equalTo "Hello world"
      }
    }

    "return handle GET requests with integers in the URL path" in {
      Get("/42") ~> route ~> check {
        responseAs[String] must contain("42")
      }
    }

    "return handle GET requests with string in the URL path, but only one" in {
      Get("/asdf") ~> route ~> check {
        responseAs[String] must contain("asdf")
      }
      Get("/asdf/asdf") ~> route ~> check {
        handled must beFalse
      }
    }

    "leave POST requests to other paths unhandled" in {
      Post("/asdf") ~> route ~> check {
        handled must beFalse
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put() ~> sealRoute(route) ~> check {
        status === MethodNotAllowed
        responseAs[String] === "HTTP method not allowed, supported methods: GET"
      }
    }
  }
}
