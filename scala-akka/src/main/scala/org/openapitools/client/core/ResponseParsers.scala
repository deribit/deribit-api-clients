package org.openapitools.client.core

import org.openapitools.client.model.{Currency, CurrencyEnums, CurrencyWithdrawalPriorities}

import scala.collection.immutable.HashMap.HashTrieMap
import scala.util.{Failure, Success, Try}

object ResponseParsers {
  def withCurrenciesParser[U](withFunction : List[Currency] => U): Try[ApiResponse[Any]] => U = {
    case Success(res) => {
      val contentMap = res.content.asInstanceOf[HashTrieMap[String, Any]]
      val results = contentMap.get("result").get.asInstanceOf[List[HashTrieMap[String, Any]]]
      val all = results.map(result => {
        val priorities = Some(result.get("withdrawal_priorities").get.asInstanceOf[List[Map[String, Any]]]
          .map(e => CurrencyWithdrawalPriorities(e.get("name").get.asInstanceOf[String],
            e.get("value").get.asInstanceOf[Double])))
        Currency(
          result.get("min_confirmations").asInstanceOf[Option[BigInt]].map(_.toInt),
          result.get("withdrawal_fee").asInstanceOf[Option[Double]],
          None,
          result.get("currency").get.asInstanceOf[String],
          result.get("currency_long").get.asInstanceOf[String],
          result.get("withdrawal_fee").get.asInstanceOf[Double],
          result.get("fee_precision").asInstanceOf[Option[BigInt]].map(_.toInt),
          priorities,
          CurrencyEnums.CoinType.withName(result.get("coin_type").get.asInstanceOf[String])
        )
      }
      )
      withFunction(all)
    }
    case Failure(e) => {
      withFunction(List[Currency]())
    }
  }
}
