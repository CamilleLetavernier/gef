/*******************************************************************************
 * Copyright (c) 2009, 2018 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *     Fabian Steeg - initial API and implementation; see bug 277380
 *     Alexander Nyßen (itemis AG) - rename refactoring
 *     Tamas Miklossy  (itemis AG) - minor refactoring
 *
 *******************************************************************************/
package org.eclipse.gef.dot.tests

import org.eclipse.gef.dot.internal.DotExtractor
import org.junit.Assert
import org.junit.Test

/**
 * Tests for the {@link DotExtractor}.
 * 
 * @author Fabian Steeg (fsteeg)
 */
class DotExtractorTests {
	@Test def void testDotExtraction01() {
		testDotExtraction(DotTestGraphs.EMBEDDED_01, DotTestGraphs.EXTRACTED_01)
	}

	@Test def void testDotExtraction02() {
		testDotExtraction(DotTestGraphs.EMBEDDED_02, DotTestGraphs.EXTRACTED_02)
	}

	@Test def void testDotExtraction03() {
		testDotExtraction(DotTestGraphs.EMBEDDED_03, DotTestGraphs.EXTRACTED_03)
	}

	@Test def void testDotExtraction04() {
		testDotExtraction(DotTestGraphs.EMBEDDED_04, DotTestGraphs.EXTRACTED_04)
	}

	@Test def void testDotExtraction05() {
		testDotExtraction(DotTestGraphs.EMBEDDED_05, DotTestGraphs.EXTRACTED_05)
	}

	@Test def void testDotExtraction06() {
		testDotExtraction(DotTestGraphs.EMBEDDED_06, DotTestGraphs.EXTRACTED_06)
	}

	@Test def void testDotExtraction07() {
		testDotExtraction(DotTestGraphs.EMBEDDED_07, DotTestGraphs.EXTRACTED_07)
	}

	@Test def void testDotExtraction08() {
		testDotExtraction(DotTestGraphs.EMBEDDED_08, DotTestGraphs.EXTRACTED_08)
	}

	def private void testDotExtraction(String embedded, String expected) {
		var String extracted = new DotExtractor(embedded).getDotString()
		Assert.assertEquals(String.format("Incorrect DOT extraction for '%s';", embedded), expected.trim(), extracted)
	}
}
