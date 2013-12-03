<!doctype html>
<html>
<head>
    <title>Pageantry Test</title>
    <style type="text/css">
table {
    margin-top: 1em;
}
th, td {
    padding: .1em .4em;
}
th {
    padding: .1em .4em;
    background: #eee;
}
th.asc:after {
    content: ' \25b2';
    color: #666;
    font-weight: normal;
    vertical-align: top;
}
th.desc:after {
    content: ' \25bc';
    color: #666;
    font-weight: normal;
    vertical-align: top;
}
th.asc.secondary:after {
    content: ' \25b2 (' attr(data-sort-ordinal) ')';
    font-size: .75em;
}
th.desc.secondary:after {
    content: ' \25bc (' attr(data-sort-ordinal) ')';
    font-size: .75em;
}
.even {
    background: #fee;
}
.odd {
    background: #efe;
}
.third {
    background: #eef;
}
.pagination menu {
    list-style: none;
    margin: 0;
    padding: 0;
}
.pagination li {
    display: inline-block;
    margin: 0;
    padding: 0 .5em;
}
.pagination .active {
    font-weight: bold;
}

#adv-template {
    margin: 0;
    opacity: .5;
}
.word {
    text-align: center;
}
.A, .E, .I, .O, .U {
    color: red;
    font-weight: bold;
}
    </style>
</head>
<body>
    <form>
        <label>File: <g:textField name="q" value="${params.q}" /></label>
        <button type="submit">Load</button>
    </form>

    <pageantry:table in="${basicList}" pager="${basicPager}"
        caption="Basic Table" singleSort="true">
        <pageantry:thead>
            <pageantry:th sort="lineNumber">Line Number</pageantry:th>
            <pageantry:th sort="wordNumber">Word Number</pageantry:th>
            <pageantry:th sort="wordLower">Word</pageantry:th>
        </pageantry:thead>
        <pageantry:tbody>
            <pageantry:td>${it.lineNumber}</pageantry:td>
            <pageantry:td>${it.wordNumber}</pageantry:td>
            <pageantry:td>${it.word}</pageantry:td>
        </pageantry:tbody>
    </pageantry:table>
    <pageantry:pages />

    <pageantry:table status="i" var="row" in="${advancedList}" pager="${advancedPager}"
        id="adv" alternating="even odd third" foot="true"
        template="[
            lineNumber:'LNUM', wordNumber:'WNUM',
            first:'FIRST', last:'LAST', 
            length:'LEN', word:'WORD',
        ]">
        <pageantry:caption><em>Advanced</em> Table</pageantry:caption>
        <pageantry:thead>
            <pageantry:tr>
                <pageantry:th rowspan="2"></pageantry:th>
                <pageantry:th sort="lineNumber">Line Number</pageantry:th>
                <pageantry:th sort="wordNumber">Word Number</pageantry:th>
                <pageantry:th sort="first">First Letter</pageantry:th>
                <pageantry:th sort="last">Last Letter</pageantry:th>
                <pageantry:th sort="length">Length</pageantry:th>
            </pageantry:tr>
            <pageantry:tr class="continued">
                <pageantry:th sort="wordLower" colspan="5" col="word">Word</pageantry:th>
            </pageantry:tr>
        </pageantry:thead>
        <pageantry:tbody>
            <pageantry:tr id="w${row.wordNumber}">
                <pageantry:td rowspan="2">${i+1}.</pageantry:td>
                <pageantry:td>${row.lineNumber}</pageantry:td>
                <pageantry:td>${row.wordNumber}</pageantry:td>
                <pageantry:td class="${row.first}">${row.first}</pageantry:td>
                <pageantry:td class="${row.last}">${row.last}</pageantry:td>
                <pageantry:td>${row.length}</pageantry:td>
            </pageantry:tr>
            <pageantry:tr class="continued">
                <pageantry:td colspan="5">${row.word}</pageantry:td>
            </pageantry:tr>
        </pageantry:tbody>
    </pageantry:table>
    <pageantry:notempty pager="${advancedPager}">
        <pageantry:prevnext>
            <pageantry:total msg="Words {0} - {1} of {2}" />
            <pageantry:pages firstlast="false" />
            <pageantry:resize sizes="5 10 20 50" msg="Words/Page: " />
        </pageantry:prevnext>
    </pageantry:notempty>
    <pageantry:empty pager="${advancedPager}"><div class="empty">Empty</div></pageantry:empty>

</body>
</html>
