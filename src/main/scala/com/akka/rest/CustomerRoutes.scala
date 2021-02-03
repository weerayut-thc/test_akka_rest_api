package com.akka.rest

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.duration._
import scala.concurrent.Future

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

class CustomerRoutes(
    buildCustomerRepository: ActorRef[CustomerRepository.Command]
)(implicit
    system: ActorSystem[_]
) extends FailFastCirceSupport {

  import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
  import akka.actor.typed.scaladsl.AskPattern.Askable

  import io.circe.generic.auto._

  implicit val timeout: Timeout = 3.seconds

  lazy val theCustomerRoutes: Route =
    pathPrefix("customers") {
      concat(
        post {
          entity(as[CustomerRepository.Customer]) {
            customer =>
              val operationPerformed: Future[CustomerRepository.Response] = {
                buildCustomerRepository.ask(
                  CustomerRepository.AddCustomer(customer, _)
                )
              }
              onSuccess(operationPerformed) {
                case CustomerRepository.OK => complete("Customer added")
                case CustomerRepository.KO(reason) =>
                  complete(StatusCodes.InternalServerError -> reason)
              }
          }
        },
        (get & path(IntNumber)) { id =>
          val maybeCustomer: Future[Option[CustomerRepository.Customer]] =
            buildCustomerRepository.ask(
              CustomerRepository.GetCustomerNameById(id, _)
            )
          rejectEmptyResponse {
            complete(maybeCustomer)
          }
        },
        get {
          complete("Customer API")
        }
      )
    }
}
