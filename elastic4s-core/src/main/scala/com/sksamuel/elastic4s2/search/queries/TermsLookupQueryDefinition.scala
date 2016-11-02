package com.sksamuel.elastic4s2.search.queries

import com.sksamuel.elastic4s2.search.QueryDefinition
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.indices.TermsLookup

case class TermsLookupQueryDefinition(field: String, termsLookup: TermsLookup)
  extends QueryDefinition {

  val builder = QueryBuilders.termsLookupQuery(field, termsLookup)
  val _builder = builder

  def queryName(name: String): this.type = {
    builder.queryName(name)
    this
  }
}