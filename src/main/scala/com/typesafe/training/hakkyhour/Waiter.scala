package com.typesafe.training.hakkyhour

import akka.actor._
import com.typesafe.training.hakkyhour.Guest.DrinkServed
import com.typesafe.training.hakkyhour.Waiter.ServeDrink

object Waiter {
  def props: Props = Props(new Waiter)
  case class ServeDrink(drink: Drink)
}

class Waiter extends Actor {
  override def receive: Receive = {
    case ServeDrink(drink) => sender() ! DrinkServed(drink)
  }
}
