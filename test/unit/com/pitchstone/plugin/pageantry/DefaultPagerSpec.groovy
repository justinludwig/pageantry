package com.pitchstone.plugin.pageantry

import grails.test.mixin.support.GrailsUnitTestMixin
import org.codehaus.groovy.grails.plugins.codecs.URLCodec
import spock.lang.Specification
import spock.lang.Unroll

@Mixin(GrailsUnitTestMixin)
class DefaultPagerSpec extends Specification {

    static List TEST_ROWS = (0..99).collect { [
        id: it, name: it as String, letter: ((it % 26) + 65) as Character,
    ] }

    def setup() {
        mockCodec URLCodec
    }

    def "empty pager uses class defaults"() {
        when: def pager = new DefaultPager()
        then:
            pager.offset == 0
            pager.max == 10
            pager.sorting == []
            pager.ordering == []
    }

    def "when passed config, pager uses configured defaults"() {
        when: def pager = new DefaultPager([:], [:], new ConfigObject(
            max: 100,
            maxMax: 2000,
            maxPages: 3,
        ))
        then:
            pager.max == 100
            pager.maxMax == 2000
            pager.maxPages == 3
    }

    def "when passed defaults and config, pager uses passed and configured defaults"() {
        when: def pager = new DefaultPager([
            max: 10,
            maxMax: 200,
        ], [:], new ConfigObject(
            max: 100,
            maxMax: 2000,
            maxPages: 3,
        ))
        then:
            pager.max == 10
            pager.maxMax == 200
            pager.maxPages == 3
    }

    def "when passed params, defaults, and config, pager uses params and passed/configured defaults"() {
        when: def pager = new DefaultPager([
            max: 10,
            maxMax: 200,
        ], [
            max: '50',
        ], new ConfigObject(
            max: 100,
            maxMax: 2000,
            maxPages: 3,
        ))
        then:
            pager.max == 50
            pager.maxMax == 200
            pager.maxPages == 3
    }


    def "pager ignores offset from blank param"() {
        expect: new DefaultPager(params: [off: '']).offset == 0
    }

    def "pager sets offset from positive integer param"() {
        expect: new DefaultPager(params: [off: '100']).offset == 100
    }

    def "pager ignores offset from negative integer param"() {
        expect: new DefaultPager(params: [off: '-100']).offset == 0
    }

    def "pager ignores offset from decimal param"() {
        expect: new DefaultPager(params: [off: '100.0']).offset == 0
    }

    def "pager ignores offset from non-number param"() {
        expect: new DefaultPager(params: [off: 'foo']).offset == 0
    }

    def "pager sets offset from custom prefix"() {
        expect: new DefaultPager(['foo.off': '100'], prefix: 'foo.').offset == 100
    }

    def "pager sets offset from custom param"() {
        expect: new DefaultPager([foo: '100'], offsetName: 'foo').offset == 100
    }

    def "pager cannot set offset above maxOffset"() {
        expect: new DefaultPager([off: '100'], maxOffset: 99).offset == 99
    }


    def "pager ignores max from blank param"() {
        expect: new DefaultPager(params: [max: '']).max == 10
    }

    def "pager sets max from positive integer param"() {
        expect: new DefaultPager(params: [max: '100']).max == 100
    }

    def "pager ignores max from negative integer param"() {
        expect: new DefaultPager(params: [max: '-100']).max == 10
    }

    def "pager ignores max from decimal param"() {
        expect: new DefaultPager(params: [max: '100.0']).max == 10
    }

    def "pager ignores max from non-number param"() {
        expect: new DefaultPager(params: [max: 'foo']).max == 10
    }

    def "pager sets max from custom prefix"() {
        expect: new DefaultPager(params: ['foo.max': '100'], prefix: 'foo.').max == 100
    }

    def "pager sets max from custom param"() {
        expect: new DefaultPager([foo: '100'], maxName: 'foo').max == 100
    }

    def "pager cannot set max above maxMax"() {
        expect: new DefaultPager([max: '100'], maxMax: 99).max == 99
    }


    def "pager ignores sort from blank param"() {
        when: def pager = new DefaultPager(params: [sort: ''])
        then:
            pager.sorting == []
            pager.ordering == []
    }

    def "pager sets sorting from column name"() {
        when: def pager = new DefaultPager(params: [sort: 'foo'])
        then:
            pager.sorting == ['foo']
            pager.ordering == [false]
    }

    def "pager sets sorting from column name ascending"() {
        when: def pager = new DefaultPager(params: [sort: ' foo'])
        then:
            pager.sorting == ['foo']
            pager.ordering == [false]
    }

    def "pager sets sorting from column name descending"() {
        when: def pager = new DefaultPager(params: [sort: '-foo'])
        then:
            pager.sorting == ['foo']
            pager.ordering == [true]
    }

    def "pager sets sorting from multiple columns"() {
        when: def pager = new DefaultPager(params: [sort: 'foo bar baz'])
        then:
            pager.sorting == ['foo', 'bar', 'baz']
            pager.ordering == [false, false, false]
    }

    def "pager sets sorting from multiple ascending"() {
        when: def pager = new DefaultPager(params: [sort: ' foo bar baz'])
        then:
            pager.sorting == ['foo', 'bar', 'baz']
            pager.ordering == [false, false, false]
    }

    def "pager sets sorting from multiple descending"() {
        when: def pager = new DefaultPager(params: [sort: '-foo-bar-baz'])
        then:
            pager.sorting == ['foo', 'bar', 'baz']
            pager.ordering == [true, true, true]
    }

    def "pager sets sorting from multiple alternating"() {
        when: def pager = new DefaultPager(params: [sort: ' foo-bar baz'])
        then:
            pager.sorting == ['foo', 'bar', 'baz']
            pager.ordering == [false, true, false]
    }

    def "with ambiguous order values, pager sets order to ascending"() {
        when: def pager = new DefaultPager(params: [sort: '- foo---bar  -baz'])
        then:
            pager.sorting == ['foo', 'bar', 'baz']
            pager.ordering == [false, false, false]
    }

    def "pager sets only valid sort columns"() {
        when: def pager = new DefaultPager(
            params: [sort: 'foo-bar baz'],
            validSort: ['bar', 'baz'],
        )
        then:
            pager.sorting == ['bar', 'baz']
            pager.ordering == [true, false]
    }


    def "when sort set to null, sort is null"() {
        setup: def pager = new DefaultPager()
        when: pager.sort = null
        then:
            pager.sort == null
            pager.sorting == [null]
            pager.ordering == []
    }

    def "when sort set to blank, sort is blank"() {
        setup: def pager = new DefaultPager()
        when: pager.sort = ''
        then:
            pager.sort == ''
            pager.sorting == ['']
            pager.ordering == []
    }

    def "when sort set to column name, sort is column name"() {
        setup: def pager = new DefaultPager()
        when: pager.sort = 'foo'
        then:
            pager.sort == 'foo'
            pager.sorting == ['foo']
            pager.ordering == []
    }

    def "when existing one-column sort set to column name, sort is column name"() {
        setup: def pager = new DefaultPager(sorting: ['bar'], ordering: [true])
        when: pager.sort = 'foo'
        then:
            pager.sort == 'foo'
            pager.sorting == ['foo']
            pager.ordering == [true]
    }

    def "when existing multi-column sort set to column name, sort is first column name"() {
        setup: def pager = new DefaultPager(
            sorting: ['bar', 'baz'],
            ordering: [true, false],
        )
        when: pager.sort = 'foo'
        then:
            pager.sort == 'foo'
            pager.sorting == ['foo', 'baz']
            pager.ordering == [true, false]
    }


    def "when order set to null, order is null"() {
        setup: def pager = new DefaultPager()
        when: pager.order = null
        then:
            pager.order == 'asc'
            pager.ordering == [false]
            pager.sorting == []
    }

    def "when order set to blank, order is blank"() {
        setup: def pager = new DefaultPager()
        when: pager.order = ''
        then:
            pager.order == 'asc'
            pager.ordering == [false]
            pager.sorting == []
    }

    def "when order set to ascending, order is ascending"() {
        setup: def pager = new DefaultPager()
        when: pager.order = 'asc'
        then:
            pager.order == 'asc'
            pager.ordering == [false]
            pager.sorting == []
    }

    def "when order set to descending, order is descending"() {
        setup: def pager = new DefaultPager()
        when: pager.order = 'desc'
        then:
            pager.order == 'desc'
            pager.ordering == [true]
            pager.sorting == []
    }

    def "when existing one-column order set to ascending, order is ascending"() {
        setup: def pager = new DefaultPager(sorting: ['foo'], ordering: [true])
        when: pager.order = 'asc'
        then:
            pager.order == 'asc'
            pager.ordering == [false]
            pager.sorting == ['foo']
    }

    def "when existing one-column order set to descending, order is descending"() {
        setup: def pager = new DefaultPager(sorting: ['foo'], ordering: [false])
        when: pager.order = 'desc'
        then:
            pager.order == 'desc'
            pager.ordering == [true]
            pager.sorting == ['foo']
    }

    def "when existing multi-column order set to ascending, first order is ascending"() {
        setup: def pager = new DefaultPager(
            sorting: ['foo', 'bar'],
            ordering: [true, true],
        )
        when: pager.order = 'asc'
        then:
            pager.order == 'asc'
            pager.ordering == [false, true]
            pager.sorting == ['foo', 'bar']
    }

    def "when existing multi-column order set to descending, first order is descending"() {
        setup: def pager = new DefaultPager(
            sorting: ['foo', 'bar'],
            ordering: [false, false],
        )
        when: pager.order = 'desc'
        then:
            pager.order == 'desc'
            pager.ordering == [true, false]
            pager.sorting == ['foo', 'bar']
    }

    def "when order set to any d word, order is descending; otherwise order is ascending"() {
        setup: def pager = new DefaultPager()

        when: pager.order = 'd'
        then: pager.order == 'desc'

        when: pager.order = 'a'
        then: pager.order == 'asc'

        when: pager.order = 'D'
        then: pager.order == 'desc'

        when: pager.order = 'A'
        then: pager.order == 'asc'

        when: pager.order = 'DESC'
        then: pager.order == 'desc'

        when: pager.order = 'ASC'
        then: pager.order == 'asc'

        when: pager.order = 'descending'
        then: pager.order == 'desc'

        when: pager.order = 'ascending'
        then: pager.order == 'asc'

        when: pager.order = 'Descending'
        then: pager.order == 'desc'

        when: pager.order = 'Ascending'
        then: pager.order == 'asc'

        when: pager.order = 'd7'
        then: pager.order == 'desc'

        when: pager.order = 'x7'
        then: pager.order == 'asc'
    }


    def "default map has default max"() {
        expect: new DefaultPager().map == [max: 10]
    }

    def "when max is zeroed, map is empty"() {
        expect: new DefaultPager(max: 0).map == [:]
    }

    def "when offset and max specified, map has offset and max"() {
        expect: new DefaultPager(offset: 40, max: 20).map == [offset: 40, max: 20]
    }

    def "when sort specified, map has sort, order, and default max"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
        ).map == [max: 10, sort: 'foo', order: 'asc']
    }

    def "when order specified, map has default max"() {
        expect: new DefaultPager(
            ordering: [true, false, true],
        ).map == [max: 10]
    }

    def "when sort and order specified, map has sort, order, and default max"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            ordering: [true, false, true],
        ).map == [max: 10, sort: 'foo', order: 'desc']
    }

    def "when baseSort specified, map has sort, order, and default max"() {
        expect: new DefaultPager(
            baseSort: 'foo',
        ).map == [max: 10, sort: 'foo', order: 'asc']
    }

    def "when baseOrder specified, map has default max"() {
        expect: new DefaultPager(
            baseOrder: true,
        ).map == [max: 10]
    }

    def "when baseSort and baseOrder specified, map has sort, order, and default max"() {
        expect: new DefaultPager(
            baseSort: 'foo',
            baseOrder: true,
        ).map == [max: 10, sort: 'foo', order: 'desc']
    }

    def "when sorting and baseSort and baseOrder specified, map has sort, order, and default max"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            baseSort: 'bar',
            baseOrder: true,
        ).map == [max: 10, sort: 'foo', order: 'asc']
    }


    def "default sql has default max"() {
        expect: new DefaultPager().sql == 'LIMIT 10'
    }

    def "when max is zeroed, sql is empty"() {
        expect: new DefaultPager(max: 0).sql == ''
    }

    def "when offset and max specified, sql has offset and max"() {
        expect: new DefaultPager(offset: 40, max: 20).sql == 'LIMIT 20 OFFSET 40'
    }

    def "when sort specified without validSort, sql has default max"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
        ).sql == 'LIMIT 10'
    }

    def "when sort specified with validSort, sql has sort, order, and default max"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            validSort: [''],
        ).sql == 'ORDER BY foo ASC, bar ASC, baz ASC LIMIT 10'
    }

    def "when sort specified with quote closure, sql has quoted sort, order, and default max"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            validSort: [''],
            config: [quoteColumnName: { "`$it`" }],
        ).sql == 'ORDER BY `foo` ASC, `bar` ASC, `baz` ASC LIMIT 10'
    }

    def "when order specified, sql has default max"() {
        expect: new DefaultPager(
            ordering: [true, false, true],
        ).sql == 'LIMIT 10'
    }

    def "when sort and order specified, sql has sort, order, and default max"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            ordering: [true, false, true],
            validSort: [''],
        ).sql == 'ORDER BY foo DESC, bar ASC, baz DESC LIMIT 10'
    }

    def "when baseSort specified, sql has sort, order, and default max"() {
        expect: new DefaultPager(
            baseSort: 'foo',
        ).sql == 'ORDER BY foo ASC LIMIT 10'
    }

    def "when baseSort specified with quote closure, sql has quoted sort, order, and default max"() {
        expect: new DefaultPager(
            baseSort: 'foo',
            config: [quoteColumnName: { "`$it`" }],
        ).sql == 'ORDER BY `foo` ASC LIMIT 10'
    }

    def "when baseOrder specified, sql has default max"() {
        expect: new DefaultPager(
            baseOrder: true,
        ).sql == 'LIMIT 10'
    }

    def "when baseSort and baseOrder specified, sql has sort, order, and default max"() {
        expect: new DefaultPager(
            baseSort: 'foo',
            baseOrder: true,
        ).sql == 'ORDER BY foo DESC LIMIT 10'
    }

    def "when sorting and baseSort and baseOrder specified, sql has baseSort as final ORDER BY"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            validSort: [''],
            baseSort: 'bar',
            baseOrder: true,
        ).sql == 'ORDER BY foo ASC, bar ASC, baz ASC, bar DESC LIMIT 10'
    }


    def "slice of null list is empty"() {
        expect: new DefaultPager().slice(null) == []
    }

    def "slice of empty list is empty"() {
        expect: new DefaultPager().slice([]) == []
    }

    def "slice of test list with defaults"() {
        expect: new DefaultPager().slice(TEST_ROWS)*.id == (0..9).collect { it }
    }

    def "when max is zeroed, slice is empty"() {
        expect: new DefaultPager(max: 0).slice(TEST_ROWS) == []
    }

    def "when offset beyond list end, slice is empty"() {
        expect: new DefaultPager(offset: 100).slice(TEST_ROWS) == []
    }

    def "when offset and max specified, slice is shifted"() {
        expect: new DefaultPager(offset: 40, max: 20).slice(TEST_ROWS)*.id ==
            (40..59).collect { it }
    }

    def "when sort specified without valid sort, slice does not sort"() {
        expect: new DefaultPager(
            sorting: ['letter', 'name', 'id'],
        ).slice(TEST_ROWS)*.id == (0..9).collect { it }
    }

    def "when sort specified with valid sort, slice sorts then slices"() {
        expect: new DefaultPager(
            sorting: ['letter', 'name', 'id'],
            validSort: [''],
        ).slice(TEST_ROWS)*.id == [0, 26, 52, 78, 1, 27, 53, 79, 2, 28]
    }

    def "when order specified, slice does not sort"() {
        expect: new DefaultPager(
            ordering: [true, false, true],
            validSort: [''],
        ).slice(TEST_ROWS)*.id == (0..9).collect { it }
    }

    def "when sort and order specified, slice sorts then slices"() {
        expect: new DefaultPager(
            sorting: ['letter', 'name', 'id'],
            ordering: [true, false, true],
            validSort: [''],
        ).slice(TEST_ROWS)*.id == [25, 51, 77, 24, 50, 76, 23, 49, 75, 22]
    }

    def "when baseSort specified, slice sorts then slices"() {
        expect: new DefaultPager(
            baseSort: 'letter',
        ).slice(TEST_ROWS)*.id == [0, 26, 52, 78, 1, 27, 53, 79, 2, 28]
    }

    def "when baseOrder specified, slice does not sort"() {
        expect: new DefaultPager(
            baseOrder: true,
        ).slice(TEST_ROWS)*.id == (0..9).collect { it }
    }

    def "when baseSort and basOrder specified, slice sorts then slices"() {
        expect: new DefaultPager(
            baseSort: 'letter',
            baseOrder: true,
        ).slice(TEST_ROWS)*.id == [25, 51, 77, 24, 50, 76, 23, 49, 75, 22]
    }

    def "when sorting and baseSort and basOrder specified, slice sorts then slices"() {
        expect: new DefaultPager(
            sorting: ['letter'],
            ordering: [true],
            validSort: [''],
            baseSort: 'name',
            baseOrder: true,
        ).slice(TEST_ROWS)*.id == [77, 51, 25, 76, 50, 24, 75, 49, 23, 74]
    }

    def "slice sorts with specified comparator"() {
        setup:
            def defaultComparator = new DefaultPager().rowComparator
            def customComparator = { a, b, String sort, boolean order ->
                switch (sort) {
                    case 'foo': sort = 'letter'; break
                    case 'bar': sort = 'name'; break
                    case 'baz': sort = 'id'; break
                    default: return 0
                }
                defaultComparator a, b, sort, order
            }
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            ordering: [true, false, true],
            validSort: [''],
        ).slice(TEST_ROWS, customComparator)*.id ==
            [25, 51, 77, 24, 50, 76, 23, 49, 75, 22]
    }


    def "when total not set, totalPages is -1"() {
        expect: new DefaultPager().totalPages == -1
    }

    def "when total is zero, totalPages is 0"() {
        expect: new DefaultPager(total: 0).totalPages == 0
    }

    def "when total is greater than zero and less than or equal to max, totalPages is 1"() {
        expect:
            new DefaultPager(max: max, total: total).totalPages == pages
        where:
            max | total | pages
             10 |     1 |     1
             10 |     9 |     1
             10 |    10 |     1
              3 |     1 |     1
              3 |     2 |     1
              3 |     3 |     1
    }

    def "when total is greater than max, totalPages is greater than 1"() {
        expect:
            new DefaultPager(max: max, total: total).totalPages == pages
        where:
            max | total | pages
             10 |    11 |     2
             10 |    20 |     2
             10 |    21 |     3
              3 |     4 |     2
              3 |     5 |     2
              3 |     6 |     2
              3 |     7 |     3
    }

    def "updating total updates totalPages"() {
        when: def pager = new DefaultPager(total: 1, max: 10)
        then: pager.totalPages == 1
        when: pager.total = 100
        then: pager.totalPages == 10
    }

    def "updating max updates totalPages"() {
        when: def pager = new DefaultPager(total: 100, max: 10)
        then: pager.totalPages == 10
        when: pager.max = 100
        then: pager.totalPages == 1
    }


    def "when offset is less than  max, currentPage is 0"() {
        expect:
            new DefaultPager(offset: offset, max: max).currentPage == page
        where:
            offset | max | page
                 0 |  10 |    0
                 1 |  10 |    0
                 9 |  10 |    0
                 0 |   3 |    0
                 1 |   3 |    0
                 2 |   3 |    0
    }

    def "when offset is greater than or equal to max, currentPage is greater than 0"() {
        expect:
            new DefaultPager(offset: offset, max: max).currentPage == page
        where:
            offset | max | page
                10 |  10 |    1
                19 |  10 |    1
                20 |  10 |    2
                 3 |   3 |    1
                 4 |   3 |    1
                 5 |   3 |    1
                 6 |   3 |    2
    }

    def "updating offset updates currentPage"() {
        when: def pager = new DefaultPager(offset: 0, max: 10)
        then: pager.currentPage == 0
        when: pager.offset = 100
        then: pager.currentPage == 10
    }

    def "updating max updates currentPage"() {
        when: def pager = new DefaultPager(offset: 100, max: 10)
        then: pager.currentPage == 10
        when: pager.max = 100
        then: pager.currentPage == 1
    }


    def "when total not set, firstPage is 0 and lastPage at maxPages"() {
        when: def pager = new DefaultPager()
        then:
            pager.firstPage == 0
            pager.lastPage == 9
    }

    def "when total is zero, firstPage and lastPage is 0"() {
        when: def pager = new DefaultPager(total: 0)
        then:
            pager.firstPage == 0
            pager.lastPage == 0
    }

    def "when total is greater than zero and less than or equal to max, firstPage and lastPage is 0"() {
        setup: def pager = new DefaultPager(max: max, total: total)
        expect:
            pager.firstPage == first
            pager.lastPage == last
        where:
            max | total | first | last
             10 |     1 |     0 |    0
             10 |     9 |     0 |    0
             10 |    10 |     0 |    0
              3 |     1 |     0 |    0
              3 |     2 |     0 |    0
              3 |     3 |     0 |    0
    }

    @Unroll
    def "when offset is zero (of #total for #pages pages), firstPage is zero and lastPage is always less than maxPages"() {
        setup: def pager = new DefaultPager(max: max, total: total, maxPages: pages)
        expect:
            pager.firstPage == first
            pager.lastPage == last
        where:
            max | total | pages | first | last
             10 |    11 |    10 |     0 |    1
             10 |    20 |    10 |     0 |    1
             10 |    21 |    10 |     0 |    2
             10 |    99 |    10 |     0 |    9
             10 |   100 |    10 |     0 |    9
             10 |   101 |    10 |     0 |    8 // 9 ... 11
             10 |   102 |    10 |     0 |    8 // 9 ... 11
              3 |     4 |     3 |     0 |    1
              3 |     5 |     3 |     0 |    1
              3 |     6 |     3 |     0 |    1
              3 |     7 |     3 |     0 |    2
              3 |     9 |     3 |     0 |    2
              3 |    10 |     3 |     0 |    1 // 1 2 ... 4
              3 |    11 |     3 |     0 |    1 // 1 2 ... 4
    }

    @Unroll
    def "when offset is in last page (of #total for #pages pages), lastPage is one less than totalPages"() {
        setup: def pager = new DefaultPager(
            offset: offset, max: max, total: total, maxPages: pages,
        )
        expect:
            pager.firstPage == first
            pager.lastPage == last
        where:
            offset | max | total | pages | first | last
                10 |  10 |    11 |    10 |     0 |    1
                10 |  10 |    20 |    10 |     0 |    1
                20 |  10 |    21 |    10 |     0 |    2
                90 |  10 |    99 |    10 |     0 |    9
                90 |  10 |   100 |    10 |     0 |    9
               100 |  10 |   101 |    10 |     2 |   10 // 1 ... 3 of 11
               100 |  10 |   102 |    10 |     2 |   10 // 1 ... 3 of 11
               100 |  10 |   110 |    10 |     2 |   10 // 1 ... 3 of 11
               110 |  10 |   111 |    10 |     3 |   11 // 1 ... 4 of 12
               110 |  10 |   120 |    10 |     3 |   11 // 1 ... 4 of 12
               120 |  10 |   121 |    10 |     4 |   12 // 1 ... 5 of 13
               990 |  10 |  1000 |    10 |    91 |   99 // 1 ... 92 of 100
              1000 |  10 |  1001 |    10 |    92 |  100 // 1 ... 94 of 101
                 3 |   3 |     4 |     3 |     0 |    1
                 3 |   3 |     5 |     3 |     0 |    1
                 3 |   3 |     6 |     3 |     0 |    1
                 6 |   3 |     7 |     3 |     0 |    2
                 6 |   3 |     9 |     3 |     0 |    2
                 9 |   3 |    10 |     3 |     2 |    3 // 1 ... 3 4
                 9 |   3 |    11 |     3 |     2 |    3 // 1 ... 3 4
                 9 |   3 |    12 |     3 |     2 |    3 // 1 ... 3 4
                12 |   3 |    13 |     3 |     3 |    4 // 1 ... 4 5
    }

    @Unroll
    def "0 to 15 of 15 by 3, at #offset"() {
        setup: def pager = new DefaultPager(
            offset: offset, max: 3, total: 15, maxPages: 3,
        )
        expect:
            pager.firstPage == first
            pager.lastPage == last
            pager.currentPage == cur
        where:
            offset | first | cur | last
                 0 |     0 |   0 |    1 // ! 2 ... 5
                 1 |     0 |   0 |    1 // ! 2 ... 5
                 2 |     0 |   0 |    1 // ! 2 ... 5
                 3 |     0 |   1 |    1 // 1 @ ... 5
                 4 |     0 |   1 |    1 // 1 @ ... 5
                 5 |     0 |   1 |    1 // 1 @ ... 5
                 6 |     2 |   2 |    2 // 1 ... # ... 5
                 7 |     2 |   2 |    2 // 1 ... # ... 5
                 8 |     2 |   2 |    2 // 1 ... # ... 5
                 9 |     3 |   3 |    4 // 1 ... $ 5
                10 |     3 |   3 |    4 // 1 ... $ 5
                11 |     3 |   3 |    4 // 1 ... $ 5
                12 |     3 |   4 |    4 // 1 ... 4 %
                13 |     3 |   4 |    4 // 1 ... 4 %
                14 |     3 |   4 |    4 // 1 ... 4 %
                15 |     3 |   5 |    4 // 1 ... 4 5
    }

    @Unroll
    def "0 to 15 of unknown by 3, at #offset"() {
        setup: def pager = new DefaultPager(
            offset: offset, max: 3, maxPages: 3,
        )
        expect:
            pager.firstPage == first
            pager.lastPage == last
            pager.currentPage == cur
        where:
            offset | first | cur | last
                 0 |     0 |   0 |    2 // ! 2 3 ...
                 1 |     0 |   0 |    2 // ! 2 3 ...
                 2 |     0 |   0 |    2 // ! 2 3 ...
                 3 |     0 |   1 |    2 // 1 @ 3 ...
                 4 |     0 |   1 |    2 // 1 @ 3 ...
                 5 |     0 |   1 |    2 // 1 @ 3 ...
                 6 |     2 |   2 |    3 // 1 ... # 4 ...
                 7 |     2 |   2 |    3 // 1 ... # 4 ...
                 8 |     2 |   2 |    3 // 1 ... # 4 ...
                 9 |     3 |   3 |    4 // 1 ... $ 5 ...
                10 |     3 |   3 |    4 // 1 ... $ 5 ...
                11 |     3 |   3 |    4 // 1 ... $ 5 ...
                12 |     4 |   4 |    5 // 1 ... % 6 ...
                13 |     4 |   4 |    5 // 1 ... % 6 ...
                14 |     4 |   4 |    5 // 1 ... % 6 ...
                15 |     5 |   5 |    6 // 1 ... ^ 7 ...
    }

    @Unroll
    def "0 to 200 of 200 by 10, at #offset"() {
        setup: def pager = new DefaultPager(
            offset: offset, max: 10, total: 200, maxPages: 10,
        )
        expect:
            pager.firstPage == first
            pager.lastPage == last
            pager.currentPage == cur
        where:
            offset | first | cur | last
                 0 |     0 |   0 |    8 // * 2 3 4 5 6 7 8 9 ... 20
                10 |     0 |   1 |    8 // 1 * 3 4 5 6 7 8 9 ... 20
                20 |     0 |   2 |    8 // 1 2 * 4 5 6 7 8 9 ... 20
                30 |     0 |   3 |    8 // 1 2 3 * 5 6 7 8 9 ... 20
                40 |     0 |   4 |    8 // 1 2 3 4 * 6 7 8 9 ... 20
                50 |     0 |   5 |    8 // 1 2 3 4 5 * 7 8 9 ... 20
                60 |     2 |   6 |    9 // 1 ... 3 4 5 6 * 8 9 10 ... 20
                70 |     3 |   7 |   10 // 1 ... 4 5 6 7 * 9 10 11 ... 20
                80 |     4 |   8 |   11 // 1 ... 5 6 7 8 * 10 11 12 ... 20
                90 |     5 |   9 |   12 // 1 ... 6 7 8 9 * 11 12 13 ... 20
               100 |     6 |  10 |   13 // 1 ... 7 8 9 10 * 12 13 14 ... 20
               110 |     7 |  11 |   14 // 1 ... 8 9 10 11 * 13 14 15 ... 20
               120 |     8 |  12 |   15 // 1 ... 9 10 11 12 * 14 15 16 ... 20
               130 |     9 |  13 |   16 // 1 ... 10 11 12 13 * 15 16 17 ... 20
               140 |    10 |  14 |   17 // 1 ... 11 12 13 14 * 16 17 18 ... 20
               150 |    11 |  15 |   19 // 1 ... 12 13 14 15 * 17 18 19 20
               160 |    11 |  16 |   19 // 1 ... 12 13 14 15 16 * 18 19 20
               170 |    11 |  17 |   19 // 1 ... 12 13 14 15 16 17 * 19 20
               180 |    11 |  18 |   19 // 1 ... 12 13 14 15 16 17 18 * 20
               190 |    11 |  19 |   19 // 1 ... 12 13 14 15 16 17 18 19 *
    }

    def "updating total updates firstPage"() {
        when: def pager = new DefaultPager(offset: 100, max: 10, total: 1000, maxPages: 10)
        then: pager.firstPage == 6
        when: pager.total = 0
        then: pager.firstPage == 0
    }

    def "updating max updates firstPage"() {
        when: def pager = new DefaultPager(offset: 100, max: 10, total: 1000, maxPages: 10)
        then: pager.firstPage == 6
        when: pager.max = 100
        then: pager.firstPage == 0
    }

    def "updating offset updates firstPage"() {
        when: def pager = new DefaultPager(offset: 100, max: 10, total: 1000, maxPages: 10)
        then: pager.firstPage == 6
        when: pager.offset = 0
        then: pager.firstPage == 0
    }

    def "updating maxPages updates firstPage"() {
        when: def pager = new DefaultPager(offset: 100, max: 10, total: 1000, maxPages: 10)
        then: pager.firstPage == 6
        when: pager.maxPages = 5
        then: pager.firstPage == 9
    }

    def "updating total updates lastPage"() {
        when: def pager = new DefaultPager(max: 10, total: 1000, maxPages: 10)
        then: pager.lastPage == 8
        when: pager.total = 0
        then: pager.lastPage == 0
    }

    def "updating max updates lastPage"() {
        when: def pager = new DefaultPager(max: 10, total: 1000, maxPages: 10)
        then: pager.lastPage == 8
        when: pager.max = 100
        then: pager.lastPage == 9
    }

    def "updating offset updates lastPage"() {
        when: def pager = new DefaultPager(max: 10, total: 1000, maxPages: 10)
        then: pager.lastPage == 8
        when: pager.offset = 100
        then: pager.lastPage == 13
    }

    def "updating maxPages updates lastPage"() {
        when: def pager = new DefaultPager(max: 10, total: 1000, maxPages: 10)
        then: pager.lastPage == 8
        when: pager.maxPages = 5
        then: pager.lastPage == 3
    }


    def "join null params results in empty string"() {
        expect: new DefaultPager().joinParams(null) == ''
    }

    def "join empty params results in empty string"() {
        expect: new DefaultPager().joinParams([:]) == ''
    }

    def "join simple string param results in simple string"() {
        expect: new DefaultPager().joinParams(foo: 'bar') == 'foo=bar'
    }

    def "join two string params results in simple string"() {
        expect: new DefaultPager().joinParams(foo: 'bar', q: 'p') == 'foo=bar&q=p'
    }

    def "join null key param results in empty string"() {
        expect: new DefaultPager().joinParams((null): 'bar') == ''
    }

    def "join null value param results in just key"() {
        expect: new DefaultPager().joinParams(foo: null) == 'foo'
    }

    def "join empty key param results in empty string"() {
        expect: new DefaultPager().joinParams('': 'bar') == ''
    }

    def "join empty value param results in just key"() {
        expect: new DefaultPager().joinParams(foo: '') == 'foo'
    }

    def "join true key param results in true param name"() {
        expect: new DefaultPager().joinParams((true): 'bar') == 'true=bar'
    }

    def "join true value param results true param value"() {
        expect: new DefaultPager().joinParams(foo: true) == 'foo=true'
    }

    def "join false key param results in empty string"() {
        expect: new DefaultPager().joinParams((false): 'bar') == ''
    }

    def "join false value param results false param value"() {
        expect: new DefaultPager().joinParams(foo: false) == 'foo=false'
    }

    def "join reserved key param results in escaped param name"() {
        expect: new DefaultPager().joinParams('&=': 'bar') == '%26%3D=bar'
    }

    def "join reserved value param results escaped param value"() {
        expect: new DefaultPager().joinParams(foo: '&=') == 'foo=%26%3D'
    }


    def "when null column specified, sort param value is empty"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            ordering: [true, false, true],
        ).sortForColumn(null) == ''
    }

    def "when empty column specified, sort param value is empty"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            ordering: [true, false, true],
        ).sortForColumn('') == ''
    }

    def "when no sorting configured, sort param value is just column name"() {
        expect: new DefaultPager(
            ordering: [true, false, true],
        ).sortForColumn('foo') == 'foo'
    }

    def "when only ordering configured, sort param value is just column name"() {
        expect: new DefaultPager(
            ordering: [true, false, true],
        ).sortForColumn('foo') == 'foo'
    }

    def "when column not in configured sorting, sort param value is column ascending and then configured sorting"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            ordering: [true, false, true],
        ).sortForColumn('baa') == 'baa-foo bar-baz'
    }

    def "when column first in configured sorting, sort param value is column flipped and then rest of sorting"() {
        expect:
            new DefaultPager(
                sorting: ['foo', 'bar', 'baz'],
                ordering: [true, false, true],
            ).sortForColumn('foo') == 'foo bar-baz'
            new DefaultPager(
                sorting: ['foo', 'bar', 'baz'],
                ordering: [false, true, false],
            ).sortForColumn('foo') == '-foo-bar baz'
    }

    def "when column later in configured sorting, sort param value is column pulled out (not flipped) and then rest of sorting"() {
        expect:
            new DefaultPager(
                sorting: ['foo', 'bar', 'baz'],
                ordering: [true, false, true],
            ).sortForColumn('bar') == 'bar-foo-baz'
            new DefaultPager(
                sorting: ['foo', 'bar', 'baz'],
                ordering: [false, true, false],
            ).sortForColumn('bar') == '-bar foo baz'
    }

    def "when only sorting configured, ordering is assumed to be ascending"() {
        expect:
            new DefaultPager(
                sorting: ['foo', 'bar', 'baz'],
            ).sortForColumn('baa') == 'baa foo bar baz'
            new DefaultPager(
                sorting: ['foo', 'bar', 'baz'],
            ).sortForColumn('foo') == '-foo bar baz'
            new DefaultPager(
                sorting: ['foo', 'bar', 'baz'],
            ).sortForColumn('bar') == 'bar foo baz'
            new DefaultPager(
                sorting: ['foo', 'bar', 'baz'],
            ).sortForColumn('baz') == 'baz foo bar'
    }

    def "when single sort, sort param value is just first column"() {
        expect:
            new DefaultPager(
                sorting: ['foo', 'bar', 'baz'],
                ordering: [true, false, true],
            ).sortForColumn('baa', true) == 'baa'
            new DefaultPager(
                sorting: ['foo', 'bar', 'baz'],
                ordering: [true, false, true],
            ).sortForColumn('foo', true) == 'foo'
            new DefaultPager(
                sorting: ['foo', 'bar', 'baz'],
                ordering: [false, true, false],
            ).sortForColumn('foo', true) == '-foo'
            new DefaultPager(
                sorting: ['foo', 'bar', 'baz'],
                ordering: [true, false, true],
            ).sortForColumn('bar', true) == 'bar'
            new DefaultPager(
                sorting: ['foo', 'bar', 'baz'],
                ordering: [false, true, false],
            ).sortForColumn('bar', true) == '-bar'
    }


    def "when null args specified, sort url is empty"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            ordering: [true, false, true],
        ).urlForColumn(null) == ''
    }

    def "when only column specified, sort url is sort param"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            ordering: [true, false, true],
        ).urlForColumn(sort: 'bar') == '?sort=bar-foo-baz&max=10'
    }

    def "when single column specified, sort url is single sort param"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            ordering: [true, false, true],
        ).urlForColumn(sort: 'bar', single: true) == '?sort=bar&max=10'
    }

    def "when custom paging params configured, sort url uses custom params"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            ordering: [true, false, true],
            prefix: 'x.', sortName: 's',
        ).urlForColumn(sort: 'bar') == '?x.s=bar-foo-baz&x.max=10'
    }

    def "when offset and max configured, sort url does not include them"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            ordering: [true, false, true],
            offset: 100, max: 10,
        ).urlForColumn(sort: 'bar') == '?sort=bar-foo-baz&max=10'
    }

    def "when path specified, sort url includes path"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            ordering: [true, false, true],
        ).urlForColumn(sort: 'bar', path: '/x') == '/x?sort=bar-foo-baz&max=10'
    }

    def "when non-removable params specified, sort url includes other params"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            ordering: [true, false, true],
        ).urlForColumn(sort: 'bar', params: [q: 'p', a: null]) ==
            '?q=p&a&sort=bar-foo-baz&max=10'
    }

    def "when action params specified, sort url does not inclued action params"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            ordering: [true, false, true],
        ).urlForColumn(sort: 'bar', params: [
            q: 'p', a: null, controller: 'x', action: 'y', id: 'z',
        ]) == '?q=p&a&sort=bar-foo-baz&max=10'
    }

    def "when default paging params specified, sort url does not include paging from params"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            ordering: [true, false, true],
        ).urlForColumn(sort: 'bar', params: [
            q: 'p', a: null, off: '100', max: '100', sort: '-foo bar-baz',
        ]) == '?q=p&a&sort=bar-foo-baz&max=10'
    }

    def "when custom paging params specified, sort url does not include paging from params"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            ordering: [true, false, true],
            prefix: 'x.', offsetName: 'o', maxName: 'm', sortName: 's',
        ).urlForColumn(sort: 'bar', params: [
            q: 'p', a: null, 'x.o': '100', 'x.m': '100', 'x.s': '-foo bar-baz',
        ]) == '?q=p&a&x.s=bar-foo-baz&x.m=10'
    }

    def "when no column specified, sort url includes everything but sort param"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            ordering: [true, false, true],
            offset: 100, max: 100,
            prefix: 'x.', offsetName: 'o', maxName: 'm', sortName: 's',
        ).urlForColumn(path: '/x', params:[
            q: 'p', a: null, controller: 'x', action: 'y', id: 'z',
            'x.o': '100', 'x.m': '100', 'x.s': '-foo bar-baz',
        ]) == '/x?q=p&a&x.m=100'
    }

    def "sort url escapes query params"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            ordering: [false, true, false],
            prefix: '//', sortName: '%%',
        ).urlForColumn(sort: 'bar', path: '/x+y;z', params:[
            'x+y;z': 'http://w3.org/q?=p&a'
        ]) == '/x+y;z?x%2By%3Bz=http%3A%2F%2Fw3.org%2Fq%3F%3Dp%26a&%2F%2F%25%25=-bar+foo+baz&%2F%2Fmax=10'
    }


    def "when null args specified, row url is empty"() {
        expect: new DefaultPager().urlForRow(null) == ''
    }

    def "when only offset specified, row url is offset and max params"() {
        expect: new DefaultPager().urlForRow(offset: 20) == '?off=20&max=10'
    }

    def "when offset specified as zero, row url is just max param"() {
        expect: new DefaultPager().urlForRow(offset: 0) == '?max=10'
    }

    def "when max is negative, row url is just offset param"() {
        expect: new DefaultPager(max: -1).urlForRow(offset: 20) == '?off=20'
    }

    def "when custom paging params configured, row url uses custom params"() {
        expect: new DefaultPager(
            prefix: 'x.', offsetName: 'o', maxName: 'm',
        ).urlForRow(offset: 20) == '?x.o=20&x.m=10'
    }

    def "when sorting configured, row url does not include them"() {
        expect: new DefaultPager(
            sorting: ['foo', 'bar', 'baz'],
            ordering: [true, false, true],
        ).urlForRow(offset: 20) == '?off=20&max=10'
    }

    def "when path specified, row url includes path"() {
        expect: new DefaultPager().urlForRow(
            offset: 20, path: '/x',
        ) == '/x?off=20&max=10'
    }

    def "when non-removable params specified, row url includes other params"() {
        expect: new DefaultPager().urlForRow(
            offset: 20, params: [q: 'p', a: null],
        ) == '?q=p&a&off=20&max=10'
    }

    def "when action params specified, row url does not inclued action params"() {
        expect: new DefaultPager().urlForRow(offset: 20, params: [
            q: 'p', a: null, controller: 'x', action: 'y', id: 'z',
        ]) == '?q=p&a&off=20&max=10'
    }

    def "when default paging params specified, row url includes only sort param"() {
        expect: new DefaultPager().urlForRow(offset: 20, params: [
            q: 'p', a: null, off: '100', max: '100', sort: 'foo-bar-baz',
        ]) == '?q=p&a&sort=foo-bar-baz&off=20&max=10'
    }

    def "when custom paging params specified, row url includes only sort param"() {
        expect: new DefaultPager(
            prefix: 'x.', offsetName: 'o', maxName: 'm', sortName: 's',
        ).urlForRow(offset: 20, params: [
            q: 'p', a: null, 'x.o': '100', 'x.m': '100', 'x.s': 'foo-bar-baz',
        ]) == '?q=p&a&x.s=foo-bar-baz&x.o=20&x.m=10'
    }

    def "when no offset specified, row url includes everything but offset param"() {
        expect: new DefaultPager(
            offset: 10, max: 10,
            prefix: 'x.', offsetName: 'o', maxName: 'm', sortName: 's',
        ).urlForRow(path: '/x', params:[
            q: 'p', a: null, controller: 'x', action: 'y', id: 'z',
            'x.o': '100', 'x.m': '100', 'x.s': 'foo-bar-baz',
        ]) == '/x?q=p&a&x.s=foo-bar-baz&x.m=10'
    }

    def "row url escapes query params"() {
        expect: new DefaultPager(
            prefix: '//', sortName: '%%', maxName: '&=',
        ).urlForRow(path: '/x+y;z', params:[
            'x+y;z': 'http://w3.org/q?=p&a', '//%%': 'foo bar baz',
        ]) == '''
            /x+y;z
            ?x%2By%3Bz=http%3A%2F%2Fw3.org%2Fq%3F%3Dp%26a
            &%2F%2F%25%25=foo+bar+baz
            &%2F%2F%26%3D=10
        '''.replaceAll(/\s+/, '')
    }


}
