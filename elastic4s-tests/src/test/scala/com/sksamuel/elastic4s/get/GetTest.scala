package com.sksamuel.elastic4s.get

import com.sksamuel.elastic4s.http.ElasticDsl
import com.sksamuel.elastic4s.testkit.ResponseConverterImplicits._
import com.sksamuel.elastic4s.testkit.{DualClient, DualElasticSugar}
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

class GetTest extends FlatSpec with Matchers with ScalaFutures with ElasticDsl with DualElasticSugar with DualClient {

  import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._

  override protected def beforeRunTests() = {
    execute {
      createIndex("beer").mappings {
        mapping("lager").fields(
          textField("name").stored(true),
          textField("brand").stored(true),
          textField("ingredients").stored(true)
        )
      }
    }.await

    execute {
      bulk(
        indexInto("beer/lager") fields(
          "name" -> "coors light",
          "brand" -> "coors",
          "ingredients" -> Seq("hops", "barley", "water", "yeast")
        ) id 4,
        indexInto("beer/lager") fields(
          "name" -> "bud lite",
          "brand" -> "bud",
          "ingredients" -> Seq("hops", "barley", "water", "yeast")
        ) id 8
      ).refresh(RefreshPolicy.IMMEDIATE)
    }.await
  }

  "A Get request" should "retrieve a document by id" in {

    val resp = execute {
      get(8) from "beer/lager"
    }.await

    resp.exists shouldBe true
    resp.id shouldBe "8"
  }

  it should "retrieve a document by id with source" in {

    val resp = execute {
      get(8) from "beer/lager"
    }.await

    resp.exists shouldBe true
    resp.id shouldBe "8"
    resp.sourceAsMap.keySet shouldBe Set("name", "brand", "ingredients")
  }

  it should "retrieve a document by id without source" in {

    val resp = execute {
      get(8) from "beer/lager" fetchSourceContext false
    }

    whenReady(resp) { response =>
      response.exists should be(true)
      response.id shouldBe "8"
      response.sourceAsMap shouldBe Map.empty
      response.storedFieldsAsMap shouldBe Map.empty
    }
  }

  it should "support source includes" in {

    val resp = execute {
      get(8) from "beer/lager" fetchSourceInclude "brand"
    }.await

    resp.exists should be(true)
    resp.id shouldBe "8"
    resp.sourceAsMap shouldBe Map("brand" -> "bud")
  }

  it should "support source excludes" in {

    val resp = execute {
      get(8) from "beer/lager" fetchSourceExclude "brand"
    }.await

    resp.exists should be(true)
    resp.id shouldBe "8"
    resp.sourceAsMap shouldBe Map("name" -> "bud lite", "ingredients" -> List("hops", "barley", "water", "yeast"))
  }

  it should "support source includes and excludes" in {

    val resp = execute {
      get(8) from "beer/lager" fetchSourceContext(List("name"), List("brand"))
    }.await

    resp.exists should be(true)
    resp.id shouldBe "8"
    resp.sourceAsMap shouldBe Map("name" -> "bud lite")
  }

  it should "retrieve a document supporting stored fields" in {

    val resp = execute {
      get(4) from "beer/lager" storedFields("name", "brand")
    }

    whenReady(resp) { response =>
      response.exists should be(true)
      response.id shouldBe "4"
      response.storedFieldsAsMap.keySet shouldBe Set("name", "brand")
    }
  }

  it should "not retrieve any documents w/ unknown id" in {

    val resp = execute {
      get(131313) from "beer/lager"
    }

    whenReady(resp) { result =>
      result.exists shouldBe false
    }
  }

  it should "retrieve multi value fields" in {

    val resp = execute {
      get(4) from "beer/lager" storedFields "ingredients"
    }

    whenReady(resp) { response =>
      val field = response.storedField("ingredients")
      field.values shouldBe Seq("hops", "barley", "water", "yeast")
    }
  }
}
