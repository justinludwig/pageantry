package test

import org.c02e.plugin.pageantry.DefaultPager

class TestController {
    def testService

    def index() {
        def basicPager = new DefaultPager(
            // slice requires validSort
            validSort: ['lineNumber', 'wordNumber', 'wordLower'],
            params: params,
        )
        def basicList = testService.list(params.q, basicPager)

        def advancedPager = new DefaultPager(
            sorting: ['length', 'wordLower'],
            ordering: [true, false],
            baseSort: 'wordNumber',
            validSort: [
                'lineNumber', 'wordNumber', 'word', 'wordLower',
                'length', 'first', 'last',
            ],
            max: 5,
            maxMax: 50,
            maxPages: 5,
            prefix: 'adv.',
            offsetName: 'o',
            maxName: 'm',
            sortName: 's',
            params)
        def advancedList = testService.list(params.q, advancedPager)

        [
            basicList: basicList,
            basicPager: basicPager,
            advancedList: advancedList,
            advancedPager: advancedPager,
        ]
    }
}
