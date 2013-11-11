package com.pitchstone.plugin.pageantry

/**
 * Pagenation calculator.
 * For example, when <code>offset=100, max=10, total=1000, maxPages=10</code>,
 * calculates <code>totalPages=100, currentPage=10, firstPage=6, lastPage=13</code> (0-based);
 * the <code>pages</code> tag would display it like this:
 * <samp>1 ... 7 8 9 10 <b>11</b> 12 13 14 ... 100</samp> (1-based)
 */
interface Pager {

    /** 0-based offset. */
    int offset
    /** Maximum rows per page. */
    int max
    /** List of columns by which to sort. */
    List<String> sorting
    /** List of asceding/descending values (false = ascend / true = descend). */
    List<Boolean> ordering

    /** Total number of rows, or -1 if unknown. */
    int total

    /** Primary sort column, or empty string. */
    String getSort()
    /** Primary order direction ('asc' / 'desc'). */
    String getOrder()

    /** Map for GORM pagination, with 'offset', 'max', 'sort', and 'order' keys. */
    Map getMap()
    /** SQL ORDER and LIMIT clauses. */
    String getSql()

    /** Total number of pages, or -1 if unknown. */
    int getTotalPages()
    /** Current page number (0-based). */
    int getCurrentPage()
    /** First page number to display in pagination (0-based). */
    int getFirstPage()
    /** Last page number to display in pagination (0-based). */
    int getLastPage()

    /**
     * Creates an url to sort by the specified column.
     * @param sort Column to sort by.
     * @param single (optional) True to sort by only one column at a time (defaults to false).
     * @param path (optional) Request path (defaults to '').
     * @param params (optional) Request parameter map of other params to include in url.
     * @return String url (never null).
     */
    String urlForColumn(Map m)

    /**
     * Creates an url to the specified row.
     * @param offset Row offset.
     * @param path (optional) Request path (defaults to '').
     * @param params (optional) Request parameter map of other params to include in url.
     * @return String url (never null).
     */
    String urlForRow(Map m)
}
