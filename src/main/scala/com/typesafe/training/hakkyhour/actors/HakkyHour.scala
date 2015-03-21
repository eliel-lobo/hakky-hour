package com.typesafe.training.hakkyhour.actors

import java.util.concurrent.TimeUnit

import akka.actor._
import com.typesafe.training.hakkyhour.Drink
import com.typesafe.training.hakkyhour.actors.Barkeeper.PrepareDrink
import com.typesafe.training.hakkyhour.actors.HakkyHour.{ ApproveDrink, CreateGuests }

import scala.concurrent.duration._

object HakkyHour {
  def props(maxDrinkCount: Int): Props =
    Props(new HakkyHour(maxDrinkCount))

  case class CreateGuests(favoriteDrink: Drink)
  case class ApproveDrink(drink: Drink, guest: ActorRef)
}

class HakkyHour(maxDrinkCount: Int) extends Actor with ActorLogging {

  val waiter = context.actorOf(Waiter.props(self))
  val barkeeper = createBarkeeper()
  var numberOfDrinksPerGuest: Map[String, Int] = Map()

  override def receive: Receive = {
    case CreateGuests(favoriteDrink) =>
      val duration = getDuration("hakky-hour.guest.finish-drink-duration")
      log.info(s"duration is: $duration")
      createGuest(waiter, favoriteDrink, duration)
    case ApproveDrink(drink, guest) =>
      val guestName: String = guest.path.name
      val drinkCount = numberOfDrinksPerGuest.get(guestName).getOrElse(0);
      if (drinkCount > maxDrinkCount) {
        log.info(s"sorry ${guestName}, time to go home")
        context.stop(guest)
      } else {
        numberOfDrinksPerGuest = numberOfDrinksPerGuest + (guestName -> (drinkCount + 1))
        barkeeper ! PrepareDrink(drink, guest)
      }
  }

  def createBarkeeper(): ActorRef = {
    context.actorOf(Barkeeper.props(getDuration("hakky-hour.barkeeper.prepare-drink-duration"), waiter), "barkeeper")
  }

  def getDuration(key: String): FiniteDuration = {
    context.system.settings.config.getDuration(key, TimeUnit.SECONDS) seconds
  }

  def createGuest(waiter: ActorRef, favoriteDrink: Drink, finishDrinkDuration: FiniteDuration) = {
    context.actorOf(Guest.props(waiter, favoriteDrink, finishDrinkDuration))
  }

}
