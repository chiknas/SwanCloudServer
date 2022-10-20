package com.chiknas.swancloudserver.cursorpagination;

import java.util.List;

/**
 * @author nkukn
 * @since 2/7/2021
 */
public interface CursorPage<T> {

    String getNextCursor();

    List<T> getNodes();
}
