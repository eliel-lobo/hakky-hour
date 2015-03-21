package com.typesafe.training.hakkyhour.actors

import akka.actor.Actor.Receive
import akka.actor.{ ActorLogging, ActorRef, Actor, Props }
import com.typesafe.training.hakkyhour.Drink
import com.typesafe.training.hakkyhour.actors.Barkeeper.{ PrepareDrink }
import com.typesafe.training.hakkyhour.actors.Waiter.DrinkPrepared
import com.typesafe.training.hakkyhour.busy

import scala.concurrent.duration.FiniteDuration

object Barkeeper {
  def props(prepareDrinkDuration: FiniteDuration, waiter: ActorRef): Props = Props(new Barkeeper(prepareDrinkDuration, waiter))
  case class PrepareDrink(drink: Drink, guest: ActorRef)
}

class Barkeeper(prepareDrinkDuration: FiniteDuration, waiter: ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {
    case PrepareDrink(drink, guest) =>
      log.debug(s"preparing a $drink")
      busy(prepareDrinkDuration)
      waiter ! DrinkPrepared(drink, guest)
  }
}
