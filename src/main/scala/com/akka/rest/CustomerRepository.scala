package com.akka.rest

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import scala.collection.immutable

object CustomerRepository {

  sealed trait Status
  object Successful extends Status
  object Failed extends Status

  final case class Customer(id: Int, name: String)

  sealed trait Response
  case object OK extends Response
  final case class KO(reason: String) extends Response

  sealed trait Command
  final case class AddCustomer(customer: Customer, replyTo: ActorRef[Response])
      extends Command
  final case class GetCustomerNameById(
      id: Int,
      replyTo: ActorRef[Option[Customer]]
  ) extends Command

  def apply(customers: Map[Int, Customer] = Map.empty): Behavior[Command] =
    Behaviors.receiveMessage {
      case AddCustomer(customer, replyTo) =>
        replyTo ! OK
        CustomerRepository(customers.+(customer.id -> customer))
      case GetCustomerNameById(id, replyTo) =>
        replyTo ! customers.get(id)
        Behaviors.same
    }

}
