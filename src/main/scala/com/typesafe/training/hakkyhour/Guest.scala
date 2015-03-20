package com.typesafe.training.hakkyhour

import akka.actor.{ ActorRef, ActorLogging, Actor, Props }
import com.typesafe.training.hakkyhour.Guest.{ DrinkFinished, DrinkServed }
import com.typesafe.training.hakkyhour.Waiter.ServeDrink

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
}
