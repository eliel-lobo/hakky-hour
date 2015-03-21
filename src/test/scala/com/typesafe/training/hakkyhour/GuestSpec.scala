package com.typesafe.training.hakkyhour

import akka.testkit.{ TestActorRef, TestProbe }
import com.typesafe.training.hakkyhour.Drink.Akkarita
import com.typesafe.training.hakkyhour.actors.{Waiter, Guest}
import Guest.DrinkServed
import Waiter.ServeDrink

import scala.concurrent.duration._
/**
 * Created by eliel on 5/02/15.
 */

class GuestSpec extends BaseAkkaSpec {

  "Sendin DrinkServe to Guest" should {
    "increment drink count by one" in {
      val guest = TestActorRef(new Guest(TestProbe().ref, Akkarita, 2 seconds))
      val guestActor = guest.underlyingActor
      guest ! DrinkServed(Akkarita)
      guestActor.drinkCount shouldEqual 1
    }

    "send Message ServeDrink to waiter" in {
      val actorRef = TestProbe()
      val guest = TestActorRef(new Guest(actorRef.ref, Akkarita, 0 seconds))
      guest ! DrinkServed(Akkarita)
      actorRef.expectMsg(ServeDrink(Akkarita))
    }
  }

}
