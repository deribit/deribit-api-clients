import org.scalatest.AsyncFunSpec
import akka.actor.ActorSystem
import org.openapitools.client.api.MarketDataApi
import org.openapitools.client.core.{ApiInvoker, ApiResponse, BasicCredentials, ResponseParsers}
import org.openapitools.client.model.CurrencyEnums

import scala.concurrent.Future
import scala.util.Try

class ResponseParserTest extends AsyncFunSpec {
  val email = "bob@example.com"
  val password = "password"

  describe("The first list of response parsers.") {
    it("A response parser example where a list of currencies are obtained.") {
      val credentials = new BasicCredentials(email, password)
      val bitcoin: String = CurrencyEnums.CoinType.BITCOIN.toString()
      val request = MarketDataApi.apply().publicGetCurrenciesGet()(credentials)
      val invoker = ApiInvoker()(ActorSystem())
      val future : Future[ApiResponse[Any]] = invoker.execute(request)
      val primaryFuture = future.map(response => {
        println("Parsing Api response")
        ResponseParsers.withCurrenciesParser(currencyList => {
          println(currencyList.toString())
        })(Try(response))
        assert(2 == 2)
      })
      println(s"Done main thread")
      primaryFuture
    }
  }
}
