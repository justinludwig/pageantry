package com.pitchstone.plugin.pageantry

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(PageantryTagLib)
class PageantryTagLibSpec extends Specification {

	def "table renders empty table by default"() {
        expect: applyTemplate('<pageantry:table>body</pageantry:table>') ==
            '<table>body</table>'
	}

	def "table renders table attributes"() {
        expect: applyTemplate('''
            <pageantry:table id="foo" class="bar" title="baz" style="x:y"
                summary="it's up!" caption="it's down!"
                data-red="green" data-blue="black"
            >body</pageantry:table>
        '''.trim()) == '''
            <table id="foo" class="bar" title="baz" data-red="green" data-blue="black">
                <caption>it&#39;s down!</caption>body</table>
        '''.trim().replaceAll(/>\s+</, '><')
	}

	def "table renders basic table"() {
        expect: applyTemplate('''
            <pageantry:table in="${list}" pager="${pager}">
                <pageantry:thead>
                    <pageantry:th sort="id">ID</pageantry:th>
                    <pageantry:th sort="name">Name</pageantry:th>
                </pageantry:thead>
                <pageantry:tbody>
                    <pageantry:td>${it.id}</pageantry:td>
                    <pageantry:td>${it.name}</pageantry:td>
                </pageantry:tbody>
            </pageantry:table>
        '''.trim().replaceAll(/>\s+</, '><'), [
            list: [
                [id: 'one', name: 'ONE'],
                [id: 'two', name: 'TWO'],
                [id: 'three', name: 'THREE'],
            ],
            pager: new DefaultPager(),
        ]) == '''
            <table>
                <thead><tr>
                    <th class="sortable"><a href="?sort=id">ID</a></th>
                    <th class="sortable"><a href="?sort=name">Name</a></th>
                </tr></thead>
                <tbody>
                    <tr class="even"><td>one</td><td>ONE</td></tr>
                    <tr class="odd"><td>two</td><td>TWO</td></tr>
                    <tr class="even"><td>three</td><td>THREE</td></tr>
                </tbody>
            </table>
        '''.trim().replaceAll(/>\s+</, '><')
	}

	def "table renders sorting"() {
        expect: applyTemplate('''
            <pageantry:table in="${list}" pager="${pager}">
                <pageantry:thead>
                    <pageantry:th sort="id">ID</pageantry:th>
                    <pageantry:th sort="name">Name</pageantry:th>
                </pageantry:thead>
                <pageantry:tbody>
                    <pageantry:td>${it.id}</pageantry:td>
                    <pageantry:td>${it.name}</pageantry:td>
                </pageantry:tbody>
            </pageantry:table>
        '''.trim().replaceAll(/>\s+</, '><'), [
            list: [
                [id: 'one', name: 'ONE'],
                [id: 'two', name: 'TWO'],
                [id: 'three', name: 'THREE'],
            ],
            pager: new DefaultPager(
                sorting: ['id', 'name'],
                ordering: [false, true],
            ),
        ]) == '''
            <table>
                <thead><tr>
                    <th class="sortable sorted primary asc" data-sort-ordinal="1">
                        <a href="?sort=-id-name">ID</a>
                    </th>
                    <th class="sortable sorted secondary desc" data-sort-ordinal="2">
                        <a href="?sort=-name+id">Name</a>
                    </th>
                </tr></thead>
                <tbody>
                    <tr class="even"><td>one</td><td>ONE</td></tr>
                    <tr class="odd"><td>two</td><td>TWO</td></tr>
                    <tr class="even"><td>three</td><td>THREE</td></tr>
                </tbody>
            </table>
        '''.trim().replaceAll(/>\s+</, '><')
	}

	def "table renders template"() {
        expect: applyTemplate('''
            <pageantry:table in="${list}" pager="${pager}" status="i"
                template="[id:'X', name:'Z']">
                <pageantry:thead>
                    <pageantry:th sort="id">ID</pageantry:th>
                    <pageantry:th sort="name">Name</pageantry:th>
                </pageantry:thead>
                <pageantry:tbody>
                    <pageantry:td id="x${i}">${it.id}</pageantry:td>
                    <pageantry:td>${it.name}</pageantry:td>
                </pageantry:tbody>
            </pageantry:table>
        '''.trim().replaceAll(/>\s+</, '><'), [
            list: [
                [id: 'one', name: 'ONE'],
                [id: 'two', name: 'TWO'],
                [id: 'three', name: 'THREE'],
            ],
            pager: new DefaultPager(),
        ]) == '''
            <table>
                <thead><tr>
                    <th class="sortable"><a href="?sort=id">ID</a></th>
                    <th class="sortable"><a href="?sort=name">Name</a></th>
                </tr></thead>
                <tbody>
                    <tr class="even"><td id="x0">one</td><td>ONE</td></tr>
                    <tr class="odd"><td id="x1">two</td><td>TWO</td></tr>
                    <tr class="even"><td id="x2">three</td><td>THREE</td></tr>
                </tbody>
            </table>
            <table>
                <tbody>
                    <tr class="odd"><td id="x-1">X</td><td>Z</td></tr>
                </tbody>
            </table>
        '''.trim().replaceAll(/>\s+</, '><')
	}

	def "table renders complex table"() {
        expect: applyTemplate('''
            <pageantry:table var="row" in="${list}" pager="${pager}" status="i"
                alternating="even odd third" foot="true">
                <pageantry:caption>A <em>table</em>!</pageantry:caption>
                <pageantry:thead>
                    <pageantry:tr>
                        <pageantry:th id="col-id"
                            sort="id" col="number">ID</pageantry:th>
                        <pageantry:th id="col-name"
                            sort="name" col="text">Name</pageantry:th>
                        <pageantry:th id="col-delete"
                            col="delete" class="blank" data-ref="delete"></pageantry:th>
                    </pageantry:tr>
                    <pageantry:tr class="continued">
                        <pageantry:th id="col-desc"
                            sort="description" colspan="3">Description</pageantry:th>
                    </pageantry:tr>
                </pageantry:thead>
                <pageantry:tbody>
                    <pageantry:tr id="row-${i}" class="${row.css}">
                        <pageantry:td headers="col-id">${row.id}</pageantry:td>
                        <pageantry:td headers="col-name"
                            class="${row.nameCss}">${row.name}</pageantry:td>
                        <pageantry:td headers="col-delete"
                            id="delete-${row.id}">X</pageantry:td>
                    </pageantry:tr>
                    <pageantry:tr class="${row.css} continued">
                        <pageantry:td headers="col-desc"
                            colspan="3" data-more="${row.more}"
                            >${row.description}</pageantry:td>
                    </pageantry:tr>
                </pageantry:tbody>
            </pageantry:table>
        '''.trim().replaceAll(/>\s+</, '><'), [
            list: [
                [id: 'one', name: 'ONE', description: 'a one',
                    css: 'o', nameCss: 'hot', more: '.'],
                [id: 'two', name: 'TWO', description: 'some twos',
                    css: 'tt', nameCss: 'warm', more: '..'],
                [id: 'three', name: 'THREE', description: 'more threes',
                    css: 'ttt', nameCss: 'mild', more: '...'],
                [id: 'four', name: 'FOUR', description: 'many fours',
                    css: 'ffff', nameCss: 'cool', more: '....'],
                [id: 'five', name: 'FIVE', description: 'max five',
                    css: 'fffff', nameCss: 'cold', more: '.....'],
            ],
            pager: new DefaultPager(),
        ]) == '''
            <table>
                <caption>A <em>table</em>!</caption>
                <thead>
                    <tr>
                        <th class="number sortable" id="col-id">
                            <a href="?sort=id">ID</a>
                        </th>
                        <th class="text sortable" id="col-name">
                            <a href="?sort=name">Name</a>
                        </th>
                        <th class="blank delete" id="col-delete" data-ref="delete"></th>
                    </tr>
                    <tr class="continued">
                        <th class="sortable" id="col-desc" colspan="3">
                            <a href="?sort=description">Description</a>
                        </th>
                    </tr>
                </thead>
                <tfoot>
                    <tr>
                        <th class="number sortable" id="col-id">
                            <a href="?sort=id">ID</a>
                        </th>
                        <th class="text sortable" id="col-name">
                            <a href="?sort=name">Name</a>
                        </th>
                        <th class="blank delete" id="col-delete" data-ref="delete"></th>
                    </tr>
                    <tr class="continued">
                        <th class="sortable" id="col-desc" colspan="3">
                            <a href="?sort=description">Description</a>
                        </th>
                    </tr>
                </tfoot>
                <tbody>
                    <tr class="o even" id="row-0">
                        <td class="number" headers="col-id">one</td>
                        <td class="hot text" headers="col-name">ONE</td>
                        <td class="delete" headers="col-delete" id="delete-one">X</td>
                    </tr>
                    <tr class="o continued even">
                        <td headers="col-desc" colspan="3" data-more=".">a one</td>
                    </tr>

                    <tr class="tt odd" id="row-1">
                        <td class="number" headers="col-id">two</td>
                        <td class="warm text" headers="col-name">TWO</td>
                        <td class="delete" headers="col-delete" id="delete-two">X</td>
                    </tr>
                    <tr class="tt continued odd">
                        <td headers="col-desc" colspan="3" data-more="..">some twos</td>
                    </tr>

                    <tr class="ttt third" id="row-2">
                        <td class="number" headers="col-id">three</td>
                        <td class="mild text" headers="col-name">THREE</td>
                        <td class="delete" headers="col-delete" id="delete-three">X</td>
                    </tr>
                    <tr class="ttt continued third">
                        <td headers="col-desc" colspan="3" data-more="...">more threes</td>
                    </tr>

                    <tr class="ffff even" id="row-3">
                        <td class="number" headers="col-id">four</td>
                        <td class="cool text" headers="col-name">FOUR</td>
                        <td class="delete" headers="col-delete" id="delete-four">X</td>
                    </tr>
                    <tr class="ffff continued even">
                        <td headers="col-desc" colspan="3" data-more="....">many fours</td>
                    </tr>

                    <tr class="fffff odd" id="row-4">
                        <td class="number" headers="col-id">five</td>
                        <td class="cold text" headers="col-name">FIVE</td>
                        <td class="delete" headers="col-delete" id="delete-five">X</td>
                    </tr>
                    <tr class="fffff continued odd">
                        <td headers="col-desc" colspan="3" data-more=".....">max five</td>
                    </tr>
                </tbody>
            </table>
        '''.trim().replaceAll(/>\s+</, '><')
	}


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

	def "full paging"() {
        expect: applyTemplate('''
            <pageantry:notempty pager="${pager}">
                <pageantry:prevnext>
                    <pageantry:total />
                    <pageantry:pages />
                    <pageantry:resize />
                </pageantry:prevnext>
            </pageantry:notempty>
            <pageantry:empty pager="${pager}">Empty</pageantry:empty>
        '''.trim().replaceAll(/>\s+</, '><'), [pager: new DefaultPager(
            offset: 500, total: 1000
        )]) == '''
            <div class="pagination"><menu>
                <li class="previous"><a href="?off=490&amp;max=10">&laquo; Prev</a></li>
                <li class="total"><span>501 - 510 of 1,000</span></li>
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
                <li class="max"><span>Items Per Page:</span>
                <select name="max" data-href="?off=500&amp;max=MAX" onchange="location.href=this.attributes['data-href'].value.replace(/MAX/,this.value)">
                    <option selected>10</option>
                    <option>20</option>
                    <option>50</option>
                    <option>100</option>
                    <option value="1000">All</option>
                </select></li>
                <li class="next"><a href="?off=510&amp;max=10">Next &raquo;</a></li>
            </menu></div>
        '''.trim().replaceAll(/>\s+</, '><')
	}

}
