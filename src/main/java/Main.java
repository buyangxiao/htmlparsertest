import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.LinkedList;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

/**
 * Created by xuefengwang on 2018/1/24.
 */
public class Main {
  public static void main(String[] args) {
    String html = "您好，您可以点击 <a lizard-catch=\"off\" onclick=\"chatUrlJumpLib.Jump('http://123123123', 2)\" >这里</a> ssssssss。";

    String result = TransCodeHtmlToListV2(html);
    System.out.println(result);
  }

  public static HtmlNode CreateDOMTree(String html) {
    HtmlNode root = HtmlNode.CreateRoot();
    //无内容
    if (StringUtils.isEmpty(html))
      return root;
    Document doc = Jsoup.parse(html);
    Elements body = doc.select("body");
    //再以此处理各个位置的关系
    root = IterateIDOMElement(body.get(0),null);
    return root;
  }
  private static TagType NameToTagType(String tagname)
  {
    if(StringUtils.isEmpty(tagname)) return TagType.Default;
    String s = tagname.toLowerCase().trim();
    if (s.equals("p") || s.equals("br")) {
      return TagType.Breaking;
    } else if (s.equals("img")) {
      return TagType.Image;
    } else {
      return TagType.Default;
    }
  }
  private static HtmlNode IterateIDOMElement(Node element, HtmlNode parent)
  {
    HtmlNode current = null;
    if (parent == null) //root节点
    {
      current = HtmlNode.CreateRoot();
    }
    else
    {
      if (element.nodeName() == "#text")
      {
        current = HtmlNode.CreatePlainTag(((TextNode) element).getWholeText());
      }
      else
      {
        String link = null;
        if (element.attributes()!=null && element.attributes().size()>0)
        {
          if(element.attributes().hasKeyIgnoreCase("href")){
            String href = element.attributes().get("href");
            if(current!=null)
              current.setLink(href);
          }
        }
        current = HtmlNode.Create(element.attributes().toString(), NameToTagType(element.nodeName()));
        current.setLink(link);
      }
      parent.AddChild(current);
    }
    //遍历子树
    if (element.childNodeSize()!=0)
    {
      for (Node i :element.childNodes())
      {
        IterateIDOMElement(i, current);
      }
    }
    return current;
  }
  private static String TransCodeHtmlToListV2(String html)
  {
    //htmlToTree
    HtmlNode mockHtmlTag = CreateDOMTree(html);
    //TreeToList
    LinkedList<ClientContentItem> returnList = mockHtmlTag.BackOrderTravel();

    //List中的特殊处理
    if (returnList != null && !returnList.isEmpty())
    {
//      去掉最后多余的换行符
      while (!returnList.isEmpty() && !StringUtils.isEmpty(returnList.getLast().Text) && returnList.getLast().Text.endsWith("\r\n"))
      {
        StringUtils.removeEnd(returnList.getLast().Text,"\r\n");
        if (StringUtils.isEmpty(returnList.getLast().Text) && returnList.getLast().TextType == ClientTextType.Text)
        {
          returnList.removeLast();
        }
      }
      for (int i = 0; i < returnList.size() - 1; i++)
      {
        if (returnList.get(i).TextType == ClientTextType.Photo &&
            returnList.get(i + 1).TextType == ClientTextType.Href &&
            !StringUtils.isEmpty(returnList.get(i + 1).Link) &&
            StringUtils.isEmpty(returnList.get(i + 1).Text))
        {
          returnList.get(i).Link = returnList.get(i + 1).Link;
        }
      }
      //过滤邮件和电话链接，产生新的Type
      for (ClientContentItem clientOntentItem : returnList)
      {
        if(clientOntentItem.TextType == ClientTextType.Href && !StringUtils.isEmpty(clientOntentItem.Link))
        if (clientOntentItem.Link.toLowerCase().indexOf("mailto:")>=0)
        {
          clientOntentItem.Link = "";
          clientOntentItem.TextType = ClientTextType.Email;
        }
        if (clientOntentItem.Link.toLowerCase().indexOf("telto:")>=0)
        {
          clientOntentItem.Link = "";
          clientOntentItem.TextType = ClientTextType.Tel;
        }
      }
    }
    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    return gson.toJson(returnList);
  }
}
