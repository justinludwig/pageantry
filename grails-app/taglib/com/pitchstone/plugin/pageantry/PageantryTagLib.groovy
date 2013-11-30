package com.pitchstone.plugin.pageantry

import com.pitchstone.plugin.pagentry.HtmlUtil as H

class PageantryTagLib {
    static namespace = 'pageantry'

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
        ).trim().split(/\s+/)

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
}
