import com.feisttech.trendsevidence.CasePlan;
public final class CasePlanTest {
 public static void main(String[] a) {
  String csv="case_id,case_label,terms_exact_json,geo_code,category_id,explore_url\r\n"+
   "demo,Example,\"[\"\"one, two\"\"]\",US,0,https://trends.google.com/trends/explore?q=%22one%22\r\n";
  CasePlan p=CasePlan.parse(csv);
  if(p.count!=1||p.cases.size()!=1||!p.cases.get("demo").get(0).termsJson.contains("one, two"))throw new AssertionError();
  System.out.println("CasePlan tests passed");
 }
}
