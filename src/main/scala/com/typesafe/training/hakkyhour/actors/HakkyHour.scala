package com.typesafe.training.hakkyhour.actors

import java.util.concurrent.TimeUnit

import akka.actor.SupervisorStrategy.{ Restart, Resume, Stop }
import akka.actor._
import com.typesafe.training.hakkyhour.Drink
import com.typesafe.training.hakkyhour.actors.Barkeeper.PrepareDrink
import com.typesafe.training.hakkyhour.actors.HakkyHour.{ ApproveDrink, CreateGuests }
import com.typesafe.training.hakkyhour.exception.{ FrustratedException, DrunkException }
import com.typesafe.config.Config

import scala.concurrent.duration._

object HakkyHour {
  def props(maxDrinkCount: Int): Props =
    Props(new HakkyHour(maxDrinkCount))

  case class CreateGuests(favoriteDrink: Drink, maxDrinkCount: Int)
  case class ApproveDrink(drink: Drink, guest: ActorRef)
}

class HakkyHour(maxDrinkCount: Int) extends Actor with ActorLogging {

  val config: Config = context.system.settings.config

  private val accuracy: Int = config.getInt("hakky-hour.barkeeper.accuracy")
  private val prepareDrinkDuration = (config.getDuration("hakky-hour.barkeeper.prepare-drink-duration", TimeUnit.SECONDS) seconds)
  private val maxComplaintCount: Int = config.getInt("hakky-hour.waiter.max-complaint-count")
  private val finishDrinkDuration = (config.getDuration("hakky-hour.guest.finish-drink-duration", TimeUnit.SECONDS) seconds)

  val barkeeper = createBarkeeper(accuracy, prepareDrinkDuration)
  val waiter = createWaiter(self, barkeeper, maxComplaintCount)

  var numberOfDrinksPerGuest: Map[String, Int] = Map()

  override def receive: Receive = {

    case CreateGuests(favoriteDrink, maxDrinkCount) =>
      val guest: ActorRef = createGuest(waiter, favoriteDrink, finishDrinkDuration, maxDrinkCount)
      context.watch(guest)

    case ApproveDrink(drink, guest) =>
      val guestName: String = guest.path.name
      val drinkCount = numberOfDrinksPerGuest.get(guestName).getOrElse(0);
      if (drinkCount > maxDrinkCount) {
        log info s"sorry ${guestName}, time to go home"
        context.stop(guest)
      } else {
        numberOfDrinksPerGuest = numberOfDrinksPerGuest + (guestName -> (drinkCount + 1))
        barkeeper ! PrepareDrink(drink, guest)
      }

    case Terminated(guest) =>
      val guestName: String = guest.path.name
      log info s"Thanks, ${guestName}, for being our guest!"
      numberOfDrinksPerGuest = numberOfDrinksPerGuest - guestName
  }

  def createBarkeeper(accuracy: Int, prepareDrinkDuration: FiniteDuration): ActorRef = {
    context.actorOf(Barkeeper.props(prepareDrinkDuration, accuracy), "barkeeper")
  }

  def createGuest(waiter: ActorRef, favoriteDrink: Drink, finishDrinkDuration: FiniteDuration, maxDrinkCount: Int) = {
    context.actorOf(Guest.props(waiter, favoriteDrink, finishDrinkDuration, maxDrinkCount))
  }

  def createWaiter(hakkyHour: ActorRef, mybarkeeper: ActorRef, maxComplaintCount: Int): ActorRef = {
    context.actorOf(Waiter.props(hakkyHour, mybarkeeper, maxComplaintCount), "waiter")
  }

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case DrunkException         => Stop
    case FrustratedException(_) => Restart
  }

}
