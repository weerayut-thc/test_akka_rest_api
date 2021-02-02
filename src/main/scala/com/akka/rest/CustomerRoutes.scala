package com.akka.rest

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.duration._
import scala.concurrent.Future
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.json.DeserializationException
import spray.json.JsString
import spray.json.JsValue
import spray.json.RootJsonFormat

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._
  import CustomerRepository._

  implicit object StatusFormat extends RootJsonFormat[Status] {
    def write(status: Status): JsValue =
      status match {
        case Failed     => JsString("Failed")
        case Successful => JsString("Successful")
      }

    def read(json: JsValue): Status =
      json match {
        case JsString("Failed")     => Failed
        case JsString("Successful") => Successful
        case _                      => throw new DeserializationException("Status unexpected")
      }
  }

  implicit val customerFormat = jsonFormat2(Customer)
}

class CustomerRoutes(
    buildCustomerRepository: ActorRef[CustomerRepository.Command]
)(implicit
    system: ActorSystem[_]
) extends JsonSupport {

  import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
  import akka.actor.typed.scaladsl.AskPattern.Askable

  // asking someone requires a timeout and a scheduler, if the timeout hits without response
  // the ask is failed with a TimeoutException
  implicit val timeout: Timeout = 3.seconds

  lazy val theCustomerRoutes: Route =
    pathPrefix("customers") {
      concat(
        post {
          entity(as[CustomerRepository.Customer]) {
            customer =>
              val operationPerformed: Future[CustomerRepository.Response] =
                buildCustomerRepository.ask(
                  CustomerRepository.AddCustomer(customer, _)
                )
              onSuccess(operationPerformed) {
                case CustomerRepository.OK => complete("Customer added")
                case CustomerRepository.KO(reason) =>
                  complete(StatusCodes.InternalServerError -> reason)
              }
          }
        },
        (get & path(IntNumber)) { id =>
          val maybeJob: Future[Option[CustomerRepository.Customer]] =
            buildCustomerRepository.ask(
              CustomerRepository.GetCustomerNameById(id, _)
            )
          rejectEmptyResponse {
            complete(maybeJob)
          }
        },
        get {
          complete("Customer API")
        }
      )
    }
}
