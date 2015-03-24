package com.typesafe.training.hakkyhour.actors

import akka.actor.Actor.Receive
import akka.actor.{ ActorLogging, ActorRef, Actor, Props }
import com.typesafe.training.hakkyhour.Drink
import com.typesafe.training.hakkyhour.actors.Barkeeper.{ Done, PrepareDrink }
import com.typesafe.training.hakkyhour.actors.Waiter.DrinkPrepared
import com.typesafe.training.hakkyhour.busy
import akka.actor.Stash

import scala.concurrent.duration._
import scala.util.Random

object Barkeeper {
  def props(prepareDrinkDuration: FiniteDuration, accuracy: Int): Props = Props(new Barkeeper(prepareDrinkDuration, accuracy))
  case class PrepareDrink(drink: Drink, guest: ActorRef)
  case class Done(drink: Drink, guest: ActorRef)
}

class Barkeeper(prepareDrinkDuration: FiniteDuration, accuracy: Int) extends Actor with ActorLogging with Stash {

  import context.dispatcher

  val waiter = context.actorSelection("/user/hakky-hour/waiter")

  override def receive: Receive = {
    case PrepareDrink(drink, guest) =>

      context.become(busy, discardOld = false)
      val preparedDrink = if (Random.nextInt(100) < accuracy) drink else Drink.anyOther(drink)
      log debug s"preparing a $preparedDrink"
      //busy(prepareDrinkDuration)

      context.system.scheduler.scheduleOnce(prepareDrinkDuration, self, Done(preparedDrink, guest))

  }

  def busy: Receive = {
    case Done(preparedDrink, guest) =>
      waiter ! DrinkPrepared(preparedDrink, guest)
      unstashAll()
      context.unbecome
    case _ =>
      log debug "stashed message"
      stash()
  }
}
