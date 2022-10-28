package com.chiknas.swancloudserver.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;

class OffsetPagedRequestTest {

    // Tests page number is offset / limit
    @Test
    void getPageNumber() {
        OffsetPagedRequest thirdPageRequest = new OffsetPagedRequest(5, 10);
        assertEquals(2, thirdPageRequest.getPageNumber());

        OffsetPagedRequest secondPageRequest = new OffsetPagedRequest(5, 5);
        assertEquals(1, secondPageRequest.getPageNumber());

        OffsetPagedRequest firstPageRequest = new OffsetPagedRequest(5, 0);
        assertEquals(0, firstPageRequest.getPageNumber());
    }

    // Tests page size is set to limit
    @Test
    void getPageSize() {
        assertEquals(5, new OffsetPagedRequest(5, 10).getPageSize());
        assertEquals(15, new OffsetPagedRequest(15, 10).getPageSize());
        assertEquals(5000000, new OffsetPagedRequest(5000000, 10).getPageSize());
    }

    // Test getOffset always returns the specified offset
    @Test
    void getOffset() {
        assertEquals(5, new OffsetPagedRequest(10, 5).getOffset());
        assertEquals(15, new OffsetPagedRequest(10, 15).getOffset());
        assertEquals(5000000, new OffsetPagedRequest(10, 5000000).getOffset());
    }

    // Tests the sort property is returned as is
    @Test
    void getSort() {
        Sort sort = Sort.by(Sort.Direction.ASC, "testProperty");
        assertEquals(sort, new OffsetPagedRequest(10, 5, sort).getSort());
    }

    // Tests the next page is returned on the paged request
    @Test
    void next() {
        OffsetPagedRequest secondPageRequest = new OffsetPagedRequest(5, 9);
        assertEquals(1, secondPageRequest.getPageNumber());
        assertEquals(2, secondPageRequest.next().getPageNumber());
    }

    // Tests that previous method returns a paged request for the previous page
    @Test
    void previous() {
        OffsetPagedRequest secondPageRequest = new OffsetPagedRequest(5, 9);
        assertEquals(1, secondPageRequest.getPageNumber());
        assertEquals(0, secondPageRequest.previous().getPageNumber());
    }

    // Tests that previous method does not return a page number below 0
    @Test
    void previousNotExists() {
        OffsetPagedRequest secondPageRequest = new OffsetPagedRequest(5, 0);
        assertEquals(0, secondPageRequest.getPageNumber());
        assertEquals(0, secondPageRequest.previous().getPageNumber());
    }

    // Tests that previous or first returns the previous page if it exists
    @Test
    void previousOrFirst() {
        OffsetPagedRequest secondPageRequest = new OffsetPagedRequest(5, 9);
        assertEquals(1, secondPageRequest.getPageNumber());
        assertEquals(0, secondPageRequest.previousOrFirst().getPageNumber());
    }

    // Tests that previous method does not return a page number below 0
    @Test
    void previousOrFirstNotExists() {
        OffsetPagedRequest secondPageRequest = new OffsetPagedRequest(5, 0);
        assertEquals(0, secondPageRequest.getPageNumber());
        assertEquals(0, secondPageRequest.previousOrFirst().getPageNumber());
    }

    // Tests we can instantly get a request for the first page
    @Test
    void first() {
        OffsetPagedRequest secondPageRequest = new OffsetPagedRequest(5, 50);
        assertEquals(10, secondPageRequest.getPageNumber());
        assertEquals(0, secondPageRequest.first().getPageNumber());
    }

    // Tests we can get a request to the specified page
    @Test
    void withPage() {
        OffsetPagedRequest secondPageRequest = new OffsetPagedRequest(5, 50);
        assertEquals(10, secondPageRequest.getPageNumber());

        // Offset should change to get us to the 20th page
        // pages * pageSize = offset -> 20 * 5 = 100
        assertEquals(100, secondPageRequest.withPage(20).getOffset());
        assertEquals(20, secondPageRequest.withPage(20).getPageNumber());
        assertEquals(5, secondPageRequest.withPage(20).getPageSize());
    }

    // Tests there is a previous page we can request from the current request
    @Test
    void hasPrevious() {
        OffsetPagedRequest secondPageRequest = new OffsetPagedRequest(5, 50);
        assertTrue(secondPageRequest.hasPrevious());
    }

    // Tests there is no previous page if we are on the last item of the current page
    // offset < limit = no previous
    @Test
    void hasPreviousFalse() {
        OffsetPagedRequest secondPageRequest = new OffsetPagedRequest(5, 4);
        assertFalse(secondPageRequest.hasPrevious());
    }
}