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
package org.jenetics.stat;

import static java.lang.Double.NaN;
import static java.lang.String.format;
import static org.jenetics.internal.util.Equality.eq;

import org.jenetics.internal.util.Equality;
import org.jenetics.internal.util.Hash;

/**
 * <p>Calculate the variance from a finite sample of <i>N</i> observations.</p>
 * <p><img src="doc-files/variance.gif"
 *         alt="s^2_{N-1}=\frac{1}{N-1}\sum_{i=1}^{N}\left ( x_i - \bar{x} \right )^2"
 *    >
 * </p>
 *
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong> If
 * multiple threads access this object concurrently, and at least one of the
 * threads modifies it, it must be synchronized externally.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance" >
 *       Wikipedia: Algorithms for calculating variance</a>
 * @see <a href="http://mathworld.wolfram.com/Variance.html">
 *       Wolfram MathWorld: Variance</a>
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @since 1.0
 * @version 2.0 &mdash; <em>$Date$</em>
 */
public class Variance<N extends Number> extends Mean<N> {

	private double _m2 = NaN;

	public Variance() {
	}

	/**
	 * Return the variance of the accumulated values.
	 * <p><img src="doc-files/variance.gif" alt="Variance" ></p>
	 *
	 * @return the variance of the accumulated values, or {@link java.lang.Double#NaN}
	 *          if {@code getSamples() == 0}.
	 */
	public double getVariance() {
		double variance = NaN;

		if (_samples == 1) {
			variance = _m2;
		} else if (_samples > 1) {
			variance = _m2/(_samples - 1);
		}

		return variance;
	}

	/**
	 * @throws NullPointerException if the given {@code value} is {@code null}.
	 */
	@Override
	public void accumulate(final N value) {
		accumulate(value.doubleValue());
	}

	public void accumulate(final double value) {
		if (_samples == 0) {
			_mean = 0;
			_m2 = 0;
		}

		final double data = value;
		final double delta = data - _mean;

		_mean += delta/(++_samples);
		_m2 += delta*(data - _mean);
	}

	/**
	 * Merges the result of two variances into one.
	 *
	 * @see <a href="http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Parallel_algorithm">
	 *     Parallel variance calculation</a>
	 *
	 * @param other the other variance to combine.
	 * @return a new variance containing the merged result of {@code this} and
	 *         {@code other}.
	 */
	public Variance<N> merge(final Variance<N> other) {
		final Variance<N> result = new Variance<>();

		final double r = other._mean - _mean;
		result._samples = _samples + other._samples;
		result._mean = _mean + r*(other._samples/(double)result._samples);
		result._m2 = _m2 + other._m2 + r*r*(_samples*other._samples/(double)result._samples);

		return result;
	}

	@Override
	public int hashCode() {
		return Hash.of(getClass()).and(super.hashCode()).and(_m2).value();
	}

	@Override
	public boolean equals(final Object obj) {
		return Equality.of(this, obj).test(variance ->
			eq(_m2, variance._m2) &&
			super.equals(variance)
		);
	}

	@Override
	public String toString() {
		return format(
			"%s[samples=%d, mean=%f, stderr=%f, var=%f]",
			getClass().getSimpleName(),
			getSamples(),
			getMean(),
			getStandardError(),
			getVariance()
		);
	}

	@Override
	public Variance<N> clone() {
		return (Variance<N>)super.clone();
	}

}
