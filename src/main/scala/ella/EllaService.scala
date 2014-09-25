package ella

import akka.actor.{Terminated, Props, Actor}
import spray.routing._
import akka.routing._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class EllaActor extends Actor with EllaService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(route)
}


/* Master worker which holds pool of workers*/
class Master extends Actor {
  var router = {
    val routees = Vector.fill(100) {
      val r = context.actorOf(Props[EllaActor])
      context watch r
      ActorRefRoutee(r)
    }

    /** Routing strategies have huge impact on throughput */
    Router(RoundRobinRoutingLogic(), routees)
    //Router(SmallestMailboxRoutingLogic(), routees)
    //Router(RandomRoutingLogic(), routees) // worst results among others
  }

  def receive = {
    case Terminated(a) =>
      println("Terminated")
      router = router.removeRoutee(a)
      val r = context.actorOf(Props[EllaActor])
      context watch r
      router = router.addRoutee(r)

    case w =>
      router.route(w, sender())

  }
}

// this trait defines our service behavior independently from the service actor
trait EllaService extends HttpService {

  val route =
    path("") {
      get (ctx => ctx.complete("Hello world"))
    } ~
    path("sleep") {
      get { ctx =>
        Thread.sleep(1000)
        ctx.complete("sleep")
      }
    } ~
    pathPrefix(IntNumber) { num =>
      get (_.complete("number: " + num.toString))
    } ~
    path(Segment) { str =>
      get (_.complete("string: " + str))
    }
}