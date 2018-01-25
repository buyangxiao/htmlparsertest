/**
 * Created by xuefengwang on 2018/1/25.
 */
public class ClientContentItem {
  public ClientContentItem()
  {
    Text = "";
    Link = "";
    TextType = ClientTextType.Text;
  }

  public ClientTextType TextType;
  public String Text;
  public String Link;

  public boolean JoinAbleWith(ClientContentItem curItem)
  {
    if (this.TextType == curItem.TextType)
    {
      switch (this.TextType)
      {
        case Href:
        {
          return this.Link == curItem.Link;
        }
        case Text:
        {
          return true;
        }
      }
    }
    return false;
  }

  public void JoinWith(ClientContentItem curItem)
  {
    if (this.TextType == curItem.TextType)
    {
      switch (this.TextType)
      {
        case Href:
        {
          if (this.Link == curItem.Link)
          {
            this.Text = (this.Text==null?"":this.Text) + curItem.Text;
          }
          break;
        }
        case Text:
        {
          this.Text = (this.Text==null?"":this.Text) + curItem.Text;
          break;
        }
      }
    }
  }
}
