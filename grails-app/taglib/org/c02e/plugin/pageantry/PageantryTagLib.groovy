package org.c02e.plugin.pageantry

import org.c02e.plugin.pagentry.HtmlUtil as H

class PageantryTagLib {
    static namespace = 'pageantry'

    /**
     * Displays a table.
     * Echoes class, id, title, and data-* attrs.
     * @param in Collection of rows. The tbody tag is rendered once for each row.
     * @param template (optional) Template row.
     * The template is rendered as a separate table, directly below the main table,
     * with an id of "${id}-template".
     * @param var (optional) Row variable (defaults to "it").
     * @param status (optional) Row-index variable (defaults to "rowIndex").
     * @param caption (optional) Un-escaped text to render as table caption.
     * @param foot (optional) True duplicate header as footer to (defaults to false).
     * @param alternating (optional) Space-speparated list of classes
     * over which to altenatingly apply to table rows (defaults to "even odd").
     * @param pager (optional) Pager used for (optional) column header links.
     * @param SingleSort (optional) True to limit sort links
     * to a single sorting column * (defaults to false).
     */
    def table = { attrs, body ->
        def pager = this.pager = attrs.pager ?: this.pager
        tableAttrs = new LinkedHashMap(attrs)

        out << '<table' << H.attrs(
            attrs.findAll { k,v -> k ==~ /id|class|title|data-.+/ }
        ) << '>'
        tableAttrs.currentTag = 'table'
        if (attrs.caption)
            out << caption { out << attrs.caption.encodeAsHTML() }
        out << body()
        out << '</table>'

        // output template content rendered by tbody
        def template = tableAttrs.templateContent
        if (template)
            out << '<table' << H.attrs(
                id: attrs.id ? "${attrs.id}-template" : ''
            ) << '><tbody>' << template << '</tbody></table>'

        tableAttrs = null
    }

    /**
     * Displays a table caption.
     */
    def caption = { attrs, body ->
        out << '<caption>' << body() << '</caption>'
    }

    /**
     * Demarcates the header of a table.
     * The table header is rendered once for the header,
     * and optionally a second time for the footer.
     */
    def thead = { attrs, body ->
        def tableAttrs = this.tableAttrs
        def tags = tableAttrs.foot ? ['thead','tfoot'] : ['thead']

        tags.each { tag ->
            out << '<' << tag << '>'
            tableAttrs.currentTag = tag
            out << body()
            // close implicit <tr>
            if (tableAttrs.currentTag == 'tr')
                out << '</tr>'
            out << '</' << tag << '>'
            tableAttrs.currentTag = 'table'
        }
    }

    /**
     * Demarcates the body of a table.
     * The table body is rendered once for each row in the table
     * (plus once for the template row, if specified).
     */
    def tbody = { attrs, body ->
        def tableAttrs = this.tableAttrs

        out << '<tbody>'

        def var = tableAttrs.var ?: 'it'
        def status = tableAttrs.status ?: 'rowIndex'

        tableAttrs.currentRow = -1
        tableAttrs.in.each { row ->
            tableAttrs.currentRow++
            tableAttrs.currentCol = -1
            tableAttrs.currentTag = 'tbody'

            out << body((var): row, (status): tableAttrs.currentRow)
            // close implicit <tr>
            if (tableAttrs.currentTag == 'tr')
                out << '</tr>'
        }

        out << '</tbody>'
        tableAttrs.currentTag = 'table'

        // render template row now, store its output for later
        def template = tableAttrs.template
        if (template) {
            tableAttrs.currentCol = -1
            tableAttrs.currentRow = -1
            tableAttrs.currentTag = 'tbody'

            tableAttrs.templateContent = body((var): template, (status): -1) +
            // close implicit <tr>
            (tableAttrs.currentTag == 'tr' ? '</tr>' : '')
        }
    }

    /**
     * Displays a table row.
     * Echoes class, id, title, and data-* attrs.
     * May be omitted (a single, attributeless tr is implied
     * in thead or tbody if omitted).
     */
    def tr = { attrs, body ->
        def tableAttrs = this.tableAttrs
        def prevTag = tableAttrs.currentTag
        def cls = attrs.class ?: ''
        if (prevTag == 'tbody') {
            if (!(cls instanceof Collection))
                cls = cls.toString().trim().split(/\s+/) as List
            // add alternating row classes
            cls += [trClass]
        }

        out << '<tr' << H.attrs(
            [class: cls] + attrs.findAll { k,v -> k ==~ /id|title|data-.+/ }
        ) << '>'
        tableAttrs.currentTag = 'tr'
        out << body()
        out << '</tr>'
        tableAttrs.currentTag = prevTag
    }

    /**
     * Displays a table header cell.
     * Echoes class, id, title, data-*, scope, rowspan, colspan, and headers attrs.
     * Also appends "col" attr to class of this cell and corresponding body cells.
     * <p>
     * If "sort" attr is specified, wraps body with link to sort by the column
     * named by the "sort" attr.
     * Adds "sortable" as a class to cells with sort links; "sorted"
     * if the specified sort is in the pager's sorting (see {@link Pager#sorting});
     * and "primary" if the sort is first in the pager's sorting, or "secondary"
     * if the sort is later in the pager's sorting.
     * Also adds a "data-sort-ordinal" attr to the cell with the 1-based index
     * of the sort in the pager's sorting.
     */
    def th = { attrs, body ->
        def pager = this.pager
        def tableAttrs = this.tableAttrs

        def prevTag = tableAttrs.currentTag
        // open implicit <tr>
        if (prevTag != 'tr') {
            prevTag = 'tr'
            out << '<tr>'
        }

        def cls = attrs.class ?: ''
        if (!(cls instanceof Collection))
            cls = cls.toString().trim().split(/\s+/) as List

        def colClass = attrs.col ?: ''
        cls << colClass

        def colClasses = tableAttrs.colClasses
        if (!colClasses)
            tableAttrs.colClasses = colClasses = []
        colClasses << colClass

        def ordinal = null
        def sort = attrs.sort
        if (sort) {
            cls << 'sortable'

            ordinal = pager.sorting?.indexOf(sort)
            if (ordinal >= 0)
                cls << 'sorted' << (ordinal ? 'secondary' : 'primary') << (
                    pager.ordering?.size() >= ordinal && pager.ordering[ordinal] ?
                    'desc' : 'asc'
                )
        }

        out << '<th' << H.attrs(
            [
                class: cls,
                'data-sort-ordinal': ordinal >= 0 ? ordinal + 1 : null,
            ] + attrs.findAll { k,v ->
                k ==~ /id|title|data-.+|scope|rowspan|colspan|headers/
            }
        ) << '>'
        tableAttrs.currentTag = 'th'

        if (sort)
            out << '<a' << H.attrs(href: pager.urlForColumn(
                sort: sort, single: tableAttrs.singleSort, params: params,
            )) << '>'
        out << body()
        if (sort)
            out << '</a>'

        out << '</th>'
        tableAttrs.currentTag = prevTag
    }

    /**
     * Displays a table body cell.
     * Echoes class, id, title, data-*, rowspan, colspan, and headers attrs.
     */
    def td = { attrs, body ->
        def tableAttrs = this.tableAttrs
        def prevTag = tableAttrs.currentTag
        // open implicit <tr>
        if (prevTag != 'tr') {
            prevTag = 'tr'
            // add alternating row classes
            out << '<tr' << H.attrs(class: trClass) << '>'
        }

        tableAttrs.currentCol = tableAttrs.currentCol != null ?
            tableAttrs.currentCol + 1 : 0

        // add classes from <th>
        def cls = attrs.class ?: ''
        if (tableAttrs.currentCol < tableAttrs.colClasses?.size()) {
            if (!(cls instanceof Collection))
                cls = cls.toString().trim().split(/\s+/) as List
            cls << tableAttrs.colClasses[tableAttrs.currentCol]
        }

        // allow th to be used for tag
        def tag = attrs.tag ?: 'td'

        out << '<' << tag << H.attrs(
            [class: cls] + attrs.findAll { k,v ->
                k ==~ /id|title|data-.+|scope|rowspan|colspan|headers/
            }
        ) << '>'
        tableAttrs.currentTag = 'td'
        out << body()
        out << '</' << tag << '>'
        tableAttrs.currentTag = prevTag
    }

    /**
     * Executes body if no pager or pager total == 0.
     */
    def empty = { attrs, body ->
        def pager = this.pager = attrs.pager ?: this.pager
        if (pager?.total) return

        out << body()
    }

    /**
     * Executes body if pager total != 0.
     */
    def notempty = { attrs, body ->
        def pager = this.pager = attrs.pager ?: this.pager
        if (!pager?.total) return

        out << body()
    }

    /**
     * Outputs wapper div/menu html.
     */
    def container = { attrs, body ->
        def inContainer = request.inPageantryContainer

        if (!inContainer) {
            out << containerStart
            request.inPageantryContainer = true
        }

        out << body()

        if (!inContainer) {
            request.inPageantryContainer = false
            out << containerEnd
        }
    }

    /**
     * Outputs container item (eg li) html.
     */
    def item = { attrs, body ->
        def inContainer = request.inPageantryContainer

        if (inContainer)
            out << getItemStart(attrs.css)

        out << body()

        if (inContainer)
            out << itemEnd
    }

    /**
     * Displays prev/next links.
     * @param prev (optional) Text for prev link; defaults to
     * 'grails.plugin.pageantry.prevnext.prev' message key or '&laquo; Prev'.
     * @param next (optional) Text for next link; defaults to
     * 'grails.plugin.pageantry.prevnext.next' message key or 'Next &raquo;'.
     */
    def prevnext = { attrs, body ->
        def pager = this.pager = attrs.pager ?: this.pager
        if (!pager) return

        def prev = g.message(
            code: 'grails.plugin.pageantry.prevnext.prev',
            default: attrs.prev ?: '\u00ab Prev',
            encodeAs: 'HTML',
        )
        def next = g.message(
            code: 'grails.plugin.pageantry.prevnext.next',
            default: attrs.next ?: 'Next \u00bb',
            encodeAs: 'HTML',
        )

        out << container(attrs) {
            if (pager.offset > 0) {
                out << item(css: 'previous') {
                    out << '<a' << H.attrs(
                        href: pager.urlForRow(
                            offset: Math.max(0, pager.offset - pager.max),
                            params: params,
                        )
                    ) << '>'<< prev << '</a>'
                }
            } else {
                out << item(css: 'previous disabled') {
                    out << '<span>' << prev << '</span>'
                }
            }

            out << body()

            if (pager.with { total < 0 || (max > 0 && offset + max < total) }) {
                out << item(css: 'next') {
                    out << '<a' << H.attrs(
                        href: pager.urlForRow(
                            offset: pager.offset + pager.max,
                            params: params,
                        )
                    ) << '>' << next << '</a>'
                }
            } else {
                out << item(css: 'next disabled') {
                    out << '<span>' << next << '</span>'
                }
            }
        }
    }

    /**
     * Displays page links between pager.firstPage and pager.lastPage, inclusive.
     * @param firstlast (optional) True to always show links to overall
     * first and last pages (eg 1..11 12 13..100 instead of ..11 12 13..).
     * Defaults to true.
     * @param more (optional) Text for "more" spacers (...); defaults to
     * 'grails.plugin.pageantry.pages.more' message key or '&hellip;'.
     */
    def pages = { attrs, body ->
        def pager = this.pager = attrs.pager ?: this.pager
        if (!pager) return

        out << container(attrs) {
            // add 1...
            if (pager.firstPage > 0) {
                if (attrs.firstlast as String != 'false')
                    out << item([:]) {
                        out << '<a' << H.attrs(
                            href: pager.urlForRow(
                                offset: 0,
                                params: params,
                            )
                        ) << '>1</a>'
                    }
                out << item(css: 'more disabled') {
                    out << '<span>' << g.message(
                        code: 'grails.plugin.pageantry.pages.more',
                        default: attrs.more ?: '\u2026',
                        encodeAs: 'HTML',
                    ) << '</span>'
                }
            }

            // firstPage to lastPage
            for (def i = pager.firstPage; i <= pager.lastPage; i++)
                if (i == pager.currentPage) {
                    out << item(css: 'active') {
                        out << '<span>' << (i + 1) << '</span>'
                    }
                } else {
                    out << item([:]) {
                        out << '<a' << H.attrs(
                            href: pager.urlForRow(
                                offset: i * pager.max,
                                params: params,
                            )
                        ) << '>' << (i + 1) << '</a>'
                    }
                }

            // add ...n
            if (pager.totalPages < 0 || pager.lastPage < pager.totalPages - 1) {
                out << item(css: 'more disabled') {
                    out << '<span>' << g.message(
                        code: 'grails.plugin.pageantry.pages.more',
                        default: attrs.more ?: '\u2026',
                        encodeAs: 'HTML',
                    ) << '</span>'
                }
                if (pager.totalPages > 0 && attrs.firstlast as String != 'false')
                    out << item([:]) {
                        out << '<a' << H.attrs(
                            href: pager.urlForRow(
                                offset: (pager.totalPages - 1) * pager.max,
                                params: params,
                            )
                        ) << '>' << pager.totalPages << '</a>'
                    }
            }
        }
    }

    /**
     * Displays total message.
     * @param msg (optional) Text for standard message; defaults to
     * 'grails.plugin.pageantry.total.msg' message key or '{0} - {1} of {2}'
     * (ie 'START - END of TOTAL').
     * @param unknown (optional) Text for message with unknown total; defaults to
     * 'grails.plugin.pageantry.total.unknown' message key or '{0} - {1}'
     * (ie 'START - END').
     * @param empty (optional) Text for message when empty; defaults to
     * 'grails.plugin.pageantry.total.empty' message key, or re-uses standard message
     * (ie '0 - 0 of 0').
     */
    def total = { attrs, body ->
        def pager = this.pager = attrs.pager ?: this.pager
        if (!pager) return

        def msg = ''

        if (pager.total < 0)
            msg = g.message(
                code: 'grails.plugin.pageantry.total.unknown',
                default: attrs.unknown ?: '{0} - {1}',
                // assume full range; eg 1 - 10
                args: [pager.offset + 1, pager.offset + pager.max],
                encodeAs: 'HTML',
            )
        else if (pager.total == 0)
            msg = g.message(
                code: 'grails.plugin.pageantry.total.empty',
                default: attrs.empty ?: '',
                encodeAs: 'HTML',
            )

        // if total > 0, or total == 0 and no empty message
        if (!msg)
            msg = g.message(
                code: 'grails.plugin.pageantry.total.msg',
                default: attrs.msg ?: '{0} - {1} of {2}',
                args: [
                    Math.min(pager.offset + 1, pager.total),
                    Math.min(pager.offset + pager.max, pager.total),
                    pager.total,
                ],
                encodeAs: 'HTML',
            )

        out << item(css: 'total') {
            out << '<span>' << msg << '</span>'
        }
    }

    /**
     * Displays dropdown to change max.
     * @param msg (optional) Text for dropdown label; defaults to
     * 'grails.plugin.pageantry.resize.msg' message key or 'Items Per Page:'.
     * @param sizes (optional) Space separated size options; defaults to
     * 'grails.plugin.pageantry.total.sizes' message key or '10 20 50 100 All'.
     * @param all (optional) Max value for 'All' option; defaults to
     * pager.maxMax or 1000.
     * @param name (optional) Dropdown name; defaults to
     * "${pager.prefix}${pager.maxName}".
     * @param onchange (optional) Onchange handler; defaults to
     * a basic page-changing script.
     */
    def resize = { attrs, body ->
        def pager = this.pager = attrs.pager ?: this.pager
        if (!pager) return

        def msg = attrs.msg == '' ? '' : g.message(
            code: 'grails.plugin.pageantry.resize.msg',
            default: attrs.msg ?: 'Items Per Page:',
            encodeAs: 'HTML',
        )
        def sizes = g.message(
            code: 'grails.plugin.pageantry.resize.sizes',
            default: attrs.sizes ?: '10 20 50 100 All',
            encodeAs: 'HTML',
        ).trim().split(/\s+/) as List

        def all = attrs.all ?: pager.hasProperty('maxMax') ? pager.maxMax : 1000
        def selected = pager.max as String

        out << item(css: 'max') {
            if (msg)
                out << '<span>' << msg << '</span>'

            out << '<select' << H.attrs(
                name: attrs.name ?: pager.hasProperty('maxName') ? (
                    pager.hasProperty('prefix') ?
                        "${pager.prefix}${pager.maxName}" : pager.maxName
                ) : '',
                'data-href': pager.urlForRow(
                    offset: pager.offset,
                    max: 'MAX',
                    params: params,
                ),
                onchange: attrs.onchange == '' ? '' : attrs.onchange ?: '''
                    location.href=this.attributes['data-href'].value.replace(/MAX/,this.value)
                '''.trim(),
            ) << '>'

            sizes.each { size ->
                out << '<option' << H.attrs(
                    value: size ==~ /\d+/ ? '' : all,
                    selected: size == selected,
                ) << '>' << size << '</option>'
            }

            out << '</select>'
        }
    }


    protected String getContainerStart() {
        '<div class="pagination"><menu>'
    }

    protected String getContainerEnd() {
        '</menu></div>'
    }

    protected String getItemStart(css = null) {
        css ?  "<li class=\"${css}\">" : '<li>'
    }

    protected String getItemEnd() {
        '</li>'
    }

    protected Pager getPager() {
        request.pager
    }

    protected void setPager(Pager x) {
        request.pager = x
    }

    protected Map getTableAttrs() {
        request.pageantryTableAttrs
    }

    protected void setTableAttrs(Map x) {
        request.pageantryTableAttrs = x
    }

    protected String getTrClass() {
        def alt = tableAttrs.alternating
        if (alt == '') return ''

        if (!(alt instanceof Collection))
            tableAttrs.alternating = alt =
                (alt ?: 'even odd').toString().trim().split(/\s+/) as List

        alt[(tableAttrs.currentRow?:0) % alt.size()]
    }
}
