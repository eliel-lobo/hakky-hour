package com.typesafe.training.hakkyhour.exception

import akka.actor.ActorRef

case class FrustratedException(currentSender: ActorRef) extends IllegalStateException {

}
