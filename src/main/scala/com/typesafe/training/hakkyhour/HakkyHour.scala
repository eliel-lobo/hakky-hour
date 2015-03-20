package com.typesafe.training.hakkyhour

import java.util.concurrent.TimeUnit

import akka.actor._
import com.typesafe.training.hakkyhour.HakkyHour.CreateGuests

import scala.concurrent.duration._

/**
 * Created by eliel on 4/02/15.
 */
object HakkyHour {
  def props(): Props =
    Props(new HakkyHour())

  case class CreateGuests(favoriteDrink: Drink)
}

class HakkyHour extends Actor with ActorLogging {

  val waiter = context.actorOf(Waiter.props)

  override def receive: Receive = {
    case CreateGuests(favoriteDrink) =>
      val duration = context.system.settings.config.getDuration("hakky-hour.guest.finish-drink-duration", TimeUnit.SECONDS)
      log.info(s"duration is: $duration")
      createGuest(waiter, favoriteDrink, duration seconds)
    //val strDuration = System.getProperty("hakky-hour.guest.finish-drink-duration")
  }

  def createGuest(waiter: ActorRef, favoriteDrink: Drink, finishDrinkDuration: FiniteDuration) = {
    context.actorOf(Guest.props(waiter, favoriteDrink, finishDrinkDuration))
  }

}
