package org.c02e.plugin.pageantry

/**
 * Default pagenation calculator.
 * For example, when <code>offset=100, max=10, total=1000, maxPages=10</code>,
 * calculates <code>totalPages=100, currentPage=10, firstPage=6, lastPage=13</code> (0-based);
 * the <code>pages</code> tag would display it like this:
 * <samp>1 ... 7 8 9 10 <b>11</b> 12 13 14 ... 100</samp> (1-based)
 */
class DefaultPager implements Pager {

    /** 0-based offset. */
    int offset = 0
    /** Maximum rows per page. */
    int max = 10
    /** List of columns by which to sort. */
    List<String> sorting = []
    /**
     * List of asceding/descending values (false = ascend / true = descend).
     * Must be same length as {@link #sorting}.
     */
    List<Boolean> ordering = []

    /** Total number of rows, or -1 if unknown. */
    int total = -1
    /**
     * Name of column by which to sort as last,
     * regardless of request parameters (ie to always sort what would be
     * otherwise unsorted rows by some unique key).
     */
    String baseSort = ''
    /** Order for base sort (false = ascend / true = descend). */
    boolean baseOrder

    /** List of valid sort column names, or empty to not validate names. */
    List<String> validSort = []
    /** 
     * Map of public column names to literal column names.
     * If validSort not set, the keys of this map are used for validSort.
     */
    Map<String,String> translatedSort = [:]
    /** Maximum offset, or zero not to limit maximum offset. */
    int maxOffset = 0
    /** Maximum offset, or zero not to limit maximum max. */
    int maxMax = 1000
    /** Maximum number of pages to display in pagination. */
    int maxPages = 10

    /** Request-parameter prefix (like 'list1.'). */
    String prefix = ''
    /** Offset request-parameter name. */
    String offsetName = 'off'
    /** Max request-parameter name. */
    String maxName = 'max'
    /** Sort request-parameter name. */
    String sortName = 'sort'

    /** List of standard action params (like 'controller' or 'action'). */
    List<String> actionParams = ['controller', 'action', 'id']
    /**
     * Closure that quotes the specified column name.
     * For example, for mysql specify
     * { s -&gt; s.split(/\./).collect { "`$it`" }.join('.') }.
     */
    Closure quoteColumnName = { String s -> s }
    /**
     * Closure that translates a conceptual sort name to literal column name.
     * For example, this might translate 'total' to 'SUM(widget_count)'
     * or to 'production_summary.widget_count', etc.
     */
    Closure translateSortToColumn = { String s -> translatedSort[s] ?: s }
    /**
     * Closure that compares two rows in a list (a and b)
     * with the specified sort column and order.
     * @param a Row one.
     * @param b Row two.
     * @param sort Sort column.
     * @param order True for descending.
     * @return Negative number if a < b, positive number if a > b, 0 if a == b.
     */
    Closure rowComparator = { a, b, String sort, boolean order ->
        (a[sort] <=> b[sort]) * (order ? -1 : 1)
    }

    /**
     * Creates new default pager.
     * @param defaults Default settings for this pager (string names and typed values).
     * @param params Request parameters (string names and values).
     * @param config Global application settings for pager plugin (string names and values).
     * For example, this might be instantiated like:
     * <code>new DefaultPager(max:10, sort:'id', params,
     * grailsApplication.config.grails.plugin.pageantry)</code>.
     * It could also be instantiated like:
     * <code>new DefaultPager(params: request.params,
     * config: grailsApplication.config.grails.plugin.pageantry)</code>.
     */
    DefaultPager(Map defaults = [:], Map params = [:], Map config = [:]) {
        if (defaults.params && !params)
            params = defaults.params
        if (defaults.config && !config)
            config = defaults.config

        config.findAll { k,v -> this.hasProperty k }.each { k,v -> this[k] = v }
        defaults.findAll { k,v ->
            k != 'params' && k != 'config' && this.hasProperty(k)
        }.each { k,v -> this[k] = v }

        setParams params
    }

    void setOffset(int x) {
        offset = x

        currentPage = -1
        firstPage = -1
        lastPage = -1
    }

    void setMax(int x) {
        max = x

        totalPages = -1
        currentPage = -1
        firstPage = -1
        lastPage = -1
    }

    void setTotal(int x) {
        total = x

        totalPages = -1
        firstPage = -1
        lastPage = -1
    }

    void setMaxPages(int x) {
        maxPages = x

        firstPage = -1
        lastPage = -1
    }

    /** Primary sort column, or empty string. */
    String getSort() {
        sorting ? sorting[0] : ''
    }

    /** Sets primary sort column, replacing current primary sort column. */
    void setSort(String x) {
        if (sorting)
            sorting[0] = x
        else
            sorting << x
    }

    /** Primary order direction ('asc' / 'desc'). */
    String getOrder() {
        !ordering || !ordering[0] ? 'asc' : 'desc'
    }

    /** Sets primary order direction, replacing current primary order direction. */
    void setOrder(String x) {
        boolean o = x ==~ /(?i)^d.*/
        if (ordering)
            ordering[0] = o
        else
            ordering << o
    }

    List<String> getValidSort() {
        if (!validSort && translatedSort)
            validSort = translatedSort.keySet() as List
        return validSort
    }

    /** Map for GORM pagination (using translated sorting). */
    Map getMap() {
        def m = [:]

        if (offset > 0) m.offset = offset
        if (max > 0) m.max = max

        if (sort) {
            m.sort = translateSortToColumn(sort)
            m.order = order
        } else if (baseSort) {
            m.sort = translateSortToColumn(baseSort)
            m.order = !baseOrder ? 'asc' : 'desc'
        }

        return m
    }

    /** 
     * SQL ORDER and LIMIT clauses.
     * For safety, includes sorting values only if validSort has been set.
     */
    String getSql() {
        [sqlOrder, sqlLimit].findAll { it }.join(' ')
    }

    /** 
     * SQL ORDER clause.
     * For safety, includes sorting values only if validSort has been set.
     */
    String getSqlOrder() {
        def sql = []

        if (getValidSort())
            sorting.eachWithIndex { sort, index ->
                sql << (sql ? ',' : 'ORDER BY')
                def order = ordering.size() > index ? ordering[index] : ''
                def col = quoteColumnName(translateSortToColumn(sort))
                sql << " ${col} ${order ? 'DESC' : 'ASC'}"
            }

        if (baseSort)
            sql << """
                ${sql ? ',' : 'ORDER BY'}
                ${quoteColumnName(translateSortToColumn(baseSort))}
                ${baseOrder ? 'DESC' : 'ASC'}
            """.trim().replaceAll(/\s+/, ' ')

        sql.join('')
    }

    /** 
     * SQL LIMIT clause.
     */
    String getSqlLimit() {
        def sql = []

        if (max > 0)
            sql << "LIMIT ${max}"
        if (offset > 0)
            sql << "OFFSET ${offset}"

        sql.join(' ')
    }

    /**
     * Slices the specified full list with the current paging,
     * using the optional comparator to first sort the list.
     * For safety, sorts only if validSort has been set.
     * @param list Full list of rows.
     * @param comparator (optional) Closure to compare two rows
     * (defaults to {@link #rowComparator}).
     */
    List slice(Collection list, Closure comparator = null) {
        total = list?.size() ?: 0
        if (!max || offset >= total) return []

        if (sorting || baseSort) {
            comparator = comparator ?: rowComparator

            def translatedSorting = getValidSort() ? sorting.collect {
                translateSortToColumn it
            }: []
            def translatedBaseSort = translateSortToColumn(baseSort)

            // ensure ordering list is at least as long as sorting list
            if (sorting.size() - ordering.size() > 0)
                (sorting.size() - ordering.size()).times { ordering << false }

            list = list.sort(false) { a,b ->
                // loop through all sorting levels
                // and keep comparing while result is 0,
                // trying baseSort if all levels were 0
                def diff = 0, index = 0
                diff = translatedSorting.inject(diff) { diffMemo, sort ->
                    diffMemo ?: comparator(a, b, sort, ordering[index++])
                }
                if (!diff && translatedBaseSort)
                    diff = comparator(a, b, translatedBaseSort, baseOrder)
                return diff
            }
        }

        list[(offset)..(Math.min(offset + max, total) - 1)]
    }

    protected int totalPages = -1
    /** Total number of pages, or -1 if unknown. */
    int getTotalPages() {
        if (totalPages >= 0) return totalPages
        if (total < 0 || max < 0) return -1

        totalPages = Math.floor((total - 1) / max + 1)
        return totalPages
    }

    protected int currentPage = -1
    /** Current page number (0-based). */
    int getCurrentPage() {
        if (currentPage >= 0) return currentPage
        if (offset < 0 || max <= 0) return 0

        currentPage = Math.floor(offset / max)
        return currentPage
    }

    protected int firstPage = -1
    /** First page number to display in pagination (0-based). */
    int getFirstPage() {
        if (firstPage >= 0) return firstPage

        calculateFirstAndLastPage()
        return firstPage
    }

    protected int lastPage = -1
    /** Last page number to display in pagination (0-based). */
    int getLastPage() {
        if (lastPage >= 0) return lastPage

        calculateFirstAndLastPage()
        return lastPage
    }

    protected void calculateFirstAndLastPage() {
        int totalPages = getTotalPages()

        // if within max pages, first is 0 and last is total-1
        if (totalPages >= 0 && totalPages <= maxPages) {
            firstPage = 0
            lastPage = totalPages ? totalPages - 1 : 0
            return
        }

        // if more than maxPages, show half to left and half to right of currentPage
        int currentPage = getCurrentPage()
        int half = Math.round(maxPages / 2 - 1)
        firstPage = currentPage - half - ((maxPages + 1) % 2)
        lastPage = currentPage + half

        // shift range upward to at least start at 0
        if (firstPage < 0) {
            lastPage -= firstPage
            if (totalPages >= 0 && lastPage >= totalPages)
                lastPage = totalPages - 1

            firstPage = 0

        // shift range downward to not exceed total
        } else if (totalPages >= 0 && lastPage >= totalPages) {
            firstPage -= lastPage - totalPages + 1
            lastPage = totalPages - 1
        }

        // remove a consecutive page to make room for ellipsis
        if (lastPage - firstPage >= maxPages - 1) {
            if (firstPage > 0)
                firstPage++
            if (lastPage < totalPages - 1)
                lastPage--
        }
    }

    /** Sets request parameters (map of string names to string values). */
    void setParams(Map params) {
        if (!params) return

        setOffsetParam params."$prefix$offsetName"
        setMaxParam params."$prefix$maxName"
        setSortParam params."$prefix$sortName"
    }

    /** Sets offset from string request param. */
    void setOffsetParam(String s) {
        if (!(s ==~ /\d+/)) return

        def x = s as Integer
        setOffset maxOffset && x > maxOffset ? maxOffset : x
    }

    /** Sets offset from string request param. */
    void setMaxParam(String s) {
        if (!(s ==~ /\d+/)) return

        def x = s as Integer
        setMax maxMax && x > maxMax ? maxMax : x
    }

    /** Sets sorting and ordering from string request param. */
    void setSortParam(String s) {
        if (!s) return

        def validSort = getValidSort()
        sorting = []
        ordering = []

        (s =~ /([ -]+)?([^ -]+)/).findAll { match, order, sort ->
            !validSort || sort in validSort
        }.each { match, order, sort ->
            sorting << sort
            ordering << (order == '-')
        }
    }

    /*
     * Returns the sort parameter value needed to sort by the specified column.
     * @param sort Column to sort by.
     * @param single True to sort by only one column at a time (defaults to false).
     * @return Sort string (never null).
     */
    String sortForColumn(String column, boolean single = false) {
        if (!column) return ''

        // ensure ordering list is at least as long as sorting list
        if (sorting.size() - ordering.size() > 0)
            (sorting.size() - ordering.size()).times { ordering << false }

        def index = sorting.indexOf(column)
        // extract order corresponding to column
        def order = index >= 0 && ordering[index] ? '-' : ''
        // flip order if column is first
        if (index == 0)
            order = order ? '' : '-'
        def sort = ["$order$column"]

        if (!single)
            sorting.eachWithIndex { s, i ->
                if (i == index) return
                sort << "${ordering[i] ? '-' : ' '}${s}"
            }

        sort.join('')
    }

    /**
     * Creates an url to sort by the specified column.
     * @param sort Column to sort by.
     * @param single (optional) True to sort by only one column at a time (defaults to false).
     * @param path (optional) Request path (defaults to '').
     * @param params (optional) Request parameter map of other params to include in url.
     * @return String url (never null).
     */
    String urlForColumn(Map m) {
        if (!m) return ''

        def removeParams = actionParams + [
            "$prefix$offsetName" as String,
            "$prefix$maxName" as String,
            "$prefix$sortName" as String,
        ]
        def params = (m.params ?: [:]).findAll { k,v -> !(k in removeParams) }

        def sort = sortForColumn(m.sort, !!m.single)
        if (sort)
            params."$prefix$sortName" = sort
        if (max >= 0)
            params."$prefix$maxName" = max

        "${m.path?:''}?${joinParams(params)}"
    }

    /**
     * Creates an url to the specified row.
     * @param offset Row offset.
     * @param max (optional) Custom max (useful mainly for creating template urls).
     * @param path (optional) Request path (defaults to '').
     * @param params (optional) Request parameter map of other params to include in url.
     * @return String url (never null).
     */
    String urlForRow(Map m) {
        if (!m) return ''

        def removeParams = actionParams + [
            "$prefix$offsetName" as String,
            "$prefix$maxName" as String,
        ]
        def params = (m.params ?: [:]).findAll { k,v -> !(k in removeParams) }

        if (m.offset)
            params."$prefix$offsetName" = m.offset
        if (m.max)
            params."$prefix$maxName" = m.max
        else if (max >= 0)
            params."$prefix$maxName" = max

        "${m.path?:''}?${joinParams(params)}"
    }

    /**
     * Returns a string of formatted, escaped url parameters
     * for the specified map of attribute name,value pairs.
     * For example, <code>[foo:'bar', q:'where/what?']</code> returns:
     * <code>foo=bar&amp;where%2Fwhat%3F</code>
     */
    String joinParams(Map m) {
        if (!m) return ''

        // filter out any nested parameters
        m.findAll { k,v -> k && !(v instanceof Map) }.collect { k,v ->
            def name = k.toString().encodeAsURL()

            if (v instanceof Collection || v instanceof Object[])
                return v.collect {
                    def value = it?.toString()?.encodeAsURL() ?: ''
                    "${name}${value ? '=' : ''}${value}"
                }.join('&')

            def value = v?.toString()?.encodeAsURL() ?: ''
            "${name}${value ? '=' : ''}${value}"
        }.join('&')
    }
}
