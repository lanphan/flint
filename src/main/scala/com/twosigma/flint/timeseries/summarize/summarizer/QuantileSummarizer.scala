/*
 *  Copyright 2015-2016 TWO SIGMA OPEN SOURCE, LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.twosigma.flint.timeseries.summarize.summarizer

import com.twosigma.flint.rdd.function.summarize.summarizer.{ QuantileSummarizer => QSummarizer }
import com.twosigma.flint.timeseries.Schema

import org.apache.spark.sql.catalyst.expressions.GenericInternalRow
import org.apache.spark.sql.types._

import scala.collection.mutable.ArrayBuffer

case class QuantileSummarizerFactory(column: String, p: Array[Double]) extends SummarizerFactory {
  override def apply(inputSchema: StructType): QuantileSummarizer =
    QuantileSummarizer(inputSchema, alias, column, p)
}

case class QuantileSummarizer(
  override val inputSchema: StructType,
  override val alias: Option[String],
  column: String,
  p: Array[Double]
) extends Summarizer {
  private val columnIndex = inputSchema.fieldIndex(column)
  private val toDouble = anyToDouble(inputSchema(columnIndex).dataType)

  override type T = Double
  override type U = ArrayBuffer[Double]
  override type V = Array[Double]

  override val summarizer = QSummarizer(p)
  override val schema = Schema.of(p.map { q => s"${column}_${q}quantile" -> DoubleType }: _*)

  override def toT(r: GenericInternalRow): T = toDouble(r.get(columnIndex, schema(columnIndex).dataType))

  override def fromV(v: V): GenericInternalRow = new GenericInternalRow(Array[Any]() ++ v)
}
