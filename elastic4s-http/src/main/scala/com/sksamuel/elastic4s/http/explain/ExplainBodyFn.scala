package com.sksamuel.elastic4s.http.explain

import com.sksamuel.elastic4s.explain.ExplainDefinition
import com.sksamuel.elastic4s.http.search.queries.QueryBuilderFn
import org.elasticsearch.common.xcontent.{XContentBuilder, XContentFactory}

object ExplainBodyFn {
  def apply(v: ExplainDefinition): XContentBuilder = {
    val builder = XContentFactory.jsonBuilder()
    builder.startObject()
    builder.rawField("query", QueryBuilderFn(v.query.get).bytes())
    builder.endObject()
  }
}
