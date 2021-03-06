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

package org.platanios.tensorflow.api.learn.layers.rnn

import org.platanios.tensorflow.api.implicits.helpers.Zero
import org.platanios.tensorflow.api.learn.Mode
import org.platanios.tensorflow.api.learn.layers.Layer
import org.platanios.tensorflow.api.learn.layers.rnn.cell.{RNNCell, Tuple}
import org.platanios.tensorflow.api.ops
import org.platanios.tensorflow.api.tensors.Tensor

/** Creates a dynamic RNN layer.
  *
  * $OpDocRNNDynamicRNN
  *
  * @param  name               Name scope (also acting as variable scope) for this layer.
  * @param  cell               RNN cell to use.
  * @param  initialState       Initial state to use for the RNN, which is a structure over tensors with shapes
  *                            `[batchSize, stateShape(i)(0), stateShape(i)(1), ...]`, where `i` corresponds to the
  *                            index of the corresponding state. Defaults to a zero state.
  * @param  timeMajor          Boolean value indicating whether the inputs are provided in time-major format (i.e.,
  *                            have shape `[time, batch, depth]`) or in batch-major format (i.e., have shape
  *                            `[batch, time, depth]`).
  * @param  parallelIterations Number of RNN loop iterations allowed to run in parallel.
  * @param  swapMemory         If `true`, GPU-CPU memory swapping support is enabled for the RNN loop.
  * @param  sequenceLengths    Optional tensor with shape `[batchSize]` containing the sequence lengths for
  *                            each row in the batch.
  *
  * @author Emmanouil Antonios Platanios
  */
class RNN[O: Zero, S: Zero](
    override val name: String,
    val cell: RNNCell[O, S],
    val initialState: () => S = null,
    val timeMajor: Boolean = false,
    val parallelIterations: Int = 32,
    val swapMemory: Boolean = false,
    val sequenceLengths: Tensor[Int] = null
) extends Layer[O, Tuple[O, S]](name) {
  override val layerType: String = "RNN"

  override def forwardWithoutContext(input: O)(implicit mode: Mode): Tuple[O, S] = {
    val state = if (initialState == null) None else Some(initialState())
    val lengths = if (sequenceLengths == null) null else ops.Basic.constant(sequenceLengths)
    val createdCell = cell.createCell(mode, Zero[O].structure.shapeFromOutput(input))(Zero[O].structure)
    ops.rnn.RNN.dynamicRNN(
      createdCell, input, state, timeMajor, parallelIterations,
      swapMemory, lengths, name)(Zero[O], Zero[S])
  }
}

object RNN {
  def apply[O: Zero, S: Zero](
      variableScope: String,
      cell: RNNCell[O, S],
      initialState: () => S = null,
      timeMajor: Boolean = false,
      parallelIterations: Int = 32,
      swapMemory: Boolean = false,
      sequenceLengths: Tensor[Int] = null
  ): RNN[O, S] = {
    new RNN(variableScope, cell, initialState, timeMajor, parallelIterations, swapMemory, sequenceLengths)
  }
}
