/*
 * Java Genetic Algorithm Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmx.at)
 */
package org.jenetics;

import static java.lang.Math.abs;
import static org.jenetics.internal.math.arithmetic.pow;
import static org.jenetics.internal.math.base.ulpDistance;
import static org.jenetics.internal.math.statistics.min;

import java.util.Arrays;

import org.jenetics.internal.math.DoubleAdder;
import org.jenetics.internal.util.Equality;
import org.jenetics.internal.util.Hash;

/**
 * The roulette-wheel selector is also known as fitness proportional selector,
 * but in the <em>Jenetics</em> library it is implemented as probability selector.
 * The fitness value <i>f<sub>i</sub></i>  is used to calculate the selection
 * probability of individual <i>i</i>.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Roulette_wheel_selection">
 *          Wikipedia: Roulette wheel selection
 *      </a>
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @since 1.0
 * @version 2.0 &mdash; <em>$Date: 2014-12-08 $</em>
 */
public class RouletteWheelSelector<
	G extends Gene<?, G>,
	N extends Number & Comparable<? super N>
>
	extends ProbabilitySelector<G, N>
{

	private static final long MAX_ULP_DISTANCE = pow(10, 9);

	public RouletteWheelSelector() {
	}

	@Override
	protected double[] probabilities(
		final Population<G, N> population,
		final int count
	) {
		assert(population != null) : "Population can not be null. ";
		assert(count > 0) : "Population to select must be greater than zero. ";

		// Copy the fitness values to probabilities arrays.
		final double[] fitness = new double[population.size()];
		for (int i = population.size(); --i >= 0;) {
			fitness[i] = population.get(i).getFitness().doubleValue();
		}

		final double worst = Math.min(min(fitness), 0.0);
		final double sum = DoubleAdder.sum(fitness) - worst*population.size();

		if (abs(ulpDistance(sum, 0.0)) > MAX_ULP_DISTANCE) {
			for (int i = population.size(); --i >= 0;) {
				fitness[i] = (fitness[i] - worst)/sum;
			}
		} else {
			Arrays.fill(fitness, 1.0/population.size());
		}

		assert (sum2one(fitness)) : "Probabilities doesn't sum to one.";
		return fitness;
	}

	@Override
	public int hashCode() {
		return Hash.of(getClass()).value();
	}

	@Override
	public boolean equals(final Object obj) {
		return Equality.ofType(this, obj);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
