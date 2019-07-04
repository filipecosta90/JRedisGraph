package com.redislabs.redisgraph;

import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.LookupTranslator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utilities class
 */
public class Utils {

    public static final CharSequenceTranslator ESCAPE_CHYPER;
    static {
        final Map<CharSequence, CharSequence> escapeJavaMap = new HashMap<>();
        escapeJavaMap.put("\'", "\\'");
        escapeJavaMap.put("\"", "\\\"");
        ESCAPE_CHYPER = new AggregateTranslator(new LookupTranslator(Collections.unmodifiableMap(escapeJavaMap)));
    }

    /**
     *
     * @param str - a string
     * @return the input string surrounded with quotation marks, if needed
     */
    public static String quoteString(String str){
        if(str.startsWith("\"") && str.endsWith("\"")){
            return str;
        }

        StringBuilder sb = new StringBuilder(str.length()+2);
        if(str.charAt(0)!='"'){
            sb.append('"');
        }
        sb.append(str);
        if (str.charAt(str.length()-1)!= '"'){
            sb.append('"');
        }
        return sb.toString();
    }

    /**
     * Prepare and formats a query and query arguments
     * @param query - query
     * @param args - query arguments
     * @return formatted query
     */
    public static String prepareQuery(String query, Object ...args){
        if(args.length > 0) {
            for(int i=0; i<args.length; ++i) {
                if(args[i] instanceof String) {
                    args[i] = "\'" + ESCAPE_CHYPER.translate((String)args[i]) + "\'";
                }
            }
            query = String.format(query, args);
        }
        return query;
    }

    /**
     * Prepare and format a procedure call and its arguments
     * @param procedure - procedure to invoke
     * @param args - procedure arguments
     * @param kwargs - procedure output arguments
     * @return formatter procedure call
     */
    public static String prepareProcedure(String procedure, List<String> args  , Map<String, List<String>> kwargs){
        args = args.stream().map( s -> Utils.quoteString(s)).collect(Collectors.toList());
        StringBuilder queryStringBuilder =  new StringBuilder();
        queryStringBuilder.append(String.format("CALL %s(%s)", procedure, String.join(",", args)));
        List<String> kwargsList = kwargs.getOrDefault("y", null);
        if(kwargsList != null){
            queryStringBuilder.append(String.join(",", kwargsList));
        }
        return queryStringBuilder.toString();
    }
}
