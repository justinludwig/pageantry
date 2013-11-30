package com.pitchstone.plugin.pageantry

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(PageantryTagLib)
class PageantryTagLibSpec extends Specification {

	def "empty renders body by default"() {
        expect: applyTemplate('<pageantry:empty>body</pageantry:empty>') == 'body'
	}

	def "when total, empty renders nothing"() {
        expect: applyTemplate(
            '<pageantry:empty pager="${pager}">body</pageantry:empty>',
            [pager: new DefaultPager(total: 1)]
        ) == ''
	}


	def "notempty renders nothing by default"() {
        expect: applyTemplate('<pageantry:notempty>body</pageantry:notempty>') == ''
	}

	def "when total, notempty renders body"() {
        expect: applyTemplate(
            '<pageantry:notempty pager="${pager}">body</pageantry:notempty>',
            [pager: new DefaultPager(total: 1)]
        ) == 'body'
	}


	def "when no prev or next, prevnext renders disabled prev and next"() {
        expect: applyTemplate(
            '<pageantry:prevnext pager="${pager}"><br></pageantry:prevnext>',
            [pager: new DefaultPager(total: 0)]
        ) == '''
            <div class="pagination"><menu>
                <li class="previous disabled"><span>&laquo; Prev</span></li>
                <br>
                <li class="next disabled"><span>Next &raquo;</span></li>
            </menu></div>
        '''.trim().replaceAll(/>\s+</, '><')
	}

	def "when prev or next, prevnext renders prev and next links"() {
        expect: applyTemplate(
            '<pageantry:prevnext pager="${pager}"><br></pageantry:prevnext>',
            [pager:new DefaultPager(offset: 10, total: 100)]
        ) == '''
            <div class="pagination"><menu>
                <li class="previous"><a href="?max=10">&laquo; Prev</a></li>
                <br>
                <li class="next"><a href="?off=20&amp;max=10">Next &raquo;</a></li>
            </menu></div>
        '''.trim().replaceAll(/>\s+</, '><')
	}


	def "when no pages, pages renders 1 page"() {
        expect: applyTemplate(
            '<pageantry:pages pager="${pager}"><br></pageantry:pages>',
            [pager: new DefaultPager(total: 0)]
        ) == '''
            <div class="pagination"><menu>
                <li class="active"><span>1</span></li>
            </menu></div>
        '''.trim().replaceAll(/>\s+</, '><')
	}

	def "when 1 page, pages renders 1 page"() {
        expect: applyTemplate(
            '<pageantry:pages pager="${pager}"><br></pageantry:pages>',
            [pager: new DefaultPager(total: 10)]
        ) == '''
            <div class="pagination"><menu>
                <li class="active"><span>1</span></li>
            </menu></div>
        '''.trim().replaceAll(/>\s+</, '><')
	}

	def "when 10 pages, pages renders 10 pages"() {
        expect: applyTemplate(
            '<pageantry:pages pager="${pager}"><br></pageantry:pages>',
            [pager: new DefaultPager(total: 100)]
        ) == ('''
            <div class="pagination"><menu>
                <li class="active"><span>1</span></li>
        ''' + (2..10).collect { """
                <li><a href="?off=${(it - 1) * 10}&amp;max=10">${it}</a></li>
        """ }.join('') + '''
            </menu></div>
        ''').trim().replaceAll(/>\s+</, '><')
	}

	def "when in middle of 100 pages, pages renders 10 pages"() {
        expect: applyTemplate(
            '<pageantry:pages pager="${pager}"><br></pageantry:pages>',
            [pager: new DefaultPager(offset: 500, total: 1000)]
        ) == '''
            <div class="pagination"><menu>
                <li><a href="?max=10">1</a></li>
                <li class="more disabled"><span>&hellip;</span></li>
                <li><a href="?off=460&amp;max=10">47</a></li>
                <li><a href="?off=470&amp;max=10">48</a></li>
                <li><a href="?off=480&amp;max=10">49</a></li>
                <li><a href="?off=490&amp;max=10">50</a></li>
                <li class="active"><span>51</span></li>
                <li><a href="?off=510&amp;max=10">52</a></li>
                <li><a href="?off=520&amp;max=10">53</a></li>
                <li><a href="?off=530&amp;max=10">54</a></li>
                <li class="more disabled"><span>&hellip;</span></li>
                <li><a href="?off=990&amp;max=10">100</a></li>
            </menu></div>
        '''.trim().replaceAll(/>\s+</, '><')
	}


	def "when no pages, total renders 0"() {
        expect: applyTemplate(
            '<pageantry:total pager="${pager}" />',
            [pager: new DefaultPager(total: 0)]
        ) == '<span>0 - 0 of 0</span>'
	}

	def "when no pages and empty message, total renders empty message"() {
        expect: applyTemplate(
            '<pageantry:total pager="${pager}" empty="Empty" />',
            [pager: new DefaultPager(total: 0)]
        ) == '<span>Empty</span>'
	}

	def "when total, total renders"() {
        expect: applyTemplate(
            '<pageantry:total pager="${pager}" />',
            [pager: new DefaultPager(total: 100)]
        ) == '<span>1 - 10 of 100</span>'
	}

	def "when unknown total, total renders unknown"() {
        expect: applyTemplate(
            '<pageantry:total pager="${pager}" />',
            [pager: new DefaultPager(total: -1)]
        ) == '<span>1 - 10</span>'
	}


	def "resize renders defaults"() {
        expect: applyTemplate(
            '<pageantry:resize pager="${pager}" />',
            [pager: new DefaultPager()]
        ) == '''
            <span>Items Per Page:</span>
            <select name="max" data-href="?max=MAX" onchange="location.href=this.attributes['data-href'].value.replace(/MAX/,this.value)">
                <option selected>10</option>
                <option>20</option>
                <option>50</option>
                <option>100</option>
                <option value="1000">All</option>
            </select>
        '''.trim().replaceAll(/>\s+</, '><')
	}

	def "resize renders with custom attributes"() {
        expect: applyTemplate(
            '''
                <pageantry:resize pager="${pager}"
                    msg="" sizes="10 100 1000 All" all="100000"
                    name="foo" onchange="" />
            '''.trim(),
            [pager: new DefaultPager()]
        ) == '''
            <select name="foo" data-href="?max=MAX">
                <option selected>10</option>
                <option>100</option>
                <option>1000</option>
                <option value="100000">All</option>
            </select>
        '''.trim().replaceAll(/>\s+</, '><')
	}

	def "resize renders with custom configuration"() {
        expect: applyTemplate(
            '<pageantry:resize pager="${pager}" />',
            [pager: new DefaultPager(
                offset: 10,
                max: 20,
                sorting: ['x', 'y', 'z'],
                prefix: 'foo.',
                maxName: 'bar',
                maxMax: 100000,
            )]
        ) == '''
            <span>Items Per Page:</span>
            <select name="foo.bar" data-href="?foo.off=10&amp;foo.bar=MAX" onchange="location.href=this.attributes['data-href'].value.replace(/MAX/,this.value)">
                <option>10</option>
                <option selected>20</option>
                <option>50</option>
                <option>100</option>
                <option value="100000">All</option>
            </select>
        '''.trim().replaceAll(/>\s+</, '><')
	}


}
