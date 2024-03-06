/*
 * Copyright 2017-Present Author or Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package examples.meta;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Unit Tests for {@literal Meta's Rabbit Hole Problem}.
 *
 * @author John Blum
 * @see org.junit.jupiter.api.Test
 * @since 1.0.0
 */
@Getter(AccessLevel.PROTECTED)
public class RabbitHoleUnitTests {

	private final Solution solution = new SolutionOne();

	@Test
	void metaSampleTestCaseOne() {

		int[] links = { 4, 1, 2, 1 };

		assertThat(getSolution().getMaxVisitableWebpages(links.length, links)).isEqualTo(4);
	}

	@Test
	void metaSampleTestCaseTwo() {

		int[] links = { 4, 3, 5, 1, 2 };

		assertThat(getSolution().getMaxVisitableWebpages(links.length, links)).isEqualTo(3);
	}

	@Test
	void metaSampleTestCaseThree() {

		int[] links = { 2, 4, 2, 2, 3 };

		assertThat(getSolution().getMaxVisitableWebpages(links.length, links)).isEqualTo(4);
	}

	interface Solution {
		int getMaxVisitableWebpages(int pageCount, int[] pageLinks);
	}

	static abstract class AbstractSolution implements Solution {

	}

	static class SolutionOne extends AbstractSolution {

		@Override
		public int getMaxVisitableWebpages(int pageCount, int[] pageLinks) {

			int max = 0;

			Page[] pages = buildWebOfPages(pageCount, pageLinks);
			Page page = pages[0];
			max = page.countLinkedPages();

			for (int index = 1; index < pages.length; index++) {
				Page nextPage = pages[index];
				int linkedPageCount = nextPage.countLinkedPages();
				if (linkedPageCount > max) {
					max = linkedPageCount;
					page = nextPage;
				}
			}

			return max;
		}

		private Page[] buildWebOfPages(int pageCount, int[] pageLinks) {

			assert pageCount == pageLinks.length : "Page count must equal number of Page Links";

			Page[] pages = new Page[pageCount];

			for (int index = 0; index < pageCount; index++) {
				Page page = pages[index];
				if (page == null) {
					page = Page.numbered(Page.toPageNumber(index));
					pages[index] = page;
				}
				int linkPagedNumber = pageLinks[index];
				Page link = pages[Page.toIndex(linkPagedNumber)];
				if (link == null) {
					link = Page.numbered(linkPagedNumber);
					pages[link.toIndex()] = link;
				}
				page.withLink(link);
			}

			return pages;
		}
	}

	@Getter
	@ToString(of = "number")
	@EqualsAndHashCode(of = "number")
	@Setter(AccessLevel.PROTECTED)
	@RequiredArgsConstructor(staticName = "numbered")
	static class Page {

		// from (array) index to page numbers
		static int toPageNumber(int index) {
			return index + 1;
		}

		// from page number to (array) index
		static int toIndex(int pageNumber) {
			return pageNumber - 1;
		}

		private final int number;

		private Page link;

		public int countLinkedPages() {
			Set<Integer> pageNumbers = new HashSet<>();
			pageNumbers.add(getNumber());
			Page nextPage = visitLink();
			while (pageNumbers.add(nextPage.getNumber())) {
				nextPage = nextPage.visitLink();
			}
			return pageNumbers.size();
		}

		public int toIndex() {
			return Page.toIndex(getNumber());
		}

		public Page visitLink() {
			return getLink();
		}

		public Page withLink(Page link) {
			setLink(link);
			return this;
		}
	}
}
