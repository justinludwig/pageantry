package org.c02e.plugin.pageantry

class HtmlUtil {

    static String attrs(Map m) {
        m.findAll { k,v -> k }.collect { key, value ->
            // strip illegal name chars from key
            key = attrName(key)

            // special-case map of css style property:values
            if (value instanceof Map && key == 'style')
                value = value.findAll { k,v ->
                    k && (v || v == 0)
                }.collect { k,v -> "$k:$v" }

            // if value is list, use first entry
            // except for class/style: concat values
            if (value instanceof Collection) {
                value = value.flatten()
                switch (key) {
                    case 'class':
                        value = value.findAll { it }.join(' '); break
                    case 'style':
                        value = value.findAll { it }.join(';'); break
                    default:
                        value = value.find { it }
                }
            }

            // special case booleans
            if (value == true)
                return " ${key}"
            // print out encoded string value
            else if (value || value == 0)
                return " ${key}=\"${attrValue(value)}\""
            else
                return ''
        }.join('') ?: ''
    }

    static String text(s) {
        if (!s && s != 0) return ''
        s.toString().
            replaceAll(/&/, '&amp;').
            replaceAll(/</, '&lt;').
            replaceAll(/>/, '&gt;')
    }

    static String attrName(s) {
        if (!s && s != 0) return ''
        s.toString().replaceAll(/[^\w-.:]+/, '')
    }

    static String attrValue(s) {
        if (!s && s != 0) return ''
        s.toString().
            replaceAll(/&/, '&amp;').
            replaceAll(/</, '&lt;').
            replaceAll(/>/, '&gt;').
            replaceAll(/"/, '&quot;')
            // always use double-quotes to enclose attr value
            // so single-quote doesn't need to be escaped
    }
}
