package com.feisttech.trendsevidence;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Pure Java planner. A translation is a proposed string until the user approves it. */
public final class LanguagePlanner {
    public static final String[] LANGUAGES = {"en", "he", "es", "fr", "ar", "zh"};
    public static final class Variant {
        public final String language, text, provenance;
        public final boolean approved;
        public Variant(String language, String text, String provenance, boolean approved) {
            if (!isSupported(language)) throw new IllegalArgumentException("Unsupported language: " + language);
            if (text == null || text.trim().isEmpty() || text.indexOf('"') >= 0)
                throw new IllegalArgumentException("Review empty or embedded-quote strings");
            this.language=language; this.text=text.trim(); this.provenance=provenance; this.approved=approved;
        }
    }
    public static final class Query {
        public final List<Variant> variants;
        public final String url, contextId;
        Query(List<Variant> variants, String url, String contextId) {
            this.variants=Collections.unmodifiableList(new ArrayList<>(variants));this.url=url;this.contextId=contextId;
        }
    }
    private LanguagePlanner() {}
    public static boolean isSupported(String language) {
        for (String candidate:LANGUAGES) if(candidate.equals(language))return true;
        return false;
    }
    private static String enc(String value) {
        try { return URLEncoder.encode(value,"UTF-8").replace("+","%20"); }
        catch (java.io.UnsupportedEncodingException impossible) { throw new AssertionError(impossible); }
    }
    private static String digest(String value) {
        try {
            byte[] bytes=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder b=new StringBuilder();for(byte x:bytes)b.append(String.format(Locale.US,"%02x",x&255));return b.toString();
        } catch(Exception e) { throw new IllegalStateException(e); }
    }
    public static Query query(List<Variant> variants, String start, String end,
                              String geo, String property, int category) {
        if(variants==null||variants.isEmpty()||variants.size()>5)throw new IllegalArgumentException("Select 1–5 variants");
        if(!start.matches("\\d{4}-\\d{2}-\\d{2}")||!end.matches("\\d{4}-\\d{2}-\\d{2}"))throw new IllegalArgumentException("Use ISO dates");
        if(start.compareTo(end)>0)throw new IllegalArgumentException("Start after end");
        if(geo==null||!geo.matches("[A-Za-z0-9-]{1,24}"))throw new IllegalArgumentException("Invalid geo code");
        if(property==null||!property.matches("|news|images|froogle|youtube"))throw new IllegalArgumentException("Invalid search type");
        if(category<0)throw new IllegalArgumentException("Invalid category");
        StringBuilder terms=new StringBuilder(),canonical=new StringBuilder();
        for(Variant v:variants){
            if(!v.approved)throw new IllegalArgumentException("Approve each translation before running");
            if(terms.length()>0)terms.append(',');terms.append('"').append(v.text).append('"');
            canonical.append(v.language.length()).append(':').append(v.language)
                .append(v.text.length()).append(':').append(v.text);
        }
        canonical.append('|').append(start).append('|').append(end).append('|').append(geo)
            .append('|').append(property).append('|').append(category);
        String url="https://trends.google.com/trends/explore?date="+enc(start+" "+end)
            +"&geo="+enc(geo)+"&gprop="+enc(property)+"&cat="+category+"&q="+enc(terms.toString());
        return new Query(variants,url,digest(canonical.toString()));
    }
    /** English anchor appears in each batch; separate batches remain separate scales. */
    public static List<Query> cascade(List<Variant> variants, String start, String end,
                                      String geo, String property, int category) {
        if(variants==null||variants.isEmpty())throw new IllegalArgumentException("No variants");
        Variant anchor=variants.get(0);if(!"en".equals(anchor.language))throw new IllegalArgumentException("English anchor first");
        ArrayList<Query> result=new ArrayList<>();
        if(variants.size()==1){result.add(query(variants,start,end,geo,property,category));return result;}
        for(int i=1;i<variants.size();i+=4){
            ArrayList<Variant> batch=new ArrayList<>();batch.add(anchor);
            batch.addAll(variants.subList(i,Math.min(i+4,variants.size())));
            result.add(query(batch,start,end,geo,property,category));
        }
        return result;
    }
}
