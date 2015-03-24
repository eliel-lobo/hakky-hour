package com.typesafe.training.hakkyhour.actors

import akka.actor.SupervisorStrategy.Stop
import akka.actor._
import com.typesafe.training.hakkyhour.Drink
import com.typesafe.training.hakkyhour.actors.Waiter.{ Complaint, ServeDrink }
import com.typesafe.training.hakkyhour.actors.Guest.{ DrinkServed, DrinkFinished }
import com.typesafe.training.hakkyhour.exception.DrunkException

import scala.concurrent.duration.FiniteDuration

object Guest {
  def props(waiter: ActorRef, favoriteDrink: Drink, finishDrinkDuration: FiniteDuration, maxDrinkCount: Int): Props =
    Props(new Guest(waiter, favoriteDrink, finishDrinkDuration, maxDrinkCount))
  case class DrinkServed(drink: Drink)
  private case object DrinkFinished
}

class Guest(waiter: ActorRef, favoriteDrink: Drink, finishDrinkDuration: FiniteDuration, maxDrinkCount: Int) extends Actor with ActorLogging {
  import context.dispatcher

  var drinkCount: Int = 0
  log info s"Created with drink $favoriteDrink"

  var requestTime = System.currentTimeMillis()
  self ! DrinkFinished

  override def receive: Receive = {

    case DrinkServed(drink) => {
      val orderDuration: Float = (System.currentTimeMillis() - requestTime) / 1000f
      if (drink != favoriteDrink) {
        log info s"Waiter I did not ordered $drink!"
        waiter ! Complaint(favoriteDrink)
      } else {
        drinkCount += 1

        if (drinkCount > maxDrinkCount)
          throw DrunkException

        if (drinkCount > 0)
          log info s"Enjoying my $drinkCount. yummy $drink! After $orderDuration seconds"

        context.system.scheduler.scheduleOnce(
          finishDrinkDuration,
          self,
          DrinkFinished
        )
      }
    }

    case DrinkFinished =>
      requestTime = System.currentTimeMillis()
      waiter ! ServeDrink(favoriteDrink)
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = log info "Good bye!"
}
