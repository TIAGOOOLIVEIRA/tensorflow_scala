/* Copyright 2017-18, Emmanouil Antonios Platanios. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.platanios.tensorflow.api.ops.rnn.cell

import org.platanios.tensorflow.api.implicits.helpers.{NestedStructure, Zero}
import org.platanios.tensorflow.api.ops.Output

/** Contains functions for constructing ops related to recurrent neural network (RNN) cells.
  *
  * @author Emmanouil Antonios Platanios
  */
abstract class RNNCell[O, S] {
  def outputShape[OS](implicit evStructureO: NestedStructure.Aux[O, _, _, OS]): OS
  def stateShape[SS](implicit evStructureS: NestedStructure.Aux[S, _, _, SS]): SS

  def zeroOutput(
      batchSize: Output[Int],
      name: String = "ZeroOutput"
  )(implicit evZeroO: Zero[O]): O = {
    evZeroO.zero(batchSize, outputShape(evZeroO.structure), name)
  }

  def zeroState(
      batchSize: Output[Int],
      name: String = "ZeroState"
  )(implicit evZeroS: Zero[S]): S = {
    evZeroS.zero(batchSize, stateShape(evZeroS.structure), name)
  }

  @throws[IllegalArgumentException]
  def forward(input: Tuple[O, S]): Tuple[O, S]

  def apply(input: Tuple[O, S]): Tuple[O, S] = {
    forward(input)
  }
}
