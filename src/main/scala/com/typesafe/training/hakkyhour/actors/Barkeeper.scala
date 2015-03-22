package com.typesafe.training.hakkyhour.actors

import akka.actor.Actor.Receive
import akka.actor.{ ActorLogging, ActorRef, Actor, Props }
import com.typesafe.training.hakkyhour.Drink
import com.typesafe.training.hakkyhour.actors.Barkeeper.{ PrepareDrink }
import com.typesafe.training.hakkyhour.actors.Waiter.DrinkPrepared
import com.typesafe.training.hakkyhour.busy

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object Barkeeper {
  def props(prepareDrinkDuration: FiniteDuration, accuracy: Int): Props = Props(new Barkeeper(prepareDrinkDuration, accuracy))
  case class PrepareDrink(drink: Drink, guest: ActorRef)
}

class Barkeeper(prepareDrinkDuration: FiniteDuration, accuracy: Int) extends Actor with ActorLogging {

  val waiter = context.actorSelection("/user/hakky-hour/waiter")

  override def receive: Receive = {
    case PrepareDrink(drink, guest) =>
      val preparedDrink = if (Random.nextInt(100) < accuracy) drink else Drink.anyOther(drink)
      log.debug(s"preparing a $preparedDrink")
      busy(prepareDrinkDuration)
      waiter ! DrinkPrepared(preparedDrink, guest)
  }
}
