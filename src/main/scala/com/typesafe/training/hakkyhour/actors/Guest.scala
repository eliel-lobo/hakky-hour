package com.typesafe.training.hakkyhour.actors

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import com.typesafe.training.hakkyhour.Drink
import Waiter.ServeDrink
import com.typesafe.training.hakkyhour.actors.Guest.{ DrinkServed, DrinkFinished }

import scala.concurrent.duration.FiniteDuration

object Guest {
  def props(waiter: ActorRef, favoriteDrink: Drink, finishDrinkDuration: FiniteDuration): Props = Props(new Guest(waiter, favoriteDrink, finishDrinkDuration))
  case class DrinkServed(drink: Drink)
  private case object DrinkFinished
}

class Guest(waiter: ActorRef, favoriteDrink: Drink, finishDrinkDuration: FiniteDuration) extends Actor with ActorLogging {
  import context.dispatcher

  var drinkCount: Int = 0
  log.info(s"Created with drink $favoriteDrink")

  self ! DrinkFinished

  override def receive: Receive = {

    case DrinkServed(drink) => {
      drinkCount += 1
      log info s"Enjoying my $drinkCount. yummy $drink!"

      context.system.scheduler.scheduleOnce(
        finishDrinkDuration,
        self,
        DrinkFinished
      )
    }

    case DrinkFinished => waiter ! ServeDrink(favoriteDrink)
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = log.info("Good bye!")
}
