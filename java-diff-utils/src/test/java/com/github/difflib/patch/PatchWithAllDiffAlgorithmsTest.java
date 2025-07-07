package com.github.difflib.patch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffAlgorithmFactory;
import com.github.difflib.algorithm.myers.MyersDiff;
import com.github.difflib.algorithm.myers.MyersDiffWithLinearSpace;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PatchWithAllDiffAlgorithmsTest {

		private static Stream<Arguments> provideAlgorithms() {
				return Stream.of(Arguments.of(MyersDiff.factory()), Arguments.of(MyersDiffWithLinearSpace.factory()));
		}

		@AfterAll
		public static void afterAll() {
				DiffUtils.withDefaultDiffAlgorithmFactory(MyersDiff.factory());
		}

		@ParameterizedTest
		@MethodSource("provideAlgorithms")
		public void testPatch_Insert(DiffAlgorithmFactory factory) {
				DiffUtils.withDefaultDiffAlgorithmFactory(factory);

				final List<String> insertTest_from = Arrays.asList("hhh");
				final List<String> insertTest_to = Arrays.asList("hhh", "jjj", "kkk", "lll");

				final Patch<String> patch = DiffUtils.diff(insertTest_from, insertTest_to);
				try {
						assertEquals(insertTest_to, DiffUtils.patch(insertTest_from, patch));
				} catch (PatchFailedException e) {
						fail(e.getMessage());
				}
		}

		@ParameterizedTest
		@MethodSource("provideAlgorithms")
		public void testPatch_Delete(DiffAlgorithmFactory factory) {
				DiffUtils.withDefaultDiffAlgorithmFactory(factory);

				final List<String> deleteTest_from = Arrays.asList("ddd", "fff", "ggg", "hhh");
				final List<String> deleteTest_to = Arrays.asList("ggg");

				final Patch<String> patch = DiffUtils.diff(deleteTest_from, deleteTest_to);
				try {
						assertEquals(deleteTest_to, DiffUtils.patch(deleteTest_from, patch));
				} catch (PatchFailedException e) {
						fail(e.getMessage());
				}
		}

		@ParameterizedTest
		@MethodSource("provideAlgorithms")
		public void testPatch_Change(DiffAlgorithmFactory factory) {
				DiffUtils.withDefaultDiffAlgorithmFactory(factory);

				final List<String> changeTest_from = Arrays.asList("aaa", "bbb", "ccc", "ddd");
				final List<String> changeTest_to = Arrays.asList("aaa", "bxb", "cxc", "ddd");

				final Patch<String> patch = DiffUtils.diff(changeTest_from, changeTest_to);
				try {
						assertEquals(changeTest_to, DiffUtils.patch(changeTest_from, patch));
				} catch (PatchFailedException e) {
						fail(e.getMessage());
				}
		}

		@ParameterizedTest
		@MethodSource("provideAlgorithms")
		public void testPatch_Serializable(DiffAlgorithmFactory factory) throws IOException, ClassNotFoundException {
				DiffUtils.withDefaultDiffAlgorithmFactory(factory);

				final List<String> changeTest_from = Arrays.asList("aaa", "bbb", "ccc", "ddd");
				final List<String> changeTest_to = Arrays.asList("aaa", "bxb", "cxc", "ddd");

				final Patch<String> patch = DiffUtils.diff(changeTest_from, changeTest_to);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(baos);
				out.writeObject(patch);
				out.close();
				ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
				ObjectInputStream in = new ObjectInputStream(bais);
				Patch<String> result = (Patch<String>) in.readObject();
				in.close();

				try {
						assertEquals(changeTest_to, DiffUtils.patch(changeTest_from, result));
				} catch (PatchFailedException e) {
						fail(e.getMessage());
				}
		}
}
