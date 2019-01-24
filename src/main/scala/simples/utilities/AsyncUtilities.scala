package simples.utilities

import scala.util.Try
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration

object AsyncUtilities {

  implicit class TryToFutureWrapper(tried: Try[Any]) {

    def async = Future.fromTry(tried)

  }

  implicit class FutureToTryWrapper(future: Future[Any]) {

    def await = Await.result(future, Duration.Inf)

  }

}