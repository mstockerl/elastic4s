package com.sksamuel.elastic4s.http.search.queries

import com.sksamuel.elastic4s.searches.queries.HasParentQueryDefinition
import org.elasticsearch.common.xcontent.{XContentBuilder, XContentFactory}

object HasParentBodyFn {

  def apply(q: HasParentQueryDefinition): XContentBuilder = {
    val builder = XContentFactory.jsonBuilder()
    builder.startObject()
    builder.startObject("has_parent")
    builder.field("parent_type", q.`type`)
    builder.rawField("query", QueryBuilderFn(q.query).bytes)
    q.ignoreUnmapped.foreach(builder.field("ignore_unmapped", _))
    if (q.score)
      builder.field("score", true)
    q.boost.foreach(builder.field("boost", _))
    q.queryName.foreach(builder.field("_name", _))
    builder.endObject()
    builder.endObject()
    builder
  }
}
