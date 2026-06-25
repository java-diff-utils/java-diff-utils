package com.github.difflib;

import com.github.difflib.algorithm.DiffAlgorithmFactory;
import com.github.difflib.algorithm.myers.MyersDiff;

/**
 * Default algorithm configuration for DiffUtils.
 */
public final class DiffAlgorithmDefaults {
		public static DiffAlgorithmFactory getDefault() {
				return MyersDiff.factory();
		}

		private DiffAlgorithmDefaults() {}
}
