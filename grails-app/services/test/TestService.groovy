package test

import org.c02e.plugin.pageantry.Pager

class TestService {
    static transactional = false

    Map sources = [:]

    List list(String source, Pager pager) {
        if (!source) {
            pager.total = 0
            return []
        }

        def full = sources[source]
        if (!full) {
            def lineNumber = 0, wordNumber = 0
            sources[source] = full = new File(source).readLines().collect { line ->
                ++lineNumber
                line.replaceAll(/[^\w\s]+/, '').trim().split(/\s+/).
                findAll { it }.collect { word ->
                    [
                        lineNumber: lineNumber,
                        wordNumber: ++wordNumber,
                        word: word,
                        wordLower: word.toLowerCase(),
                        length: word.length(),
                        first: word[0].toUpperCase(),
                        last: word[-1].toUpperCase(),
                    ]
                }
            }.flatten()
        }
        
        pager.slice full
    }
}
