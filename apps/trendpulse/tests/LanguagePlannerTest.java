import com.feisttech.trendsevidence.LanguagePlanner;
import java.util.*;
public final class LanguagePlannerTest {
 public static void main(String[] args) {
  List<LanguagePlanner.Variant> v=new ArrayList<>();
  String[] text={"market trend", "מגמת שוק", "tendencia del mercado", "tendance du marché", "اتجاه السوق", "市场趋势"};
  for(int i=0;i<6;i++)v.add(new LanguagePlanner.Variant(LanguagePlanner.LANGUAGES[i],text[i],"user-approved",true));
  List<LanguagePlanner.Query> q=LanguagePlanner.cascade(v,"2025-01-01","2025-02-01","US","",0);
  if(q.size()!=2||q.get(0).variants.size()!=5||q.get(1).variants.size()!=2)throw new AssertionError("batch size");
  if(q.get(0).contextId.equals(q.get(1).contextId))throw new AssertionError("different contexts");
  if(!q.get(0).url.contains("%22market%20trend%22")||!q.get(0).url.contains("%D7"))throw new AssertionError("encoding");
  try {LanguagePlanner.query(Collections.singletonList(new LanguagePlanner.Variant("en","sample","suggested",false)),"2025-01-01","2025-02-01","US","",0);throw new AssertionError("unapproved");}catch(IllegalArgumentException expected){}
  System.out.println("LanguagePlanner tests passed");
 }
}
