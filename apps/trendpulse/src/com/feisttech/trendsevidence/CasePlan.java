package com.feisttech.trendsevidence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** RFC-4180 style CSV reader for the case plan's named columns. */
public final class CasePlan {
    public static final class Row {
        public final String caseId,caseLabel,url,termsJson,geo,category;
        Row(String id,String label,String url,String terms,String geo,String category){
            this.caseId=id;this.caseLabel=label;this.url=url;this.termsJson=terms;this.geo=geo;this.category=category;
        }
    }
    public final LinkedHashMap<String,List<Row>> cases=new LinkedHashMap<>();
    public final LinkedHashMap<String,String> labels=new LinkedHashMap<>();
    public int count;
    private CasePlan(){}
    public static CasePlan parse(String csv){
        if(csv.startsWith("\uFEFF"))csv=csv.substring(1);
        ArrayList<List<String>> records=new ArrayList<>();ArrayList<String> row=new ArrayList<>();StringBuilder field=new StringBuilder();boolean quoted=false;
        for(int i=0;i<csv.length();i++){
            char c=csv.charAt(i);
            if(quoted){if(c=='"'){if(i+1<csv.length()&&csv.charAt(i+1)=='"'){field.append('"');i++;}else quoted=false;}else field.append(c);}
            else if(c=='"'&&field.length()==0)quoted=true;
            else if(c==','){row.add(field.toString());field.setLength(0);}
            else if(c=='\n'){row.add(field.toString());field.setLength(0);records.add(row);row=new ArrayList<>();}
            else if(c!='\r')field.append(c);
        }
        if(quoted)throw new IllegalArgumentException("Unclosed CSV quote");
        if(field.length()>0||!row.isEmpty()){row.add(field.toString());records.add(row);}
        if(records.isEmpty())throw new IllegalArgumentException("Empty CSV");
        List<String> head=records.get(0);int id=head.indexOf("case_id"),label=head.indexOf("case_label"),url=head.indexOf("explore_url"),terms=head.indexOf("terms_exact_json"),geo=head.indexOf("geo_code"),cat=head.indexOf("category_id");
        if(id<0||label<0||url<0||terms<0||geo<0||cat<0)throw new IllegalArgumentException("Missing case plan columns");
        CasePlan result=new CasePlan();
        for(int i=1;i<records.size();i++){
            List<String> r=records.get(i);if(r.size()==1&&r.get(0).isEmpty())continue;
            if(r.size()!=head.size())throw new IllegalArgumentException("CSV column count on row "+(i+1));
            if(!r.get(url).startsWith("https://trends.google.com/trends/explore?"))throw new IllegalArgumentException("Invalid Explore URL on row "+(i+1));
            String key=r.get(id);if(key.isEmpty())throw new IllegalArgumentException("Empty case ID");
            Row item=new Row(key,r.get(label),r.get(url),r.get(terms),r.get(geo),r.get(cat));
            if(!result.cases.containsKey(key)){result.cases.put(key,new ArrayList<Row>());result.labels.put(key,item.caseLabel);}
            result.cases.get(key).add(item);result.count++;
        }
        return result;
    }
}
