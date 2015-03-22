package com.typesafe.training.hakkyhour.actors

import akka.actor._
import com.typesafe.training.hakkyhour.Drink
import com.typesafe.training.hakkyhour.actors.Barkeeper.PrepareDrink
import com.typesafe.training.hakkyhour.actors.Guest.DrinkServed
import com.typesafe.training.hakkyhour.actors.HakkyHour.ApproveDrink
import com.typesafe.training.hakkyhour.actors.Waiter.{ Complaint, DrinkPrepared, ServeDrink }
import com.typesafe.training.hakkyhour.exception.FrustratedException

object Waiter {
  def props(hakkyHour: ActorRef, barkeeper: ActorRef, maxComplaintCount: Int): Props =
    Props(new Waiter(hakkyHour, barkeeper, maxComplaintCount))
  case class ServeDrink(drink: Drink)
  case class Complaint(drink: Drink)
  case class DrinkPrepared(drink: Drink, guest: ActorRef)
}

class Waiter(hakkyHour: ActorRef, barkeeper: ActorRef, maxComplaintCount: Int) extends Actor with ActorLogging {

  var complaints = 0

  override def receive: Receive = {
    case ServeDrink(drink) =>
      hakkyHour ! ApproveDrink(drink, sender())

    case DrinkPrepared(drink, guest) =>
      guest ! DrinkServed(drink)

    case Complaint(drink) =>
      complaints = complaints + 1
      log debug s"$complaints complaints"
      if (complaints == maxComplaintCount) throw FrustratedException(sender())
      barkeeper ! PrepareDrink(drink, sender())
  }

  @throws[Exception](classOf[Exception])
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    reason match {
      case FrustratedException(sender) =>
        self tell (message.get, sender)
    }
  }

}
