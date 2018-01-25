import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by xuefengwang on 2018/1/25.
 */
public class HtmlNode {
  private static Pattern regexFun = Pattern.compile("(?i)\\bchatUrlJumpLib.Jump\\b\\('(?<url>.*)\\',(?<type>.*)\\)");
  private static Pattern REGEX_URL = Pattern.compile("(?i)((http|https)://)?(www.)?(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,9})*(/[a-zA-Z0-9\\&#,%_\\./-~-]*)?");
  private static Pattern REGEX_HREF = Pattern.compile("(?i)href=\"\"\\s*(.+?)\\s*\"\"");
  private static Pattern REGEX_PICTURE_URL = Pattern.compile("(?i)src=['\"\"](\\S+)['\"\"]\\s");

  public static HtmlNode Create(String properties, TagType type)
  {
    return new HtmlNode(type,properties,null);
  }
  public static HtmlNode Create(String properties, TagType type, String content)
  {
    return new HtmlNode(type,properties,content);
  }
  public static HtmlNode CreateRoot()
  {
    return Create(null, TagType.Default);
  }
  public static HtmlNode CreatePlainTag(String text)
  {
    return Create(null, TagType.Default, text);
  }

  public HtmlNode(TagType type, String properties, String content) {
    this.type = type;
    this.properties = properties;
    this.content = content;
  }

  private TagType type ;
  private String properties;
  private String content ;
  private ArrayList<HtmlNode> ChildTags = new ArrayList<HtmlNode>();
  private String _link;
  public void setLink(String value){_link = value;}
  public String getLink()
  {
      if (StringUtils.isNotEmpty(_link)) return _link;
      if (StringUtils.isEmpty(properties)) return "";
      Matcher mcFun = regexFun.matcher(properties);
      if (mcFun.find())
      {
        this.setLink(mcFun.group("url"));
        return _link;
      }
      mcFun = REGEX_HREF.matcher(properties);
      if(mcFun.find())
      {
        _link = mcFun.group(1);
        return _link;
      }
      mcFun = REGEX_URL.matcher(properties);
      if (mcFun.find())
      {
        _link = mcFun.group(1);
        return _link;
      }
      return "";
  }
  private String getSrc()
  {
      if (StringUtils.isEmpty(properties)) return "";
      Matcher mcFun = REGEX_PICTURE_URL.matcher(properties);
      if (mcFun.find())
      {
        return mcFun.group(1);
      }
      else
      {
        return "";
      }
  }
  public void AddChild(HtmlNode child)
  {
    if (child == null) return;
    if (child.type == TagType.Default && StringUtils.isNotEmpty(child.content))
      child.content = StringUtils.replace(child.content,"&nbsp;", " ");
    ChildTags.add(child);
  }

  public LinkedList<ClientContentItem> BackOrderTravel()
  {
    LinkedList<ClientContentItem> reList = new LinkedList<ClientContentItem>();
    //后续遍历
    if (ChildTags != null && !ChildTags.isEmpty()){
      for(HtmlNode r:ChildTags){
        reList = AddClientContentItems(reList, r.BackOrderTravel(),this.getLink());
      }
    }

    //访问根节点
    ClientContentItem item = TransCurrentNodeToClientContentItem();

    //加入根节点
    if (item != null)
    {
      if (!reList.isEmpty())
      {
        ClientContentItem lastitem = reList.getLast();
        if (lastitem.JoinAbleWith(item))
        {
          lastitem.JoinWith(item);
        }
        else
        {
          reList.add(item);
        }
      }
      else
      {
        reList.add(item);
      }

    }

    return reList;
  }

  private ClientContentItem TransCurrentNodeToClientContentItem()
  {
    if (StringUtils.isEmpty(content) && StringUtils.isEmpty(this.getLink()) && this.type == TagType.Default)
      return null;
    ClientContentItem currentItem = new ClientContentItem();
    if (this.type == TagType.Image)
    {
      currentItem.Text = this.getSrc();
      currentItem.TextType = ClientTextType.Photo;
      if (StringUtils.isNotEmpty(this.getLink())) currentItem.Link = this.getLink();
    }
    else if (this.type == TagType.Breaking)
    {
      if (StringUtils.isNotEmpty(content))
        currentItem.Text = content;
      else
        currentItem.Text = "";
      currentItem.Text += "\r\n";
      currentItem.TextType = ClientTextType.Text;
      if (StringUtils.isNotEmpty(this.getLink()))
      {
        currentItem.Link = this.getLink();
        currentItem.TextType = ClientTextType.Href;
      }
    }
    else if (this.type == TagType.Default && StringUtils.isNotEmpty(this.getLink()))
    {
      if (StringUtils.isNotEmpty(content))
        currentItem.Text = content;
      currentItem.Link = this.getLink();
      currentItem.TextType = ClientTextType.Href;
    }
    else
    {
      if (StringUtils.isNotEmpty(content))
        currentItem.Text = content;
      currentItem.TextType = ClientTextType.Text;
    }
    return currentItem;
  }

  /// <summary>
  /// 合并List
  /// </summary>
  /// <param name="reList"></param>
  /// <param name="backOrderTravel"></param>
  /// <param name="olink">上层链接</param>
  private LinkedList<ClientContentItem> AddClientContentItems(LinkedList<ClientContentItem> reList, LinkedList<ClientContentItem> backOrderTravel,String olink)
  {
    if (backOrderTravel == null || backOrderTravel.isEmpty()) return null;
    //上层Link影响下层内容
    if (StringUtils.isNotEmpty(olink))
    {
      for(ClientContentItem x:backOrderTravel)
      {
        if(x.TextType == ClientTextType.Text){
          x.TextType = ClientTextType.Href;
          x.Link = olink;
        }
        else if (x.TextType == ClientTextType.Photo){
          x.Link = olink;
        }
      };
    }

    if (reList == null || reList.isEmpty())
    {
      reList = backOrderTravel;
      return reList;
    }
    ClientContentItem lastitem = reList.getLast();
    for (ClientContentItem curItem : backOrderTravel) {
      if (lastitem.JoinAbleWith(curItem)) {
        lastitem.JoinWith(curItem);
      } else {
        lastitem = curItem;
        reList.add(lastitem);
      }
    }
    return reList;
  }
}
