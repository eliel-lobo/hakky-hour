package com.typesafe.training.hakkyhour.actors

import akka.actor._
import com.typesafe.training.hakkyhour.Drink
import com.typesafe.training.hakkyhour.actors.Barkeeper.PrepareDrink
import com.typesafe.training.hakkyhour.actors.Guest.DrinkServed
import com.typesafe.training.hakkyhour.actors.HakkyHour.ApproveDrink
import com.typesafe.training.hakkyhour.actors.Waiter.{ DrinkPrepared, ServeDrink }

object Waiter {
  def props(hakkyHour: ActorRef): Props = Props(new Waiter(hakkyHour))
  case class ServeDrink(drink: Drink)
  case class DrinkPrepared(drink: Drink, guest: ActorRef)
}

class Waiter(hakkyHour: ActorRef) extends Actor {

  override def receive: Receive = {
    case ServeDrink(drink) =>
      hakkyHour ! ApproveDrink(drink, sender())
    case DrinkPrepared(drink, guest) =>
      guest ! DrinkServed(drink)
  }
}
